package com.clipboardmanager.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.ContentPaste
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPasteGo
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipboardmanager.app.data.model.ClipCategory
import com.clipboardmanager.app.data.model.SortOrder
import com.clipboardmanager.app.ui.ClipboardUiState
import com.clipboardmanager.app.ui.ClipboardViewModel
import com.clipboardmanager.app.ui.components.CategoryTabBar
import com.clipboardmanager.app.ui.components.ClipEditorBottomSheet
import com.clipboardmanager.app.ui.components.ClipNoteCard
import com.clipboardmanager.app.ui.components.ClipQuickActionSheet
import com.clipboardmanager.app.ui.components.FilterChipBar
import com.clipboardmanager.app.ui.components.PinPromptDialog
import com.clipboardmanager.app.ui.components.PushPinGraphic
import com.clipboardmanager.app.ui.components.SettingsBottomSheet
import com.clipboardmanager.app.ui.theme.AppColors
import kotlinx.coroutines.launch

@Composable
fun ClipboardHomeScreen(
    uiState: ClipboardUiState,
    viewModel: ClipboardViewModel,
    snackbarHostState: SnackbarHostState,
    systemClipboardSnapshot: () -> String?,
    onCopyDirect: (String, String) -> Unit,
    onShareDirect: (String, String) -> Unit,
    onOpenUrlDirect: (String) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var overflowExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                uiState = uiState,
                onSelectCategory = { category ->
                    viewModel.selectCategory(category)
                    scope.launch { drawerState.close() }
                },
                onCaptureClipboard = {
                    scope.launch { drawerState.close() }
                    viewModel.onSystemClipboardSeen(systemClipboardSnapshot(), forceCapture = true)
                },
                onOpenPinSettings = {
                    scope.launch { drawerState.close() }
                    viewModel.openPinSettings()
                },
                onOpenSettings = {
                    scope.launch { drawerState.close() }
                    viewModel.setShowSettingsSheet(true)
                },
                onExportBackup = {
                    scope.launch { drawerState.close() }
                    onExportBackup()
                }
            )
        }
    ) {
        Scaffold(
            containerColor = AppColors.WarmPaperBg,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppColors.PureWhite)
                        .statusBarsPadding()
                ) {
                    TopHeaderBar(
                        isSearchActive = uiState.isSearchActive,
                        searchQuery = uiState.searchQuery,
                        onMenuClick = { scope.launch { drawerState.open() } },
                        onSearchToggle = { viewModel.toggleSearch(it) },
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        overflowExpanded = overflowExpanded,
                        onOverflowToggle = { overflowExpanded = it },
                        sortMenuExpanded = sortMenuExpanded,
                        onSortMenuToggle = { sortMenuExpanded = it },
                        currentSortOrder = uiState.settings.sortOrder,
                        onSelectSortOrder = {
                            viewModel.setSortOrder(it)
                            sortMenuExpanded = false
                            overflowExpanded = false
                        },
                        onCaptureClipboard = {
                            overflowExpanded = false
                            viewModel.onSystemClipboardSeen(systemClipboardSnapshot(), forceCapture = true)
                        },
                        onClearHistory = {
                            overflowExpanded = false
                            viewModel.clearUnlockedHistory()
                        },
                        onOpenSettings = {
                            overflowExpanded = false
                            viewModel.setShowSettingsSheet(true)
                        }
                    )
                    CategoryTabBar(
                        selectedCategory = uiState.selectedCategory,
                        counts = uiState.categoryCounts,
                        onSelectCategory = viewModel::selectCategory
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { viewModel.openNewClipEditor(prefillText = null) },
                    containerColor = AppColors.TealPrimary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "New clip"
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                FilterChipBar(
                    selectedChip = uiState.selectedFilterChip,
                    onSelectChip = viewModel::selectFilterChip
                )

                // Pending clipboard quick-save banner (shown when auto-capture is disabled)
                AnimatedVisibility(visible = !uiState.pendingClipboardText.isNullOrBlank()) {
                    val preview = uiState.pendingClipboardText.orEmpty()
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.savePendingClipboard() },
                        color = AppColors.PureWhite,
                        border = BorderStroke(1.dp, AppColors.TealPrimary.copy(alpha = 0.35f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ContentPasteGo,
                                contentDescription = null,
                                tint = AppColors.TealPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "New clipboard item detected",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppColors.TealPrimary
                                )
                                Text(
                                    text = preview.take(64),
                                    fontSize = 12.sp,
                                    color = AppColors.TextSecondary,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "Save",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.TealPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                            IconButton(
                                onClick = viewModel::dismissPendingClipboard,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "Dismiss",
                                    tint = AppColors.TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                if (uiState.clips.isEmpty()) {
                    EmptyClipsState(
                        category = uiState.selectedCategory,
                        hasSearchQuery = uiState.searchQuery.isNotBlank(),
                        onAddClip = { viewModel.openNewClipEditor(systemClipboardSnapshot()) }
                    )
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 4.dp,
                            bottom = 92.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalItemSpacing = 12.dp
                    ) {
                        items(
                            items = uiState.clips,
                            key = { it.id }
                        ) { clip ->
                            ClipNoteCard(
                                clip = clip,
                                isUnlockedInSession = clip.id in uiState.unlockedClipIds,
                                onCardClick = { viewModel.onCardClick(clip) },
                                onCardLongClick = { viewModel.onCardLongClick(clip) },
                                onQuickCopy = { viewModel.onQuickCopy(clip) }
                            )
                        }

                        item(span = StaggeredGridItemSpan.FullLine) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    // Editor / Detail Bottom Sheet
    uiState.editorState?.let { editor ->
        ClipEditorBottomSheet(
            state = editor,
            systemClipboardText = systemClipboardSnapshot(),
            onUpdate = viewModel::updateEditor,
            onSwitchType = viewModel::switchEditorType,
            onToggleChecklistItem = viewModel::toggleChecklistItemInEditor,
            onAddChecklistItem = viewModel::addChecklistItemInEditor,
            onRemoveChecklistItem = viewModel::removeChecklistItemInEditor,
            onCopy = onCopyDirect,
            onShare = onShareDirect,
            onOpenUrl = onOpenUrlDirect,
            onDelete = viewModel::deleteClip,
            onSave = viewModel::saveEditor,
            onDismiss = viewModel::dismissEditor
        )
    }

    // Long-press Quick Action Sheet
    uiState.quickActionClip?.let { clip ->
        ClipQuickActionSheet(
            clip = clip,
            onCopy = {
                viewModel.dismissQuickActions()
                viewModel.onQuickCopy(clip)
            },
            onEdit = {
                viewModel.dismissQuickActions()
                viewModel.onCardClick(clip)
            },
            onTogglePin = { viewModel.togglePin(clip) },
            onToggleFavorite = { viewModel.toggleFavorite(clip) },
            onToggleLock = { viewModel.toggleLockFromQuickActions(clip) },
            onShare = { viewModel.onShareClip(clip) },
            onDelete = { viewModel.deleteClip(clip) },
            onDismiss = viewModel::dismissQuickActions
        )
    }

    // Vault PIN Prompt
    uiState.pinPromptState?.let { prompt ->
        PinPromptDialog(
            state = prompt,
            onSubmitPin = viewModel::submitPin,
            onDismiss = viewModel::dismissPinPrompt
        )
    }

    // Settings Sheet
    if (uiState.showSettingsSheet) {
        SettingsBottomSheet(
            settings = uiState.settings,
            onToggleAutoCapture = viewModel::setAutoCapture,
            onToggleDeduplicate = viewModel::setDeduplicate,
            onToggleMonitorNotification = viewModel::setMonitorNotification,
            onSelectSortOrder = viewModel::setSortOrder,
            onChangePin = {
                viewModel.setShowSettingsSheet(false)
                viewModel.openPinSettings()
            },
            onRemovePin = {
                viewModel.setShowSettingsSheet(false)
                viewModel.requestRemovePin()
            },
            onRelockSession = viewModel::lockAllSessionClips,
            onExportBackup = {
                viewModel.setShowSettingsSheet(false)
                onExportBackup()
            },
            onImportBackup = {
                viewModel.setShowSettingsSheet(false)
                onImportBackup()
            },
            onClearHistory = viewModel::clearUnlockedHistory,
            onDismiss = { viewModel.setShowSettingsSheet(false) }
        )
    }
}

@Composable
private fun TopHeaderBar(
    isSearchActive: Boolean,
    searchQuery: String,
    onMenuClick: () -> Unit,
    onSearchToggle: (Boolean) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    overflowExpanded: Boolean,
    onOverflowToggle: (Boolean) -> Unit,
    sortMenuExpanded: Boolean,
    onSortMenuToggle: (Boolean) -> Unit,
    currentSortOrder: SortOrder,
    onSelectSortOrder: (SortOrder) -> Unit,
    onCaptureClipboard: () -> Unit,
    onClearHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSearchActive) {
            IconButton(onClick = { onSearchToggle(false) }) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close search",
                    tint = AppColors.TealPrimary
                )
            }
            TextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search clips, notes, links…", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onSearchQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = AppColors.TextSecondary)
                }
            }
        } else {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = "Open menu",
                    tint = AppColors.TealPrimary
                )
            }
            Text(
                text = "Clipboard Manager",
                color = AppColors.TealPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp)
            )
            IconButton(onClick = { onSearchToggle(true) }) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = "Search clips",
                    tint = Color(0xFF4285B4)
                )
            }
            Box {
                IconButton(onClick = { onOverflowToggle(true) }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = AppColors.TealPrimary
                    )
                }
                DropdownMenu(
                    expanded = overflowExpanded,
                    onDismissRequest = { onOverflowToggle(false) },
                    containerColor = AppColors.PureWhite
                ) {
                    DropdownMenuItem(
                        text = { Text("Paste from Clipboard") },
                        leadingIcon = { Icon(Icons.Outlined.ContentPaste, null, tint = AppColors.TealPrimary) },
                        onClick = onCaptureClipboard
                    )
                    DropdownMenuItem(
                        text = { Text("Sort: ${currentSortOrder.label}") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Sort, null, tint = AppColors.TextSecondary) },
                        onClick = {
                            onOverflowToggle(false)
                            onSortMenuToggle(true)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Clear unpinned history") },
                        leadingIcon = { Icon(Icons.Outlined.DeleteSweep, null, tint = AppColors.DangerRed) },
                        onClick = onClearHistory
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        leadingIcon = { Icon(Icons.Outlined.Settings, null, tint = AppColors.TextSecondary) },
                        onClick = onOpenSettings
                    )
                }
                DropdownMenu(
                    expanded = sortMenuExpanded,
                    onDismissRequest = { onSortMenuToggle(false) },
                    containerColor = AppColors.PureWhite
                ) {
                    SortOrder.entries.forEach { order ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = order.label,
                                    fontWeight = if (order == currentSortOrder) FontWeight.Bold else FontWeight.Normal,
                                    color = if (order == currentSortOrder) AppColors.TealPrimary else AppColors.TextPrimary
                                )
                            },
                            onClick = { onSelectSortOrder(order) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DrawerContent(
    uiState: ClipboardUiState,
    onSelectCategory: (ClipCategory) -> Unit,
    onCaptureClipboard: () -> Unit,
    onOpenPinSettings: () -> Unit,
    onOpenSettings: () -> Unit,
    onExportBackup: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = AppColors.PureWhite
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.WarmPaperBg)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Clipboard Manager",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TealPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val total = uiState.categoryCounts[ClipCategory.ALL] ?: 0
                    val pinned = uiState.categoryCounts[ClipCategory.PINNED] ?: 0
                    Text(
                        text = "$total saved clips • $pinned pinned",
                        fontSize = 12.5.sp,
                        color = AppColors.TextSecondary
                    )
                }
                PushPinGraphic(size = 32.dp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        val categoryIcons: Map<ClipCategory, ImageVector> = mapOf(
            ClipCategory.ALL to Icons.Outlined.ContentPaste,
            ClipCategory.PINNED to Icons.Rounded.PushPin,
            ClipCategory.FAVORITES to Icons.Rounded.Star,
            ClipCategory.NOTES to Icons.Rounded.EditNote,
            ClipCategory.LINKS to Icons.Rounded.Link
        )

        ClipCategory.entries.forEach { category ->
            val selected = uiState.selectedCategory == category
            val count = uiState.categoryCounts[category] ?: 0
            NavigationDrawerItem(
                label = { Text(category.label, fontWeight = FontWeight.Medium) },
                badge = {
                    if (count > 0) {
                        Text(
                            text = "$count",
                            fontSize = 12.sp,
                            color = if (selected) AppColors.TealPrimary else AppColors.TextSecondary
                        )
                    }
                },
                selected = selected,
                onClick = { onSelectCategory(category) },
                icon = {
                    Icon(
                        imageVector = categoryIcons[category] ?: Icons.Outlined.ContentPaste,
                        contentDescription = null
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = AppColors.TealSoftTint,
                    selectedTextColor = AppColors.TealPrimary,
                    selectedIconColor = AppColors.TealPrimary
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AppColors.WarmDivider)

        NavigationDrawerItem(
            label = { Text("Save Current Clipboard") },
            selected = false,
            onClick = onCaptureClipboard,
            icon = { Icon(Icons.Rounded.ContentPasteGo, null, tint = AppColors.TealPrimary) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text(if (uiState.settings.hasPinConfigured) "Vault PIN Settings" else "Set Vault PIN") },
            selected = false,
            onClick = onOpenPinSettings,
            icon = { Icon(Icons.Outlined.Lock, null, tint = AppColors.LockBlue) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Export JSON Backup") },
            selected = false,
            onClick = onExportBackup,
            icon = { Icon(Icons.Outlined.FileDownload, null, tint = AppColors.TextSecondary) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        NavigationDrawerItem(
            label = { Text("Settings") },
            selected = false,
            onClick = onOpenSettings,
            icon = { Icon(Icons.Outlined.Settings, null, tint = AppColors.TextSecondary) },
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}

@Composable
private fun EmptyClipsState(
    category: ClipCategory,
    hasSearchQuery: Boolean,
    onAddClip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PushPinGraphic(size = 44.dp)
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (hasSearchQuery) {
                "No matching clips"
            } else {
                "Nothing in ${category.label} yet"
            },
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (hasSearchQuery) {
                "Try a different keyword or clear your filter chips."
            } else {
                "Copy text anywhere or tap + to pin a new note, checklist, or link."
            },
            fontSize = 13.5.sp,
            color = AppColors.TextSecondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onAddClip),
            color = AppColors.TealPrimary,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = "Create Clip",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
            )
        }
    }
}
