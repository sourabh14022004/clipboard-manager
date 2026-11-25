package com.clipboardmanager.app.data.repository

import com.clipboardmanager.app.data.local.AppSettings
import com.clipboardmanager.app.data.local.ClipDao
import com.clipboardmanager.app.data.local.ClipEntity
import com.clipboardmanager.app.data.local.SettingsStore
import com.clipboardmanager.app.data.model.CardColor
import com.clipboardmanager.app.data.model.ChecklistItem
import com.clipboardmanager.app.data.model.ClipBackupItem
import com.clipboardmanager.app.data.model.ClipBackupPayload
import com.clipboardmanager.app.data.model.ClipTextHelper
import com.clipboardmanager.app.data.model.ClipType
import com.clipboardmanager.app.data.model.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

sealed interface CaptureResult {
    data class Added(val clip: ClipEntity) : CaptureResult
    data class UpdatedExisting(val clip: ClipEntity) : CaptureResult
    data object IgnoredEmpty : CaptureResult
    data object IgnoredDuplicateLatest : CaptureResult
}

interface ClipRepository {
    val clipsFlow: Flow<List<ClipEntity>>
    val settingsFlow: Flow<AppSettings>

    suspend fun seedInitialSamplesIfNeeded()
    suspend fun captureClipboardText(text: String, sourceApp: String = "Clipboard"): CaptureResult
    suspend fun saveClip(
        existingId: Long? = null,
        title: String,
        content: String,
        checklist: List<ChecklistItem> = emptyList(),
        clipType: ClipType,
        cardColor: CardColor,
        isPinned: Boolean,
        isFavorite: Boolean,
        isLocked: Boolean,
        sourceApp: String = "Manual"
    ): ClipEntity

    suspend fun togglePin(clip: ClipEntity)
    suspend fun toggleFavorite(clip: ClipEntity)
    suspend fun setLocked(clip: ClipEntity, locked: Boolean)
    suspend fun toggleChecklistItem(clip: ClipEntity, itemId: String)
    suspend fun recordCopy(clip: ClipEntity)
    suspend fun deleteClip(id: Long)
    suspend fun restoreClip(clip: ClipEntity)
    suspend fun clearUnlockedHistory(): Int

    suspend fun setAutoCapture(enabled: Boolean)
    suspend fun setDeduplicateClips(enabled: Boolean)
    suspend fun setMonitorNotification(enabled: Boolean)
    suspend fun setSortOrder(sortOrder: SortOrder)
    suspend fun setVaultPin(pin: String)
    suspend fun verifyVaultPin(pin: String): Boolean
    suspend fun clearVaultPin()

    suspend fun exportBackupJson(): String
    suspend fun importBackupJson(rawJson: String): Int
}

