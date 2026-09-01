package com.dorino.game.ui.turnstyleselect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.R
import com.dorino.game.data.model.TurnStyle
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoPrimary
import com.dorino.game.ui.theme.DorinoSecondary
import com.dorino.game.ui.theme.DorinoSurfaceElevated

/**
 * اولین قدمِ ساخت بازی: انتخاب شیوه‌ی بازی — استقامتی (رالی) یا سرعتی (دست‌به‌دستِ سرویوایور).
 * چون این انتخاب روی تعداد دور و مدت تایمرِ مجاز در گام‌های بعدی اثر می‌گذارد، اول از همه پرسیده می‌شود.
 */
@Composable
fun TurnStyleSelectScreen(
    onStyleSelected: (TurnStyle) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.turn_style_select_title),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(24.dp))

        TurnStyleCard(
            emoji = "🏃",
            title = stringResource(R.string.turn_style_rally),
            description = stringResource(R.string.turn_style_rally_desc),
            accent = DorinoPrimary,
            onClick = { onStyleSelected(TurnStyle.RALLY) }
        )

        Spacer(Modifier.height(18.dp))

        TurnStyleCard(
            emoji = "⚡",
            title = stringResource(R.string.turn_style_rotating),
            description = stringResource(R.string.turn_style_rotating_desc),
            accent = DorinoSecondary,
            onClick = { onStyleSelected(TurnStyle.ROTATING) }
        )

        Spacer(Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.back_button),
            color = DorinoOnSurfaceMuted,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack)
                .padding(8.dp)
        )
    }
}

@Composable
private fun TurnStyleCard(
    emoji: String,
    title: String,
    description: String,
    accent: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(DorinoSurfaceElevated)
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(22.dp)
    ) {
        Text(emoji, fontSize = 40.sp)
        Spacer(Modifier.height(12.dp))
        Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accent)
        Spacer(Modifier.height(6.dp))
        Text(description, fontSize = 13.sp, color = DorinoOnSurfaceMuted, lineHeight = 20.sp)
    }
}
