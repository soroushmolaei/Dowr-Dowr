package com.dorino.game.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.ui.theme.DorinoError
import com.dorino.game.ui.theme.DorinoSecondary
import com.dorino.game.ui.theme.DorinoSuccess

/** حلقه‌ی زمان‌سنج زیبا و انیمیشنی؛ رنگ با نزدیک شدن به پایان زمان تغییر می‌کند. */
@Composable
fun TimerRing(
    totalSeconds: Int,
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 140.dp
) {
    val fraction = if (totalSeconds <= 0) 1f else (remainingSeconds.toFloat() / totalSeconds).coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(fraction, tween(400), label = "timerFraction")

    val color = when {
        totalSeconds == 0 -> DorinoSecondary
        fraction > 0.5f -> DorinoSuccess
        fraction > 0.2f -> Color(0xFFFBBF24)
        else -> DorinoError
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = size.toPx() * 0.08f, cap = StrokeCap.Round)
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                size = Size(this.size.width, this.size.height)
            )
            if (totalSeconds > 0) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedFraction,
                    useCenter = false,
                    style = stroke,
                    size = Size(this.size.width, this.size.height)
                )
            } else {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = stroke,
                    size = Size(this.size.width, this.size.height)
                )
            }
        }
        Text(
            text = if (totalSeconds == 0) "∞" else remainingSeconds.toString(),
            fontSize = (size.value / 3.4f).sp,
            fontWeight = FontWeight.Black,
            color = Color.White
        )
    }
}
