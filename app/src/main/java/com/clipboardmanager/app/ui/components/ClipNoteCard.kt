package com.clipboardmanager.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipboardmanager.app.data.local.ClipEntity
import com.clipboardmanager.app.data.model.ClipType
import com.clipboardmanager.app.ui.theme.AppColors
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClipNoteCard(
    clip: ClipEntity,
    isUnlockedInSession: Boolean,
    onCardClick: () -> Unit,
    onCardLongClick: () -> Unit,
    onQuickCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = Color(clip.cardColor.containerHex)
    val borderColor = Color(clip.cardColor.borderHex)
    val isProtected = clip.isLocked && !isUnlockedInSession
    var justCopied by remember(clip.id) { mutableStateOf(false) }

    LaunchedEffect(justCopied) {
        if (justCopied) {
            delay(1300)
            justCopied = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = if (clip.isPinned) 8.dp else 2.dp, end = if (clip.isPinned) 4.dp else 0.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .combinedClickable(
                    onClick = onCardClick,
                    onLongClick = onCardLongClick
                ),
            shape = RoundedCornerShape(14.dp),
            color = containerColor,
            border = BorderStroke(1.1.dp, borderColor),
            shadowElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 13.dp, vertical = 12.dp)
            ) {
                // Header: Title + Lock/Star icons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = clip.title,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = if (clip.isPinned) 14.dp else 4.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (clip.isFavorite) {
                            Icon(
                                imageVector = Icons.Rounded.Star,
                                contentDescription = "Favorite",
                                tint = AppColors.StarGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        if (clip.isLocked) {
                            Icon(
                                imageVector = if (isProtected) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                                contentDescription = if (isProtected) "Locked" else "Unlocked",
                                tint = AppColors.LockBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Date row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarToday,
                        contentDescription = null,
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = formatClipDate(clip.updatedAt),
                        fontSize = 11.5.sp,
                        color = AppColors.TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(9.dp))

                // Body preview
                when {
                    isProtected -> {
                        Text(
                            text = "*****",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 2.sp,
                            color = AppColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to unlock",
                            fontSize = 11.sp,
                            color = AppColors.LockBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    clip.clipType == ClipType.CHECKLIST && clip.checklistItems.isNotEmpty() -> {
                        val previewItems = clip.checklistItems.take(5)
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            previewItems.forEach { item ->
                                Text(
                                    text = item.text,
                                    fontSize = 13.5.sp,
                                    lineHeight = 18.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (item.isDone) AppColors.TextStrikethrough else AppColors.TextPrimary,
                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                )
                            }
                            if (clip.checklistItems.size > 5) {
                                Text(
                                    text = "+${clip.checklistItems.size - 5} more",
                                    fontSize = 11.5.sp,
                                    color = AppColors.TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    clip.clipType == ClipType.LINK -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color.White.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 0.75.dp,
                                    color = borderColor.copy(alpha = 0.8f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Link,
                                contentDescription = null,
                                tint = AppColors.TealPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = clip.content,
                                fontSize = 12.5.sp,
                                color = AppColors.TealPrimaryDark,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 17.sp
                            )
                        }
                    }

                    else -> {
                        Text(
                            text = clip.content,
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            color = AppColors.TextPrimary,
                            maxLines = 6,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Subtle footer with copy action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val badgeText = when {
                        clip.clipType == ClipType.CHECKLIST -> {
                            val items = clip.checklistItems
                            val done = items.count { it.isDone }
                            "$done/${items.size} done"
                        }
                        clip.copyCount > 0 -> "Copied ${clip.copyCount}×"
                        else -> clip.sourceApp
                    }
                    Text(
                        text = badgeText,
                        fontSize = 10.5.sp,
                        color = AppColors.TextSecondary.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Surface(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .clickable {
                                if (!isProtected) {
                                    justCopied = true
                                }
                                onQuickCopy()
                            },
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.72f),
                        border = BorderStroke(0.6.dp, borderColor.copy(alpha = 0.7f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            AnimatedContent(
                                targetState = justCopied,
                                label = "copy_icon"
                            ) { copied ->
                                Icon(
                                    imageVector = if (copied) Icons.Rounded.Check else Icons.Outlined.ContentCopy,
                                    contentDescription = "Copy clip",
                                    tint = if (copied) AppColors.TealPrimary else AppColors.TextSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Signature 3D Red Pushpin on pinned cards
        if (clip.isPinned) {
            PushPinGraphic(
                size = 27.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-10).dp)
            )
        }
    }
}

fun formatClipDate(epochMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val zone = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}
