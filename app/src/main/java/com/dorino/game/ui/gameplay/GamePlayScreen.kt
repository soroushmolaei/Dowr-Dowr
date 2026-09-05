package com.dorino.game.ui.gameplay

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.GameMode
import com.dorino.game.data.model.GameState
import com.dorino.game.data.model.TurnStyle
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.components.TimerRing
import com.dorino.game.ui.theme.DorinoAccentGold
import com.dorino.game.ui.theme.DorinoError
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoSuccess
import com.dorino.game.ui.theme.DorinoSurfaceElevated

@Composable
fun GamePlayScreen(
    state: GameState,
    passCooldownRemaining: Int,
    onCorrect: () -> Unit,
    onPass: () -> Unit,
    onFinishTurnManually: () -> Unit
) {
    val team = state.currentTeam
    val teamColor = team?.colorHex?.let {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary
    val isUnlimited = state.settings.timerDurationSeconds == 0

    val nextPlayer = if (state.players.isNotEmpty()) {
        state.players[(state.currentPlayerIndex + 1) % state.players.size]
    } else null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                team?.let {
                    Text(it.name, color = teamColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.round_label, state.round),
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = if (state.settings.turnStyle == TurnStyle.ROTATING)
                        stringResource(R.string.turn_style_rotating)
                    else
                        stringResource(R.string.turn_style_rally),
                    color = teamColor.copy(alpha = 0.8f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            state.teams.forEach { t ->
                val c = runCatching { Color(android.graphics.Color.parseColor(t.colorHex)) }
                    .getOrDefault(MaterialTheme.colorScheme.primary)
                val eliminated = state.isTeamEliminated(t)
                val displaySeconds = if (state.isSurvivorMode) {
                    (state.settings.timerDurationSeconds - t.activeTimeSeconds).coerceAtLeast(0)
                } else {
                    t.activeTimeSeconds
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.alpha(if (eliminated) 0.35f else 1f)
                ) {
                    Text(t.name, fontSize = 12.sp, color = c)
                    if (eliminated) {
                        Text(
                            text = stringResource(R.string.team_eliminated),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = DorinoError
                        )
                    } else {
                        Text(
                            formatMmSs(displaySeconds),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        AnimatedContent(
            targetState = state.currentPlayerIndex,
            transitionSpec = {
                (slideInVertically(tween(320)) { h -> h } + fadeIn(tween(320))) togetherWith
                    (slideOutVertically(tween(320)) { h -> -h } + fadeOut(tween(200)))
            },
            modifier = Modifier.fillMaxWidth(),
            label = "playerHandoff"
        ) { _ ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.currentPlayer?.name ?: "",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    maxLines = 1
                )
                if (nextPlayer != null) {
                    Text(
                        text = "   ←   ",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                    Text(
                        text = nextPlayer.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1
                    )
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TimerRing(
                totalSeconds = state.settings.timerDurationSeconds,
                remainingSeconds = state.displayTimeRemaining
            )
        }

        Spacer(Modifier.height(28.dp))

        if (state.mode == GameMode.PANTOMIME) {
            Text(
                text = stringResource(R.string.pantomime_instruction),
                color = DorinoAccentGold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(DorinoSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = state.currentWord,
                transitionSpec = {
                    (fadeIn(tween(220))).togetherWith(fadeOut(tween(140)))
                },
                label = "wordChange"
            ) { word ->
                Text(
                    text = word ?: "",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        if (isUnlimited) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PassButton(
                    cooldownRemaining = passCooldownRemaining,
                    onClick = onPass,
                    modifier = Modifier.weight(1f)
                )
                GradientButton(
                    text = stringResource(R.string.correct_button),
                    onClick = onCorrect,
                    modifier = Modifier.weight(1f),
                    colors = listOf(DorinoSuccess, DorinoSuccess.copy(alpha = 0.85f))
                )
            }
            Spacer(Modifier.height(10.dp))
            GradientButton(
                text = "پایان نوبت",
                onClick = onFinishTurnManually,
                modifier = Modifier.fillMaxWidth(),
                colors = listOf(DorinoError, DorinoError)
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                PassButton(
                    cooldownRemaining = passCooldownRemaining,
                    onClick = onPass,
                    modifier = Modifier.weight(1f)
                )
                GradientButton(
                    text = stringResource(R.string.correct_button),
                    onClick = onCorrect,
                    modifier = Modifier.weight(1f),
                    colors = listOf(DorinoSuccess, DorinoSuccess.copy(alpha = 0.85f))
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text = if (state.isSurvivorMode)
                    stringResource(R.string.pass_turn_survivor)
                else
                    "پایان زودهنگام نوبت (ذخیره زمان)",
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onFinishTurnManually)
                    .padding(6.dp)
            )
        }
    }
}

@Composable
private fun PassButton(cooldownRemaining: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val onCooldown = cooldownRemaining > 0
    Column(modifier = modifier) {
        GradientButton(
            text = if (onCooldown)
                stringResource(R.string.pass_word_button_cooldown, cooldownRemaining)
            else
                stringResource(R.string.pass_word_button),
            onClick = onClick,
            enabled = !onCooldown,
            modifier = Modifier.fillMaxWidth(),
            colors = listOf(DorinoError.copy(alpha = 0.85f), DorinoError)
        )
    }
}

private fun formatMmSs(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%d:%02d".format(m, s)
}
