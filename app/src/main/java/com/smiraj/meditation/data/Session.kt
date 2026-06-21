package com.smiraj.meditation.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A completed (or stopped) meditation session.
 *
 * This is the ONLY thing the app persists. Per SKADI design notes, no scan
 * results, no evidence, no "secret mode" flag ever touches disk — only these
 * innocuous wellness records, which make the cover convincing.
 *
 * @param startedAt   epoch millis when the session began
 * @param durationSec actual seconds meditated (may be < planned if stopped early)
 * @param plannedMin  the preset/custom length the user chose, in minutes
 * @param completed   true if the timer ran to the end
 */
@Entity(tableName = "sessions")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val durationSec: Int,
    val plannedMin: Int,
    val completed: Boolean,
)
