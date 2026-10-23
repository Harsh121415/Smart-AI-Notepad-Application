
package com.example.notepad.ui.editor

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

fun exportNoteToUri(
    context: Context,
    uri: Uri,
    title: String,
    content: String
): Boolean {
    return try {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
            writer.write(title.ifBlank { "Untitled Note" })
            writer.write("\n\n")
            writer.write(content)
        } ?: return false

        true
    } catch (exception: Exception) {
        false
    }
}

fun createExportIntent(title: String): Intent {
    return Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TITLE,
            "${title.ifBlank { "Untitled Note" }}.txt"
        )
    }
}
