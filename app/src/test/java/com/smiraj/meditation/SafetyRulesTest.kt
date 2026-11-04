package com.smiraj.meditation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the "keep nothing on disk" rules from AGENTS.md. These are static checks
 * over the source tree, so they run on the plain JVM with no device.
 */
class SafetyRulesTest {

    private val sourceRoot = File("src/main/java/com/smiraj/meditation")

    private fun kotlinFiles(dir: String): List<File> =
        File(sourceRoot, dir).walkTopDown().filter { it.extension == "kt" }.toList()

    @Test
    fun triggerCodeIsOutsideTheNormalSessionRange() {
        // Real sessions are coerced to 1..180 minutes, so the trigger must not collide.
        assertTrue(AppViewModel.TRIGGER_CODE !in 1..180)
    }

    @Test
    fun persistenceLayerNeverReferencesScanData() {
        val forbidden = listOf("ScanSnapshot", "Finding", "SafetyMode", "scan.")
        val files = kotlinFiles("data")
        assertTrue("expected to find the data/ sources", files.isNotEmpty())
        for (file in files) {
            val text = file.readText()
            for (word in forbidden) {
                assertFalse("${file.name} must not reference '$word'", text.contains(word))
            }
        }
    }

    @Test
    fun hiddenScreensNeverWriteToDiskOrLogs() {
        val forbidden = listOf("AppDatabase", "SettingsStore", "SharedPreferences", "Log.", "File(")
        val files = kotlinFiles("diagnostics") + kotlinFiles("safety") + kotlinFiles("scan")
        assertTrue("expected to find hidden-layer sources", files.isNotEmpty())
        for (file in files) {
            val text = file.readText()
            for (word in forbidden) {
                assertFalse("${file.name} must not use '$word'", text.contains(word))
            }
        }
    }

    @Test
    fun onlyOneFunnelCanReachTheHiddenLayer() {
        val entries = kotlinFiles(".")
            .filter { it.name != "AppViewModel.kt" }
            .count { it.readText().contains("Screen.Diagnostics") && it.readText().contains("_screen.value") }
        assertEquals(0, entries)
    }
}
