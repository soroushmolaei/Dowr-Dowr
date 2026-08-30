package com.dorino.game.ui.survivor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.GameState
import com.dorino.game.data.model.SurvivorCheckpointType
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.theme.DorinoError
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoSurfaceElevated

/**
 * صفحه‌ی توقفِ سرویوایور: یا یک تیم حذف شده، یا به آستانه‌ی پایان یک دور رسیده‌ایم.
 * با «ادامه»، طبق نوع رویداد یا همان بازیکن با کلمه‌ی جدید ادامه می‌دهد، یا نوبت به تیمِ زنده‌ی بعدی می‌رسد.
 */
@Composable
fun SurvivorCheckpointScreen(
    state: GameState,
    onContinue: () -> Unit
) {
    val checkpoint = state.survivorCheckpoint
    val isElimination = checkpoint?.type == SurvivorCheckpointType.TEAM_ELIMINATED

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(DorinoError.copy(alpha = 0.18f), MaterialTheme.colorScheme.background)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            Text(text = if (isElimination) "💥" else "🚩", fontSize = 56.sp)
            Spacer(Modifier.height(20.dp))

            Text(
                text = if (isElimination) {
                    stringResource(R.string.checkpoint_team_eliminated, checkpoint?.eliminatedTeamName ?: "")
                } else {
                    stringResource(R.string.checkpoint_round_advanced, state.round)
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))
            Text(
                text = if (isElimination)
                    stringResource(R.string.checkpoint_next_team_hint)
                else
                    stringResource(R.string.checkpoint_same_player_hint, state.currentPlayer?.name ?: ""),
                fontSize = 13.sp,
                color = DorinoOnSurfaceMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(DorinoSurfaceElevated)
                    .padding(16.dp)
            ) {
                state.teams.forEach { team ->
                    val eliminated = state.isTeamEliminated(team)
                    val color = runCatching { Color(android.graphics.Color.parseColor(team.colorHex)) }
                        .getOrDefault(MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .alpha(if (eliminated) 0.4f else 1f),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(team.name, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = if (eliminated) stringResource(R.string.team_eliminated) else formatMmSs(
                                (state.settings.timerDurationSeconds - team.activeTimeSeconds).coerceAtLeast(0)
                            ),
                            color = if (eliminated) DorinoError else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            GradientButton(
                text = stringResource(R.string.checkpoint_continue),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun formatMmSs(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
