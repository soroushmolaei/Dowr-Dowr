package com.dorino.game.ui.players

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.PlayerProfile
import com.dorino.game.ui.components.PlayerAvatar
import com.dorino.game.ui.theme.DorinoAccentGold
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoSurfaceElevated
import com.dorino.game.ui.theme.TeamPaletteColors

/**
 * پروفایل ماندگارِ بازیکنان، بر اساس مجموع پاسخ‌های صحیح مرتب می‌شود —
 * دقیقاً همان معیاری که «بهترین بازیکن» در خود یک بازی هم با آن تعیین می‌شود (GameEngine.computeResult).
 */
@Composable
fun PlayersScreen(profiles: List<PlayerProfile>, onBack: () -> Unit = {}) {
    val sorted = remember(profiles) { profiles.sortedByDescending { it.totalCorrect } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.back_button),
            color = DorinoOnSurfaceMuted,
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onBack)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.players_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(16.dp))

        if (sorted.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.players_empty), color = DorinoOnSurfaceMuted)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(sorted) { index, profile -> PlayerProfileRow(rank = index + 1, profile = profile) }
            }
        }
    }
}

@Composable
private fun PlayerProfileRow(rank: Int, profile: PlayerProfile) {
    val avatarColor = TeamPaletteColors[(profile.name.hashCode() and 0x7fffffff) % TeamPaletteColors.size]
    val rankLabel = when (rank) {
        1 -> "🥇"
        2 -> "🥈"
        3 -> "🥉"
        else -> rank.toString()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DorinoSurfaceElevated)
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = rankLabel,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = DorinoAccentGold,
                modifier = Modifier.width(28.dp)
            )
            PlayerAvatar(
                name = profile.name,
                teamColor = avatarColor,
                size = 40.dp,
                isActive = false
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(profile.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = stringResource(R.string.players_games_and_wins, profile.gamesPlayed, profile.gamesWon),
                    color = DorinoOnSurfaceMuted,
                    fontSize = 12.sp
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = stringResource(R.string.players_total_correct_value, profile.totalCorrect),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            if (profile.bestPlayerAwards > 0) {
                Text(
                    text = stringResource(R.string.players_best_awards, profile.bestPlayerAwards),
                    color = DorinoOnSurfaceMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
