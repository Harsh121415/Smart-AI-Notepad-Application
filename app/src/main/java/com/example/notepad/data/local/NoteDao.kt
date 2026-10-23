
package com.example.notepad.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.notepad.data.model.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    // Get active notes only
    @Query("""
        SELECT * FROM notes
        WHERE isArchived = 0
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getAllNotes(): Flow<List<Note>>

    // Get archived notes only
    @Query("""
        SELECT * FROM notes
        WHERE isArchived = 1
        ORDER BY updatedAt DESC
    """)
    fun getArchivedNotes(): Flow<List<Note>>

    // Get a note by ID
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): Note?

    // Insert a note
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    // Update a note
    @Update
    suspend fun updateNote(note: Note)

    // Permanently delete a note
    @Delete
    suspend fun deleteNote(note: Note)

    // Search active notes only
    @Query("""
        SELECT * FROM notes
        WHERE isArchived = 0
        AND (
            title LIKE '%' || :query || '%'
            OR content LIKE '%' || :query || '%'
        )
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    // Pin or unpin a note
    @Query("UPDATE notes SET isPinned = :isPinned WHERE id = :noteId")
    suspend fun setPinned(noteId: String, isPinned: Boolean)

    // Archive or restore a note
    @Query("UPDATE notes SET isArchived = :isArchived WHERE id = :noteId")
    suspend fun setArchived(noteId: String, isArchived: Boolean)
}
