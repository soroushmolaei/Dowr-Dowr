package com.dorino.game.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.GameSettings
import com.dorino.game.data.model.WordCategory
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoPrimary
import com.dorino.game.ui.theme.DorinoSurfaceElevated

@Composable
fun SettingsScreen(
    settings: GameSettings,
    onUpdate: ((GameSettings) -> GameSettings) -> Unit,
    onBack: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.back_button),
                color = DorinoOnSurfaceMuted,
                fontSize = 14.sp,
                modifier = Modifier.clickable(onClick = onBack)
            )
        }
        item {
            Text(
                stringResource(R.string.settings_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            SettingsSwitchRow(
                label = stringResource(R.string.settings_sound),
                checked = settings.soundEnabled
            ) { onUpdate { it.copy(soundEnabled = !it.soundEnabled) } }
        }
        item {
            SettingsSwitchRow(
                label = stringResource(R.string.settings_music),
                checked = settings.musicEnabled
            ) { onUpdate { it.copy(musicEnabled = !it.musicEnabled) } }
        }
        item {
            SettingsSwitchRow(
                label = stringResource(R.string.settings_vibration),
                checked = settings.vibrationEnabled
            ) { onUpdate { it.copy(vibrationEnabled = !it.vibrationEnabled) } }
        }
        item {
            SettingsSwitchRow(
                label = stringResource(R.string.settings_privacy_mode),
                checked = settings.privacyModeEnabled
            ) { onUpdate { it.copy(privacyModeEnabled = !it.privacyModeEnabled) } }
        }

        item {
            Text(stringResource(R.string.settings_timer_duration), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameSettings.TIMER_OPTIONS.forEach { seconds ->
                    val label = if (seconds == 0) stringResource(R.string.duration_unlimited) else "${seconds}s"
                    Chip(
                        text = label,
                        selected = settings.timerDurationSeconds == seconds
                    ) { onUpdate { it.copy(timerDurationSeconds = seconds) } }
                }
            }
        }

        item {
            Text(stringResource(R.string.settings_round_count), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameSettings.ROUND_OPTIONS.forEach { count ->
                    Chip(
                        text = count.toString(),
                        selected = settings.roundCount == count
                    ) { onUpdate { it.copy(roundCount = count) } }
                }
            }
        }

        item {
            Text(stringResource(R.string.settings_word_categories), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WordCategory.entries.forEach { category ->
                    val selected = category in settings.selectedCategories
                    Chip(
                        text = stringResource(category.labelRes),
                        selected = selected
                    ) {
                        onUpdate {
                            val newSet = if (selected) it.selectedCategories - category else it.selectedCategories + category
                            it.copy(selectedCategories = if (newSet.isEmpty()) it.selectedCategories else newSet)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(label: String, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DorinoSurfaceElevated)
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.White, fontSize = 15.sp)
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(checkedTrackColor = DorinoPrimary)
        )
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) DorinoPrimary else Color.White.copy(alpha = 0.06f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp)
    ) {
        Text(text, color = Color.White, fontSize = 13.sp)
    }
}
