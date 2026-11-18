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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.domain.FavoriteFilterType
import com.example.domain.FavoriteItem
import com.example.domain.FavoriteSortOption
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FavoriteStarButton
import com.example.ui.components.PercentPill
import com.example.ui.components.TacticalCard
import com.example.ui.components.TacticalProgressBar
import com.example.ui.components.TacticalSearchBar
import com.example.ui.components.TypeTagChip
import com.example.ui.theme.LocalTacticalColors

@Composable
fun FavoritesScreen(
    favorites: List<FavoriteItem>,
    searchQuery: String,
    typeFilter: FavoriteFilterType,
    sortOption: FavoriteSortOption,
    hapticsEnabled: Boolean,
    onSearchChange: (String) -> Unit,
    onTypeFilterChange: (FavoriteFilterType) -> Unit,
    onSortChange: (FavoriteSortOption) -> Unit,
    onToggleFavoriteItem: (FavoriteItem) -> Unit,
    onOpenWeaponDetail: (String) -> Unit,
    onOpenCategoryWeapons: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactical = LocalTacticalColors.current
    var showSortMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
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
                        text = "FAVORITES",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "${favorites.size} starred item${if (favorites.size == 1) "" else "s"} • Independent tracking",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("favorites_sort_button"),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = "Sort favorites",
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                    ) {
                        FavoriteSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(if (option == sortOption) "✓ ${option.label}" else option.label)
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

            Spacer(Modifier.height(10.dp))
            TacticalSearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange,
                placeholder = "Search favorites…",
                testTag = "favorites_search_input",
            )

            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(FavoriteFilterType.entries) { filter ->
                    FilterChip(
                        selected = typeFilter == filter,
                        onClick = { onTypeFilterChange(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            if (favorites.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Filled.Star,
                    title = "No Favorites Yet",
                    subtitle = "Tap the star icon (☆) on any Weapon, Camo, Challenge, or Category to pin it here. Unstarring never deletes your data.",
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(favorites, key = { "${it.typeLabel}_${it.id}" }) { item ->
                        TacticalCard(
                            onClick = {
                                item.weaponId?.let(onOpenWeaponDetail)
                                    ?: item.categoryId?.let(onOpenCategoryWeapons)
                            },
                            accentColor = tactical.gold,
                            modifier = Modifier.testTag("favorite_item_${item.id}"),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        TypeTagChip(label = item.typeLabel)
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Text(
                                        text = item.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                PercentPill(
                                    percent = item.completionPercent,
                                    isCompleted = item.completionPercent == 100,
                                )
                                FavoriteStarButton(
                                    isFavorite = true,
                                    onToggle = { onToggleFavoriteItem(item) },
                                    contentDescription = "Remove ${item.title} from favorites",
                                    hapticsEnabled = hapticsEnabled,
                                    testTag = "unfavorite_${item.id}",
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            TacticalProgressBar(
                                fraction = item.completionPercent / 100f,
                                isCompleted = item.completionPercent == 100,
                            )
                        }
                    }
                }
            }
        }
    }
}
