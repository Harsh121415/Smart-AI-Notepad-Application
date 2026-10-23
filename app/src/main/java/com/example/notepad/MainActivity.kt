package com.example.notepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.notepad.navigation.AppNavigation
import com.example.notepad.ui.theme.NotepadTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isDarkMode by rememberSaveable {
                mutableStateOf(false)
            }

            NotepadTheme(
                darkTheme = isDarkMode,
                dynamicColor = false
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        isDarkMode = isDarkMode,
                        onToggleTheme = { isDarkMode = !isDarkMode }
                    )
                }
            }
        }
    }
}
