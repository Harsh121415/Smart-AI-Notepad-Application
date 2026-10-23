
package com.example.notepad.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean = false,
    val color: String = "default",
    val isArchived: Boolean = false
)
