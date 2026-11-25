package com.clipboardmanager.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipboardmanager.app.data.local.AppSettings
import com.clipboardmanager.app.data.local.ClipEntity
import com.clipboardmanager.app.data.model.CardColor
import com.clipboardmanager.app.data.model.ClipTextHelper
import com.clipboardmanager.app.data.model.ClipType
import com.clipboardmanager.app.data.model.SortOrder
import com.clipboardmanager.app.ui.ClipEditorState
import com.clipboardmanager.app.ui.PinFlowMode
import com.clipboardmanager.app.ui.PinPromptState
import com.clipboardmanager.app.ui.theme.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipEditorBottomSheet(
    state: ClipEditorState,
    systemClipboardText: String?,
    onUpdate: ((ClipEditorState) -> ClipEditorState) -> Unit,
    onSwitchType: (ClipType) -> Unit,
    onToggleChecklistItem: (String) -> Unit,
    onAddChecklistItem: (String) -> Unit,
    onRemoveChecklistItem: (String) -> Unit,
    onCopy: (String, String) -> Unit,
    onShare: (String, String) -> Unit,
    onOpenUrl: (String) -> Unit,
    onDelete: (ClipEntity) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newChecklistInput by remember { mutableStateOf("") }
    val bgColor = Color(state.cardColor.containerHex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 24.dp)
        ) {
            // Top bar: Type selector + Pin/Favorite/Lock toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        ClipType.NOTE to "Note",
                        ClipType.CHECKLIST to "Checklist",
                        ClipType.LINK to "Link"
                    ).forEach { (type, label) ->
                        val selected = state.clipType == type
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSwitchType(type) },
                            color = if (selected) AppColors.TealPrimary else Color.White.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (selected) Color.White else AppColors.TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onUpdate { it.copy(isPinned = !it.isPinned) } }) {
                        Icon(
                            imageVector = Icons.Rounded.PushPin,
                            contentDescription = "Pin",
                            tint = if (state.isPinned) AppColors.PinRed else AppColors.TextSecondary
                        )
                    }
                    IconButton(onClick = { onUpdate { it.copy(isFavorite = !it.isFavorite) } }) {
                        Icon(
                            imageVector = if (state.isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (state.isFavorite) AppColors.StarGold else AppColors.TextSecondary
                        )
                    }
                    IconButton(onClick = { onUpdate { it.copy(isLocked = !it.isLocked) } }) {
                        Icon(
                            imageVector = if (state.isLocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = "Lock",
                            tint = if (state.isLocked) AppColors.LockBlue else AppColors.TextSecondary
                        )
                    }
                }
            }

            // Paste from system clipboard banner when creating a new clip
            if (state.existingClip == null && !systemClipboardText.isNullOrBlank() && state.content.isBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            val detected = ClipTextHelper.detectClipType(systemClipboardText)
                            val title = ClipTextHelper.generateSmartTitle(systemClipboardText, detected)
                            val items = if (detected == ClipType.CHECKLIST) {
                                ClipTextHelper.plainTextToChecklist(systemClipboardText)
                            } else {
                                emptyList()
                            }
                            onUpdate {
                                it.copy(
                                    title = if (it.title.isBlank()) title else it.title,
                                    content = systemClipboardText,
                                    clipType = detected,
                                    checklistItems = items
                                )
                            }
                        },
                    color = Color.White.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, AppColors.TealPrimary.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = null,
                            tint = AppColors.TealPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Tap to paste current clipboard: \"${systemClipboardText.take(36)}\"",
                            fontSize = 12.5.sp,
                            color = AppColors.TealPrimaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Color palette picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CardColor.entries.forEach { colorOption ->
                    val selected = state.cardColor == colorOption
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(colorOption.containerHex))
                            .border(
                                width = if (selected) 2.5.dp else 1.dp,
                                color = if (selected) AppColors.TealPrimary else Color(colorOption.borderHex),
                                shape = CircleShape
                            )
                            .clickable { onUpdate { it.copy(cardColor = colorOption) } },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = colorOption.displayName,
                                tint = AppColors.TealPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = state.title,
                onValueChange = { value -> onUpdate { it.copy(title = value) } },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.72f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.55f),
                    focusedBorderColor = AppColors.TealPrimary,
                    unfocusedBorderColor = Color(state.cardColor.borderHex)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (state.clipType == ClipType.CHECKLIST) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.checklistItems.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White.copy(alpha = 0.65f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = item.isDone,
                                    onCheckedChange = { onToggleChecklistItem(item.id) },
                                    colors = CheckboxDefaults.colors(checkedColor = AppColors.TealPrimary)
                                )
                                Text(
                                    text = item.text,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onToggleChecklistItem(item.id) },
                                    fontSize = 14.sp,
                                    color = if (item.isDone) AppColors.TextStrikethrough else AppColors.TextPrimary,
                                    textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                                )
                                IconButton(
                                    onClick = { onRemoveChecklistItem(item.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Remove item",
                                        tint = AppColors.TextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newChecklistInput,
                            onValueChange = { newChecklistInput = it },
                            placeholder = { Text("Add checklist item…") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newChecklistInput.isNotBlank()) {
                                    onAddChecklistItem(newChecklistInput)
                                    newChecklistInput = ""
                                }
                            }),
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.72f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.55f),
                                focusedBorderColor = AppColors.TealPrimary,
                                unfocusedBorderColor = Color(state.cardColor.borderHex)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newChecklistInput.isNotBlank()) {
                                    onAddChecklistItem(newChecklistInput)
                                    newChecklistInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.TealPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add item")
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = state.content,
                    onValueChange = { value -> onUpdate { it.copy(content = value) } },
                    label = { Text(if (state.clipType == ClipType.LINK) "URL or Link Note" else "Clip Content") },
                    minLines = 5,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.72f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.55f),
                        focusedBorderColor = AppColors.TealPrimary,
                        unfocusedBorderColor = Color(state.cardColor.borderHex)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val copyText = if (state.clipType == ClipType.CHECKLIST && state.checklistItems.isNotEmpty()) {
                ClipTextHelper.checklistToPlainText(state.checklistItems)
            } else {
                state.content
            }

            // Utility actions row
            var justCopiedInEditor by remember { mutableStateOf(false) }
            LaunchedEffect(justCopiedInEditor) {
                if (justCopiedInEditor) {
                    delay(1300)
                    justCopiedInEditor = false
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        justCopiedInEditor = true
                        onCopy(state.title.ifBlank { "Clip" }, copyText)
                    },
                    enabled = copyText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (justCopiedInEditor) Icons.Rounded.Check else Icons.Outlined.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(if (justCopiedInEditor) "Copied!" else "Copy")
                }

                OutlinedButton(
                    onClick = { onShare(state.title.ifBlank { "Clip" }, copyText) },
                    enabled = copyText.isNotBlank(),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Outlined.Share, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Share")
                }

                if (state.clipType == ClipType.LINK || ClipTextHelper.containsUrl(copyText)) {
                    val url = ClipTextHelper.extractUrls(copyText).firstOrNull() ?: copyText
                    OutlinedButton(
                        onClick = { onOpenUrl(url) },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, null, modifier = Modifier.size(16.dp))
                    }
                }

                if (state.existingClip != null) {
                    OutlinedButton(
                        onClick = { onDelete(state.existingClip) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.DangerRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.TealPrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (state.existingClip == null) "Save Clip" else "Save Changes",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipQuickActionSheet(
    clip: ClipEntity,
    onCopy: () -> Unit,
    onEdit: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onToggleLock: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppColors.PureWhite
    ) {
        Column(modifier = Modifier.padding(bottom = 20.dp)) {
            Text(
                text = clip.title,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = AppColors.TextPrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
            )
            HorizontalDivider(color = AppColors.WarmDivider)
            ListItem(
                headlineContent = { Text("Copy to clipboard") },
                leadingContent = { Icon(Icons.Outlined.ContentCopy, null, tint = AppColors.TealPrimary) },
                modifier = Modifier.clickable(onClick = onCopy)
            )
            ListItem(
                headlineContent = { Text("Open & Edit") },
                leadingContent = { Icon(Icons.Rounded.Check, null, tint = AppColors.TealPrimary) },
                modifier = Modifier.clickable(onClick = onEdit)
            )
            ListItem(
                headlineContent = { Text(if (clip.isPinned) "Unpin clip" else "Pin to top") },
                leadingContent = {
                    Icon(
                        Icons.Rounded.PushPin,
                        null,
                        tint = if (clip.isPinned) AppColors.PinRed else AppColors.TextSecondary
                    )
                },
                modifier = Modifier.clickable(onClick = onTogglePin)
            )
            ListItem(
                headlineContent = { Text(if (clip.isFavorite) "Remove from Favorites" else "Add to Favorites") },
                leadingContent = {
                    Icon(
                        Icons.Rounded.Star,
                        null,
                        tint = if (clip.isFavorite) AppColors.StarGold else AppColors.TextSecondary
                    )
                },
                modifier = Modifier.clickable(onClick = onToggleFavorite)
            )
            ListItem(
                headlineContent = { Text(if (clip.isLocked) "Unlock clip" else "Lock with Vault PIN") },
                leadingContent = { Icon(Icons.Rounded.Lock, null, tint = AppColors.LockBlue) },
                modifier = Modifier.clickable(onClick = onToggleLock)
            )
            ListItem(
                headlineContent = { Text("Share") },
                leadingContent = { Icon(Icons.Outlined.Share, null, tint = AppColors.TextSecondary) },
                modifier = Modifier.clickable(onClick = onShare)
            )
            ListItem(
                headlineContent = { Text("Delete", color = AppColors.DangerRed) },
                leadingContent = { Icon(Icons.Outlined.Delete, null, tint = AppColors.DangerRed) },
                modifier = Modifier.clickable(onClick = onDelete)
            )
        }
    }
}

@Composable
fun PinPromptDialog(
    state: PinPromptState,
    onSubmitPin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var enteredPin by remember(state) { mutableStateOf("") }

    val title = when (state.mode) {
        PinFlowMode.UNLOCK_CLIP -> "Unlock Private Clip"
        PinFlowMode.SET_NEW_PIN_FOR_LOCK -> "Set 4-Digit Vault PIN"
        PinFlowMode.SET_OR_CHANGE_PIN -> "Set Vault PIN"
        PinFlowMode.REMOVE_PIN -> "Enter PIN to Remove Lock"
    }
    val subtitle = when (state.mode) {
        PinFlowMode.UNLOCK_CLIP -> "Enter your 4-digit Vault PIN to access \"${state.targetClip?.title.orEmpty()}\"."
        PinFlowMode.SET_NEW_PIN_FOR_LOCK -> "Create a 4-digit PIN to protect locked clips."
        PinFlowMode.SET_OR_CHANGE_PIN -> "Choose a 4-digit PIN for your private clip vault."
        PinFlowMode.REMOVE_PIN -> "Confirm your current 4-digit PIN to remove PIN protection."
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.PureWhite,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = AppColors.LockBlue,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 4 PIN dots
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    repeat(4) { idx ->
                        val filled = idx < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) AppColors.TealPrimary else Color.Transparent)
                                .border(
                                    width = 1.5.dp,
                                    color = if (filled) AppColors.TealPrimary else AppColors.ChipBorder,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (!state.errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = state.errorMessage,
                        color = AppColors.DangerRed,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Numeric keypad
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            row.forEach { key ->
                                when (key) {
                                    "" -> Spacer(modifier = Modifier.size(56.dp))
                                    "DEL" -> Surface(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            },
                                        shape = CircleShape,
                                        color = AppColors.WarmPaperBg
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                                contentDescription = "Delete digit",
                                                tint = AppColors.TextPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    else -> Surface(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                if (enteredPin.length < 4) {
                                                    val next = enteredPin + key
                                                    enteredPin = next
                                                    if (next.length == 4) {
                                                        onSubmitPin(next)
                                                    }
                                                }
                                            },
                                        shape = CircleShape,
                                        color = AppColors.WarmPaperBg
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = key,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = AppColors.TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: AppSettings,
    onToggleAutoCapture: (Boolean) -> Unit,
    onToggleDeduplicate: (Boolean) -> Unit,
    onToggleMonitorNotification: (Boolean) -> Unit,
    onSelectSortOrder: (SortOrder) -> Unit,
    onChangePin: () -> Unit,
    onRemovePin: () -> Unit,
    onRelockSession: () -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    onClearHistory: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showClearConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColors.PureWhite
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(bottom = 26.dp)
        ) {
            Text(
                text = "Clipboard Settings",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TealPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            SettingSwitchRow(
                title = "Auto-capture on open",
                subtitle = "Automatically save new text from your system clipboard when opening the app",
                checked = settings.autoCaptureEnabled,
                onCheckedChange = onToggleAutoCapture
            )
            SettingSwitchRow(
                title = "Merge duplicate clips",
                subtitle = "Bump existing matching clips to the top instead of creating duplicates",
                checked = settings.deduplicateClips,
                onCheckedChange = onToggleDeduplicate
            )
            SettingSwitchRow(
                title = "Quick-Capture notification",
                subtitle = "Keep a silent notification with a 1-tap Capture Now shortcut",
                checked = settings.monitorNotificationEnabled,
                onCheckedChange = onToggleMonitorNotification
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = AppColors.WarmDivider)

            Text(
                text = "Sort Order",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOrder.entries.forEach { order ->
                    val selected = order == settings.sortOrder
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectSortOrder(order) },
                        color = if (selected) AppColors.TealPrimary else AppColors.WarmPaperBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = order.label,
                            color = if (selected) Color.White else AppColors.TextPrimary,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = AppColors.WarmDivider)

            Text(
                text = "Vault & Backup",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onChangePin,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(if (settings.hasPinConfigured) "Change PIN" else "Set Vault PIN")
                }
                if (settings.hasPinConfigured) {
                    OutlinedButton(
                        onClick = onRemovePin,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Remove PIN")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onExportBackup,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Export JSON")
                }
                OutlinedButton(
                    onClick = onImportBackup,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Import JSON")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRelockSession,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Re-lock Clips")
                }
                OutlinedButton(
                    onClick = { showClearConfirm = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.DangerRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Clear History")
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = AppColors.PureWhite,
            title = { Text("Clear unpinned history?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "All unpinned, non-favorite, unlocked clips will be permanently removed. Pinned, favorite, and locked clips will be kept.",
                    fontSize = 13.5.sp,
                    color = AppColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearHistory()
                    }
                ) {
                    Text("Clear", color = AppColors.DangerRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = AppColors.TextSecondary, lineHeight = 16.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.TealPrimary
            )
        )
    }
}
