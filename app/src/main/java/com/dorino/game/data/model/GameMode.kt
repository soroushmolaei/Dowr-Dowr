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

/**
 * شیوه‌ی نوبت‌گیری در طول یک دور (مستقل از حالت بازی تیمی).
 */
@Serializable
enum class TurnStyle {
    /** رالی: هرکی جواب بده، نوبتش رو نگه می‌داره تا زمان تمام شود. */
    RALLY,

    /** دست‌به‌دست: بعد از هر پاسخ صحیح، نوبت فوراً به نفر بعدی می‌رسد. */
    ROTATING
}
