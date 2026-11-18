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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.CategorySummary
import com.example.domain.ChallengeStatus
import com.example.domain.SearchFilterStatus
import com.example.domain.SearchResultItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FavoriteStarButton
import com.example.ui.components.PercentPill
import com.example.ui.components.StatusBadge
import com.example.ui.components.TacticalCard
import com.example.ui.components.TacticalProgressBar
import com.example.ui.components.TacticalSearchBar
import com.example.ui.components.TypeTagChip
import com.example.ui.theme.LocalTacticalColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    query: String,
    statusFilter: SearchFilterStatus,
    categoryFilter: String?,
    categories: List<CategorySummary>,
    results: List<SearchResultItem>,
    hapticsEnabled: Boolean,
    onBack: () -> Unit,
    onQueryChange: (String) -> Unit,
    onStatusFilterChange: (SearchFilterStatus) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onToggleFavorite: (SearchResultItem) -> Unit,
    onOpenWeaponDetail: (String) -> Unit,
    onOpenCategoryWeapons: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactical = LocalTacticalColors.current

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
                    Text("Global Search", style = MaterialTheme.typography.headlineSmall)
                },
            )
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
                TacticalSearchBar(
                    query = query,
                    onQueryChange = onQueryChange,
                    placeholder = "Search weapons, camos, challenges, categories…",
                    testTag = "global_search_input",
                )
                Spacer(Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(SearchFilterStatus.entries) { status ->
                        FilterChip(
                            selected = statusFilter == status,
                            onClick = { onStatusFilterChange(status) },
                            label = { Text(status.label) },
                        )
                    }
                }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = categoryFilter == null,
                            onClick = { onCategoryFilterChange(null) },
                            label = { Text("All Categories") },
                        )
                    }
                    items(categories, key = { it.id }) { cat ->
                        FilterChip(
                            selected = categoryFilter == cat.id,
                            onClick = {
                                onCategoryFilterChange(if (categoryFilter == cat.id) null else cat.id)
                            },
                            label = { Text(cat.name) },
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${results.size} result${if (results.size == 1) "" else "s"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))

                if (results.isEmpty()) {
                    EmptyStateView(
                        icon = Icons.Filled.Search,
                        title = "No Matching Items",
                        subtitle = "Try another search term or clear active filters.",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(results, key = { "${it.typeLabel}_${it.id}" }) { item ->
                            TacticalCard(
                                onClick = {
                                    when (item) {
                                        is SearchResultItem.WeaponResult -> onOpenWeaponDetail(item.weapon.id)
                                        is SearchResultItem.CamoResult -> onOpenWeaponDetail(item.camo.entity.weaponId)
                                        is SearchResultItem.ChallengeResult -> onOpenWeaponDetail(item.challenge.entity.weaponId)
                                        is SearchResultItem.CategoryResult -> onOpenCategoryWeapons(item.category.id)
                                    }
                                },
                                accentColor = if (item.status == ChallengeStatus.COMPLETED) tactical.success else null,
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
                                        isCompleted = item.status == ChallengeStatus.COMPLETED,
                                    )
                                    FavoriteStarButton(
                                        isFavorite = item.isFavorite,
                                        onToggle = { onToggleFavorite(item) },
                                        contentDescription = "Favorite ${item.title}",
                                        hapticsEnabled = hapticsEnabled,
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    TacticalProgressBar(
                                        fraction = item.completionPercent / 100f,
                                        height = 6.dp,
                                        isCompleted = item.status == ChallengeStatus.COMPLETED,
                                        modifier = Modifier.weight(1f),
                                    )
                                    StatusBadge(status = item.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
