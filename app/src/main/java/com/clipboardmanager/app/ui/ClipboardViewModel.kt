package com.clipboardmanager.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.clipboardmanager.app.data.local.AppSettings
import com.clipboardmanager.app.data.local.ClipEntity
import com.clipboardmanager.app.data.model.CardColor
import com.clipboardmanager.app.data.model.ChecklistItem
import com.clipboardmanager.app.data.model.ClipCategory
import com.clipboardmanager.app.data.model.ClipFilterChip
import com.clipboardmanager.app.data.model.ClipTextHelper
import com.clipboardmanager.app.data.model.ClipType
import com.clipboardmanager.app.data.model.SortOrder
import com.clipboardmanager.app.data.repository.CaptureResult
import com.clipboardmanager.app.data.repository.ClipRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

enum class PinFlowMode {
    UNLOCK_CLIP,
    SET_NEW_PIN_FOR_LOCK,
    SET_OR_CHANGE_PIN,
    REMOVE_PIN
}

data class PinPromptState(
    val mode: PinFlowMode,
    val targetClip: ClipEntity? = null,
    val errorMessage: String? = null,
    val pendingAction: PendingLockedAction = PendingLockedAction.OPEN_DETAIL
)

enum class PendingLockedAction {
    OPEN_DETAIL,
    COPY,
    SHARE,
    DELETE
}

data class ClipEditorState(
    val existingClip: ClipEntity? = null,
    val title: String = "",
    val content: String = "",
    val checklistItems: List<ChecklistItem> = emptyList(),
    val clipType: ClipType = ClipType.NOTE,
    val cardColor: CardColor = CardColor.SKY_BLUE,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false,
    val isLocked: Boolean = false
)

sealed interface UiEffect {
    data class ShowMessage(val message: String, val undoClip: ClipEntity? = null) : UiEffect
    data class CopyToSystemClipboard(val label: String, val text: String) : UiEffect
    data class ShareClip(val title: String, val text: String) : UiEffect
    data class OpenUrl(val url: String) : UiEffect
    data class ToggleForegroundMonitor(val enabled: Boolean) : UiEffect
}

data class ClipboardUiState(
    val clips: List<ClipEntity> = emptyList(),
    val categoryCounts: Map<ClipCategory, Int> = emptyMap(),
    val selectedCategory: ClipCategory = ClipCategory.ALL,
    val selectedFilterChip: ClipFilterChip = ClipFilterChip.ALL,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val settings: AppSettings = AppSettings(),
    val unlockedClipIds: Set<Long> = emptySet(),
    val pendingClipboardText: String? = null,
    val editorState: ClipEditorState? = null,
    val quickActionClip: ClipEntity? = null,
    val pinPromptState: PinPromptState? = null,
    val showSettingsSheet: Boolean = false
)

