package com.dorino.game.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PlayerAvatar(
    name: String,
    teamColor: Color,
    size: androidx.compose.ui.unit.Dp,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatarPulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = modifier
            .size(if (isActive) size * pulse else size)
            .drawBehind {
                if (isActive) {
                    drawCircle(
                        color = teamColor.copy(alpha = glowAlpha * 0.5f),
                        radius = this.size.minDimension / 2 + 10.dp.toPx()
                    )
                }
            }
            .background(teamColor.copy(alpha = 0.9f), CircleShape)
            .border(
                width = if (isActive) 3.dp else 1.dp,
                color = if (isActive) Color.White else Color.White.copy(alpha = 0.25f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value / 2.6f).sp
        )
    }
}

/** آیکون شکل تیم؛ برای افراد دارای مشکل تشخیص رنگ، هر تیم یک شکل مجزا دارد. */
@Composable
fun TeamShapeIcon(shapeIndex: Int, color: Color, size: androidx.compose.ui.unit.Dp = 14.dp) {
    androidx.compose.foundation.Canvas(modifier = Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        when (shapeIndex % 6) {
            0 -> drawCircle(color = color)
            1 -> drawRect(color = color)
            2 -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w / 2, 0f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(path, color = color)
            }
            3 -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w / 2, 0f)
                    lineTo(w, h / 2)
                    lineTo(w / 2, h)
                    lineTo(0f, h / 2)
                    close()
                }
                drawPath(path, color = color)
            }
            4 -> {
                val path = androidx.compose.ui.graphics.Path()
                val cx = w / 2
                val cy = h / 2
                val r = w / 2
                for (i in 0 until 6) {
                    val angle = Math.toRadians((60 * i - 90).toDouble())
                    val x = (cx + r * cos(angle)).toFloat()
                    val y = (cy + r * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, color = color)
            }
            else -> {
                val path = androidx.compose.ui.graphics.Path()
                val cx = w / 2
                val cy = h / 2
                val outerR = w / 2
                val innerR = outerR / 2.5f
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outerR else innerR
                    val angle = Math.toRadians((36 * i - 90).toDouble())
                    val x = (cx + r * cos(angle)).toFloat()
                    val y = (cy + r * sin(angle)).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, color = color)
            }
        }
    }
}
