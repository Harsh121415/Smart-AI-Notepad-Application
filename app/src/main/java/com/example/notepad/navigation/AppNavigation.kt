
package com.example.notepad.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notepad.data.local.NoteDatabase
import com.example.notepad.data.model.Note
import com.example.notepad.data.repository.NoteRepository
import com.example.notepad.ui.editor.NoteEditorScreen
import com.example.notepad.ui.notes.NotesScreen
import com.example.notepad.ui.notes.NotesViewModel
import com.example.notepad.ui.notes.NotesViewModelFactory

@Composable
fun AppNavigation(
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current

    val database = remember {
        NoteDatabase.getDatabase(context)
    }

    val repository = remember {
        NoteRepository(database.noteDao())
    }

    val factory = remember {
        NotesViewModelFactory(repository)
    }

    val viewModel: NotesViewModel = viewModel(
        factory = factory
    )

    val selectedNote by viewModel.selectedNote.collectAsStateWithLifecycle()

    var editingNoteId by remember {
        mutableStateOf<String?>(null)
    }

    var isEditorOpen by remember {
        mutableStateOf(false)
    }

    var isArchiveOpen by remember {
        mutableStateOf(false)
    }

    var isLoadingNote by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(isEditorOpen, editingNoteId) {
        if (isEditorOpen && editingNoteId != null) {
            isLoadingNote = true
            viewModel.loadNote(editingNoteId!!)
        } else {
            viewModel.clearSelectedNote()
            isLoadingNote = false
        }
    }

    LaunchedEffect(selectedNote, editingNoteId) {
        if (
            editingNoteId != null &&
            selectedNote?.id == editingNoteId
        ) {
            isLoadingNote = false
        }
    }

    when {
        isArchiveOpen -> {
            ArchiveScreen(
                viewModel = viewModel,
                onBack = {
                    isArchiveOpen = false
                }
            )
        }

        !isEditorOpen -> {
            NotesScreen(
                viewModel = viewModel,
                onAddNote = {
                    editingNoteId = null
                    isEditorOpen = true
                },
                onNoteClick = { noteId ->
                    editingNoteId = noteId
                    isLoadingNote = true
                    isEditorOpen = true
                },
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme,
                onOpenArchive = {
                    isArchiveOpen = true
                }
            )
        }

        else -> {
            val noteToEdit: Note? = selectedNote

            if (editingNoteId == null) {
                NoteEditorScreen(
                    onBack = {
                        isEditorOpen = false
                    },
                    onSave = { title, content ->
                        viewModel.addNote(title, content)
                        isEditorOpen = false
                    },
                    onAutosave = { id, title, content ->
                        viewModel.autosaveNote(id, title, content)
                    },
                    onColorChange = { _, _ ->
                        // The editor must persist the selected color
                        // together with the note's current content.
                    }
                )
            } else if (!isLoadingNote && noteToEdit != null) {
                NoteEditorScreen(
                    note = noteToEdit,
                    onBack = {
                        isEditorOpen = false
                    },
                    onSave = { title, content ->
                        viewModel.updateNote(
                            noteToEdit,
                            title,
                            content
                        )
                        isEditorOpen = false
                    },
                    onAutosave = { id, title, content ->
                        viewModel.autosaveNote(id, title, content)
                    },
                    onColorChange = { _, color ->
                        viewModel.updateNoteColor(
                            noteToEdit,
                            color
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArchiveScreen(
    viewModel: NotesViewModel,
    onBack: () -> Unit
) {
    val archivedNotes by viewModel.archivedNotes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Archived Notes")
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (archivedNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No archived notes yet",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = archivedNotes,
                    key = { note -> note.id }
                ) { note ->
                    ArchivedNoteCard(
                        note = note,
                        onRestore = {
                            viewModel.restoreNote(note)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchivedNoteCard(
    note: Note,
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = note.title.ifBlank { "Untitled Note" },
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.content.ifBlank { "No content" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onRestore) {
                Text("Restore Note")
            }
        }
    }
}
