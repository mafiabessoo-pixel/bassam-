package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.repository.GrindSnapshot
import com.example.domain.ChallengeStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.components.PercentPill
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatMetricCard
import com.example.ui.components.TacticalCard
import com.example.ui.components.TacticalProgressBar
import com.example.ui.theme.LocalTacticalColors
import com.example.ui.theme.MonoMetricStyle

@Composable
fun StatisticsScreen(
    snapshot: GrindSnapshot,
    onOpenWeaponDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactical = LocalTacticalColors.current
    val overview = snapshot.overview

    val mostProgressed = snapshot.weapons
        .sortedWith(
            compareByDescending<com.example.domain.WeaponWithDetails> { it.completionPercent }
                .thenByDescending { it.completedChallenges }
        )
        .take(5)

    val leastProgressed = snapshot.weapons
        .filter { it.completionPercent < 100 }
        .sortedWith(
            compareBy<com.example.domain.WeaponWithDetails> { it.completionPercent }
                .thenBy { it.name.lowercase() }
        )
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column {
                    Text(
                        text = "GRIND ANALYTICS",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Live telemetry calculated from saved progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (snapshot.weapons.isEmpty() && snapshot.challenges.isEmpty()) {
                item {
                    TacticalCard {
                        EmptyStateView(
                            icon = Icons.Filled.BarChart,
                            title = "No Statistics Available",
                            subtitle = "Add weapons and track camo challenges to see real-time completion charts and breakdowns.",
                        )
                    }
                }
                return@LazyColumn
            }

            // Donut Chart: Completed vs In Progress vs Not Started
            item {
                TacticalCard {
                    SectionHeader(title = "Challenge Breakdown")
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        Box(
                            modifier = Modifier.size(132.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val total = overview.totalChallenges.coerceAtLeast(1).toFloat()
                            val completedSweep = (overview.completedChallenges / total) * 360f
                            val inProgSweep = (overview.inProgressChallenges / total) * 360f
                            val notStartedSweep = (overview.notStartedChallenges / total) * 360f
                            val trackColor = tactical.progressTrack
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val successColor = tactical.success
                            val mutedColor = tactical.silver.copy(alpha = 0.45f)

                            Canvas(modifier = Modifier.size(120.dp)) {
                                val stroke = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Butt)
                                if (overview.totalChallenges == 0) {
                                    drawArc(
                                        color = trackColor,
                                        startAngle = -90f,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = stroke,
                                    )
                                } else {
                                    var angle = -90f
                                    if (completedSweep > 0f) {
                                        drawArc(
                                            color = successColor,
                                            startAngle = angle,
                                            sweepAngle = completedSweep,
                                            useCenter = false,
                                            style = stroke,
                                        )
                                        angle += completedSweep
                                    }
                                    if (inProgSweep > 0f) {
                                        drawArc(
                                            color = primaryColor,
                                            startAngle = angle,
                                            sweepAngle = inProgSweep,
                                            useCenter = false,
                                            style = stroke,
                                        )
                                        angle += inProgSweep
                                    }
                                    if (notStartedSweep > 0f) {
                                        drawArc(
                                            color = mutedColor,
                                            startAngle = angle,
                                            sweepAngle = notStartedSweep,
                                            useCenter = false,
                                            style = stroke,
                                        )
                                    }
                                }
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${overview.overallCompletionPercent}%",
                                    style = MaterialTheme.typography.headlineMedium,
                                )
                                Text(
                                    text = "${overview.totalChallenges} total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            LegendRow(
                                color = tactical.success,
                                label = "Completed",
                                value = "${overview.completedChallenges} (${pct(overview.completedChallenges, overview.totalChallenges)}%)",
                            )
                            LegendRow(
                                color = MaterialTheme.colorScheme.primary,
                                label = "In Progress",
                                value = "${overview.inProgressChallenges} (${pct(overview.inProgressChallenges, overview.totalChallenges)}%)",
                            )
                            LegendRow(
                                color = tactical.silver.copy(alpha = 0.5f),
                                label = "Not Started",
                                value = "${overview.notStartedChallenges} (${pct(overview.notStartedChallenges, overview.totalChallenges)}%)",
                            )
                            LegendRow(
                                color = tactical.gold,
                                label = "Remaining",
                                value = "${overview.remainingChallenges} challenges",
                            )
                        }
                    }
                }
            }

            // Favorite Weapons Stats
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatMetricCard(
                        title = "Favorite Weapons",
                        value = overview.favoriteWeaponsCount.toString(),
                        subtitle = "Avg ${overview.favoriteWeaponsAvgPercent}% complete",
                        icon = Icons.Filled.Star,
                        accentColor = tactical.gold,
                        modifier = Modifier.weight(1f),
                    )
                    StatMetricCard(
                        title = "Fav Challenges Done",
                        value = overview.favoriteWeaponsCompletedChallenges.toString(),
                        subtitle = "Across starred weapons",
                        icon = Icons.Filled.CheckCircle,
                        accentColor = tactical.success,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Weapon Progress Distribution Histogram
            item {
                TacticalCard {
                    SectionHeader(title = "Weapon Progress Distribution")
                    Spacer(Modifier.height(10.dp))
                    val labels = listOf("0%", "1–25%", "26–50%", "51–75%", "76–99%", "100%")
                    val counts = overview.progressDistribution
                    val maxCount = (counts.maxOrNull() ?: 1).coerceAtLeast(1)
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val successColor = tactical.success
                    val trackColor = tactical.progressTrack

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        counts.forEachIndexed { idx, count ->
                            val barFraction = count.toFloat() / maxCount.toFloat()
                            val barColor = if (idx == 5) successColor else primaryColor
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text(
                                    text = count.toString(),
                                    style = MonoMetricStyle,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(Modifier.height(4.dp))
                                Canvas(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(82.dp),
                                ) {
                                    drawRoundRect(
                                        color = trackColor,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                                    )
                                    if (barFraction > 0f) {
                                        val h = size.height * barFraction
                                        drawRoundRect(
                                            color = barColor,
                                            topLeft = Offset(0f, size.height - h),
                                            size = Size(size.width, h),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = labels[idx],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }

            // Most Progressed Weapons
            if (mostProgressed.isNotEmpty()) {
                item {
                    TacticalCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = tactical.success)
                            Text("Most Progressed Weapons", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            mostProgressed.forEach { weapon ->
                                WeaponStatRow(
                                    name = weapon.name,
                                    subtitle = "${weapon.categoryName} • ${weapon.completedChallenges}/${weapon.totalChallenges} challenges",
                                    percent = weapon.completionPercent,
                                    fraction = weapon.completionFraction,
                                    isCompleted = weapon.status == ChallengeStatus.COMPLETED,
                                    onClick = { onOpenWeaponDetail(weapon.id) },
                                )
                            }
                        }
                    }
                }
            }

            // Least Progressed Weapons
            if (leastProgressed.isNotEmpty()) {
                item {
                    TacticalCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(Icons.Filled.TrendingDown, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Least Progressed Weapons", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            leastProgressed.forEach { weapon ->
                                WeaponStatRow(
                                    name = weapon.name,
                                    subtitle = "${weapon.categoryName} • ${weapon.completedChallenges}/${weapon.totalChallenges} challenges",
                                    percent = weapon.completionPercent,
                                    fraction = weapon.completionFraction,
                                    isCompleted = weapon.status == ChallengeStatus.COMPLETED,
                                    onClick = { onOpenWeaponDetail(weapon.id) },
                                )
                            }
                        }
                    }
                }
            }

            // Category Progress Breakdown
            val activeCategories = snapshot.categories.filter { it.weaponCount > 0 || it.challengeCount > 0 }
            if (activeCategories.isNotEmpty()) {
                item {
                    TacticalCard {
                        SectionHeader(title = "Category Completion")
                        Spacer(Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            activeCategories.forEach { cat ->
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Text(
                                            text = "${cat.name} (${cat.weaponCount} weapons)",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = "${cat.completionPercent}%",
                                            style = MonoMetricStyle,
                                            color = if (cat.completionPercent == 100 && cat.challengeCount > 0) tactical.success else tactical.silver,
                                        )
                                    }
                                    TacticalProgressBar(
                                        fraction = cat.completionFraction,
                                        height = 6.dp,
                                        isCompleted = cat.challengeCount > 0 && cat.completedChallengeCount == cat.challengeCount,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun LegendRow(
    color: Color,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = value,
            style = MonoMetricStyle,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WeaponStatRow(
    name: String,
    subtitle: String,
    percent: Int,
    fraction: Float,
    isCompleted: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            PercentPill(percent = percent, isCompleted = isCompleted)
        }
        TacticalProgressBar(fraction = fraction, height = 6.dp, isCompleted = isCompleted)
    }
}

private fun pct(part: Int, total: Int): Int {
    if (total <= 0) return 0
    return ((part.toFloat() / total.toFloat()) * 100f).toInt()
}
