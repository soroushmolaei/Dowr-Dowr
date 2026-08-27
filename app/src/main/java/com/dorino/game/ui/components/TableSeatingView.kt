package com.dorino.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.data.model.GameMode
import com.dorino.game.data.model.Player
import com.dorino.game.data.model.Team
import com.dorino.game.domain.SeatingCalculator
import kotlin.math.min

/**
 * چیدمان کاملاً پویای بازیکنان دور میز، بر اساس فرمول دایره‌ای عمومی.
 * برای هیچ تعداد بازیکن خاصی Hardcode نشده است.
 * اگر [mode] برابر تیم‌های دونفره باشد، خط اتصال بین هر بازیکن و هم‌تیمی روبه‌رویش رسم می‌شود.
 */
@Composable
fun TableSeatingView(
    players: List<Player>,
    teams: List<Team>,
    mode: GameMode? = null,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val n = players.size
        if (n == 0) return@BoxWithConstraints

        val minDimensionDp = min(maxWidth.value, maxHeight.value)

        val radiusFraction = SeatingCalculator.tableRadiusFraction(n)
        val radiusDp = minDimensionDp * radiusFraction
        val scale = SeatingCalculator.avatarScaleFactor(n)
        val avatarSizeDp = (54 * scale).coerceIn(22f, 54f).dp

        val positions = SeatingCalculator.calculateSeatPositions(n)

        // میز مرکزی
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size((minDimensionDp * radiusFraction * 1.15f).dp)
                .background(
                    brush = Brush.radialGradient(
                        listOf(
                            Color.White.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        if (mode == GameMode.PAIR_TEAMS && n >= 4) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radiusPx = radiusDp.dp.toPx()
                for (i in 0 until n / 2) {
                    val partnerIndex = (i + n / 2) % n
                    val a = positions.getOrNull(i) ?: continue
                    val b = positions.getOrNull(partnerIndex) ?: continue
                    val player = players.getOrNull(i)
                    val team = teams.firstOrNull { it.id == player?.teamId }
                    val color = team?.colorHex
                        ?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                        ?: Color.White
                    drawLine(
                        color = color.copy(alpha = 0.45f),
                        start = Offset(cx + radiusPx * a.x, cy + radiusPx * a.y),
                        end = Offset(cx + radiusPx * b.x, cy + radiusPx * b.y),
                        strokeWidth = 3.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                    )
                }
            }
        }

        positions.forEachIndexed { index, pos ->
            val player = players.getOrNull(index) ?: return@forEachIndexed
            val team = teams.firstOrNull { it.id == player.teamId }
            val teamColor = team?.colorHex?.let { runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull() }
                ?: MaterialTheme.colorScheme.primary

            val offsetXDp = radiusDp * pos.x
            val offsetYDp = radiusDp * pos.y

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = offsetXDp.dp, y = offsetYDp.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PlayerAvatar(
                        name = player.name,
                        teamColor = teamColor,
                        size = avatarSizeDp,
                        isActive = player.isActive
                    )
                    if (avatarSizeDp.value > 30f) {
                        Text(
                            text = player.name,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 10.sp,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            fontWeight = if (player.isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