class ClipboardViewModel(
    private val repository: ClipRepository,
    private val clock: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private val selectedCategory = MutableStateFlow(ClipCategory.ALL)
    private val selectedFilterChip = MutableStateFlow(ClipFilterChip.ALL)
    private val searchQuery = MutableStateFlow("")
    private val isSearchActive = MutableStateFlow(false)
    private val unlockedClipIds = MutableStateFlow<Set<Long>>(emptySet())
    private val pendingClipboardText = MutableStateFlow<String?>(null)
    private val editorState = MutableStateFlow<ClipEditorState?>(null)
    private val quickActionClip = MutableStateFlow<ClipEntity?>(null)
    private val pinPromptState = MutableStateFlow<PinPromptState?>(null)
    private val showSettingsSheet = MutableStateFlow(false)

    private val _effects = MutableSharedFlow<UiEffect>()
    val effects: SharedFlow<UiEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialSamplesIfNeeded()
        }
    }

    private val filterInputs = combine(
        selectedCategory,
        selectedFilterChip,
        searchQuery,
        isSearchActive,
        unlockedClipIds
    ) { category, chip, query, searchOpen, unlocked ->
        FilterInputs(category, chip, query, searchOpen, unlocked)
    }

    private val modalInputs = combine(
        pendingClipboardText,
        editorState,
        quickActionClip,
        pinPromptState,
        showSettingsSheet
    ) { pending, editor, quickAction, pinPrompt, settingsSheet ->
        ModalInputs(pending, editor, quickAction, pinPrompt, settingsSheet)
    }

    val uiState: StateFlow<ClipboardUiState> = combine(
        repository.clipsFlow,
        repository.settingsFlow,
        filterInputs,
        modalInputs
    ) { allClips, settings, filters, modals ->
        val filtered = filterAndSortClips(
            clips = allClips,
            category = filters.category,
            filterChip = filters.chip,
            searchQuery = filters.query,
            sortOrder = settings.sortOrder,
            unlockedClipIds = filters.unlockedIds,
            nowMillis = clock()
        )
        val counts = computeCategoryCounts(allClips)
        ClipboardUiState(
            clips = filtered,
            categoryCounts = counts,
            selectedCategory = filters.category,
            selectedFilterChip = filters.chip,
            searchQuery = filters.query,
            isSearchActive = filters.searchOpen,
            settings = settings,
            unlockedClipIds = filters.unlockedIds,
            pendingClipboardText = modals.pendingClipboard,
            editorState = modals.editor,
            quickActionClip = modals.quickAction,
            pinPromptState = modals.pinPrompt,
            showSettingsSheet = modals.settingsSheet
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ClipboardUiState()
    )

    fun selectCategory(category: ClipCategory) {
        selectedCategory.value = category
    }

    fun selectFilterChip(chip: ClipFilterChip) {
        selectedFilterChip.value = chip
    }

    fun toggleSearch(active: Boolean) {
        isSearchActive.value = active
        if (!active) searchQuery.value = ""
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun onSystemClipboardSeen(text: String?, forceCapture: Boolean = false) {
        val cleaned = text?.trim()?.takeIf { it.isNotEmpty() } ?: run {
            pendingClipboardText.value = null
            return
        }
        viewModelScope.launch {
            val currentSettings = uiState.value.settings
            if (currentSettings.autoCaptureEnabled || forceCapture) {
                when (val result = repository.captureClipboardText(cleaned, "Clipboard")) {
                    is CaptureResult.Added -> {
                        pendingClipboardText.value = null
                        if (forceCapture) {
                            _effects.emit(UiEffect.ShowMessage("Saved from clipboard"))
                        }
                    }
                    is CaptureResult.UpdatedExisting -> {
                        pendingClipboardText.value = null
                        if (forceCapture) {
                            _effects.emit(UiEffect.ShowMessage("Moved existing clip to top"))
                        }
                    }
                    CaptureResult.IgnoredDuplicateLatest, CaptureResult.IgnoredEmpty -> {
                        pendingClipboardText.value = null
                    }
                }
            } else {
                val alreadySaved = uiState.value.clips.any { it.content == cleaned }
                pendingClipboardText.value = if (alreadySaved) null else cleaned
            }
        }
    }

    fun savePendingClipboard() {
        val text = pendingClipboardText.value ?: return
        onSystemClipboardSeen(text, forceCapture = true)
    }

    fun dismissPendingClipboard() {
        pendingClipboardText.value = null
    }

    fun onSharedTextReceived(text: String) {
        viewModelScope.launch {
            when (repository.captureClipboardText(text, sourceApp = "Shared")) {
                is CaptureResult.Added -> _effects.emit(UiEffect.ShowMessage("Clip saved"))
                is CaptureResult.UpdatedExisting -> _effects.emit(UiEffect.ShowMessage("Clip updated"))
                else -> Unit
            }
        }
    }

    fun onCardClick(clip: ClipEntity) {
        if (clip.isLocked && clip.id !in unlockedClipIds.value) {
            requestUnlock(clip, PendingLockedAction.OPEN_DETAIL)
        } else {
            openEditorFor(clip)
        }
    }

    fun onCardLongClick(clip: ClipEntity) {
        quickActionClip.value = clip
    }

    fun dismissQuickActions() {
        quickActionClip.value = null
    }

    fun onQuickCopy(clip: ClipEntity) {
        if (clip.isLocked && clip.id !in unlockedClipIds.value) {
            requestUnlock(clip, PendingLockedAction.COPY)
            return
        }
        performCopy(clip)
    }

    private fun performCopy(clip: ClipEntity) {
        viewModelScope.launch {
            repository.recordCopy(clip)
            _effects.emit(UiEffect.CopyToSystemClipboard(clip.title, clip.copyableText))
            _effects.emit(UiEffect.ShowMessage("Copied to clipboard"))
        }
    }

    fun onShareClip(clip: ClipEntity) {
        quickActionClip.value = null
        if (clip.isLocked && clip.id !in unlockedClipIds.value) {
            requestUnlock(clip, PendingLockedAction.SHARE)
            return
        }
        viewModelScope.launch {
            _effects.emit(UiEffect.ShareClip(clip.title, clip.copyableText))
        }
    }

    fun onOpenLink(clip: ClipEntity) {
        val url = ClipTextHelper.extractUrls(clip.content).firstOrNull() ?: clip.content
        viewModelScope.launch {
            _effects.emit(UiEffect.OpenUrl(url))
        }
    }

    fun togglePin(clip: ClipEntity) {
        quickActionClip.value = null
        viewModelScope.launch {
            repository.togglePin(clip)
        }
    }

    fun toggleFavorite(clip: ClipEntity) {
        quickActionClip.value = null
        viewModelScope.launch {
            repository.toggleFavorite(clip)
        }
    }

    fun toggleLockFromQuickActions(clip: ClipEntity) {
        quickActionClip.value = null
        if (clip.isLocked) {
            if (clip.id in unlockedClipIds.value || !uiState.value.settings.hasPinConfigured) {
                viewModelScope.launch {
                    repository.setLocked(clip, false)
                    unlockedClipIds.update { it - clip.id }
                    _effects.emit(UiEffect.ShowMessage("Clip unlocked"))
                }
            } else {
                requestUnlock(clip, PendingLockedAction.OPEN_DETAIL)
            }
        } else {
            if (uiState.value.settings.hasPinConfigured) {
                viewModelScope.launch {
                    repository.setLocked(clip, true)
                    unlockedClipIds.update { it - clip.id }
                    _effects.emit(UiEffect.ShowMessage("Clip locked"))
                }
            } else {
                pinPromptState.value = PinPromptState(
                    mode = PinFlowMode.SET_NEW_PIN_FOR_LOCK,
                    targetClip = clip
                )
            }
        }
    }

    fun deleteClip(clip: ClipEntity) {
        quickActionClip.value = null
        if (clip.isLocked && clip.id !in unlockedClipIds.value && uiState.value.settings.hasPinConfigured) {
            requestUnlock(clip, PendingLockedAction.DELETE)
            return
        }
        performDelete(clip)
    }

    private fun performDelete(clip: ClipEntity) {
        viewModelScope.launch {
            repository.deleteClip(clip.id)
            if (editorState.value?.existingClip?.id == clip.id) {
                editorState.value = null
            }
            _effects.emit(UiEffect.ShowMessage("Clip deleted", undoClip = clip))
        }
    }

    fun undoDelete(clip: ClipEntity) {
        viewModelScope.launch {
            repository.restoreClip(clip)
            _effects.emit(UiEffect.ShowMessage("Clip restored"))
        }
    }

    fun openNewClipEditor(prefillText: String? = null) {
        val initialText = prefillText.orEmpty()
        val detectedType = if (initialText.isNotBlank()) {
            ClipTextHelper.detectClipType(initialText)
        } else {
            ClipType.NOTE
        }
        val checklist = if (detectedType == ClipType.CHECKLIST) {
            ClipTextHelper.plainTextToChecklist(initialText)
        } else {
            emptyList()
        }
        val nextColor = CardColor.forIndex(uiState.value.settings.nextColorIndex)
        editorState.value = ClipEditorState(
            existingClip = null,
            title = if (initialText.isNotBlank()) ClipTextHelper.generateSmartTitle(initialText, detectedType) else "",
            content = initialText,
            checklistItems = checklist,
            clipType = detectedType,
            cardColor = nextColor,
            isPinned = selectedCategory.value == ClipCategory.PINNED,
            isFavorite = selectedCategory.value == ClipCategory.FAVORITES
        )
    }

    fun openEditorFor(clip: ClipEntity) {
        editorState.value = ClipEditorState(
            existingClip = clip,
            title = clip.title,
            content = clip.content,
            checklistItems = clip.checklistItems,
            clipType = clip.clipType,
            cardColor = clip.cardColor,
            isPinned = clip.isPinned,
            isFavorite = clip.isFavorite,
            isLocked = clip.isLocked
        )
    }

    fun updateEditor(transform: (ClipEditorState) -> ClipEditorState) {
        editorState.update { current -> current?.let(transform) }
    }

    fun toggleChecklistItemInEditor(itemId: String) {
        editorState.update { current ->
            current ?: return@update null
            val updated = current.checklistItems.map { item ->
                if (item.id == itemId) item.copy(isDone = !item.isDone) else item
            }
            current.copy(
                checklistItems = updated,
                content = ClipTextHelper.checklistToPlainText(updated)
            )
        }
    }

    fun addChecklistItemInEditor(text: String) {
        val cleaned = text.trim()
        if (cleaned.isEmpty()) return
        editorState.update { current ->
            current ?: return@update null
            val updated = current.checklistItems + ChecklistItem(text = cleaned, isDone = false)
            current.copy(
                checklistItems = updated,
                content = ClipTextHelper.checklistToPlainText(updated)
            )
        }
    }

    fun removeChecklistItemInEditor(itemId: String) {
        editorState.update { current ->
            current ?: return@update null
            val updated = current.checklistItems.filterNot { it.id == itemId }
            current.copy(
                checklistItems = updated,
                content = ClipTextHelper.checklistToPlainText(updated)
            )
        }
    }

    fun switchEditorType(newType: ClipType) {
        editorState.update { current ->
            current ?: return@update null
            if (current.clipType == newType) return@update current
            when (newType) {
                ClipType.CHECKLIST -> {
                    val items = if (current.checklistItems.isNotEmpty()) {
                        current.checklistItems
                    } else {
                        ClipTextHelper.plainTextToChecklist(current.content)
                    }
                    current.copy(clipType = newType, checklistItems = items)
                }
                ClipType.NOTE, ClipType.LINK -> {
                    val text = if (current.clipType == ClipType.CHECKLIST && current.checklistItems.isNotEmpty()) {
                        ClipTextHelper.checklistToPlainText(current.checklistItems)
                    } else {
                        current.content
                    }
                    current.copy(clipType = newType, content = text)
                }
            }
        }
    }

    fun dismissEditor() {
        editorState.value = null
    }

    fun saveEditor() {
        val state = editorState.value ?: return
        val hasBody = if (state.clipType == ClipType.CHECKLIST) {
            state.checklistItems.any { it.text.isNotBlank() }
        } else {
            state.content.isNotBlank()
        }
        if (!hasBody && state.title.isBlank()) {
            viewModelScope.launch {
                _effects.emit(UiEffect.ShowMessage("Add some text before saving"))
            }
            return
        }

        if (state.isLocked && !uiState.value.settings.hasPinConfigured) {
            pinPromptState.value = PinPromptState(
                mode = PinFlowMode.SET_NEW_PIN_FOR_LOCK,
                targetClip = null
            )
            return
        }

        viewModelScope.launch {
            val saved = repository.saveClip(
                existingId = state.existingClip?.id,
                title = state.title,
                content = state.content,
                checklist = state.checklistItems.filter { it.text.isNotBlank() },
                clipType = state.clipType,
                cardColor = state.cardColor,
                isPinned = state.isPinned,
                isFavorite = state.isFavorite,
                isLocked = state.isLocked,
                sourceApp = state.existingClip?.sourceApp ?: "Manual"
            )
            if (!saved.isLocked) {
                unlockedClipIds.update { it - saved.id }
            }
            editorState.value = null
            _effects.emit(UiEffect.ShowMessage(if (state.existingClip == null) "Clip created" else "Clip updated"))
        }
    }

    private fun requestUnlock(clip: ClipEntity, action: PendingLockedAction) {
        if (!uiState.value.settings.hasPinConfigured) {
            // No PIN configured yet: prompt user to set one or unlock immediately
            pinPromptState.value = PinPromptState(
                mode = PinFlowMode.SET_OR_CHANGE_PIN,
                targetClip = clip,
                pendingAction = action
            )
        } else {
            pinPromptState.value = PinPromptState(
                mode = PinFlowMode.UNLOCK_CLIP,
                targetClip = clip,
                pendingAction = action
            )
        }
    }

    fun openPinSettings() {
        pinPromptState.value = PinPromptState(
            mode = PinFlowMode.SET_OR_CHANGE_PIN
        )
    }

    fun requestRemovePin() {
        pinPromptState.value = PinPromptState(
            mode = PinFlowMode.REMOVE_PIN
        )
    }

    fun dismissPinPrompt() {
        pinPromptState.value = null
    }

    fun submitPin(pin: String) {
        val prompt = pinPromptState.value ?: return
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            pinPromptState.value = prompt.copy(errorMessage = "Enter a 4-digit PIN")
            return
        }

        viewModelScope.launch {
            when (prompt.mode) {
                PinFlowMode.UNLOCK_CLIP -> {
                    if (repository.verifyVaultPin(pin)) {
                        val target = prompt.targetClip
                        pinPromptState.value = null
                        if (target != null) {
                            unlockedClipIds.update { it + target.id }
                            when (prompt.pendingAction) {
                                PendingLockedAction.OPEN_DETAIL -> openEditorFor(target)
                                PendingLockedAction.COPY -> performCopy(target)
                                PendingLockedAction.SHARE -> _effects.emit(UiEffect.ShareClip(target.title, target.copyableText))
                                PendingLockedAction.DELETE -> performDelete(target)
                            }
                        }
                    } else {
                        pinPromptState.value = prompt.copy(errorMessage = "Incorrect PIN. Try again.")
                    }
                }

                PinFlowMode.SET_NEW_PIN_FOR_LOCK -> {
                    repository.setVaultPin(pin)
                    val target = prompt.targetClip
                    pinPromptState.value = null
                    if (target != null) {
                        repository.setLocked(target, true)
                        unlockedClipIds.update { it - target.id }
                        _effects.emit(UiEffect.ShowMessage("PIN saved and clip locked"))
                    } else {
                        saveEditor()
                    }
                }

                PinFlowMode.SET_OR_CHANGE_PIN -> {
                    repository.setVaultPin(pin)
                    val target = prompt.targetClip
                    pinPromptState.value = null
                    if (target != null) {
                        unlockedClipIds.update { it + target.id }
                        when (prompt.pendingAction) {
                            PendingLockedAction.OPEN_DETAIL -> openEditorFor(target)
                            PendingLockedAction.COPY -> performCopy(target)
                            PendingLockedAction.SHARE -> _effects.emit(UiEffect.ShareClip(target.title, target.copyableText))
                            PendingLockedAction.DELETE -> performDelete(target)
                        }
                        _effects.emit(UiEffect.ShowMessage("Vault PIN set"))
                    } else {
                        _effects.emit(UiEffect.ShowMessage("Vault PIN updated"))
                    }
                }

                PinFlowMode.REMOVE_PIN -> {
                    if (repository.verifyVaultPin(pin)) {
                        repository.clearVaultPin()
                        pinPromptState.value = null
                        _effects.emit(UiEffect.ShowMessage("Vault PIN removed"))
                    } else {
                        pinPromptState.value = prompt.copy(errorMessage = "Incorrect PIN")
                    }
                }
            }
        }
    }

    fun lockAllSessionClips() {
        unlockedClipIds.value = emptySet()
        viewModelScope.launch {
            _effects.emit(UiEffect.ShowMessage("All locked clips re-secured"))
        }
    }

    fun setShowSettingsSheet(show: Boolean) {
        showSettingsSheet.value = show
    }

    fun setSortOrder(order: SortOrder) {
        viewModelScope.launch { repository.setSortOrder(order) }
    }

    fun setAutoCapture(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoCapture(enabled) }
    }

    fun setDeduplicate(enabled: Boolean) {
        viewModelScope.launch { repository.setDeduplicateClips(enabled) }
    }

    fun setMonitorNotification(enabled: Boolean) {
        viewModelScope.launch {
            repository.setMonitorNotification(enabled)
            _effects.emit(UiEffect.ToggleForegroundMonitor(enabled))
        }
    }

    fun clearUnlockedHistory() {
        viewModelScope.launch {
            val count = repository.clearUnlockedHistory()
            _effects.emit(
                UiEffect.ShowMessage(
                    if (count > 0) "Cleared $count history items" else "No unpinned history to clear"
                )
            )
        }
    }

    suspend fun exportBackupJson(): String = repository.exportBackupJson()

    fun importBackupJson(rawJson: String) {
        viewModelScope.launch {
            val count = repository.importBackupJson(rawJson)
            _effects.emit(
                UiEffect.ShowMessage(
                    if (count > 0) "Imported $count clips" else "No new clips found in backup"
                )
            )
        }
    }

    private data class FilterInputs(
        val category: ClipCategory,
        val chip: ClipFilterChip,
        val query: String,
        val searchOpen: Boolean,
        val unlockedIds: Set<Long>
    )

    private data class ModalInputs(
        val pendingClipboard: String?,
        val editor: ClipEditorState?,
        val quickAction: ClipEntity?,
        val pinPrompt: PinPromptState?,
        val settingsSheet: Boolean
    )

    class Factory(private val repository: ClipRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ClipboardViewModel(repository) as T
        }
    }
}

