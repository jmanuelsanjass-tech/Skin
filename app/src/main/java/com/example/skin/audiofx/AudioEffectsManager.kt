package com.example.skin.audiofx

import android.media.AudioTrack
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer

class AudioEffectsManager(audioSessionId: Int) {
    var equalizer: Equalizer? = null
        private set
    var bassBoost: BassBoost? = null
        private set
    var virtualizer: Virtualizer? = null
        private set

    init {
        if (audioSessionId != AudioTrack.ERROR_BAD_VALUE && audioSessionId != 0) {
            try {
                equalizer = Equalizer(0, audioSessionId).apply { enabled = true }
                bassBoost = BassBoost(0, audioSessionId).apply { enabled = true }
                virtualizer = Virtualizer(0, audioSessionId).apply { enabled = true }
            } catch (e: Exception) {}
        }
    }

    fun setPreset(presetName: String) {
        val eq = equalizer ?: return
        val numPresets = eq.numberOfPresets
        for (i in 0 until numPresets) {
            val name = eq.getPresetName(i.toShort())
            if (name.equals(presetName, ignoreCase = true)) {
                eq.usePreset(i.toShort())
                break
            }
        }
    }

    fun setCustomBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
        } catch (e: Exception) {}
    }

    fun getBandLevel(band: Short): Short {
        return try {
            equalizer?.getBandLevel(band) ?: 0
        } catch (e: Exception) { 0 }
    }

    fun getNumberOfBands(): Short {
        return try {
            equalizer?.numberOfBands ?: 0
        } catch (e: Exception) { 0 }
    }

    fun getCenterFreq(band: Short): Int {
        return try {
            equalizer?.getCenterFreq(band) ?: 0
        } catch (e: Exception) { 0 }
    }

    fun getBandLevelRange(): ShortArray {
        return try {
            equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
        } catch (e: Exception) { shortArrayOf(-1500, 1500) }
    }

    fun setBassBoost(strength: Short) {
        try {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength(strength)
            }
        } catch (e: Exception) {}
    }

    fun setSurroundSound(enabled: Boolean, strength: Short = 500) {
        try {
            virtualizer?.enabled = enabled
            if (enabled) {
                virtualizer?.setStrength(strength)
            }
        } catch (e: Exception) {}
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
        } catch (e: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}
