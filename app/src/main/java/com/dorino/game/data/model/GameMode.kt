package com.dorino.game.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class GameMode {
    /** نبرد دو تیم: بازیکنان یکی‌درمیان دور میز می‌نشینند. */
    TEAM_BATTLE,

    /** تیم‌های دونفره: هر بازیکن روبه‌روی هم‌تیمی خودش می‌نشیند. */
    PAIR_TEAMS,

    /**
     * پانتومیم: از نظر ساختار تیم/چیدمان دقیقاً مثل نبرد دو تیم است (فقط دو تیم، یکی‌درمیان) —
     * تفاوتش فقط در نحوه‌ی رساندن کلمه است: باید با حرکات بدن نشان داده شود، نه با حرف زدن.
     * عمداً نسخه‌ی «دونفره»‌ای برای این حالت وجود ندارد.
     */
    PANTOMIME
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

/** دلیل توقفِ بازی در حالت سرویوایور. */
@Serializable
enum class SurvivorCheckpointType {
    /** موجودیِ زمانِ یک تیم تمام شد و آن تیم حذف شد. */
    TEAM_ELIMINATED,

    /** به آستانه‌ی زمانیِ پایانِ یک دور رسیدیم. */
    ROUND_ADVANCED
}

/** اطلاعات لازم برای نمایش صفحه‌ی توقفِ سرویوایور. */
@Serializable
data class SurvivorCheckpoint(
    val type: SurvivorCheckpointType,
    val eliminatedTeamName: String? = null
)
