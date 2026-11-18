package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.repository.GrindSnapshot
import com.example.domain.ChallengeStatus
import com.example.domain.FavoriteItem
import com.example.ui.components.EmptyStateView
import com.example.ui.components.FavoriteStarButton
import com.example.ui.components.PercentPill
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatMetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.components.TacticalCard
import com.example.ui.components.TacticalProgressBar
import com.example.ui.components.TypeTagChip
import com.example.ui.components.WeaponAvatar
import com.example.ui.components.weaponIconForKey
import com.example.ui.theme.LocalTacticalColors
import com.example.ui.theme.MonoMetricStyle

@Composable
fun DashboardScreen(
    snapshot: GrindSnapshot,
    favorites: List<FavoriteItem>,
    hapticsEnabled: Boolean,
    onOpenSearch: () -> Unit,
    onOpenCategories: () -> Unit,
    onAddWeaponClick: () -> Unit,
    onOpenWeaponDetail: (String) -> Unit,
    onOpenCategoryWeapons: (String) -> Unit,
    onOpenFavoritesTab: () -> Unit,
    onOpenWeaponsTab: () -> Unit,
    onAdjustChallengeProgress: (String, Int) -> Unit,
    onToggleWeaponFavorite: (String) -> Unit,
    onToggleFavoriteItem: (FavoriteItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactical = LocalTacticalColors.current
    val haptics = LocalHapticFeedback.current
    val overview = snapshot.overview

    val inProgressChallenges = snapshot.challenges
        .filter { it.status == ChallengeStatus.IN_PROGRESS }
        .sortedByDescending { it.entity.updatedAt }
        .take(5)

    val completedChallenges = snapshot.challenges
        .filter { it.isCompleted }
        .sortedByDescending { it.entity.updatedAt }
        .take(5)

    val recentWeapons = snapshot.weapons
        .sortedByDescending { it.entity.updatedAt }
        .take(5)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Top Header Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "CODM GRIND TRACKER",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                        Text(
                            text = "Tactical Camo & Weapon Progression",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onOpenCategories,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dashboard_categories_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Category,
                                contentDescription = "Manage Categories",
                                tint = tactical.silver,
                            )
                        }
                        IconButton(
                            onClick = onOpenSearch,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dashboard_search_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Global Search",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                }
            }

            // Hero Overall Progress Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_tactical),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize(),
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xEB0B0C0F),
                                            Color(0xCC12141A),
                                            Color(0x99181B23),
                                        )
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = "OVERALL GRIND COMPLETION",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = tactical.silver,
                                )
                                Text(
                                    text = "${overview.overallCompletionPercent}%",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = Color.White,
                                )
                                TacticalProgressBar(
                                    fraction = overview.overallCompletionFraction,
                                    isCompleted = overview.totalChallenges > 0 && overview.remainingChallenges == 0,
                                    height = 9.dp,
                                )
                                Text(
                                    text = if (overview.totalChallenges == 0) {
                                        "Add weapons and camo challenges to begin tracking."
                                    } else {
                                        "${overview.completedChallenges} of ${overview.totalChallenges} challenges completed"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD0D6E0),
                                )
                                Spacer(Modifier.height(2.dp))
                                Button(
                                    onClick = onAddWeaponClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White,
                                    ),
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("dashboard_add_weapon_button"),
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Add Weapon", style = MaterialTheme.typography.labelLarge)
                                }
                            }

                            // Circular Progress Ring
                            Box(
                                modifier = Modifier.size(88.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                val trackColor = Color(0xFF2B303D)
                                val ringColor = if (overview.totalChallenges > 0 && overview.remainingChallenges == 0) {
                                    tactical.success
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                                Canvas(modifier = Modifier.size(82.dp)) {
                                    drawArc(
                                        color = trackColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                                    )
                                    drawArc(
                                        color = ringColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f * overview.overallCompletionFraction,
                                        useCenter = false,
                                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                                    )
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${overview.totalWeapons}",
                                        style = MaterialTheme.typography.titleLarge,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = "WEAPONS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFA0AAB8),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2x2 Overview Stats Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        StatMetricCard(
                            title = "Total Weapons",
                            value = overview.totalWeapons.toString(),
                            subtitle = "${overview.totalCamos} camos tracked",
                            icon = Icons.Filled.GpsFixed,
                            accentColor = tactical.silver,
                            modifier = Modifier.weight(1f),
                        )
                        StatMetricCard(
                            title = "Completed",
                            value = overview.completedChallenges.toString(),
                            subtitle = "Challenges unlocked",
                            icon = Icons.Filled.CheckCircle,
                            accentColor = tactical.success,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        StatMetricCard(
                            title = "Remaining",
                            value = overview.remainingChallenges.toString(),
                            subtitle = "${overview.inProgressChallenges} active now",
                            icon = Icons.Filled.HourglassTop,
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f),
                        )
                        StatMetricCard(
                            title = "Overall Rate",
                            value = "${overview.overallCompletionPercent}%",
                            subtitle = "${favorites.size} starred items",
                            icon = Icons.Filled.TrendingUp,
                            accentColor = tactical.gold,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // Quick Access to Weapon Categories
            if (snapshot.categories.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Weapon Categories",
                        trailingText = "Manage",
                        onTrailingClick = onOpenCategories,
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(snapshot.categories, key = { it.id }) { cat ->
                            Surface(
                                modifier = Modifier
                                    .width(148.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable { onOpenCategoryWeapons(cat.id) },
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, tactical.cardBorder),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Icon(
                                            imageVector = weaponIconForKey(cat.entity.iconKey),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Text(
                                            text = "${cat.completionPercent}%",
                                            style = MonoMetricStyle,
                                            color = if (cat.challengeCount > 0 && cat.completionPercent == 100) tactical.success else tactical.silver,
                                        )
                                    }
                                    Text(
                                        text = cat.name,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = "${cat.weaponCount} weapons • ${cat.challengeCount} ch.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    TacticalProgressBar(
                                        fraction = cat.completionFraction,
                                        height = 5.dp,
                                        isCompleted = cat.challengeCount > 0 && cat.completedChallengeCount == cat.challengeCount,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // First-Launch / Empty Arsenal Guide
            if (snapshot.weapons.isEmpty()) {
                item {
                    TacticalCard {
                        EmptyStateView(
                            icon = Icons.Filled.MilitaryTech,
                            title = "Your Arsenal is Ready",
                            subtitle = "Add your first CODM weapon to track Sand, Dragon, Splinter, Tiger, Jungle, Reptile, Gold, and Diamond camo challenges — 100% offline.",
                            actionLabel = "Add First Weapon",
                            onAction = onAddWeaponClick,
                        )
                    }
                }
            }

            // Favorite Items Strip
            if (favorites.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Favorites",
                        trailingText = "See All (${favorites.size})",
                        onTrailingClick = onOpenFavoritesTab,
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(favorites.take(8), key = { "${it.typeLabel}_${it.id}" }) { fav ->
                            Surface(
                                modifier = Modifier
                                    .width(210.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable {
                                        fav.weaponId?.let(onOpenWeaponDetail)
                                            ?: fav.categoryId?.let(onOpenCategoryWeapons)
                                    },
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, tactical.gold.copy(alpha = 0.4f)),
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        TypeTagChip(label = fav.typeLabel)
                                        FavoriteStarButton(
                                            isFavorite = true,
                                            onToggle = { onToggleFavoriteItem(fav) },
                                            contentDescription = "Remove ${fav.title} from favorites",
                                            hapticsEnabled = hapticsEnabled,
                                            modifier = Modifier.size(36.dp),
                                        )
                                    }
                                    Text(
                                        text = fav.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = fav.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        TacticalProgressBar(
                                            fraction = fav.completionPercent / 100f,
                                            height = 6.dp,
                                            isCompleted = fav.completionPercent == 100,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = "${fav.completionPercent}%",
                                            style = MonoMetricStyle,
                                            color = tactical.gold,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // In-Progress Challenges (with inline +1 / -1)
            if (inProgressChallenges.isNotEmpty()) {
                item {
                    SectionHeader(title = "In-Progress Challenges")
                }
                items(inProgressChallenges, key = { "dash_inprog_${it.id}" }) { ch ->
                    TacticalCard(
                        onClick = { onOpenWeaponDetail(ch.entity.weaponId) },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ch.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${ch.weaponName} • ${ch.camoName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                FilledTonalIconButton(
                                    onClick = {
                                        if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onAdjustChallengeProgress(ch.id, -1)
                                    },
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = "Decrease progress")
                                }
                                FilledTonalIconButton(
                                    onClick = {
                                        if (hapticsEnabled) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onAdjustChallengeProgress(ch.id, 1)
                                    },
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = "Increase progress")
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            TacticalProgressBar(
                                fraction = ch.completionFraction,
                                height = 7.dp,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${ch.currentProgress}/${ch.goal} (${ch.completionPercent}%)",
                                style = MonoMetricStyle,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }

            // Recently Updated Weapons
            if (recentWeapons.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recently Updated Weapons",
                        trailingText = "All Weapons",
                        onTrailingClick = onOpenWeaponsTab,
                    )
                }
                items(recentWeapons, key = { "dash_wpn_${it.id}" }) { weapon ->
                    TacticalCard(
                        onClick = { onOpenWeaponDetail(weapon.id) },
                        accentColor = if (weapon.status == ChallengeStatus.COMPLETED) tactical.success else null,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            WeaponAvatar(
                                iconKey = weapon.entity.iconKey,
                                imageUri = weapon.entity.imageUri,
                                contentDescription = weapon.name,
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
                                    text = "${weapon.categoryName} • ${weapon.completedChallenges}/${weapon.totalChallenges} challenges",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            PercentPill(
                                percent = weapon.completionPercent,
                                isCompleted = weapon.status == ChallengeStatus.COMPLETED,
                            )
                            FavoriteStarButton(
                                isFavorite = weapon.isFavorite,
                                onToggle = { onToggleWeaponFavorite(weapon.id) },
                                contentDescription = "Favorite ${weapon.name}",
                                hapticsEnabled = hapticsEnabled,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        TacticalProgressBar(
                            fraction = weapon.completionFraction,
                            isCompleted = weapon.status == ChallengeStatus.COMPLETED,
                        )
                    }
                }
            }

            // Completed Challenges
            if (completedChallenges.isNotEmpty()) {
                item {
                    SectionHeader(title = "Completed Challenges")
                }
                items(completedChallenges, key = { "dash_done_${it.id}" }) { ch ->
                    TacticalCard(
                        onClick = { onOpenWeaponDetail(ch.entity.weaponId) },
                        accentColor = tactical.success,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ch.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${ch.weaponName} • ${ch.camoName} (${ch.goal}/${ch.goal})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            StatusBadge(status = ChallengeStatus.COMPLETED)
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
