package com.dorino.game.data.model

import kotlinx.serialization.Serializable

/** durationSeconds برابر ۰ به معنای «بدون محدودیت» است. */
@Serializable
data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val timerDurationSeconds: Int = 60,
    val selectedCategories: Set<WordCategory> = WordCategory.entries.toSet(),
    val selectedDifficulties: Set<Difficulty> = Difficulty.entries.toSet(),
    val roundCount: Int = 3,
    val privacyModeEnabled: Boolean = false,
    val turnStyle: TurnStyle = TurnStyle.RALLY,
    val language: String = "fa"
) {
    companion object {
        val TIMER_OPTIONS = listOf(30, 45, 60, 90, 120, 180, 0)
        val ROUND_OPTIONS = listOf(1, 2, 3, 4, 5, 6)
    }
}
