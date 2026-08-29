package com.dorino.game.ui.roundresult

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.domain.GameResult
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.theme.DorinoAccentGold
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoSurfaceElevated

@Composable
fun GameResultScreen(
    result: GameResult,
    onPlayAgain: () -> Unit,
    onBackHome: () -> Unit
) {
    val winnerLabel = when {
        result.winnerTeams.size == 1 -> stringResource(R.string.winner_team, result.winnerTeams.first().name)
        else -> "مساوی شد 🤝"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        Text(
            text = stringResource(R.string.result_title),
            fontSize = 15.sp,
            color = DorinoOnSurfaceMuted
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = winnerLabel,
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            color = DorinoAccentGold
        )

        Spacer(Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(result.teams) { team ->
                val color = runCatching { Color(android.graphics.Color.parseColor(team.colorHex)) }
                    .getOrDefault(MaterialTheme.colorScheme.primary)
                val eliminated = result.wasSurvivorMode && result.winnerTeams.none { it.id == team.id }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(DorinoSurfaceElevated)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(team.name, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (eliminated) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.team_eliminated),
                                color = DorinoOnSurfaceMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Text(team.score.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
            }

            item {
                Spacer(Modifier.height(12.dp))
                StatRow(stringResource(R.string.total_saved_time), formatSeconds(result.totalSavedTimeSeconds))
                StatRow(stringResource(R.string.total_correct), result.totalCorrect.toString())
                StatRow(stringResource(R.string.total_pass), result.totalPass.toString())
                result.bestPlayer?.let {
                    StatRow(stringResource(R.string.best_player), it.name)
                }
            }
        }

        GradientButton(
            text = stringResource(R.string.play_again),
            onClick = onPlayAgain,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.back_to_home),
            color = DorinoOnSurfaceMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .clickable(onClick = onBackHome)
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = DorinoOnSurfaceMuted, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

private fun formatSeconds(total: Int): String {
    val m = total / 60
    val s = total % 60
    return "%02d:%02d".format(m, s)
}
