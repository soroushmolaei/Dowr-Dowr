package com.dorino.game.data.model

import kotlinx.serialization.Serializable

/** durationSeconds برابر ۰ به معنای «بدون محدودیت» است (فقط برای استقامتی؛ در سرعتی مجاز نیست). */
@Serializable
data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val timerDurationSeconds: Int = 60,
    val selectedCategories: Set<WordCategory> = WordCategory.entries.toSet(),
    val selectedDifficulty: Difficulty = Difficulty.RANDOM,
    val roundCount: Int = 3,
    val privacyModeEnabled: Boolean = false,
    val turnStyle: TurnStyle = TurnStyle.RALLY,
    val passCooldownSeconds: Int = 5,
    val customWords: List<String> = emptyList(),
    val language: String = "fa"
) {
    companion object {
        val TIMER_OPTIONS = listOf(30, 45, 60, 90, 120, 180, 0)
        val ROUND_OPTIONS = listOf(1, 2, 3, 4, 5, 6)
        val PASS_COOLDOWN_OPTIONS = listOf(0, 3, 5, 8, 10)

        /** سرعتی به تایمر محدود نیاز دارد (موجودی تیم)، پس گزینه‌ی «بدون محدودیت» حذف می‌شود. */
        fun timerOptionsFor(turnStyle: TurnStyle): List<Int> =
            if (turnStyle == TurnStyle.ROTATING) TIMER_OPTIONS.filter { it != 0 } else TIMER_OPTIONS

        /** در سرعتی حداقل ۳ دور لازم است تا آستانه‌های زمانی معنا داشته باشند. */
        fun roundOptionsFor(turnStyle: TurnStyle): List<Int> =
            if (turnStyle == TurnStyle.ROTATING) ROUND_OPTIONS.filter { it >= 3 } else ROUND_OPTIONS
    }
}
