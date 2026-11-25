package com.clipboardmanager.app.ui

import com.clipboardmanager.app.data.local.AppSettings
import com.clipboardmanager.app.data.local.ClipDao
import com.clipboardmanager.app.data.local.ClipEntity
import com.clipboardmanager.app.data.local.SettingsStore
import com.clipboardmanager.app.data.model.CardColor
import com.clipboardmanager.app.data.model.ClipCategory
import com.clipboardmanager.app.data.model.ClipFilterChip
import com.clipboardmanager.app.data.model.ClipType
import com.clipboardmanager.app.data.model.SortOrder
import com.clipboardmanager.app.data.repository.CaptureResult
import com.clipboardmanager.app.data.repository.DefaultClipRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ClipFilterAndViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filterAndSortClips filters by category, time chip, length, and locked search privacy`() {
        val now = 1_750_000_000_000L
        val zone = ZoneId.of("UTC")

        val clips = listOf(
            ClipEntity(
                id = 1,
                title = "Pinned Today Short",
                content = "Buy milk",
                clipType = ClipType.NOTE,
                isPinned = true,
                updatedAt = now - TimeUnit.MINUTES.toMillis(15)
            ),
            ClipEntity(
                id = 2,
                title = "Favorite Link",
                content = "https://kotlinlang.org",
                clipType = ClipType.LINK,
                isFavorite = true,
                updatedAt = now - TimeUnit.DAYS.toMillis(3)
            ),
            ClipEntity(
                id = 3,
                title = "My Dreams",
                content = "Secret personal goal",
                clipType = ClipType.NOTE,
                isPinned = true,
                isLocked = true,
                updatedAt = now - TimeUnit.DAYS.toMillis(10)
            ),
            ClipEntity(
                id = 4,
                title = "Long Essay",
                content = "A".repeat(140),
                clipType = ClipType.NOTE,
                updatedAt = now - TimeUnit.DAYS.toMillis(2)
            )
        )

        // Category: LINKS
        val links = filterAndSortClips(clips, ClipCategory.LINKS, ClipFilterChip.ALL, "", SortOrder.NEWEST, nowMillis = now, zoneId = zone)
        assertEquals(listOf(2L), links.map { it.id })

        // Chip: TODAY
        val today = filterAndSortClips(clips, ClipCategory.ALL, ClipFilterChip.TODAY, "", SortOrder.NEWEST, nowMillis = now, zoneId = zone)
        assertEquals(listOf(1L), today.map { it.id })

        // Chip: THIS_WEEK
        val thisWeek = filterAndSortClips(clips, ClipCategory.ALL, ClipFilterChip.THIS_WEEK, "", SortOrder.NEWEST, nowMillis = now, zoneId = zone)
        assertEquals(listOf(1L, 4L, 2L), thisWeek.map { it.id })

        // Chip: LONG_TEXT
        val longText = filterAndSortClips(clips, ClipCategory.ALL, ClipFilterChip.LONG_TEXT, "", SortOrder.NEWEST, nowMillis = now, zoneId = zone)
        assertEquals(listOf(4L), longText.map { it.id })

        // Search must NOT match hidden content of locked clip unless unlocked
        val lockedSearch = filterAndSortClips(clips, ClipCategory.ALL, ClipFilterChip.ALL, "Secret", SortOrder.NEWEST, unlockedClipIds = emptySet(), nowMillis = now, zoneId = zone)
        assertTrue(lockedSearch.isEmpty())

        val unlockedSearch = filterAndSortClips(clips, ClipCategory.ALL, ClipFilterChip.ALL, "Secret", SortOrder.NEWEST, unlockedClipIds = setOf(3L), nowMillis = now, zoneId = zone)
        assertEquals(listOf(3L), unlockedSearch.map { it.id })
    }

    @Test
    fun `repository deduplicates clips and detects links`() = runTest(testDispatcher) {
        var now = 1_000_000L
        val fakeDao = FakeClipDao()
        val fakeSettings = FakeSettingsStore()
        val repo = DefaultClipRepository(fakeDao, fakeSettings) { now }

        val first = repo.captureClipboardText("https://developer.android.com", "Clipboard")
        assertTrue(first is CaptureResult.Added)
        val firstClip = (first as CaptureResult.Added).clip
        assertEquals(ClipType.LINK, firstClip.clipType)

        // Capture a different item so the link isn't the latest
        now += 1000L
        repo.captureClipboardText("Quick note", "Clipboard")

        // Capturing the same link again bumps existing item instead of duplicating
        now += 1000L
        val dup = repo.captureClipboardText("https://developer.android.com", "Clipboard")
        assertTrue(dup is CaptureResult.UpdatedExisting)
        assertEquals(2, fakeDao.countClips())
    }

    @Test
    fun `repository seeds initial samples once and exports-imports backup`() = runTest(testDispatcher) {
        val fakeDao = FakeClipDao()
        val fakeSettings = FakeSettingsStore()
        val repo = DefaultClipRepository(fakeDao, fakeSettings) { 1_750_000_000_000L }

        repo.seedInitialSamplesIfNeeded()
        val seededCount = fakeDao.countClips()
        assertTrue(seededCount >= 5)

        val backup = repo.exportBackupJson()
        val freshDao = FakeClipDao()
        val freshRepo = DefaultClipRepository(freshDao, FakeSettingsStore()) { 1_750_000_000_000L }
        val imported = freshRepo.importBackupJson(backup)
        assertEquals(seededCount, imported)
        assertFalse(backup.isBlank())
    }

    @Test
    fun `viewModel supports creating checklist, locking with PIN, unlocking, and undo delete`() = runTest(testDispatcher) {
        val fakeDao = FakeClipDao()
        val fakeSettings = FakeSettingsStore()
        val repo = DefaultClipRepository(fakeDao, fakeSettings) { 1_750_000_000_000L }
        val vm = ClipboardViewModel(repo) { 1_750_000_000_000L }
        backgroundScope.launch { vm.uiState.collect {} }
        advanceUntilIdle()

        // Create a new checklist clip via editor
        vm.openNewClipEditor(null)
        vm.switchEditorType(ClipType.CHECKLIST)
        vm.updateEditor { it.copy(title = "Weekend Errands", cardColor = CardColor.MINT_CYAN, isPinned = true) }
        vm.addChecklistItemInEditor("Buy groceries")
        vm.addChecklistItemInEditor("Wash car")
        advanceUntilIdle()
        val firstItemId = vm.uiState.value.editorState!!.checklistItems.first().id
        vm.toggleChecklistItemInEditor(firstItemId)
        vm.saveEditor()
        advanceUntilIdle()

        val created = fakeDao.getAllClipsOnce().find { it.title == "Weekend Errands" }!!
        assertEquals(ClipType.CHECKLIST, created.clipType)
        assertTrue(created.checklistItems.first().isDone)
        assertFalse(created.checklistItems.last().isDone)

        // Lock it by setting a new PIN
        vm.toggleLockFromQuickActions(created)
        vm.submitPin("1234")
        advanceUntilIdle()

        val lockedClip = fakeDao.getClipById(created.id)!!
        assertTrue(lockedClip.isLocked)

        // Clicking locked clip prompts unlock; wrong PIN shows error; right PIN opens editor
        vm.onCardClick(lockedClip)
        vm.submitPin("9999")
        advanceUntilIdle()
        assertEquals("Incorrect PIN. Try again.", vm.uiState.value.pinPromptState?.errorMessage)

        vm.submitPin("1234")
        advanceUntilIdle()
        assertEquals(lockedClip.id, vm.uiState.value.editorState?.existingClip?.id)

        // Delete and undo restore
        vm.deleteClip(lockedClip)
        advanceUntilIdle()
        assertEquals(null, fakeDao.getClipById(lockedClip.id))

        vm.undoDelete(lockedClip)
        advanceUntilIdle()
        assertEquals(lockedClip.title, fakeDao.getClipById(lockedClip.id)?.title)
    }
}

private class FakeClipDao : ClipDao {
    private val items = mutableListOf<ClipEntity>()
    private val flow = MutableStateFlow<List<ClipEntity>>(emptyList())
    private var nextId = 1L

    private fun emit() {
        flow.value = items.sortedWith(
            compareByDescending<ClipEntity> { it.isPinned }.thenByDescending { it.updatedAt }
        )
    }

    override fun observeAllClips(): Flow<List<ClipEntity>> = flow.asStateFlow()

    override suspend fun getAllClipsOnce(): List<ClipEntity> = flow.value

    override suspend fun getClipById(id: Long): ClipEntity? = items.find { it.id == id }

    override suspend fun getLatestClip(): ClipEntity? = items.maxByOrNull { it.updatedAt }

    override suspend fun findByContent(content: String): ClipEntity? = items.find { it.content == content }

    override suspend fun insert(clip: ClipEntity): Long {
        val assigned = if (clip.id == 0L) clip.copy(id = nextId++) else clip
        items.removeAll { it.id == assigned.id }
        items.add(assigned)
        emit()
        return assigned.id
    }

    override suspend fun insertAll(clips: List<ClipEntity>) {
        clips.forEach { insert(it) }
    }

    override suspend fun update(clip: ClipEntity) {
        val index = items.indexOfFirst { it.id == clip.id }
        if (index >= 0) {
            items[index] = clip
            emit()
        }
    }

    override suspend fun deleteById(id: Long) {
        items.removeAll { it.id == id }
        emit()
    }

    override suspend fun deleteUnlockedNonPinnedHistory(): Int {
        val before = items.size
        items.removeAll { !it.isPinned && !it.isFavorite && !it.isLocked }
        emit()
        return before - items.size
    }

    override suspend fun countClips(): Int = items.size

    override suspend fun trimOldHistory(keepCount: Int) = Unit
}

private class FakeSettingsStore : SettingsStore {
    private val state = MutableStateFlow(AppSettings())
    private var storedPin: String? = null

    override val settingsFlow: Flow<AppSettings> = state.asStateFlow()

    override suspend fun setAutoCapture(enabled: Boolean) {
        state.value = state.value.copy(autoCaptureEnabled = enabled)
    }

    override suspend fun setDeduplicateClips(enabled: Boolean) {
        state.value = state.value.copy(deduplicateClips = enabled)
    }

    override suspend fun setMonitorNotification(enabled: Boolean) {
        state.value = state.value.copy(monitorNotificationEnabled = enabled)
    }

    override suspend fun setSortOrder(sortOrder: SortOrder) {
        state.value = state.value.copy(sortOrder = sortOrder)
    }

    override suspend fun setRetentionLimit(limit: Int) {
        state.value = state.value.copy(retentionLimit = limit)
    }

    override suspend fun markSamplesSeeded() {
        state.value = state.value.copy(hasSeededSamples = true)
    }

    override suspend fun consumeNextColorIndex(): Int {
        val current = state.value.nextColorIndex
        state.value = state.value.copy(nextColorIndex = (current + 1) % 6)
        return current
    }

    override suspend fun setVaultPin(pin: String) {
        storedPin = pin
        state.value = state.value.copy(hasPinConfigured = true)
    }

    override suspend fun verifyVaultPin(pin: String): Boolean = storedPin == pin

    override suspend fun clearVaultPin() {
        storedPin = null
        state.value = state.value.copy(hasPinConfigured = false)
    }
}
