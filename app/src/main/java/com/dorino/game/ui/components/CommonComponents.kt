package com.dorino.game.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.ui.theme.DorinoPrimary
import com.dorino.game.ui.theme.DorinoSecondary
import com.dorino.game.ui.theme.DorinoSurfaceElevated

private fun Color.darken(factor: Float): Color = Color(
    red = (red * (1 - factor)).coerceIn(0f, 1f),
    green = (green * (1 - factor)).coerceIn(0f, 1f),
    blue = (blue * (1 - factor)).coerceIn(0f, 1f),
    alpha = alpha
)

private fun Color.lighten(factor: Float): Color = Color(
    red = (red + (1f - red) * factor).coerceIn(0f, 1f),
    green = (green + (1f - green) * factor).coerceIn(0f, 1f),
    blue = (blue + (1f - blue) * factor).coerceIn(0f, 1f),
    alpha = alpha
)

/**
 * دکمه‌ی اصلیِ سه‌بعدی و «قابل‌لمس»، به سبک دکمه‌های بازی‌های موبایلی مدرن:
 * یک لایه‌ی پایه‌ی توپررنگ (عمق)، یک سطح روییِ گرادیانی که با فشار به سمت پایین می‌لغزد
 * و یک جلوه‌ی براقِ ملایم روی نیمه‌ی بالایی برای حس شیشه‌ای/پلاستیکیِ براق.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: List<Color> = listOf(DorinoPrimary, DorinoSecondary)
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val ledgeHeight = 6.dp
    val faceHeight = 54.dp
    val shape = RoundedCornerShape(18.dp)

    val topColor = colors.first().lighten(0.10f)
    val bottomColor = colors.last()
    val baseColor = colors.last().darken(0.32f)
    val alpha = if (enabled) 1f else 0.4f

    val pressOffset by animateDpAsState(
        targetValue = if (pressed && enabled) ledgeHeight else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "buttonPressOffset"
    )

    Box(modifier = modifier.height(faceHeight + ledgeHeight)) {
        // لایه‌ی پایه: عمق سه‌بعدیِ توپررنگ که هنگام فشار نمایان‌تر می‌شود
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(faceHeight)
                .align(Alignment.BottomCenter)
                .clip(shape)
                .background(baseColor.copy(alpha = alpha))
        )

        // سطح روییِ دکمه؛ با فشار به اندازه‌ی ledgeHeight به سمت پایین می‌لغزد
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(faceHeight)
                .align(Alignment.TopCenter)
                .offset(y = pressOffset)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            // جلوه‌ی براقِ شیشه‌ای روی نیمه‌ی بالایی
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                        )
                    )
            )
            Text(
                text = text,
                color = Color.White.copy(alpha = alpha),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** کارت شیشه‌ای ملایم (Glassmorphism محدود، بدون افراط). */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.12f),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(DorinoSurfaceElevated.copy(alpha = 0.7f))
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        content()
    }
}

/** دکمه‌ی ثانویه با نسخه‌ی ملایم‌تری از همان جلوه‌ی سه‌بعدی، برای هماهنگی بصری. */
@Composable
fun SecondaryPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val ledgeHeight = 3.dp
    val faceHeight = 44.dp

    val pressOffset by animateDpAsState(
        targetValue = if (pressed) ledgeHeight else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "pillPressOffset"
    )

    Box(modifier = modifier.height(faceHeight + ledgeHeight)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(faceHeight)
                .align(Alignment.BottomCenter)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.28f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(faceHeight)
                .align(Alignment.TopCenter)
                .offset(y = pressOffset)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(text, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
        }
    }
}
