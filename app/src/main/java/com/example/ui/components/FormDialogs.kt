package com.example.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.domain.CamoWithChallenges
import com.example.domain.CategorySummary
import com.example.domain.ChallengeItem
import com.example.domain.CodmPresets
import com.example.domain.WeaponWithDetails
import com.example.ui.theme.LocalTacticalColors

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WeaponFormDialog(
    categories: List<CategorySummary>,
    existingWeapon: WeaponWithDetails? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        categoryId: String?,
        iconKey: String,
        pickedImageUri: Uri?,
        clearExistingImage: Boolean,
        notes: String,
        includeStandardCamos: Boolean,
    ) -> Unit,
) {
    var name by remember { mutableStateOf(existingWeapon?.name ?: "") }
    var selectedCategoryId by remember {
        mutableStateOf(existingWeapon?.entity?.categoryId ?: categories.firstOrNull()?.id)
    }
    var selectedIconKey by remember {
        mutableStateOf(existingWeapon?.entity?.iconKey ?: categories.firstOrNull()?.entity?.iconKey ?: "assault")
    }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var clearExistingImage by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf(existingWeapon?.entity?.notes ?: "") }
    var includeStandardCamos by remember { mutableStateOf(existingWeapon == null) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pickedImageUri = uri
            clearExistingImage = false
        }
    }

    val currentPreviewUri: String? = when {
        pickedImageUri != null -> pickedImageUri.toString()
        clearExistingImage -> null
        else -> existingWeapon?.entity?.imageUri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingWeapon == null) "Add Weapon" else "Edit Weapon",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text("Weapon Name") },
                    placeholder = { Text("e.g., M4, AK-47, CBR4, DL Q33") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = errorText?.let { err -> { Text(err) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weapon_name_input"),
                )

                Text("Category", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("Uncategorized") },
                    )
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = {
                                selectedCategoryId = cat.id
                                if (existingWeapon == null) {
                                    selectedIconKey = cat.entity.iconKey
                                }
                            },
                            label = { Text(cat.name) },
                        )
                    }
                }

                Text("Weapon Image or Tactical Icon", style = MaterialTheme.typography.labelMedium)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (!currentPreviewUri.isNullOrBlank()) {
                        AsyncImage(
                            model = Uri.parse(currentPreviewUri),
                            contentDescription = "Selected weapon image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(10.dp)),
                        )
                    } else {
                        WeaponAvatar(
                            iconKey = selectedIconKey,
                            imageUri = null,
                            contentDescription = "Selected icon",
                            size = 52.dp,
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Pick Photo")
                    }
                    if (!currentPreviewUri.isNullOrBlank()) {
                        TextButton(
                            onClick = {
                                pickedImageUri = null
                                clearExistingImage = true
                            },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Text("Remove")
                        }
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CodmPresets.weaponIcons.forEach { iconOpt ->
                        FilterChip(
                            selected = selectedIconKey == iconOpt.key,
                            onClick = { selectedIconKey = iconOpt.key },
                            label = { Text(iconOpt.label) },
                            leadingIcon = {
                                Icon(
                                    imageVector = weaponIconForKey(iconOpt.key),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                )
                            },
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Loadout / Grind Notes (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth(),
                )

                if (existingWeapon == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { includeStandardCamos = !includeStandardCamos }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = includeStandardCamos,
                            onCheckedChange = { includeStandardCamos = it },
                        )
                        Spacer(Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Include standard CODM camos (0% progress)",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Text(
                                text = "Sand, Dragon, Splinter, Tiger, Jungle, Reptile & Diamond",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        errorText = "Enter a weapon name"
                    } else {
                        onSave(
                            name.trim(),
                            selectedCategoryId,
                            selectedIconKey,
                            pickedImageUri,
                            clearExistingImage,
                            notes.trim(),
                            includeStandardCamos,
                        )
                    }
                },
                modifier = Modifier.testTag("save_weapon_button"),
            ) {
                Text(if (existingWeapon == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CamoFormDialog(
    existingCamo: CamoWithChallenges? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, tierKey: String) -> Unit,
) {
    var name by remember { mutableStateOf(existingCamo?.name ?: "") }
    var tierKey by remember { mutableStateOf(existingCamo?.entity?.tierKey ?: "custom") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingCamo == null) "Add Camo Tier" else "Edit Camo",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text("Camo Name") },
                    placeholder = { Text("e.g., Gold, Diamond, Sand, Aether") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = errorText?.let { err -> { Text(err) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("camo_name_input"),
                )
                Text("Tier Preset", style = MaterialTheme.typography.labelMedium)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CodmPresets.camoTiers.forEach { (key, label) ->
                        FilterChip(
                            selected = tierKey == key,
                            onClick = {
                                tierKey = key
                                if (name.isBlank() && key != "custom") {
                                    name = label
                                }
                            },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        errorText = "Enter a camo name"
                    } else {
                        onSave(name.trim(), tierKey)
                    }
                },
                modifier = Modifier.testTag("save_camo_button"),
            ) {
                Text(if (existingCamo == null) "Add Camo" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChallengeFormDialog(
    camos: List<CamoWithChallenges>,
    categories: List<CategorySummary>,
    defaultCamoId: String?,
    defaultCategoryId: String?,
    existingChallenge: ChallengeItem? = null,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        goal: Int,
        initialProgress: Int,
        camoId: String?,
        categoryId: String?,
    ) -> Unit,
) {
    var name by remember { mutableStateOf(existingChallenge?.name ?: "") }
    var goalText by remember { mutableStateOf(existingChallenge?.goal?.toString() ?: "100") }
    var progressText by remember { mutableStateOf(existingChallenge?.currentProgress?.toString() ?: "0") }
    var selectedCamoId by remember {
        mutableStateOf(existingChallenge?.entity?.camoId ?: defaultCamoId ?: camos.firstOrNull()?.id)
    }
    var selectedCategoryId by remember {
        mutableStateOf(existingChallenge?.entity?.categoryId ?: defaultCategoryId)
    }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingChallenge == null) "Add Camo Challenge" else "Edit Challenge",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text("Challenge Name") },
                    placeholder = { Text("e.g., Headshot Kills, Long Shots") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = errorText?.let { err -> { Text(err) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("challenge_name_input"),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    OutlinedTextField(
                        value = goalText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 6) {
                                goalText = input
                                errorText = null
                            }
                        },
                        label = { Text("Goal") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("challenge_goal_input"),
                    )
                    if (existingChallenge == null) {
                        OutlinedTextField(
                            value = progressText,
                            onValueChange = { input ->
                                if (input.all { it.isDigit() } && input.length <= 6) {
                                    progressText = input
                                }
                            },
                            label = { Text("Initial Progress") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("challenge_initial_progress_input"),
                        )
                    }
                }

                if (camos.isNotEmpty()) {
                    Text("Camo Tier", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        camos.forEach { camo ->
                            FilterChip(
                                selected = selectedCamoId == camo.id,
                                onClick = { selectedCamoId = camo.id },
                                label = { Text(camo.name) },
                            )
                        }
                    }
                }

                if (categories.isNotEmpty()) {
                    Text("Category Tag", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        FilterChip(
                            selected = selectedCategoryId == null,
                            onClick = { selectedCategoryId = null },
                            label = { Text("Weapon Default") },
                        )
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { selectedCategoryId = cat.id },
                                label = { Text(cat.name) },
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val trimmed = name.trim()
                    val parsedGoal = goalText.toIntOrNull()
                    val parsedProgress = progressText.toIntOrNull() ?: 0
                    when {
                        trimmed.isEmpty() -> errorText = "Enter a challenge name"
                        parsedGoal == null || parsedGoal < 1 -> errorText = "Goal must be at least 1"
                        else -> onSave(
                            trimmed,
                            parsedGoal,
                            parsedProgress.coerceIn(0, parsedGoal),
                            selectedCamoId,
                            selectedCategoryId,
                        )
                    }
                },
                modifier = Modifier.testTag("save_challenge_button"),
            ) {
                Text(if (existingChallenge == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun ProgressEditDialog(
    challenge: ChallengeItem,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit,
) {
    var valueText by remember { mutableStateOf(challenge.currentProgress.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set Progress", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${challenge.name} (Goal: ${challenge.goal})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = valueText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 6) {
                            valueText = input
                            errorText = null
                        }
                    },
                    label = { Text("Current Progress (0–${challenge.goal})") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = errorText?.let { err -> { Text(err) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_progress_input"),
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = { valueText = "0" },
                        modifier = Modifier.weight(1f),
                    ) { Text("0") }
                    OutlinedButton(
                        onClick = { valueText = (challenge.goal / 2).toString() },
                        modifier = Modifier.weight(1f),
                    ) { Text("50%") }
                    OutlinedButton(
                        onClick = { valueText = challenge.goal.toString() },
                        modifier = Modifier.weight(1f),
                    ) { Text("Max") }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = valueText.toIntOrNull()
                    if (parsed == null) {
                        errorText = "Enter a valid number"
                    } else {
                        onSave(parsed.coerceIn(0, challenge.goal))
                    }
                },
                modifier = Modifier.testTag("confirm_manual_progress_button"),
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun CategoryFormDialog(
    existingCategory: CategorySummary? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, iconKey: String) -> Unit,
) {
    var name by remember { mutableStateOf(existingCategory?.name ?: "") }
    var iconKey by remember { mutableStateOf(existingCategory?.entity?.iconKey ?: "custom") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingCategory == null) "Create Category" else "Rename Category",
                style = MaterialTheme.typography.headlineSmall,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorText = null
                    },
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g., Ranked Meta, Mythics, Assault Rifles") },
                    singleLine = true,
                    isError = errorText != null,
                    supportingText = errorText?.let { err -> { Text(err) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("category_name_input"),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.trim().isEmpty()) {
                        errorText = "Enter a category name"
                    } else {
                        onSave(name.trim(), iconKey)
                    }
                },
                modifier = Modifier.testTag("save_category_button"),
            ) {
                Text(if (existingCategory == null) "Create" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
fun ConfirmDangerDialog(
    title: String,
    message: String,
    confirmLabel: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        },
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
                modifier = Modifier.testTag("confirm_danger_button"),
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
