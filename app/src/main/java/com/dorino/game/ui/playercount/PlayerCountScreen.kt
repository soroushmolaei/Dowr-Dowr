package com.dorino.game.ui.playercount

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.components.NumberStepper
import com.dorino.game.ui.theme.DorinoError
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted

private const val MIN_PLAYERS = 4

@Composable
fun PlayerCountScreen(
    initialCount: Int,
    onConfirm: (Int) -> Unit,
    onBack: () -> Unit
) {
    var count by remember { mutableIntStateOf(initialCount.coerceAtLeast(MIN_PLAYERS)) }
    val isOdd = count % 2 != 0
    val isBelowMin = count < MIN_PLAYERS
    val canContinue = !isOdd && !isBelowMin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.player_count_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.player_count_subtitle),
            fontSize = 14.sp,
            color = DorinoOnSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        NumberStepper(
            value = count,
            minValue = MIN_PLAYERS,
            onDecrease = { count = (count - 1).coerceAtLeast(MIN_PLAYERS - 1) },
            onIncrease = { count += 1 },
            modifier = Modifier.fillMaxWidth()
        )

        AnimatedVisibility(visible = isOdd || isBelowMin) {
            Text(
                text = stringResource(
                    if (isOdd) R.string.player_count_error_odd else R.string.player_count_error_min
                ),
                color = DorinoError,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(Modifier.weight(1f))

        GradientButton(
            text = stringResource(R.string.continue_button),
            onClick = { if (canContinue) onConfirm(count) },
            enabled = canContinue,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.back_button),
            color = DorinoOnSurfaceMuted,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(8.dp),
            textAlign = TextAlign.Center
        )
    }
}
