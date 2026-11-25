package com.clipboardmanager.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipboardmanager.app.data.model.ClipCategory
import com.clipboardmanager.app.ui.theme.AppColors

@Composable
fun CategoryTabBar(
    selectedCategory: ClipCategory,
    counts: Map<ClipCategory, Int>,
    onSelectCategory: (ClipCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppColors.PureWhite)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ClipCategory.entries.forEach { category ->
                val isSelected = category == selectedCategory
                val count = counts[category] ?: 0
                CategoryTabItem(
                    category = category,
                    count = count,
                    isSelected = isSelected,
                    onClick = { onSelectCategory(category) }
                )
            }
        }
        HorizontalDivider(color = AppColors.WarmDivider, thickness = 1.dp)
    }
}

@Composable
private fun CategoryTabItem(
    category: ClipCategory,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) AppColors.TealSoftTint else Color.Transparent
    val textColor = if (isSelected) AppColors.TealPrimary else Color(0xFF474E56)

    Column(
        modifier = Modifier
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CategoryTabIcon(category = category)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = category.label,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            )
            if (count > 0 && category != ClipCategory.ALL) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$count",
                    color = if (isSelected) AppColors.TealPrimary else AppColors.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .width(72.dp)
                .height(2.5.dp)
                .background(if (isSelected) AppColors.TealPrimary else Color.Transparent)
        )
    }
}

@Composable
private fun CategoryTabIcon(category: ClipCategory) {
    when (category) {
        ClipCategory.ALL -> Icon(
            imageVector = Icons.Rounded.ContentPaste,
            contentDescription = null,
            tint = AppColors.TealPrimary,
            modifier = Modifier.size(16.dp)
        )
        ClipCategory.PINNED -> Icon(
            imageVector = Icons.Rounded.PushPin,
            contentDescription = null,
            tint = AppColors.PinRed,
            modifier = Modifier.size(16.dp)
        )
        ClipCategory.FAVORITES -> Icon(
            imageVector = Icons.Rounded.Star,
            contentDescription = null,
            tint = AppColors.StarGold,
            modifier = Modifier.size(17.dp)
        )
        ClipCategory.NOTES -> Icon(
            imageVector = Icons.Rounded.EditNote,
            contentDescription = null,
            tint = Color(0xFFE09F3E),
            modifier = Modifier.size(18.dp)
        )
        ClipCategory.LINKS -> Icon(
            imageVector = Icons.Rounded.Link,
            contentDescription = null,
            tint = Color(0xFF6B7785),
            modifier = Modifier.size(17.dp)
        )
    }
}
