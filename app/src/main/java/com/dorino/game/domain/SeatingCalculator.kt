package com.dorino.game.domain

import kotlin.math.cos
import kotlin.math.sin

/** مختصات نرمال‌شده یک بازیکن روی دایره (بین -۱ و ۱)، مستقل از اندازه صفحه. */
data class SeatPosition(val x: Float, val y: Float)

/**
 * محاسبه‌ی عمومی و کاملاً پویای چیدمان دایره‌ای بازیکنان.
 * هیچ حالت خاصی برای تعداد مشخصی از بازیکنان (۴، ۶، ۸ و ...) در این الگوریتم وجود ندارد.
 *
 * فرمول:
 *   angle(i) = -π/2 + i * (2π / N)
 *   x = cos(angle)
 *   y = sin(angle)
 */
object SeatingCalculator {

    fun calculateSeatPositions(playerCount: Int): List<SeatPosition> {
        if (playerCount <= 0) return emptyList()
        val angleStep = (2.0 * Math.PI) / playerCount
        return (0 until playerCount).map { i ->
            val angle = -Math.PI / 2.0 + i * angleStep
            SeatPosition(
                x = cos(angle).toFloat(),
                y = sin(angle).toFloat()
            )
        }
    }

    /**
     * اندازه‌ی نسبی آواتار بر اساس تعداد بازیکنان (Dynamic Scaling).
     * با افزایش تعداد بازیکنان، آواتارها به‌صورت نرم کوچک‌تر می‌شوند اما هیچ‌گاه از حداقل خوانایی کمتر نمی‌شوند.
     */
    fun avatarScaleFactor(playerCount: Int): Float {
        val base = 1.0f
        val minScale = 0.42f
        // هر ۲ بازیکن اضافه، حدود ۶٪ کوچک‌تر می‌شود، اما با حد پایین محافظت‌شده.
        val raw = base - (playerCount - 4) * 0.035f
        return raw.coerceIn(minScale, base)
    }

    /** شعاع نسبی میز نسبت به کوچک‌ترین بعد صفحه، که با تعداد بازیکنان کمی افزایش می‌یابد. */
    fun tableRadiusFraction(playerCount: Int): Float {
        val base = 0.30f
        val max = 0.42f
        val raw = base + (playerCount - 4) * 0.006f
        return raw.coerceIn(base, max)
    }
}
