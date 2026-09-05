package com.dorino.game.ui.home

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.components.SecondaryPill
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoPrimary
import com.dorino.game.ui.theme.DorinoSecondary

@Composable
fun HomeScreen(
    hasSavedGame: Boolean,
    onStartGame: () -> Unit,
    onContinueGame: () -> Unit,
    onTutorial: () -> Unit,
    onHistory: () -> Unit,
    onPlayers: () -> Unit,
    onSettings: () -> Unit
) {
    var showAbout by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        DorinoPrimary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        Brush.linearGradient(listOf(DorinoPrimary, DorinoSecondary)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("د", fontSize = 44.sp, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White)
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_name),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.app_tagline),
                fontSize = 15.sp,
                color = DorinoOnSurfaceMuted,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(1f))

            if (hasSavedGame) {
                GradientButton(
                    text = stringResource(R.string.home_continue_game),
                    onClick = onContinueGame,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
            }

            GradientButton(
                text = stringResource(R.string.home_start_game),
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth(),
                colors = if (hasSavedGame)
                    listOf(DorinoSecondary, DorinoPrimary)
                else
                    listOf(DorinoPrimary, DorinoSecondary)
            )

            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryPill(
                    text = stringResource(R.string.home_tutorial),
                    onClick = onTutorial,
                    modifier = Modifier.weight(1f)
                )
                SecondaryPill(
                    text = stringResource(R.string.home_history),
                    onClick = onHistory,
                    modifier = Modifier.weight(1f)
                )
                SecondaryPill(
                    text = stringResource(R.string.home_players),
                    onClick = onPlayers,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SecondaryPill(
                    text = stringResource(R.string.home_settings),
                    onClick = onSettings,
                    modifier = Modifier.weight(1f)
                )
                SecondaryPill(
                    text = stringResource(R.string.home_about),
                    onClick = { showAbout = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) {
                    Text(stringResource(R.string.back_button))
                }
            },
            title = { Text(stringResource(R.string.about_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.about_description))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.about_version, "1.0.0"),
                        color = DorinoOnSurfaceMuted,
                        fontSize = 12.sp
                    )
                }
            }
        )
    }
}
