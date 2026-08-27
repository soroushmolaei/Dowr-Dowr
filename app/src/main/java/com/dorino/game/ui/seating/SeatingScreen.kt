package com.dorino.game.ui.seating

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.GameMode
import com.dorino.game.data.model.GameState
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.components.TableSeatingView
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted

/**
 * پیش از شروع اولین نوبت، چیدمان واقعیِ دور میز را نشان می‌دهد
 * تا بازیکنان دقیقاً به همان ترتیب بنشینند (با همان الگوریتمی که در طول بازی استفاده می‌شود).
 */
@Composable
fun SeatingScreen(
    state: GameState,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.seating_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (state.mode == GameMode.PAIR_TEAMS)
                stringResource(R.string.seating_subtitle_pairs)
            else
                stringResource(R.string.seating_subtitle_alternating),
            fontSize = 13.sp,
            color = DorinoOnSurfaceMuted,
            textAlign = TextAlign.Center
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            TableSeatingView(
                players = state.players,
                teams = state.teams,
                mode = state.mode,
                modifier = Modifier.fillMaxSize()
            )
        }

        GradientButton(
            text = stringResource(R.string.seating_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
