package com.dorino.game.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import com.dorino.game.data.model.GameMode
import com.dorino.game.domain.SeatingCalculator
import com.dorino.game.domain.TeamAssigner
import com.dorino.game.ui.theme.TeamPaletteColors

/**
 * پیش‌نمایش کوچک و متحرک از الگوی نشستن بازیکنان برای یک حالت بازی خاص،
 * با استفاده از همان الگوریتم دایره‌ای واقعی بازی (نه یک نسخه‌ی جداگانه).
 */
@Composable
fun MiniSeatingPreview(
    mode: GameMode,
    modifier: Modifier = Modifier,
    previewPlayerCount: Int = 8
) {
    val infiniteTransition = rememberInfiniteTransition(label = "seatPreview")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "dotPulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val n = previewPlayerCount
        val positions = SeatingCalculator.calculateSeatPositions(n)
        val radius = size.minDimension * 0.38f
        val center = Offset(size.width / 2f, size.height / 2f)
        val dotRadius = size.minDimension * 0.045f

        val points = positions.map { p ->
            Offset(center.x + radius * p.x, center.y + radius * p.y)
        }

        if (mode == GameMode.PAIR_TEAMS) {
            for (i in 0 until n / 2) {
                val a = points[i]
                val b = points[TeamAssigner.oppositeSeatIndex(i, n)]
                drawLine(
                    color = TeamPaletteColors[i % TeamPaletteColors.size].copy(alpha = 0.35f),
                    start = a,
                    end = b,
                    strokeWidth = 3f
                )
            }
        }

        points.forEachIndexed { index, point ->
            val teamId = TeamAssigner.teamIdForSeatIndex(mode, index, n)
            val color = TeamPaletteColors[teamId % TeamPaletteColors.size]
            drawCircle(
                color = color,
                radius = dotRadius * pulse,
                center = point
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.25f),
                radius = dotRadius * pulse,
                center = point,
                style = Stroke(width = 2f)
            )
        }
    }
}
