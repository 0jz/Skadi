package com.smiraj.meditation.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Insert
    suspend fun insert(session: Session): Long

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<Session>>

    @Query("SELECT COUNT(*) FROM sessions")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationSec), 0) FROM sessions")
    fun observeTotalSeconds(): Flow<Int>

    @Query("DELETE FROM sessions")
    suspend fun clear()
}
