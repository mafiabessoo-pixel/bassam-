package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.CamoWithChallenges
import com.example.domain.CategorySummary
import com.example.domain.ChallengeItem
import com.example.domain.ChallengeStatus
import com.example.domain.WeaponWithDetails
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FavoriteStarButton
import com.example.ui.components.PercentPill
import com.example.ui.components.StatusBadge
import com.example.ui.components.TacticalCard
import com.example.ui.components.TacticalProgressBar
import com.example.ui.components.TypeTagChip
import com.example.ui.components.WeaponAvatar
import com.example.ui.theme.LocalTacticalColors
import com.example.ui.theme.MonoMetricStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeaponDetailScreen(
    weapon: WeaponWithDetails?,
    categories: List<CategorySummary>,
    hapticsEnabled: Boolean,
    onBack: () -> Unit,
    onEditWeapon: (WeaponWithDetails) -> Unit,
    onDeleteWeapon: (WeaponWithDetails) -> Unit,
    onToggleWeaponFavorite: (String) -> Unit,
    onMoveWeaponCategory: (String, String?) -> Unit,
    onAddCamo: (String) -> Unit,
    onEditCamo: (CamoWithChallenges) -> Unit,
    onDeleteCamo: (CamoWithChallenges) -> Unit,
    onToggleCamoFavorite: (String) -> Unit,
    onAddChallenge: (weaponId: String, camoId: String?) -> Unit,
    onEditChallenge: (ChallengeItem) -> Unit,
    onDeleteChallenge: (ChallengeItem) -> Unit,
    onToggleChallengeFavorite: (String) -> Unit,
    onAdjustProgress: (String, Int) -> Unit,
    onEditManualProgress: (ChallengeItem) -> Unit,
    onMarkCompleted: (String) -> Unit,
    onResetChallenge: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactical = LocalTacticalColors.current
    val haptics = LocalHapticFeedback.current
    var showMoveCatMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = weapon?.name ?: "Weapon Not Found",
                        style = MaterialTheme.typography.headlineSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                actions = {
                    if (weapon != null) {
                        FavoriteStarButton(
                            isFavorite = weapon.isFavorite,
                            onToggle = { onToggleWeaponFavorite(weapon.id) },
                            contentDescription = "Favorite ${weapon.name}",
                            hapticsEnabled = hapticsEnabled,
                            testTag = "detail_weapon_fav",
                        )
                        IconButton(
                            onClick = { onEditWeapon(weapon) },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit weapon")
                        }
                        IconButton(
                            onClick = { onDeleteWeapon(weapon) },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = "Delete weapon",
                                tint = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                },
            )
        },
    ) { innerPadding ->
        if (weapon == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                EmptyStateView(
                    icon = Icons.Filled.MilitaryTech,
                    title = "Weapon Removed",
                    subtitle = "This weapon is no longer in your arsenal.",
                    actionLabel = "Go Back",
                    onAction = onBack,
                )
            }
            return@Scaffold
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Weapon Header Card
                item {
                    TacticalCard(
                        accentColor = if (weapon.status == ChallengeStatus.COMPLETED) tactical.success else MaterialTheme.colorScheme.primary,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            WeaponAvatar(
                                iconKey = weapon.entity.iconKey,
                                imageUri = weapon.entity.imageUri,
                                contentDescription = weapon.name,
                                size = 64.dp,
                                isCompleted = weapon.status == ChallengeStatus.COMPLETED,
                            )
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { showMoveCatMenu = true },
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Text(
                                                    text = weapon.categoryName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = tactical.silver,
                                                )
                                                Icon(
                                                    imageVector = Icons.Filled.DriveFileMove,
                                                    contentDescription = "Change category",
                                                    tint = tactical.silver,
                                                    modifier = Modifier.size(14.dp),
                                                )
                                            }
                                        }
                                        DropdownMenu(
                                            expanded = showMoveCatMenu,
                                            onDismissRequest = { showMoveCatMenu = false },
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Uncategorized") },
                                                onClick = {
                                                    onMoveWeaponCategory(weapon.id, null)
                                                    showMoveCatMenu = false
                                                },
                                            )
                                            categories.forEach { cat ->
                                                DropdownMenuItem(
                                                    text = { Text(cat.name) },
                                                    onClick = {
                                                        onMoveWeaponCategory(weapon.id, cat.id)
                                                        showMoveCatMenu = false
                                                    },
                                                )
                                            }
                                        }
                                    }
                                    StatusBadge(status = weapon.status)
                                }
                                if (weapon.entity.notes.isNotBlank()) {
                                    Text(
                                        text = weapon.entity.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                            PercentPill(
                                percent = weapon.completionPercent,
                                isCompleted = weapon.status == ChallengeStatus.COMPLETED,
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        TacticalProgressBar(
                            fraction = weapon.completionFraction,
                            height = 10.dp,
                            isCompleted = weapon.status == ChallengeStatus.COMPLETED,
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "Completed: ${weapon.completedChallenges}",
                                style = MaterialTheme.typography.labelMedium,
                                color = tactical.success,
                            )
                            Text(
                                text = "In Progress: ${weapon.inProgressChallenges}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "Remaining: ${weapon.remainingChallenges}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = { onAddCamo(weapon.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("add_camo_button"),
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Camo")
                            }
                            Button(
                                onClick = { onAddChallenge(weapon.id, weapon.camos.firstOrNull()?.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("add_challenge_button"),
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add Challenge")
                            }
                        }
                    }
                }

                // Camos & Challenges List
                if (weapon.camos.isEmpty() && weapon.challenges.isEmpty()) {
                    item {
                        TacticalCard {
                            EmptyStateView(
                                icon = Icons.Filled.MilitaryTech,
                                title = "No Camo Challenges Yet",
                                subtitle = "Add a camo tier (e.g., Gold, Diamond, Sand) or add a custom challenge directly.",
                                actionLabel = "Add First Challenge",
                                onAction = { onAddChallenge(weapon.id, null) },
                            )
                        }
                    }
                } else {
                    items(weapon.camos, key = { it.id }) { camo ->
                        CamoSectionCard(
                            camo = camo,
                            hapticsEnabled = hapticsEnabled,
                            onToggleCamoFavorite = { onToggleCamoFavorite(camo.id) },
                            onEditCamo = { onEditCamo(camo) },
                            onDeleteCamo = { onDeleteCamo(camo) },
                            onAddChallengeToCamo = { onAddChallenge(weapon.id, camo.id) },
                            onToggleChallengeFavorite = onToggleChallengeFavorite,
                            onAdjustProgress = { chId, delta ->
                                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onAdjustProgress(chId, delta)
                            },
                            onEditManualProgress = onEditManualProgress,
                            onMarkCompleted = { chId ->
                                if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onMarkCompleted(chId)
                            },
                            onResetChallenge = onResetChallenge,
                            onEditChallenge = onEditChallenge,
                            onDeleteChallenge = onDeleteChallenge,
                        )
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun CamoSectionCard(
    camo: CamoWithChallenges,
    hapticsEnabled: Boolean,
    onToggleCamoFavorite: () -> Unit,
    onEditCamo: () -> Unit,
    onDeleteCamo: () -> Unit,
    onAddChallengeToCamo: () -> Unit,
    onToggleChallengeFavorite: (String) -> Unit,
    onAdjustProgress: (String, Int) -> Unit,
    onEditManualProgress: (ChallengeItem) -> Unit,
    onMarkCompleted: (String) -> Unit,
    onResetChallenge: (String) -> Unit,
    onEditChallenge: (ChallengeItem) -> Unit,
    onDeleteChallenge: (ChallengeItem) -> Unit,
) {
    val tactical = LocalTacticalColors.current
    var showCamoMenu by remember { mutableStateOf(false) }

    TacticalCard(
        accentColor = if (camo.status == ChallengeStatus.COMPLETED) tactical.success else null,
    ) {
        // Camo Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = camo.name,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    PercentPill(
                        percent = camo.completionPercent,
                        isCompleted = camo.status == ChallengeStatus.COMPLETED,
                    )
                }
                Text(
                    text = "${camo.completedChallenges}/${camo.totalChallenges} challenges completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                FavoriteStarButton(
                    isFavorite = camo.isFavorite,
                    onToggle = onToggleCamoFavorite,
                    contentDescription = "Favorite ${camo.name} camo",
                    hapticsEnabled = hapticsEnabled,
                    testTag = "camo_fav_${camo.id}",
                )
                IconButton(
                    onClick = onAddChallengeToCamo,
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add challenge to ${camo.name}")
                }
                Box {
                    IconButton(
                        onClick = { showCamoMenu = true },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Camo options")
                    }
                    DropdownMenu(
                        expanded = showCamoMenu,
                        onDismissRequest = { showCamoMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Camo") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                showCamoMenu = false
                                onEditCamo()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Camo") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.DeleteOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                            onClick = {
                                showCamoMenu = false
                                onDeleteCamo()
                            },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        TacticalProgressBar(
            fraction = camo.completionFraction,
            height = 6.dp,
            isCompleted = camo.status == ChallengeStatus.COMPLETED,
        )

        if (camo.challenges.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = "No challenges in this camo yet. Tap + to add one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                camo.challenges.forEach { ch ->
                    ChallengeTrackerRow(
                        challenge = ch,
                        hapticsEnabled = hapticsEnabled,
                        onToggleFavorite = { onToggleChallengeFavorite(ch.id) },
                        onAdjustProgress = { delta -> onAdjustProgress(ch.id, delta) },
                        onEditManualProgress = { onEditManualProgress(ch) },
                        onMarkCompleted = { onMarkCompleted(ch.id) },
                        onReset = { onResetChallenge(ch.id) },
                        onEdit = { onEditChallenge(ch) },
                        onDelete = { onDeleteChallenge(ch) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeTrackerRow(
    challenge: ChallengeItem,
    hapticsEnabled: Boolean,
    onToggleFavorite: () -> Unit,
    onAdjustProgress: (Int) -> Unit,
    onEditManualProgress: () -> Unit,
    onMarkCompleted: () -> Unit,
    onReset: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val tactical = LocalTacticalColors.current
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (challenge.isCompleted) tactical.success.copy(alpha = 0.45f) else tactical.cardBorder,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = challenge.name,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        StatusBadge(status = challenge.status)
                        challenge.categoryName?.let { catName ->
                            TypeTagChip(label = catName)
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FavoriteStarButton(
                        isFavorite = challenge.isFavorite,
                        onToggle = onToggleFavorite,
                        contentDescription = "Favorite ${challenge.name}",
                        hapticsEnabled = hapticsEnabled,
                        testTag = "challenge_fav_${challenge.id}",
                    )
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Challenge actions")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Challenge") },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Mark Completed") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = tactical.success,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onMarkCompleted()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Reset Progress") },
                                leadingIcon = { Icon(Icons.Filled.Refresh, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    onReset()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Challenge") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.DeleteOutline,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                            )
                        }
                    }
                }
            }

            // Progress bar & clickable numeric badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TacticalProgressBar(
                    fraction = challenge.completionFraction,
                    height = 8.dp,
                    isCompleted = challenge.isCompleted,
                    modifier = Modifier.weight(1f),
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(0.8.dp, tactical.silver.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEditManualProgress)
                        .testTag("challenge_progress_pill_${challenge.id}"),
                ) {
                    Text(
                        text = "${challenge.currentProgress} / ${challenge.goal} (${challenge.completionPercent}%)",
                        style = MonoMetricStyle,
                        color = if (challenge.isCompleted) tactical.success else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    )
                }
            }

            // Stepper Controls: -5, -1, +1, +5, Done/Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = { onAdjustProgress(-5) },
                    enabled = challenge.currentProgress > 0,
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                ) {
                    Text("-5", style = MonoMetricStyle)
                }
                OutlinedButton(
                    onClick = { onAdjustProgress(-1) },
                    enabled = challenge.currentProgress > 0,
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                ) {
                    Text("-1", style = MonoMetricStyle)
                }
                FilledTonalButton(
                    onClick = { onAdjustProgress(1) },
                    enabled = challenge.currentProgress < challenge.goal,
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("challenge_plus1_${challenge.id}"),
                ) {
                    Text("+1", style = MonoMetricStyle)
                }
                FilledTonalButton(
                    onClick = { onAdjustProgress(5) },
                    enabled = challenge.currentProgress < challenge.goal,
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                ) {
                    Text("+5", style = MonoMetricStyle)
                }
                IconButton(
                    onClick = { if (challenge.isCompleted) onReset() else onMarkCompleted() },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("challenge_complete_toggle_${challenge.id}"),
                ) {
                    Icon(
                        imageVector = if (challenge.isCompleted) Icons.Filled.Refresh else Icons.Filled.CheckCircle,
                        contentDescription = if (challenge.isCompleted) "Reset progress" else "Mark completed",
                        tint = if (challenge.isCompleted) tactical.silver else tactical.success,
                    )
                }
            }
        }
    }
}
