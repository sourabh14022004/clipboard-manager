package com.clipboardmanager.app.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

enum class ClipCategory(val label: String) {
    ALL("All"),
    PINNED("Pinned"),
    FAVORITES("Favorites"),
    NOTES("Notes"),
    LINKS("Links")
}

enum class ClipFilterChip(val label: String) {
    ALL("All"),
    TODAY("Today"),
    THIS_WEEK("This Week"),
    LONG_TEXT("Long Text"),
    SHORT("Short")
}

enum class SortOrder(val label: String) {
    NEWEST("Newest First"),
    OLDEST("Oldest First"),
    TITLE_ASC("Title A–Z"),
    MOST_COPIED("Most Copied")
}

enum class ClipType {
    NOTE,
    CHECKLIST,
    LINK
}

enum class CardColor(
    val displayName: String,
    val containerHex: Long,
    val borderHex: Long
) {
    SKY_BLUE("Sky Blue", 0xFFDCF0FF, 0xFF8CC4F2),
    BLUSH_PINK("Blush Pink", 0xFFFFE2EB, 0xFFF0A5BD),
    MINT_CYAN("Mint Cyan", 0xFFCFF9F9, 0xFF7ADCE0),
    WARM_PEACH("Warm Peach", 0xFFFFEAD6, 0xFFEEBD90),
    BUTTER_YELLOW("Butter Yellow", 0xFFFFF6CC, 0xFFE8D27A),
    SOFT_LAVENDER("Soft Lavender", 0xFFECE6FF, 0xFFBEB0F0);

    companion object {
        fun forIndex(index: Int): CardColor {
            val palette = listOf(SKY_BLUE, BLUSH_PINK, MINT_CYAN, WARM_PEACH, BUTTER_YELLOW, SOFT_LAVENDER)
            return palette[Math.floorMod(index, palette.size)]
        }
    }
}

@Serializable
data class ChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false
)

@Serializable
data class ClipBackupPayload(
    val version: Int = 1,
    val exportedAt: Long,
    val items: List<ClipBackupItem>
)

@Serializable
data class ClipBackupItem(
    val title: String,
    val content: String,
    val checklist: List<ChecklistItem> = emptyList(),
    val clipType: String,
    val cardColor: String,
    val isPinned: Boolean,
    val isFavorite: Boolean,
    val isLocked: Boolean,
    val sourceApp: String,
    val createdAt: Long,
    val updatedAt: Long,
    val copyCount: Int
)

object ClipTextHelper {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val urlRegex = Regex(
        pattern = """(https?://[^\s]+|www\.[^\s]+\.[a-zA-Z]{2,})""",
        option = RegexOption.IGNORE_CASE
    )

    fun extractUrls(text: String): List<String> =
        urlRegex.findAll(text).map { it.value.trimEnd('.', ',', ')', ';') }.distinct().toList()

    fun containsUrl(text: String): Boolean = urlRegex.containsMatchIn(text)

    fun detectClipType(text: String): ClipType {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return ClipType.NOTE
        val lines = trimmed.lines().filter { it.isNotBlank() }
        val looksLikeChecklist = lines.size >= 2 && lines.all { line ->
            val l = line.trimStart()
            l.startsWith("- [ ] ") || l.startsWith("- [x] ", ignoreCase = true) ||
                l.startsWith("* [ ] ") || l.startsWith("* [x] ", ignoreCase = true) ||
                l.startsWith("☐ ") || l.startsWith("☑ ")
        }
        if (looksLikeChecklist) return ClipType.CHECKLIST

        val urls = extractUrls(trimmed)
        if (urls.isNotEmpty() && (lines.size == 1 || trimmed.length <= urls.first().length + 40)) {
            return ClipType.LINK
        }
        return ClipType.NOTE
    }

    fun generateSmartTitle(text: String, type: ClipType): String {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return "Untitled Clip"
        if (type == ClipType.LINK) {
            val firstUrl = extractUrls(trimmed).firstOrNull()
            if (firstUrl != null) {
                val cleaned = firstUrl
                    .removePrefix("https://")
                    .removePrefix("http://")
                    .removePrefix("www.")
                val host = cleaned.substringBefore('/').substringBefore('?')
                if (host.isNotBlank()) return host.replaceFirstChar { it.uppercase() }
            }
        }
        val firstLine = trimmed.lineSequence().firstOrNull { it.isNotBlank() }?.trim() ?: trimmed
        val cleanLine = firstLine
            .removePrefix("- [ ] ")
            .removePrefix("- [x] ")
            .removePrefix("- [X] ")
            .removePrefix("☐ ")
            .removePrefix("☑ ")
            .trim()
        return if (cleanLine.length <= 34) cleanLine else cleanLine.take(31).trimEnd() + "…"
    }

    fun isLongText(text: String): Boolean {
        val trimmed = text.trim()
        return trimmed.length >= 100 || trimmed.lines().count { it.isNotBlank() } >= 3
    }

    fun parseChecklist(rawJson: String): List<ChecklistItem> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching { json.decodeFromString<List<ChecklistItem>>(rawJson) }
            .getOrDefault(emptyList())
    }

    fun encodeChecklist(items: List<ChecklistItem>): String {
        if (items.isEmpty()) return ""
        return json.encodeToString(items)
    }

    fun checklistToPlainText(items: List<ChecklistItem>): String =
        items.joinToString("\n") { it.text }

    fun plainTextToChecklist(text: String): List<ChecklistItem> {
        return text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { line ->
                val isDone = line.startsWith("- [x] ", ignoreCase = true) ||
                    line.startsWith("* [x] ", ignoreCase = true) ||
                    line.startsWith("☑ ")
                val cleaned = line
                    .removePrefix("- [ ] ")
                    .removePrefix("- [x] ")
                    .removePrefix("- [X] ")
                    .removePrefix("* [ ] ")
                    .removePrefix("* [x] ")
                    .removePrefix("* [X] ")
                    .removePrefix("☐ ")
                    .removePrefix("☑ ")
                    .trim()
                ChecklistItem(text = cleaned, isDone = isDone)
            }
    }

    fun encodeBackup(payload: ClipBackupPayload): String = json.encodeToString(payload)

    fun decodeBackup(raw: String): ClipBackupPayload? =
        runCatching { json.decodeFromString<ClipBackupPayload>(raw) }.getOrNull()
}
