package com.dorino.game.ui.gameoptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.Difficulty
import com.dorino.game.data.model.GameSettings
import com.dorino.game.data.model.TurnStyle
import com.dorino.game.data.model.WordCategory
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.components.OptionChip
import com.dorino.game.ui.components.SelectableOptionRow
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted

/**
 * صفحه‌ی شخصی‌سازی بازی، درست پیش از انتخاب تعداد و نام بازیکنان.
 * روی همان GameSettings سراسری کار می‌کند تا انتخاب‌ها برای بازی بعدی هم بمانند.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GameOptionsScreen(
    settings: GameSettings,
    onUpdate: ((GameSettings) -> GameSettings) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
        Text(
            text = stringResource(R.string.back_button),
            color = DorinoOnSurfaceMuted,
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onBack).padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(R.string.game_options_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.game_options_subtitle),
            fontSize = 13.sp,
            color = DorinoOnSurfaceMuted
        )
        Spacer(Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text(stringResource(R.string.settings_word_categories), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WordCategory.entries.forEach { category ->
                        val selected = category in settings.selectedCategories
                        OptionChip(text = stringResource(category.labelRes), selected = selected) {
                            onUpdate {
                                val newSet = if (selected) it.selectedCategories - category else it.selectedCategories + category
                                it.copy(selectedCategories = if (newSet.isEmpty()) it.selectedCategories else newSet)
                            }
                        }
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
                Text(stringResource(R.string.settings_round_count), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameSettings.ROUND_OPTIONS.forEach { count ->
                        OptionChip(text = count.toString(), selected = settings.roundCount == count) {
                            onUpdate { it.copy(roundCount = count) }
                        }
                    }
                }
            }

            item {
                Text(stringResource(R.string.settings_timer_duration), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameSettings.TIMER_OPTIONS.forEach { seconds ->
                        val label = if (seconds == 0) stringResource(R.string.duration_unlimited) else "${seconds}s"
                        OptionChip(text = label, selected = settings.timerDurationSeconds == seconds) {
                            onUpdate { it.copy(timerDurationSeconds = seconds) }
                        }
                    }
                }
            }

            item {
                Text(stringResource(R.string.settings_pass_cooldown), color = DorinoOnSurfaceMuted, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameSettings.PASS_COOLDOWN_OPTIONS.forEach { seconds ->
                        val label = if (seconds == 0) stringResource(R.string.pass_cooldown_disabled) else "${seconds}s"
                        OptionChip(text = label, selected = settings.passCooldownSeconds == seconds) {
                            onUpdate { it.copy(passCooldownSeconds = seconds) }
                        }
                    }
                }
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
        }

        Spacer(Modifier.height(12.dp))
        GradientButton(
            text = stringResource(R.string.continue_button),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
