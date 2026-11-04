package com.smiraj.meditation.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class StreakTest {

    private val now: Long = Calendar.getInstance().apply {
        set(2026, Calendar.JUNE, 15, 12, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun sessionDaysAgo(days: Int) = Session(
        startedAt = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -days)
        }.timeInMillis,
        durationSec = 300,
        plannedMin = 5,
        completed = true,
    )

    @Test
    fun noSessionsMeansNoStreak() {
        assertEquals(0, computeStreak(emptyList(), now))
    }

    @Test
    fun sessionTodayCountsAsOne() {
        assertEquals(1, computeStreak(listOf(sessionDaysAgo(0)), now))
    }

    @Test
    fun consecutiveDaysAreCounted() {
        val sessions = listOf(0, 1, 2).map(::sessionDaysAgo)
        assertEquals(3, computeStreak(sessions, now))
    }

    @Test
    fun streakHoldsWhenOnlyYesterdayHasASession() {
        val sessions = listOf(1, 2).map(::sessionDaysAgo)
        assertEquals(2, computeStreak(sessions, now))
    }

    @Test
    fun gapOfOneDayBreaksTheStreak() {
        val sessions = listOf(0, 2, 3).map(::sessionDaysAgo)
        assertEquals(1, computeStreak(sessions, now))
    }

    @Test
    fun oldestSessionOlderThanYesterdayResetsToZero() {
        assertEquals(0, computeStreak(listOf(sessionDaysAgo(2)), now))
    }

    @Test
    fun multipleSessionsInOneDayCountOnce() {
        val sessions = listOf(sessionDaysAgo(0), sessionDaysAgo(0), sessionDaysAgo(1))
        assertEquals(2, computeStreak(sessions, now))
    }
}
