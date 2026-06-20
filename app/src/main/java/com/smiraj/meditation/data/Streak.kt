package com.smiraj.meditation.data

import java.util.Calendar

/**
 * Number of consecutive days (ending today or yesterday) that have at least one
 * session. If the most recent session is older than yesterday, the streak is 0.
 */
fun computeStreak(sessions: List<Session>, now: Long = System.currentTimeMillis()): Int {
    if (sessions.isEmpty()) return 0

    val days = sessions.map { dayIndex(it.startedAt) }.toHashSet()
    val today = dayIndex(now)

    // Allow the streak to "hold" if the user hasn't meditated yet today but did
    // yesterday — common when checking in the morning.
    var cursor = when {
        days.contains(today) -> today
        days.contains(today - 1) -> today - 1
        else -> return 0
    }

    var streak = 0
    while (days.contains(cursor)) {
        streak++
        cursor--
    }
    return streak
}

/** Days since epoch in the device's local timezone. */
private fun dayIndex(epochMillis: Long): Long {
    val cal = Calendar.getInstance().apply { timeInMillis = epochMillis }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis / 86_400_000L
}
