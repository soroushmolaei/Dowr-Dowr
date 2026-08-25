package com.dorino.game.ui.components

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.data.model.Player
import com.dorino.game.data.model.Team
import com.dorino.game.domain.SeatingCalculator
import kotlin.math.min

/**
 * چیدمان کاملاً پویای بازیکنان دور میز، بر اساس فرمول دایره‌ای عمومی.
 * برای هیچ تعداد بازیکن خاصی Hardcode نشده است.
 */
@Composable
fun TableSeatingView(
    players: List<Player>,
    teams: List<Team>,
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
