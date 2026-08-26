package com.dorino.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dorino.game.ui.theme.DorinoOnSurfaceMuted
import com.dorino.game.ui.theme.DorinoPrimary
import com.dorino.game.ui.theme.DorinoSurfaceElevated

/** یک گزینه‌ی کوچک و فشرده (برای مواردی مثل مدت تایمر، تعداد دور، دسته‌بندی). */
@Composable
fun OptionChip(text: String, selected: Boolean, onClick: () -> Unit) {
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

/** یک کارت انتخابیِ تمام‌عرض با عنوان و توضیح کوتاه (برای گزینه‌هایی که نیاز به شرح دارند). */
@Composable
fun SelectableOptionRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) DorinoPrimary.copy(alpha = 0.18f) else DorinoSurfaceElevated)
            .border(
                width = 1.dp,
                color = if (selected) DorinoPrimary else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (description.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(description, color = DorinoOnSurfaceMuted, fontSize = 12.sp, lineHeight = 17.sp)
            }
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(if (selected) DorinoPrimary else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (selected) DorinoPrimary else Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )
    }
}
