package com.smiraj.meditation.data

import kotlinx.coroutines.flow.Flow

class SessionRepository(private val dao: SessionDao) {

    val sessions: Flow<List<Session>> = dao.observeAll()
    val count: Flow<Int> = dao.observeCount()
    val totalSeconds: Flow<Int> = dao.observeTotalSeconds()

    suspend fun record(durationSec: Int, plannedMin: Int, completed: Boolean) {
        dao.insert(
            Session(
                startedAt = System.currentTimeMillis(),
                durationSec = durationSec,
                plannedMin = plannedMin,
                completed = completed,
            )
        )
    }

    suspend fun clearAll() = dao.clear()
}
