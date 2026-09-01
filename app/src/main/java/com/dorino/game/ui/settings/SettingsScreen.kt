package com.dorino.game.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.Difficulty
import com.dorino.game.data.model.GameSettings
import com.dorino.game.data.model.TurnStyle
import com.dorino.game.data.model.WordCategory
import com.dorino.game.ui.components.CategoryGrid
import com.dorino.game.ui.components.OptionChip
import com.dorino.game.ui.components.SelectableOptionRow
import com.dorino.game.ui.theme.DorinoError
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoPrimary
import com.dorino.game.ui.theme.DorinoSurfaceElevated

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: GameSettings,
    onUpdate: ((GameSettings) -> GameSettings) -> Unit,
    onAddCustomWord: (String) -> Unit = {},
    onRemoveCustomWord: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val isSpeedMode = settings.turnStyle == TurnStyle.ROTATING

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
            Text(stringResource(R.string.settings_turn_style), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TurnStyle.entries.forEach { style ->
                    val titleRes = if (style == TurnStyle.RALLY) R.string.turn_style_rally else R.string.turn_style_rotating
                    val descRes = if (style == TurnStyle.RALLY) R.string.turn_style_rally_desc else R.string.turn_style_rotating_desc
                    SelectableOptionRow(
                        title = stringResource(titleRes),
                        description = stringResource(descRes),
                        selected = settings.turnStyle == style,
                        onClick = { onUpdate { it.copy(turnStyle = style) } }
                    )
                }
            }
        }

        item {
            Text(
                text = if (isSpeedMode) stringResource(R.string.settings_team_time_label) else stringResource(R.string.settings_timer_duration),
                color = DorinoOnSurfaceMuted,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameSettings.timerOptionsFor(settings.turnStyle).forEach { seconds ->
                    val label = if (seconds == 0) stringResource(R.string.duration_unlimited) else "${seconds}s"
                    OptionChip(
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
                GameSettings.roundOptionsFor(settings.turnStyle).forEach { count ->
                    OptionChip(
                        text = count.toString(),
                        selected = settings.roundCount == count
                    ) { onUpdate { it.copy(roundCount = count) } }
                }
            }
        }

        item {
            Text(stringResource(R.string.settings_pass_cooldown), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GameSettings.PASS_COOLDOWN_OPTIONS.forEach { seconds ->
                    val label = if (seconds == 0) stringResource(R.string.pass_cooldown_disabled) else "${seconds}s"
                    OptionChip(
                        text = label,
                        selected = settings.passCooldownSeconds == seconds
                    ) { onUpdate { it.copy(passCooldownSeconds = seconds) } }
                }
            }
        }

        item {
            Text(stringResource(R.string.settings_difficulty), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Difficulty.entries.forEach { difficulty ->
                    OptionChip(
                        text = stringResource(difficulty.labelRes),
                        selected = settings.selectedDifficulty == difficulty
                    ) {
                        onUpdate { it.copy(selectedDifficulty = difficulty) }
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.settings_word_categories), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            CategoryGrid(
                categories = WordCategory.entries.toList(),
                selected = settings.selectedCategories,
                onToggle = { category ->
                    onUpdate {
                        val newSet = if (category in it.selectedCategories) it.selectedCategories - category else it.selectedCategories + category
                        it.copy(selectedCategories = if (newSet.isEmpty()) it.selectedCategories else newSet)
                    }
                }
            )
        }

        item {
            Text(stringResource(R.string.settings_custom_words), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.settings_custom_words_hint), color = DorinoOnSurfaceMuted, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            CustomWordsEditor(
                words = settings.customWords,
                onAdd = onAddCustomWord,
                onRemove = onRemoveCustomWord
            )
        }
    }
}

@Composable
private fun CustomWordsEditor(
    words: List<String>,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    var newWord by remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = newWord,
            onValueChange = { newWord = it },
            placeholder = { Text(stringResource(R.string.custom_word_hint), fontSize = 13.sp) },
            singleLine = true,
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White.copy(alpha = 0.85f)
            )
        )
        Row(
            modifier = Modifier
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (newWord.isNotBlank()) DorinoPrimary else DorinoPrimary.copy(alpha = 0.35f))
                .clickable(enabled = newWord.isNotBlank()) {
                    onAdd(newWord)
                    newWord = ""
                }
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.custom_word_add), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }

    Spacer(Modifier.height(10.dp))

    if (words.isEmpty()) {
        Text(stringResource(R.string.custom_words_empty), color = DorinoOnSurfaceMuted, fontSize = 12.sp)
    } else {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            words.forEach { word ->
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(DorinoSurfaceElevated)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(word, color = Color.White, fontSize = 12.sp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "✕",
                        color = DorinoError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onRemove(word) }
                    )
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
