package com.dorino.game.data.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.dorino.game.R

enum class SoundEffect {
    CLICK, CORRECT, PASS, TIMER_WARNING, TURN_CHANGE, VICTORY, DEFEAT, START, TICK_SOFT, TIME_UP
}

/** پخش افکت صوتی بازی با SoundPool، سبک و بدون نیاز به فایل‌های حجیم. */
class SoundManager(context: Context) {

    private val appContext = context.applicationContext

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val soundIds: Map<SoundEffect, Int> = mapOf(
        SoundEffect.CLICK to soundPool.load(appContext, R.raw.sfx_click, 1),
        SoundEffect.CORRECT to soundPool.load(appContext, R.raw.sfx_correct, 1),
        SoundEffect.PASS to soundPool.load(appContext, R.raw.sfx_pass, 1),
        SoundEffect.TIMER_WARNING to soundPool.load(appContext, R.raw.sfx_timer_warning, 1),
        SoundEffect.TURN_CHANGE to soundPool.load(appContext, R.raw.sfx_turn_change, 1),
        SoundEffect.VICTORY to soundPool.load(appContext, R.raw.sfx_victory, 1),
        SoundEffect.DEFEAT to soundPool.load(appContext, R.raw.sfx_defeat, 1),
        SoundEffect.START to soundPool.load(appContext, R.raw.sfx_start, 1),
        SoundEffect.TICK_SOFT to soundPool.load(appContext, R.raw.sfx_tick_soft, 1),
        SoundEffect.TIME_UP to soundPool.load(appContext, R.raw.sfx_time_up, 1)
    )

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun play(effect: SoundEffect, soundEnabled: Boolean) {
        if (!soundEnabled) return
        soundIds[effect]?.let { id ->
            soundPool.play(id, 1f, 1f, 1, 0, 1f)
        }
    }

    fun vibrate(vibrationEnabled: Boolean, durationMs: Long = 40L) {
        if (!vibrationEnabled) return
        val v = vibrator ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            v.vibrate(durationMs)
        }
    }

    fun release() {
        soundPool.release()
    }
}
