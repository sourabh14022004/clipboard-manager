package com.clipboardmanager.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipTextHelperTest {

    @Test
    fun `detectClipType identifies URLs as LINK`() {
        assertEquals(ClipType.LINK, ClipTextHelper.detectClipType("https://developer.android.com/compose"))
        assertEquals(ClipType.LINK, ClipTextHelper.detectClipType("Check out www.kotlinlang.org/docs"))
    }

    @Test
    fun `detectClipType identifies markdown checklist lines as CHECKLIST`() {
        val raw = """
            - [x] Jenny's Gift
            - [ ] Buy Cake
            - [ ] Decoration
        """.trimIndent()
        assertEquals(ClipType.CHECKLIST, ClipTextHelper.detectClipType(raw))
        val items = ClipTextHelper.plainTextToChecklist(raw)
        assertEquals(3, items.size)
        assertTrue(items[0].isDone)
        assertEquals("Jenny's Gift", items[0].text)
        assertFalse(items[1].isDone)
    }

    @Test
    fun `generateSmartTitle formats URL host cleanly`() {
        val title = ClipTextHelper.generateSmartTitle(
            "https://www.github.com/android/compose-samples",
            ClipType.LINK
        )
        assertEquals("Github.com", title)
    }

    @Test
    fun `isLongText checks length and line count thresholds`() {
        assertFalse(ClipTextHelper.isLongText("Short reminder"))
        assertTrue(ClipTextHelper.isLongText("Line 1\nLine 2\nLine 3"))
        assertTrue(ClipTextHelper.isLongText("a".repeat(100)))
    }

    @Test
    fun `checklist JSON round trip preserves items`() {
        val items = listOf(
            ChecklistItem(id = "1", text = "Sleep more", isDone = true),
            ChecklistItem(id = "2", text = "Gym workout", isDone = false)
        )
        val encoded = ClipTextHelper.encodeChecklist(items)
        val decoded = ClipTextHelper.parseChecklist(encoded)
        assertEquals(items, decoded)
    }

    @Test
    fun `backup payload encodes and decodes cleanly`() {
        val payload = ClipBackupPayload(
            exportedAt = 1700000000000L,
            items = listOf(
                ClipBackupItem(
                    title = "Birthday Party",
                    content = "Buy Cake",
                    checklist = listOf(ChecklistItem(id = "a", text = "Buy Cake", isDone = false)),
                    clipType = ClipType.CHECKLIST.name,
                    cardColor = CardColor.SKY_BLUE.name,
                    isPinned = true,
                    isFavorite = false,
                    isLocked = false,
                    sourceApp = "Manual",
                    createdAt = 1700000000000L,
                    updatedAt = 1700000000000L,
                    copyCount = 2
                )
            )
        )
        val json = ClipTextHelper.encodeBackup(payload)
        val decoded = ClipTextHelper.decodeBackup(json)
        assertNotNull(decoded)
        assertEquals(1, decoded?.items?.size)
        assertEquals("Birthday Party", decoded?.items?.first()?.title)
    }
}
