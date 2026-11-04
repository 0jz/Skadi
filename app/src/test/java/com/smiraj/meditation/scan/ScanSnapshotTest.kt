package com.smiraj.meditation.scan

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanSnapshotTest {

    @Test
    fun emptySnapshotHasNoFindingsAndNeverRan() {
        val snapshot = ScanSnapshot.empty()
        assertTrue(snapshot.findings.isEmpty())
        assertEquals(0L, snapshot.ranAtMillis)
    }

    @Test
    fun severityOrderIsLowMediumHigh() {
        assertEquals(
            listOf(FindingSeverity.Low, FindingSeverity.Medium, FindingSeverity.High),
            FindingSeverity.entries,
        )
    }
}
