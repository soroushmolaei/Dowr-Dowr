package com.dorino.game.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode {
    /** نبرد دو تیم: بازیکنان یکی‌درمیان دور میز می‌نشینند. */
    TEAM_BATTLE,

    /** تیم‌های دونفره: هر بازیکن روبه‌روی هم‌تیمی خودش می‌نشیند. */
    PAIR_TEAMS
}

@Serializable
enum class GameStatus {
    NOT_STARTED,
    TURN_TRANSITION,
    IN_PROGRESS,
    PAUSED,
    ROUND_SUMMARY,
    FINISHED
}
