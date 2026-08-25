package com.dorino.game.ui.tutorial

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.dorino.game.data.model.GameMode
import com.dorino.game.ui.components.GradientButton
import com.dorino.game.ui.components.MiniSeatingPreview
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoPrimary
import com.dorino.game.ui.theme.DorinoSurfaceElevated

private data class TutorialStep(val titleRes: Int, val descRes: Int)

private val steps = listOf(
    TutorialStep(R.string.tutorial_step1_title, R.string.tutorial_step1_desc),
    TutorialStep(R.string.tutorial_step2_title, R.string.tutorial_step2_desc),
    TutorialStep(R.string.tutorial_step3_title, R.string.tutorial_step3_desc)
)

@Composable
fun TutorialScreen(onFinish: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val isLast = index == steps.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 36.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                stringResource(R.string.tutorial_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                stringResource(R.string.tutorial_skip),
                color = DorinoOnSurfaceMuted,
                modifier = Modifier.clickable(onClick = onFinish)
            )
        }

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            steps.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= index) DorinoPrimary else Color.White.copy(alpha = 0.1f))
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(DorinoSurfaceElevated)
        ) {
            if (index < 2) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    MiniSeatingPreview(mode = GameMode.TEAM_BATTLE, modifier = Modifier.fillMaxSize())
                }
            } else {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    MiniSeatingPreview(mode = GameMode.PAIR_TEAMS, modifier = Modifier.fillMaxSize())
                }
            }
        }

        Spacer(Modifier.height(28.dp))

        AnimatedContent(
            targetState = index,
            transitionSpec = { fadeIn(tween(250)).togetherWith(fadeOut(tween(150))) },
            label = "tutorialStep"
        ) { i ->
            Column {
                Text(
                    text = stringResource(steps[i].titleRes),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(steps[i].descRes),
                    fontSize = 14.sp,
                    color = DorinoOnSurfaceMuted,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(Modifier.weight(1f))

        GradientButton(
            text = if (isLast) stringResource(R.string.tutorial_finish) else stringResource(R.string.tutorial_next),
            onClick = {
                if (isLast) onFinish() else index += 1
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}
