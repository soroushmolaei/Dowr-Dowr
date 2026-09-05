package com.dorino.game.ui.turntransition

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.GameMode
import com.dorino.game.data.model.Player
import com.dorino.game.data.model.Team
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.components.PlayerAvatar
import com.dorino.game.ui.theme.DorinoAccentGold

@Composable
fun TurnTransitionScreen(
    mode: GameMode,
    currentPlayer: Player?,
    currentTeam: Team?,
    round: Int,
    totalRounds: Int,
    privacyModeEnabled: Boolean,
    onReady: () -> Unit
) {
    var revealed by remember(currentPlayer?.id) { mutableStateOf(!privacyModeEnabled) }

    val teamColor = currentTeam?.colorHex?.let {
        runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
    } ?: MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    listOf(teamColor.copy(alpha = 0.25f), MaterialTheme.colorScheme.background)
                )
            )
    ) {
        AnimatedContent(
            targetState = revealed,
            transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(200)) },
            label = "transitionReveal"
        ) { isRevealed ->
            if (!isRevealed) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { revealed = true }
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Text("🔒", fontSize = 56.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.privacy_mode_message),
                        color = Color.White,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.round_label, round.coerceAtMost(totalRounds)),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                    if (mode == GameMode.PANTOMIME) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.pantomime_turn_badge),
                            color = DorinoAccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(24.dp))

                    if (currentPlayer != null) {
                        PlayerAvatar(
                            name = currentPlayer.name,
                            teamColor = teamColor,
                            size = 110.dp,
                            isActive = true
                        )
                        Spacer(Modifier.height(28.dp))
                        Text(
                            text = stringResource(R.string.pass_phone_to, currentPlayer.name),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        currentTeam?.let {
                            Text(
                                text = stringResource(R.string.turn_of_team, it.name),
                                fontSize = 15.sp,
                                color = teamColor
                            )
                        }
                    }

                    Spacer(Modifier.height(48.dp))
                    GradientButton(
                        text = stringResource(R.string.ready_button),
                        onClick = onReady,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
