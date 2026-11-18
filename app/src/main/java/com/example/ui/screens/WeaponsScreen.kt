package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.domain.CategorySummary
import com.example.domain.ChallengeStatus
import com.example.domain.SearchFilterStatus
import com.example.domain.WeaponSortOption
import com.example.domain.WeaponWithDetails
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FavoriteStarButton
import com.example.ui.components.PercentPill
import com.example.ui.components.StatusBadge
import com.example.ui.components.TacticalCard
import com.example.ui.components.TacticalProgressBar
import com.example.ui.components.TacticalSearchBar
import com.example.ui.components.WeaponAvatar
import com.example.ui.theme.LocalTacticalColors

@Composable
fun WeaponsScreen(
    weapons: List<WeaponWithDetails>,
    categories: List<CategorySummary>,
    searchQuery: String,
    selectedCategoryFilter: String?,
    selectedStatusFilter: SearchFilterStatus,
    selectedSort: WeaponSortOption,
    hapticsEnabled: Boolean,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onStatusFilterChange: (SearchFilterStatus) -> Unit,
    onSortChange: (WeaponSortOption) -> Unit,
    onOpenCategories: () -> Unit,
    onAddWeapon: () -> Unit,
    onEditWeapon: (WeaponWithDetails) -> Unit,
    onDeleteWeapon: (WeaponWithDetails) -> Unit,
    onOpenWeaponDetail: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactical = LocalTacticalColors.current
    var showSortMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddWeapon,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_weapon_fab"),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Weapon")
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp),
            ) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "ARSENAL",
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            text = "${weapons.size} weapon${if (weapons.size == 1) "" else "s"} shown",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = onOpenCategories,
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(Icons.Filled.Category, contentDescription = null)
                            Text(" Categories")
                        }
                        Box {
                            IconButton(
                                onClick = { showSortMenu = true },
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("weapon_sort_button"),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort weapons",
                                )
                            }
                            DropdownMenu(
                                expanded = showSortMenu,
                                onDismissRequest = { showSortMenu = false },
                            ) {
                                WeaponSortOption.entries.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = if (option == selectedSort) "✓ ${option.label}" else option.label,
                                            )
                                        },
                                        onClick = {
                                            onSortChange(option)
                                            showSortMenu = false
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                TacticalSearchBar(
                    query = searchQuery,
                    onQueryChange = onSearchChange,
                    placeholder = "Search weapons by name or category…",
                    testTag = "weapon_search_input",
                )

                Spacer(Modifier.height(8.dp))
                // Category filter strip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { onCategoryFilterChange(null) },
                            label = { Text("All Categories") },
                        )
                    }
                    items(categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat.id,
                            onClick = {
                                onCategoryFilterChange(if (selectedCategoryFilter == cat.id) null else cat.id)
                            },
                            label = { Text("${cat.name} (${cat.weaponCount})") },
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == "UNCATEGORIZED",
                            onClick = {
                                onCategoryFilterChange(
                                    if (selectedCategoryFilter == "UNCATEGORIZED") null else "UNCATEGORIZED"
                                )
                            },
                            label = { Text("Uncategorized") },
                        )
                    }
                }

                // Status filter strip
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(SearchFilterStatus.entries) { status ->
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { onStatusFilterChange(status) },
                            label = { Text(status.label) },
                        )
                    }
                }

                Spacer(Modifier.height(6.dp))
                if (weapons.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Filled.GpsFixed,
                        title = "No Weapons Found",
                        subtitle = if (searchQuery.isNotBlank() || selectedCategoryFilter != null || selectedStatusFilter != SearchFilterStatus.ALL) {
                            "Try adjusting your search or filters."
                        } else {
                            "Tap + to add your first weapon and start tracking camo challenges."
                        },
                        actionLabel = if (searchQuery.isBlank() && selectedCategoryFilter == null) "Add Weapon" else null,
                        onAction = if (searchQuery.isBlank() && selectedCategoryFilter == null) onAddWeapon else null,
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(top = 4.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(weapons, key = { it.id }) { weapon ->
                            WeaponListCard(
                                weapon = weapon,
                                hapticsEnabled = hapticsEnabled,
                                onClick = { onOpenWeaponDetail(weapon.id) },
                                onToggleFavorite = { onToggleFavorite(weapon.id) },
                                onEdit = { onEditWeapon(weapon) },
                                onDelete = { onDeleteWeapon(weapon) },
                                successColor = tactical.success,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeaponListCard(
    weapon: WeaponWithDetails,
    hapticsEnabled: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    successColor: androidx.compose.ui.graphics.Color,
) {
    var showMenu by remember { mutableStateOf(false) }

    TacticalCard(
        onClick = onClick,
        accentColor = if (weapon.status == ChallengeStatus.COMPLETED) successColor else null,
        modifier = Modifier.testTag("weapon_card_${weapon.id}"),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            WeaponAvatar(
                iconKey = weapon.entity.iconKey,
                imageUri = weapon.entity.imageUri,
                contentDescription = weapon.name,
                size = 52.dp,
                isCompleted = weapon.status == ChallengeStatus.COMPLETED,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = weapon.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${weapon.categoryName} • ${weapon.camos.size} camos • ${weapon.completedChallenges}/${weapon.totalChallenges} challenges",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FavoriteStarButton(
                isFavorite = weapon.isFavorite,
                onToggle = onToggleFavorite,
                contentDescription = "Favorite ${weapon.name}",
                hapticsEnabled = hapticsEnabled,
                testTag = "weapon_fav_${weapon.id}",
            )
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Weapon options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Weapon") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Weapon") },
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
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TacticalProgressBar(
                fraction = weapon.completionFraction,
                isCompleted = weapon.status == ChallengeStatus.COMPLETED,
                modifier = Modifier.weight(1f),
            )
            PercentPill(
                percent = weapon.completionPercent,
                isCompleted = weapon.status == ChallengeStatus.COMPLETED,
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(status = weapon.status)
            if (weapon.entity.notes.isNotBlank()) {
                Text(
                    text = weapon.entity.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}