fun computeCategoryCounts(clips: List<ClipEntity>): Map<ClipCategory, Int> {
    return mapOf(
        ClipCategory.ALL to clips.size,
        ClipCategory.PINNED to clips.count { it.isPinned },
        ClipCategory.FAVORITES to clips.count { it.isFavorite },
        ClipCategory.NOTES to clips.count { it.clipType == ClipType.NOTE || it.clipType == ClipType.CHECKLIST },
        ClipCategory.LINKS to clips.count { it.clipType == ClipType.LINK }
    )
}

fun filterAndSortClips(
    clips: List<ClipEntity>,
    category: ClipCategory,
    filterChip: ClipFilterChip,
    searchQuery: String,
    sortOrder: SortOrder,
    unlockedClipIds: Set<Long> = emptySet(),
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<ClipEntity> {
    val todayStart = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalDate()
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
    val weekStart = Instant.ofEpochMilli(nowMillis)
        .atZone(zoneId)
        .toLocalDate()
        .minusDays(6)
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()

    val normalizedQuery = searchQuery.trim().lowercase()

    val filtered = clips.filter { clip ->
        val matchesCategory = when (category) {
            ClipCategory.ALL -> true
            ClipCategory.PINNED -> clip.isPinned
            ClipCategory.FAVORITES -> clip.isFavorite
            ClipCategory.NOTES -> clip.clipType == ClipType.NOTE || clip.clipType == ClipType.CHECKLIST
            ClipCategory.LINKS -> clip.clipType == ClipType.LINK
        }
        if (!matchesCategory) return@filter false

        val matchesChip = when (filterChip) {
            ClipFilterChip.ALL -> true
            ClipFilterChip.TODAY -> clip.updatedAt >= todayStart
            ClipFilterChip.THIS_WEEK -> clip.updatedAt >= weekStart
            ClipFilterChip.LONG_TEXT -> ClipTextHelper.isLongText(clip.content)
            ClipFilterChip.SHORT -> !ClipTextHelper.isLongText(clip.content)
        }
        if (!matchesChip) return@filter false

        if (normalizedQuery.isEmpty()) return@filter true

        val canSearchBody = !clip.isLocked || clip.id in unlockedClipIds
        clip.title.lowercase().contains(normalizedQuery) ||
            (canSearchBody && clip.content.lowercase().contains(normalizedQuery))
    }

    val comparator: Comparator<ClipEntity> = when (sortOrder) {
        SortOrder.NEWEST -> compareByDescending<ClipEntity> { it.isPinned }
            .thenByDescending { it.updatedAt }
        SortOrder.OLDEST -> compareByDescending<ClipEntity> { it.isPinned }
            .thenBy { it.updatedAt }
        SortOrder.TITLE_ASC -> compareByDescending<ClipEntity> { it.isPinned }
            .thenBy { it.title.lowercase() }
        SortOrder.MOST_COPIED -> compareByDescending<ClipEntity> { it.isPinned }
            .thenByDescending { it.copyCount }
            .thenByDescending { it.updatedAt }
    }
    return filtered.sortedWith(comparator)
}
