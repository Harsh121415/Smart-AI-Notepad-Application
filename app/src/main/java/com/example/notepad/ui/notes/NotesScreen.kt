package com.example.notepad.ui.notes

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notepad.data.model.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(
    viewModel: NotesViewModel,
    onAddNote: () -> Unit,
    onNoteClick: (String) -> Unit,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit,
    onOpenArchive: () -> Unit
) {
    val notes by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val context = LocalContext.current

    fun shareNote(note: Note) {
        val noteText = buildString {
            if (note.title.isNotBlank()) {
                append(note.title)
                append("\n\n")
            }
            append(note.content)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_SUBJECT,
                note.title.ifBlank { "My Note" }
            )
            putExtra(Intent.EXTRA_TEXT, noteText)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share note using")
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNote,
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Header and theme/archive toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "YOUR SPACE",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(6.dp))

                    Text(
                        text = "SmartNotepad",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "N",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.width(4.dp))

                IconButton(
                    onClick = onOpenArchive,
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = "📁",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = if (isDarkMode) "☀️" else "🌙",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "A little space for all your thoughts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::searchNotes,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search your notes...") },
                leadingIcon = {
                    Text(
                        text = "⌕",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (searchQuery.isBlank()) {
                        "Your notes"
                    } else {
                        "Search results"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.width(10.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "${notes.size}",
                        modifier = Modifier.padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(92.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "✎",
                                    style = MaterialTheme.typography.displaySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Your story starts here"
                            } else {
                                "No matching notes"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = if (searchQuery.isBlank()) {
                                "Capture an idea, make a plan,\nor write down what's on your mind."
                            } else {
                                "Try another title or keyword."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (searchQuery.isBlank()) {
                            Spacer(Modifier.height(20.dp))

                            TextButton(onClick = onAddNote) {
                                Text("Create your first note  →")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = notes,
                        key = { it.id }
                    ) { note ->
                        NoteCard(
                            note = note,
                            onClick = { onNoteClick(note.id) },
                            onDelete = { noteToDelete = note },
                            onPinToggle = { viewModel.togglePin(note) },
                            onArchiveToggle = { viewModel.toggleArchive(note) },
                            onShare = { shareNote(note) }
                        )
                    }

                    item {
                        Spacer(Modifier.height(84.dp))
                    }
                }
            }
        }
    }

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Delete this note?") },
            text = {
                Text("This note will be permanently removed.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNote(note)
                        noteToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPinToggle: () -> Unit,
    onArchiveToggle: () -> Unit,
    onShare: () -> Unit
) {
    val wordCount = remember(note.content) {
        if (note.content.isBlank()) {
            0
        } else {
            note.content.trim()
                .split(Regex("\\s+"))
                .count { it.isNotBlank() }
        }
    }

    val cardColor = when (note.color) {
        "yellow" -> Color(0xFFFFF59D)
        "green" -> Color(0xFFC8E6C9)
        "blue" -> Color(0xFFBBDEFB)
        "pink" -> Color(0xFFF8BBD0)
        "purple" -> Color(0xFFE1BEE7)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (note.isPinned) {
                            Text(
                                text = "📌 ",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text(
                            text = note.title.ifBlank { "Untitled note" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = note.content.ifBlank { "No additional text" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.width(8.dp))

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    IconButton(
                        onClick = onPinToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = if (note.isPinned) "★" else "☆",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = onArchiveToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "📁",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "📤",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text(
                            text = "×",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "$wordCount words",
                        modifier = Modifier.padding(
                            horizontal = 9.dp,
                            vertical = 5.dp
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                Spacer(Modifier.weight(1f))

                Text(
                    text = relativeTime(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun relativeTime(timestamp: Long): String {
    val difference = (System.currentTimeMillis() - timestamp)
        .coerceAtLeast(0L)

    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour

    return when {
        difference < minute -> "Just now"
        difference < hour -> "${difference / minute} min ago"
        difference < day -> "${difference / hour} hr ago"
        difference < 2 * day -> "Yesterday"
        difference < 7 * day -> "${difference / day} days ago"
        else -> SimpleDateFormat(
            "dd MMM",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}
