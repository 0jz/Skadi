package com.smiraj.meditation.meditation

import android.content.Context
import android.media.MediaPlayer
import com.smiraj.meditation.data.Ambient

/**
 * Thin MediaPlayer wrapper for looping ambient tracks.
 *
 * STUB (Phase 1): the wiring is complete, but no audio files are bundled yet.
 * Drop loopable files into `res/raw/` named `rain` and `forest` (e.g. rain.ogg),
 * then fill in [rawResFor]. Until then, start()/stop() are safe no-ops so the
 * UI toggle works without crashing.
 */
class AmbientPlayer(private val context: Context) {

    private var player: MediaPlayer? = null

    fun play(ambient: Ambient) {
        stop()
        val res = rawResFor(ambient) ?: return // no asset bundled yet -> no-op
        player = MediaPlayer.create(context, res)?.apply {
            isLooping = true
            start()
        }
    }

    fun stop() {
        player?.run {
            runCatching { if (isPlaying) stop() }
            release()
        }
        player = null
    }

    private fun rawResFor(ambient: Ambient): Int? = when (ambient) {
        Ambient.NONE -> null
        // TODO: return R.raw.rain / R.raw.forest once audio is added.
        Ambient.RAIN -> null
        Ambient.FOREST -> null
    }
}
