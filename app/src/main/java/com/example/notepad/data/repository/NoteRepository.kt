
package com.example.notepad.data.repository

import com.example.notepad.data.local.NoteDao
import com.example.notepad.data.model.Note
import kotlinx.coroutines.flow.Flow

class NoteRepository(
    private val noteDao: NoteDao
) {

    // Get active notes
    fun getAllNotes(): Flow<List<Note>> =
        noteDao.getAllNotes()

    // Get archived notes
    fun getArchivedNotes(): Flow<List<Note>> =
        noteDao.getArchivedNotes()

    // Get a note by ID
    suspend fun getNoteById(noteId: String): Note? =
        noteDao.getNoteById(noteId)

    // Insert a note
    suspend fun insertNote(note: Note) =
        noteDao.insertNote(note)

    // Update a note
    suspend fun updateNote(note: Note) =
        noteDao.updateNote(note)

    // Delete a note permanently
    suspend fun deleteNote(note: Note) =
        noteDao.deleteNote(note)

    // Search active notes
    fun searchNotes(query: String): Flow<List<Note>> =
        noteDao.searchNotes(query)

    // Pin or unpin a note
    suspend fun setPinned(noteId: String, isPinned: Boolean) {
        noteDao.setPinned(noteId, isPinned)
    }

    // Archive or restore a note
    suspend fun setArchived(noteId: String, isArchived: Boolean) {
        noteDao.setArchived(noteId, isArchived)
    }
}
