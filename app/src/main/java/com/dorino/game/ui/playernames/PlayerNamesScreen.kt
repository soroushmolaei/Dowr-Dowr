package com.dorino.game.ui.playernames

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.components.SecondaryPill
import com.dorino.game.ui.theme.DorinoError
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerNamesScreen(
    playerCount: Int,
    onConfirm: (List<String>) -> Unit,
    onBack: () -> Unit
) {
    var names by remember(playerCount) {
        mutableStateOf((1..playerCount).map { "بازیکن $it" })
    }

    val duplicates = names.filter { it.isNotBlank() }
        .groupingBy { it.trim() }
        .eachCount()
        .filter { it.value > 1 }
        .keys

    val hasEmpty = names.any { it.isBlank() }
    val hasDuplicate = duplicates.isNotEmpty()
    val canStart = !hasEmpty && !hasDuplicate

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
        Text(
            text = stringResource(R.string.player_names_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        SecondaryPill(
            text = stringResource(R.string.randomize_teams),
            onClick = { names = names.shuffled() }
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.randomize_teams_hint),
            color = DorinoOnSurfaceMuted,
            fontSize = 11.sp
        )
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(names.size) { index ->
                val isDuplicate = names[index].trim() in duplicates && names[index].isNotBlank()
                OutlinedTextField(
                    value = names[index],
                    onValueChange = { newValue ->
                        names = names.toMutableList().also { it[index] = newValue }
                    },
                    label = { Text(stringResource(R.string.player_name_hint, index + 1)) },
                    singleLine = true,
                    isError = names[index].isBlank() || isDuplicate,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.85f)
                    )
                )
            }
        }

        if (hasEmpty || hasDuplicate) {
            Text(
                text = stringResource(if (hasEmpty) R.string.error_empty_name else R.string.error_duplicate_name),
                color = DorinoError,
                fontSize = 13.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        GradientButton(
            text = stringResource(R.string.start_game_button),
            onClick = { if (canStart) onConfirm(names.map { it.trim() }) },
            enabled = canStart,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
