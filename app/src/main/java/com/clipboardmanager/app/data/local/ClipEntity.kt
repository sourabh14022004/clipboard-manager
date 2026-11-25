package com.clipboardmanager.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.clipboardmanager.app.data.model.CardColor
import com.clipboardmanager.app.data.model.ChecklistItem
import com.clipboardmanager.app.data.model.ClipTextHelper
import com.clipboardmanager.app.data.model.ClipType

@Entity(tableName = "clips")
data class ClipEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val checklistJson: String = "",
    val clipType: ClipType = ClipType.NOTE,
    val cardColor: CardColor = CardColor.SKY_BLUE,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false,
    val sourceApp: String = "Clipboard",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val copyCount: Int = 0
) {
    val checklistItems: List<ChecklistItem>
        get() = if (clipType == ClipType.CHECKLIST) {
            ClipTextHelper.parseChecklist(checklistJson)
        } else {
            emptyList()
        }

    val copyableText: String
        get() = if (clipType == ClipType.CHECKLIST && checklistItems.isNotEmpty()) {
            checklistItems.joinToString("\n") { it.text }
        } else {
            content
        }
}

class ClipConverters {
    @TypeConverter
    fun fromClipType(type: ClipType): String = type.name

    @TypeConverter
    fun toClipType(value: String): ClipType =
        runCatching { ClipType.valueOf(value) }.getOrDefault(ClipType.NOTE)

    @TypeConverter
    fun fromCardColor(color: CardColor): String = color.name

    @TypeConverter
    fun toCardColor(value: String): CardColor =
        runCatching { CardColor.valueOf(value) }.getOrDefault(CardColor.SKY_BLUE)
}
