package com.dorino.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.ui.theme.DorinoPrimary

@Composable
fun NumberStepper(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 4
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        StepperCircleButton(symbol = "−", enabled = value > minValue, onClick = onDecrease)

        Text(
            text = value.toString(),
            fontSize = 64.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        StepperCircleButton(symbol = "+", enabled = true, onClick = onIncrease)
    }
}

@Composable
private fun StepperCircleButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .size(64.dp)
            .background(
                if (enabled) DorinoPrimary else DorinoPrimary.copy(alpha = 0.3f),
                CircleShape
            )
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(symbol, fontSize = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
