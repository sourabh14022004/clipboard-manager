package com.clipboardmanager.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipboardmanager.app.data.model.ClipFilterChip
import com.clipboardmanager.app.ui.theme.AppColors

@Composable
fun FilterChipBar(
    selectedChip: ClipFilterChip,
    onSelectChip: (ClipFilterChip) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.WarmPaperBg)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ClipFilterChip.entries.forEach { chip ->
            val isSelected = chip == selectedChip
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(7.dp))
                    .clickable { onSelectChip(chip) },
                shape = RoundedCornerShape(7.dp),
                color = if (isSelected) AppColors.TealPrimary else AppColors.PureWhite,
                border = if (isSelected) null else BorderStroke(0.9.dp, AppColors.ChipBorder),
                shadowElevation = if (isSelected) 1.dp else 0.5.dp
            ) {
                Text(
                    text = chip.label,
                    color = if (isSelected) Color.White else Color(0xFF3E454D),
                    fontSize = 12.5.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 6.dp)
                )
            }
        }
    }
}
