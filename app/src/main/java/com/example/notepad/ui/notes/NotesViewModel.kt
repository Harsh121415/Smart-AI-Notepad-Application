
package com.example.notepad.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.data.model.Note
import com.example.notepad.data.repository.NoteRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.UUID

class NotesViewModel(
    private val repository: NoteRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _archivedNotes = MutableStateFlow<List<Note>>(emptyList())
    val archivedNotes: StateFlow<List<Note>> = _archivedNotes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    private var autosaveJob: Job? = null

    init {
        observeNotes()
        observeArchivedNotes()
    }

    @OptIn(FlowPreview::class)
    private fun observeNotes() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.isBlank()) {
                        repository.getAllNotes()
                    } else {
                        repository.searchNotes(query.trim())
                    }
                }
                .collectLatest { noteList ->
                    _notes.value = noteList
                }
        }
    }

    private fun observeArchivedNotes() {
        viewModelScope.launch {
            repository.getArchivedNotes().collectLatest { noteList ->
                _archivedNotes.value = noteList
            }
        }
    }

    fun searchNotes(query: String) {
        _searchQuery.value = query
    }

    fun loadNote(noteId: String) {
        viewModelScope.launch {
            _selectedNote.value = repository.getNoteById(noteId)
        }
    }

    fun clearSelectedNote() {
        _selectedNote.value = null
    }

    fun autosaveNote(
        noteId: String,
        title: String,
        content: String
    ) {
        autosaveJob?.cancel()

        autosaveJob = viewModelScope.launch {
            delay(700)

            if (title.isBlank() && content.isBlank()) {
                return@launch
            }

            val existingNote = repository.getNoteById(noteId)
            val currentTime = System.currentTimeMillis()

            val noteToSave = if (existingNote != null) {
                existingNote.copy(
                    title = title.trim(),
                    content = content,
                    updatedAt = currentTime
                )
            } else {
                Note(
                    id = noteId,
                    title = title.trim(),
                    content = content,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
            }

            repository.insertNote(noteToSave)

            if (_selectedNote.value?.id == noteId) {
                _selectedNote.value = noteToSave
            }
        }
    }

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()

            val note = Note(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                content = content,
                createdAt = currentTime,
                updatedAt = currentTime
            )

            repository.insertNote(note)
        }
    }

    fun updateNote(note: Note, title: String, content: String) {
        viewModelScope.launch {
            val updatedNote = note.copy(
                title = title.trim(),
                content = content,
                updatedAt = System.currentTimeMillis()
            )

            repository.updateNote(updatedNote)

            if (_selectedNote.value?.id == note.id) {
                _selectedNote.value = updatedNote
            }
        }
    }

    fun updateNoteColor(note: Note, color: String) {
        viewModelScope.launch {
            val updatedNote = note.copy(color = color)
            repository.updateNote(updatedNote)

            if (_selectedNote.value?.id == note.id) {
                _selectedNote.value = updatedNote
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)

            if (_selectedNote.value?.id == note.id) {
                _selectedNote.value = null
            }
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.setPinned(
                noteId = note.id,
                isPinned = !note.isPinned
            )
        }
    }

    // Archive a note or restore it from the archive.
    fun toggleArchive(note: Note) {
        viewModelScope.launch {
            repository.setArchived(
                noteId = note.id,
                isArchived = !note.isArchived
            )
        }
    }

    // Restore a note directly from the archive screen.
    fun restoreNote(note: Note) {
        viewModelScope.launch {
            repository.setArchived(
                noteId = note.id,
                isArchived = false
            )
        }
    }
}
