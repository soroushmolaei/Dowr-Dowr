package com.dorino.game.data.model

import kotlinx.serialization.Serializable

/**
 * مدل داده یک تیم. در حالت «نبرد دو تیم» تنها ۲ تیم وجود دارد،
 * در حالت «تیم‌های دونفره» به تعداد N/2 تیم ساخته می‌شود.
 * shapeIndex برای افراد دارای مشکل تشخیص رنگ استفاده می‌شود (شکل به‌جای فقط رنگ).
 */
@Serializable
data class Team(
    val id: Int,
    val name: String,
    val colorHex: String,
    val shapeIndex: Int,
    val playerIds: List<Int>,
    val score: Int = 0,
    val savedTimeSeconds: Int = 0,
    val activeTimeSeconds: Int = 0,
    val correctCount: Int = 0,
    val passCount: Int = 0
)
