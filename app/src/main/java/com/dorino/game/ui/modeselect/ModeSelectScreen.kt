package com.dorino.game.ui.modeselect

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.GameMode
import com.dorino.game.ui.components.MiniSeatingPreview
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoPrimary
import com.dorino.game.ui.theme.DorinoSecondary
import com.dorino.game.ui.theme.DorinoSurfaceElevated

@Composable
fun ModeSelectScreen(
    onModeSelected: (GameMode) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.mode_select_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer2(24.dp)

        ModeCard(
            title = stringResource(R.string.mode1_title),
            description = stringResource(R.string.mode1_description),
            accent = DorinoPrimary,
            mode = GameMode.TEAM_BATTLE,
            onClick = { onModeSelected(GameMode.TEAM_BATTLE) }
        )

        Spacer2(18.dp)

        ModeCard(
            title = stringResource(R.string.mode2_title),
            description = stringResource(R.string.mode2_description),
            accent = DorinoSecondary,
            mode = GameMode.PAIR_TEAMS,
            onClick = { onModeSelected(GameMode.PAIR_TEAMS) }
        )
    }
}

@Composable
private fun Spacer2(h: androidx.compose.ui.unit.Dp) =
    androidx.compose.foundation.layout.Spacer(Modifier.height(h))

@Composable
private fun ModeCard(
    title: String,
    description: String,
    accent: Color,
    mode: GameMode,
    onClick: () -> Unit
) {
    val pressedScale by animateFloatAsState(1f, tween(150), label = "cardScale")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressedScale)
            .clip(RoundedCornerShape(28.dp))
            .background(DorinoSurfaceElevated)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black.copy(alpha = 0.25f))
        ) {
            MiniSeatingPreview(mode = mode, modifier = Modifier.fillMaxSize())
        }
        Spacer2(14.dp)
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accent)
        Spacer2(6.dp)
        Text(description, fontSize = 13.sp, color = DorinoOnSurfaceMuted, lineHeight = 20.sp)
    }
}
