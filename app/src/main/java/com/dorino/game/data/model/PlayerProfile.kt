package com.dorino.game.data.model

import kotlinx.serialization.Serializable

/**
 * پروفایل ماندگارِ یک بازیکن، بین همه‌ی بازی‌های گذشته.
 *
 * چون این اپ هیچ سیستم حساب‌کاربری/شناسه‌ی دائمی ندارد، تنها راه شناسایی یک بازیکن در طول
 * بازی‌های مختلف، «اسم» او (trim‌شده) است. یعنی اگر دو نفر متفاوت دقیقاً یک اسم یکسان وارد کنند
 * آمارشان با هم ترکیب می‌شود — این یک محدودیتِ آگاهانه است، نه باگ.
 */
@Serializable
data class PlayerProfile(
    val name: String,
    val gamesPlayed: Int = 0,
    val gamesWon: Int = 0,
    val totalCorrect: Int = 0,
    val totalPass: Int = 0,
    val bestPlayerAwards: Int = 0,
    val lastPlayedEpochMillis: Long = 0L
) {
    val winRate: Float
        get() = if (gamesPlayed == 0) 0f else gamesWon.toFloat() / gamesPlayed
}
