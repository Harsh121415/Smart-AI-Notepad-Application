package com.example.notepad.ui.editor

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notepad.data.model.Note
import java.util.UUID

private data class NoteColorOption(
    val key: String,
    val label: String,
    val color: Color
)

private val noteColorOptions = listOf(
    NoteColorOption("default", "Default", Color(0xFFF5F5F5)),
    NoteColorOption("yellow", "Yellow", Color(0xFFFFF59D)),
    NoteColorOption("green", "Green", Color(0xFFC8E6C9)),
    NoteColorOption("blue", "Blue", Color(0xFFBBDEFB)),
    NoteColorOption("pink", "Pink", Color(0xFFF8BBD0)),
    NoteColorOption("purple", "Purple", Color(0xFFE1BEE7))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    note: Note? = null,
    onBack: () -> Unit,
    onSave: (String, String) -> Unit,
    onAutosave: (String, String, String) -> Unit = { _, _, _ -> },
    onColorChange: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    var draftId by rememberSaveable(note?.id) {
        mutableStateOf(note?.id ?: UUID.randomUUID().toString())
    }
    var title by rememberSaveable(note?.id) {
        mutableStateOf(note?.title.orEmpty())
    }
    var content by rememberSaveable(note?.id) {
        mutableStateOf(note?.content.orEmpty())
    }
    var selectedColor by rememberSaveable(note?.id) {
        mutableStateOf(note?.color ?: "default")
    }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }

    fun requestAutosave(newTitle: String, newContent: String) {
        onAutosave(draftId, newTitle, newContent)
    }

    fun shareNote() {
        requestAutosave(title, content)
        val noteText = buildString {
            if (title.isNotBlank()) {
                append(title)
                append("\n\n")
            }
            append(content)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_SUBJECT,
                title.ifBlank { "My Note" }
            )
            putExtra(Intent.EXTRA_TEXT, noteText)
        }

        context.startActivity(
            Intent.createChooser(shareIntent, "Share note using")
        )
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == Activity.RESULT_OK && uri != null) {
            val success = exportNoteToUri(
                context = context,
                uri = uri,
                title = title,
                content = content
            )
            val message = if (success) {
                "Note exported successfully!"
            } else {
                "Failed to export note."
            }
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    val wordCount = if (content.isBlank()) {
        0
    } else {
        content.trim().split(Regex("\\s+")).size
    }

    val characterCount = content.length

    val currentBackgroundColor = when (selectedColor) {
        "yellow" -> Color(0xFFFFF59D)
        "green" -> Color(0xFFC8E6C9)
        "blue" -> Color(0xFFBBDEFB)
        "pink" -> Color(0xFFF8BBD0)
        "purple" -> Color(0xFFE1BEE7)
        else -> MaterialTheme.colorScheme.background
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (note == null) "New note" else "Edit note",
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    TextButton(
                        onClick = {
                            requestAutosave(title, content)
                            onBack()
                        }
                    ) {
                        Text("← Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { showColorPicker = true }
                    ) {
                        Text("🎨")
                    }

                    TextButton(
                        onClick = { shareNote() }
                    ) {
                        Text("📤")
                    }

                    TextButton(
                        onClick = {
                            val intent = createExportIntent(title)
                            exportLauncher.launch(intent)
                        }
                    ) {
                        Text("💾")
                    }

                    TextButton(
                        onClick = {
                            requestAutosave(title, content)
                            onBack()
                        }
                    ) {
                        Text("Done")
                    }
                }
            )
        },
        containerColor = currentBackgroundColor
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "YOUR THOUGHTS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { newTitle ->
                    title = newTitle
                    requestAutosave(newTitle, content)
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Give your note a title") },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { newContent ->
                        content = newContent
                        requestAutosave(title, newContent)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    placeholder = {
                        Text(
                            "Start writing here...\n\n" +
                                    "Your ideas, plans and thoughts belong here."
                        )
                    },
                    shape = RoundedCornerShape(18.dp),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$wordCount words",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "$characterCount characters",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Changes save automatically after you pause typing.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Choose note color") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    noteColorOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedColor = option.key
                                    onColorChange(draftId, option.key)
                                    showColorPicker = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(option.color, CircleShape)
                                    .border(
                                        width = if (selectedColor == option.key) 2.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showColorPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}