class DefaultClipRepository(
    private val clipDao: ClipDao,
    private val settingsStore: SettingsStore,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ClipRepository {

    override val clipsFlow: Flow<List<ClipEntity>> = clipDao.observeAllClips()
    override val settingsFlow: Flow<AppSettings> = settingsStore.settingsFlow

    override suspend fun seedInitialSamplesIfNeeded() {
        val settings = settingsStore.settingsFlow.first()
        if (settings.hasSeededSamples || clipDao.countClips() > 0) {
            if (!settings.hasSeededSamples) settingsStore.markSamplesSeeded()
            return
        }

        val now = clock()
        val birthdayItems = listOf(
            ChecklistItem(text = "Jenny's Gift", isDone = true),
            ChecklistItem(text = "Buy Cake", isDone = false),
            ChecklistItem(text = "Decoration", isDone = false)
        )
        val workItems = listOf(
            ChecklistItem(text = "Blog Writing", isDone = true),
            ChecklistItem(text = "Content marketing", isDone = true),
            ChecklistItem(text = "Data Analysis", isDone = false)
        )
        val todoItems = listOf(
            ChecklistItem(text = "Sleep more", isDone = true),
            ChecklistItem(text = "Learn piano", isDone = true),
            ChecklistItem(text = "Do homework", isDone = true),
            ChecklistItem(text = "Gym workout", isDone = false),
            ChecklistItem(text = "Night out", isDone = false)
        )
        val examItems = listOf(
            ChecklistItem(text = "Chapter 1", isDone = true),
            ChecklistItem(text = "Lab notes summary", isDone = false)
        )

        val samples = listOf(
            ClipEntity(
                title = "Birthday Party",
                content = ClipTextHelper.checklistToPlainText(birthdayItems),
                checklistJson = ClipTextHelper.encodeChecklist(birthdayItems),
                clipType = ClipType.CHECKLIST,
                cardColor = CardColor.SKY_BLUE,
                isPinned = true,
                isFavorite = false,
                isLocked = false,
                sourceApp = "Sample",
                createdAt = now - TimeUnit.HOURS.toMillis(2),
                updatedAt = now - TimeUnit.HOURS.toMillis(1)
            ),
            ClipEntity(
                title = "Work Task",
                content = ClipTextHelper.checklistToPlainText(workItems),
                checklistJson = ClipTextHelper.encodeChecklist(workItems),
                clipType = ClipType.CHECKLIST,
                cardColor = CardColor.BLUSH_PINK,
                isPinned = true,
                isFavorite = true,
                isLocked = false,
                sourceApp = "Sample",
                createdAt = now - TimeUnit.HOURS.toMillis(5),
                updatedAt = now - TimeUnit.HOURS.toMillis(3)
            ),
            ClipEntity(
                title = "To-Do List",
                content = ClipTextHelper.checklistToPlainText(todoItems),
                checklistJson = ClipTextHelper.encodeChecklist(todoItems),
                clipType = ClipType.CHECKLIST,
                cardColor = CardColor.MINT_CYAN,
                isPinned = true,
                isFavorite = true,
                isLocked = false,
                sourceApp = "Sample",
                createdAt = now - TimeUnit.DAYS.toMillis(2),
                updatedAt = now - TimeUnit.DAYS.toMillis(1)
            ),
            ClipEntity(
                title = "My Dreams",
                content = "Launch my own indie productivity app, travel through Japan in autumn, and master acoustic guitar.",
                clipType = ClipType.NOTE,
                cardColor = CardColor.SKY_BLUE,
                isPinned = true,
                isFavorite = false,
                isLocked = true,
                sourceApp = "Vault",
                createdAt = now - TimeUnit.DAYS.toMillis(4),
                updatedAt = now - TimeUnit.DAYS.toMillis(3)
            ),
            ClipEntity(
                title = "Science Exam",
                content = ClipTextHelper.checklistToPlainText(examItems),
                checklistJson = ClipTextHelper.encodeChecklist(examItems),
                clipType = ClipType.CHECKLIST,
                cardColor = CardColor.WARM_PEACH,
                isPinned = true,
                isFavorite = false,
                isLocked = false,
                sourceApp = "Sample",
                createdAt = now - TimeUnit.DAYS.toMillis(6),
                updatedAt = now - TimeUnit.DAYS.toMillis(5)
            ),
            ClipEntity(
                title = "Compose Staggered Grid Docs",
                content = "https://developer.android.com/develop/ui/compose/lists",
                clipType = ClipType.LINK,
                cardColor = CardColor.BUTTER_YELLOW,
                isPinned = false,
                isFavorite = true,
                isLocked = false,
                sourceApp = "Browser",
                createdAt = now - TimeUnit.MINUTES.toMillis(35),
                updatedAt = now - TimeUnit.MINUTES.toMillis(35)
            ),
            ClipEntity(
                title = "Meeting Recap & Action Items",
                content = "Finalize Q4 release checklist, verify offline Room caching, and share APK build with QA team before Friday afternoon.",
                clipType = ClipType.NOTE,
                cardColor = CardColor.SOFT_LAVENDER,
                isPinned = false,
                isFavorite = false,
                isLocked = false,
                sourceApp = "Clipboard",
                createdAt = now - TimeUnit.HOURS.toMillis(8),
                updatedAt = now - TimeUnit.HOURS.toMillis(8)
            )
        )

        clipDao.insertAll(samples)
        settingsStore.markSamplesSeeded()
    }

    override suspend fun captureClipboardText(text: String, sourceApp: String): CaptureResult {
        val normalized = text.trim()
        if (normalized.isBlank()) return CaptureResult.IgnoredEmpty

        val latest = clipDao.getLatestClip()
        if (latest != null && latest.content == normalized) {
            return CaptureResult.IgnoredDuplicateLatest
        }

        val settings = settingsStore.settingsFlow.first()
        val now = clock()

        if (settings.deduplicateClips) {
            val existing = clipDao.findByContent(normalized)
            if (existing != null) {
                val updated = existing.copy(
                    updatedAt = now,
                    sourceApp = sourceApp
                )
                clipDao.update(updated)
                return CaptureResult.UpdatedExisting(updated)
            }
        }

        val detectedType = ClipTextHelper.detectClipType(normalized)
        val checklist = if (detectedType == ClipType.CHECKLIST) {
            ClipTextHelper.plainTextToChecklist(normalized)
        } else {
            emptyList()
        }
        val cleanContent = if (checklist.isNotEmpty()) {
            ClipTextHelper.checklistToPlainText(checklist)
        } else {
            normalized
        }
        val color = CardColor.forIndex(settingsStore.consumeNextColorIndex())
        val title = ClipTextHelper.generateSmartTitle(cleanContent, detectedType)

        val clip = ClipEntity(
            title = title,
            content = cleanContent,
            checklistJson = ClipTextHelper.encodeChecklist(checklist),
            clipType = detectedType,
            cardColor = color,
            isPinned = false,
            isFavorite = false,
            isLocked = false,
            sourceApp = sourceApp,
            createdAt = now,
            updatedAt = now
        )
        val newId = clipDao.insert(clip)
        clipDao.trimOldHistory(settings.retentionLimit)
        return CaptureResult.Added(clip.copy(id = newId))
    }

    override suspend fun saveClip(
        existingId: Long?,
        title: String,
        content: String,
        checklist: List<ChecklistItem>,
        clipType: ClipType,
        cardColor: CardColor,
        isPinned: Boolean,
        isFavorite: Boolean,
        isLocked: Boolean,
        sourceApp: String
    ): ClipEntity {
        val now = clock()
        val resolvedContent = if (clipType == ClipType.CHECKLIST && checklist.isNotEmpty()) {
            ClipTextHelper.checklistToPlainText(checklist)
        } else {
            content.trim()
        }
        val resolvedTitle = title.trim().ifBlank {
            ClipTextHelper.generateSmartTitle(resolvedContent, clipType)
        }
        val checklistJson = if (clipType == ClipType.CHECKLIST) {
            ClipTextHelper.encodeChecklist(checklist)
        } else {
            ""
        }

        val existing = existingId?.let { clipDao.getClipById(it) }
        return if (existing != null) {
            val updated = existing.copy(
                title = resolvedTitle,
                content = resolvedContent,
                checklistJson = checklistJson,
                clipType = clipType,
                cardColor = cardColor,
                isPinned = isPinned,
                isFavorite = isFavorite,
                isLocked = isLocked,
                updatedAt = now
            )
            clipDao.update(updated)
            updated
        } else {
            val newClip = ClipEntity(
                title = resolvedTitle,
                content = resolvedContent,
                checklistJson = checklistJson,
                clipType = clipType,
                cardColor = cardColor,
                isPinned = isPinned,
                isFavorite = isFavorite,
                isLocked = isLocked,
                sourceApp = sourceApp,
                createdAt = now,
                updatedAt = now
            )
            val id = clipDao.insert(newClip)
            newClip.copy(id = id)
        }
    }

    override suspend fun togglePin(clip: ClipEntity) {
        clipDao.update(clip.copy(isPinned = !clip.isPinned, updatedAt = clock()))
    }

    override suspend fun toggleFavorite(clip: ClipEntity) {
        clipDao.update(clip.copy(isFavorite = !clip.isFavorite))
    }

    override suspend fun setLocked(clip: ClipEntity, locked: Boolean) {
        clipDao.update(clip.copy(isLocked = locked, updatedAt = clock()))
    }

    override suspend fun toggleChecklistItem(clip: ClipEntity, itemId: String) {
        val updatedItems = clip.checklistItems.map { item ->
            if (item.id == itemId) item.copy(isDone = !item.isDone) else item
        }
        clipDao.update(
            clip.copy(
                checklistJson = ClipTextHelper.encodeChecklist(updatedItems),
                content = ClipTextHelper.checklistToPlainText(updatedItems),
                updatedAt = clock()
            )
        )
    }

    override suspend fun recordCopy(clip: ClipEntity) {
        clipDao.update(clip.copy(copyCount = clip.copyCount + 1))
    }

    override suspend fun deleteClip(id: Long) {
        clipDao.deleteById(id)
    }

    override suspend fun restoreClip(clip: ClipEntity) {
        clipDao.insert(clip)
    }

    override suspend fun clearUnlockedHistory(): Int = clipDao.deleteUnlockedNonPinnedHistory()

    override suspend fun setAutoCapture(enabled: Boolean) = settingsStore.setAutoCapture(enabled)

    override suspend fun setDeduplicateClips(enabled: Boolean) = settingsStore.setDeduplicateClips(enabled)

    override suspend fun setMonitorNotification(enabled: Boolean) = settingsStore.setMonitorNotification(enabled)

    override suspend fun setSortOrder(sortOrder: SortOrder) = settingsStore.setSortOrder(sortOrder)

    override suspend fun setVaultPin(pin: String) = settingsStore.setVaultPin(pin)

    override suspend fun verifyVaultPin(pin: String): Boolean = settingsStore.verifyVaultPin(pin)

    override suspend fun clearVaultPin() = settingsStore.clearVaultPin()

    override suspend fun exportBackupJson(): String {
        val all = clipDao.getAllClipsOnce()
        val payload = ClipBackupPayload(
            exportedAt = clock(),
            items = all.map { clip ->
                ClipBackupItem(
                    title = clip.title,
                    content = clip.content,
                    checklist = clip.checklistItems,
                    clipType = clip.clipType.name,
                    cardColor = clip.cardColor.name,
                    isPinned = clip.isPinned,
                    isFavorite = clip.isFavorite,
                    isLocked = clip.isLocked,
                    sourceApp = clip.sourceApp,
                    createdAt = clip.createdAt,
                    updatedAt = clip.updatedAt,
                    copyCount = clip.copyCount
                )
            }
        )
        return ClipTextHelper.encodeBackup(payload)
    }

    override suspend fun importBackupJson(rawJson: String): Int {
        val payload = ClipTextHelper.decodeBackup(rawJson) ?: return 0
        val existingContents = clipDao.getAllClipsOnce().map { it.content }.toHashSet()
        val toInsert = payload.items
            .filter { it.content.isNotBlank() && it.content !in existingContents }
            .map { item ->
                val type = runCatching { ClipType.valueOf(item.clipType) }.getOrDefault(ClipType.NOTE)
                val color = runCatching { CardColor.valueOf(item.cardColor) }.getOrDefault(CardColor.SKY_BLUE)
                ClipEntity(
                    title = item.title,
                    content = item.content,
                    checklistJson = if (type == ClipType.CHECKLIST) ClipTextHelper.encodeChecklist(item.checklist) else "",
                    clipType = type,
                    cardColor = color,
                    isPinned = item.isPinned,
                    isFavorite = item.isFavorite,
                    isLocked = item.isLocked,
                    sourceApp = item.sourceApp,
                    createdAt = item.createdAt,
                    updatedAt = item.updatedAt,
                    copyCount = item.copyCount
                )
            }
        if (toInsert.isNotEmpty()) {
            clipDao.insertAll(toInsert)
        }
        return toInsert.size
    }
}
