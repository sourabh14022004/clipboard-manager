package com.clipboardmanager.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClipDao {
    @Query("SELECT * FROM clips ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAllClips(): Flow<List<ClipEntity>>

    @Query("SELECT * FROM clips ORDER BY isPinned DESC, updatedAt DESC")
    suspend fun getAllClipsOnce(): List<ClipEntity>

    @Query("SELECT * FROM clips WHERE id = :id LIMIT 1")
    suspend fun getClipById(id: Long): ClipEntity?

    @Query("SELECT * FROM clips ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestClip(): ClipEntity?

    @Query("SELECT * FROM clips WHERE content = :content LIMIT 1")
    suspend fun findByContent(content: String): ClipEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(clip: ClipEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clips: List<ClipEntity>)

    @Update
    suspend fun update(clip: ClipEntity)

    @Query("DELETE FROM clips WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM clips WHERE isPinned = 0 AND isFavorite = 0 AND isLocked = 0")
    suspend fun deleteUnlockedNonPinnedHistory(): Int

    @Query("SELECT COUNT(*) FROM clips")
    suspend fun countClips(): Int

    @Query(
        """
        DELETE FROM clips WHERE id IN (
            SELECT id FROM clips
            WHERE isPinned = 0 AND isFavorite = 0 AND isLocked = 0
            ORDER BY updatedAt DESC
            LIMIT -1 OFFSET :keepCount
        )
        """
    )
    suspend fun trimOldHistory(keepCount: Int)
}
