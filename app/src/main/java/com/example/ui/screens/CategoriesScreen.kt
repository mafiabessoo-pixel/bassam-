package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.repository.GrindSnapshot
import com.example.domain.CategorySummary
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FavoriteStarButton
import com.example.ui.components.PercentPill
import com.example.ui.components.TacticalCard
import com.example.ui.components.TacticalProgressBar
import com.example.ui.components.TypeTagChip
import com.example.ui.theme.LocalTacticalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    snapshot: GrindSnapshot,
    hapticsEnabled: Boolean,
    onBack: () -> Unit,
    onAddCategory: () -> Unit,
    onRenameCategory: (CategorySummary) -> Unit,
    onDeleteCategory: (CategorySummary) -> Unit,
    onToggleCategoryFavorite: (String) -> Unit,
    onMoveWeaponToCategory: (weaponId: String, categoryId: String?) -> Unit,
    onMoveChallengeToCategory: (challengeId: String, categoryId: String?) -> Unit,
    onOpenWeaponDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactical = LocalTacticalColors.current
    var expandedCategoryId by remember { mutableStateOf<String?>(null) }

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
                    Text("Categories & Organization", style = MaterialTheme.typography.headlineSmall)
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCategory,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_category_fab"),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Category")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (snapshot.categories.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Filled.Category,
                    title = "No Categories",
                    subtitle = "Create custom categories like Ranked Meta, Assault Rifles, or Mythics.",
                    actionLabel = "Create Category",
                    onAction = onAddCategory,
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 840.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(snapshot.categories, key = { it.id }) { cat ->
                        val isExpanded = expandedCategoryId == cat.id
                        val catWeapons = snapshot.weapons.filter { it.entity.categoryId == cat.id }
                        val catChallenges = snapshot.challenges.filter { it.entity.categoryId == cat.id }

                        TacticalCard(
                            onClick = {
                                expandedCategoryId = if (isExpanded) null else cat.id
                            },
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${cat.weaponCount} weapons • ${cat.completedChallengeCount}/${cat.challengeCount} challenges",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                PercentPill(
                                    percent = cat.completionPercent,
                                    isCompleted = cat.challengeCount > 0 && cat.completedChallengeCount == cat.challengeCount,
                                )
                                FavoriteStarButton(
                                    isFavorite = cat.isFavorite,
                                    onToggle = { onToggleCategoryFavorite(cat.id) },
                                    contentDescription = "Favorite ${cat.name}",
                                    hapticsEnabled = hapticsEnabled,
                                )
                                IconButton(
                                    onClick = { onRenameCategory(cat) },
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = "Rename ${cat.name}")
                                }
                                IconButton(
                                    onClick = { onDeleteCategory(cat) },
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(
                                        Icons.Filled.DeleteOutline,
                                        contentDescription = "Delete ${cat.name}",
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                                Icon(
                                    imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            TacticalProgressBar(
                                fraction = cat.completionFraction,
                                height = 6.dp,
                                isCompleted = cat.challengeCount > 0 && cat.completedChallengeCount == cat.challengeCount,
                            )

                            if (isExpanded) {
                                Spacer(Modifier.height(12.dp))
                                if (catWeapons.isEmpty() && catChallenges.isEmpty()) {
                                    Text(
                                        text = "No weapons or challenges assigned directly to this category.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        catWeapons.forEach { weapon ->
                                            MoveableCategoryItemRow(
                                                typeLabel = "Weapon",
                                                title = weapon.name,
                                                subtitle = "${weapon.completionPercent}% complete",
                                                categories = snapshot.categories,
                                                onOpen = { onOpenWeaponDetail(weapon.id) },
                                                onMove = { newCatId -> onMoveWeaponToCategory(weapon.id, newCatId) },
                                            )
                                        }
                                        catChallenges.forEach { ch ->
                                            MoveableCategoryItemRow(
                                                typeLabel = "Challenge",
                                                title = ch.name,
                                                subtitle = "${ch.weaponName} • ${ch.currentProgress}/${ch.goal}",
                                                categories = snapshot.categories,
                                                onOpen = { onOpenWeaponDetail(ch.entity.weaponId) },
                                                onMove = { newCatId -> onMoveChallengeToCategory(ch.id, newCatId) },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(Modifier.height(72.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MoveableCategoryItemRow(
    typeLabel: String,
    title: String,
    subtitle: String,
    categories: List<CategorySummary>,
    onOpen: () -> Unit,
    onMove: (String?) -> Unit,
) {
    val tactical = LocalTacticalColors.current
    var showMoveMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, tactical.cardBorder),
        onClick = onOpen,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    TypeTagChip(label = typeLabel)
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box {
                IconButton(
                    onClick = { showMoveMenu = true },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DriveFileMove,
                        contentDescription = "Move $title to another category",
                        tint = tactical.silver,
                    )
                }
                DropdownMenu(
                    expanded = showMoveMenu,
                    onDismissRequest = { showMoveMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Uncategorized") },
                        onClick = {
                            onMove(null)
                            showMoveMenu = false
                        },
                    )
                    categories.forEach { target ->
                        DropdownMenuItem(
                            text = { Text("Move to ${target.name}") },
                            onClick = {
                                onMove(target.id)
                                showMoveMenu = false
                            },
                        )
                    }
                }
            }
        }
    }
}
