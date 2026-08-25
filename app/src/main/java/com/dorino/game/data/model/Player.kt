package com.dorino.game.data.model

import kotlinx.serialization.Serializable

/**
 * مدل داده یک بازیکن.
 * position: جایگاه بازیکن در دور میز (صفر-پایه)، برای محاسبه چیدمان دایره‌ای استفاده می‌شود.
 */
@Serializable
data class Player(
    val id: Int,
    val name: String,
    val teamId: Int,
    val position: Int,
    val avatarIndex: Int = id % AVATAR_COUNT,
    val isActive: Boolean = false,
    val correctCount: Int = 0,
    val passCount: Int = 0
) {
    companion object {
        const val AVATAR_COUNT = 12
    }
}
