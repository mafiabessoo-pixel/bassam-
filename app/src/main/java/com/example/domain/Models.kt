package com.example.domain

import com.example.data.local.CamoEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ChallengeEntity
import com.example.data.local.WeaponEntity
import kotlin.math.roundToInt

enum class ChallengeStatus(val label: String) {
    NOT_STARTED("Not Started"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
}

enum class WeaponSortOption(val label: String) {
    RECENTLY_UPDATED("Recently Updated"),
    NAME_ASC("Name (A–Z)"),
    NAME_DESC("Name (Z–A)"),
    PROGRESS_DESC("Progress (High–Low)"),
    PROGRESS_ASC("Progress (Low–High)"),
}

enum class FavoriteFilterType(val label: String) {
    ALL("All"),
    WEAPONS("Weapons"),
    CAMOS("Camos"),
    CHALLENGES("Challenges"),
    CATEGORIES("Categories"),
}

enum class FavoriteSortOption(val label: String) {
    RECENTLY_UPDATED("Recently Updated"),
    NAME_ASC("Name (A–Z)"),
    PROGRESS_DESC("Progress (High–Low)"),
    TYPE("Item Type"),
}

enum class SearchFilterStatus(val label: String) {
    ALL("All"),
    COMPLETED("Completed"),
    IN_PROGRESS("In Progress"),
    NOT_STARTED("Not Started"),
    FAVORITES("Favorites"),
}

data class ChallengeItem(
    val entity: ChallengeEntity,
    val weaponName: String,
    val camoName: String,
    val categoryName: String?,
) {
    val id: String get() = entity.id
    val name: String get() = entity.name
    val goal: Int get() = entity.goal.coerceAtLeast(1)
    val currentProgress: Int get() = entity.currentProgress.coerceIn(0, goal)
    val isCompleted: Boolean get() = entity.isCompleted || currentProgress >= goal
    val isFavorite: Boolean get() = entity.isFavorite
    val completionFraction: Float get() = (currentProgress.toFloat() / goal.toFloat()).coerceIn(0f, 1f)
    val completionPercent: Int get() = (completionFraction * 100f).roundToInt().coerceIn(0, 100)
    val status: ChallengeStatus
        get() = when {
            isCompleted -> ChallengeStatus.COMPLETED
            currentProgress > 0 -> ChallengeStatus.IN_PROGRESS
            else -> ChallengeStatus.NOT_STARTED
        }
}

data class CamoWithChallenges(
    val entity: CamoEntity,
    val weaponName: String,
    val challenges: List<ChallengeItem>,
) {
    val id: String get() = entity.id
    val name: String get() = entity.name
    val isFavorite: Boolean get() = entity.isFavorite
    val totalChallenges: Int get() = challenges.size
    val completedChallenges: Int get() = challenges.count { it.isCompleted }
    val completionFraction: Float
        get() = if (challenges.isEmpty()) 0f else challenges.map { it.completionFraction }.average().toFloat()
    val completionPercent: Int get() = (completionFraction * 100f).roundToInt().coerceIn(0, 100)
    val status: ChallengeStatus
        get() = when {
            challenges.isNotEmpty() && completedChallenges == challenges.size -> ChallengeStatus.COMPLETED
            challenges.any { it.currentProgress > 0 || it.isCompleted } -> ChallengeStatus.IN_PROGRESS
            else -> ChallengeStatus.NOT_STARTED
        }
}

data class WeaponWithDetails(
    val entity: WeaponEntity,
    val category: CategoryEntity?,
    val camos: List<CamoWithChallenges>,
    val challenges: List<ChallengeItem>,
) {
    val id: String get() = entity.id
    val name: String get() = entity.name
    val isFavorite: Boolean get() = entity.isFavorite
    val categoryName: String get() = category?.name ?: "Uncategorized"
    val totalChallenges: Int get() = challenges.size
    val completedChallenges: Int get() = challenges.count { it.isCompleted }
    val inProgressChallenges: Int get() = challenges.count { it.status == ChallengeStatus.IN_PROGRESS }
    val notStartedChallenges: Int get() = challenges.count { it.status == ChallengeStatus.NOT_STARTED }
    val remainingChallenges: Int get() = (totalChallenges - completedChallenges).coerceAtLeast(0)
    val completionFraction: Float
        get() = if (challenges.isEmpty()) 0f else challenges.map { it.completionFraction }.average().toFloat()
    val completionPercent: Int get() = (completionFraction * 100f).roundToInt().coerceIn(0, 100)
    val status: ChallengeStatus
        get() = when {
            challenges.isNotEmpty() && completedChallenges == challenges.size -> ChallengeStatus.COMPLETED
            challenges.any { it.currentProgress > 0 || it.isCompleted } -> ChallengeStatus.IN_PROGRESS
            else -> ChallengeStatus.NOT_STARTED
        }
}

data class CategorySummary(
    val entity: CategoryEntity,
    val weaponCount: Int,
    val challengeCount: Int,
    val completedChallengeCount: Int,
    val completionFraction: Float,
) {
    val id: String get() = entity.id
    val name: String get() = entity.name
    val isFavorite: Boolean get() = entity.isFavorite
    val completionPercent: Int get() = (completionFraction * 100f).roundToInt().coerceIn(0, 100)
}

data class GrindOverviewStats(
    val totalWeapons: Int = 0,
    val totalCamos: Int = 0,
    val totalChallenges: Int = 0,
    val completedChallenges: Int = 0,
    val inProgressChallenges: Int = 0,
    val notStartedChallenges: Int = 0,
    val remainingChallenges: Int = 0,
    val overallCompletionFraction: Float = 0f,
    val overallCompletionPercent: Int = 0,
    val favoriteWeaponsCount: Int = 0,
    val favoriteWeaponsAvgPercent: Int = 0,
    val favoriteWeaponsCompletedChallenges: Int = 0,
    val progressDistribution: List<Int> = List(6) { 0 }, // 0%, 1-25, 26-50, 51-75, 76-99, 100%
)

sealed class FavoriteItem {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String
    abstract val completionPercent: Int
    abstract val updatedAt: Long
    abstract val typeLabel: String
    abstract val weaponId: String?
    abstract val categoryId: String?

    data class WeaponFav(val weapon: WeaponWithDetails) : FavoriteItem() {
        override val id: String = weapon.id
        override val title: String = weapon.name
        override val subtitle: String = "${weapon.categoryName} • ${weapon.completedChallenges}/${weapon.totalChallenges} challenges"
        override val completionPercent: Int = weapon.completionPercent
        override val updatedAt: Long = weapon.entity.updatedAt
        override val typeLabel: String = "Weapon"
        override val weaponId: String = weapon.id
        override val categoryId: String? = weapon.entity.categoryId
    }

    data class CamoFav(val camo: CamoWithChallenges) : FavoriteItem() {
        override val id: String = camo.id
        override val title: String = camo.name
        override val subtitle: String = "${camo.weaponName} • ${camo.completedChallenges}/${camo.totalChallenges} challenges"
        override val completionPercent: Int = camo.completionPercent
        override val updatedAt: Long = camo.entity.updatedAt
        override val typeLabel: String = "Camo"
        override val weaponId: String = camo.entity.weaponId
        override val categoryId: String? = null
    }

    data class ChallengeFav(val challenge: ChallengeItem) : FavoriteItem() {
        override val id: String = challenge.id
        override val title: String = challenge.name
        override val subtitle: String = "${challenge.weaponName} • ${challenge.camoName} (${challenge.currentProgress}/${challenge.goal})"
        override val completionPercent: Int = challenge.completionPercent
        override val updatedAt: Long = challenge.entity.updatedAt
        override val typeLabel: String = "Challenge"
        override val weaponId: String = challenge.entity.weaponId
        override val categoryId: String? = challenge.entity.categoryId
    }

    data class CategoryFav(val category: CategorySummary) : FavoriteItem() {
        override val id: String = category.id
        override val title: String = category.name
        override val subtitle: String = "${category.weaponCount} weapons • ${category.completedChallengeCount}/${category.challengeCount} challenges"
        override val completionPercent: Int = category.completionPercent
        override val updatedAt: Long = category.entity.updatedAt
        override val typeLabel: String = "Category"
        override val weaponId: String? = null
        override val categoryId: String = category.id
    }
}

sealed class SearchResultItem {
    abstract val id: String
    abstract val title: String
    abstract val subtitle: String
    abstract val completionPercent: Int
    abstract val isFavorite: Boolean
    abstract val status: ChallengeStatus
    abstract val categoryId: String?
    abstract val typeLabel: String

    data class WeaponResult(val weapon: WeaponWithDetails) : SearchResultItem() {
        override val id: String = weapon.id
        override val title: String = weapon.name
        override val subtitle: String = "${weapon.categoryName} • ${weapon.completedChallenges}/${weapon.totalChallenges} challenges"
        override val completionPercent: Int = weapon.completionPercent
        override val isFavorite: Boolean = weapon.isFavorite
        override val status: ChallengeStatus = weapon.status
        override val categoryId: String? = weapon.entity.categoryId
        override val typeLabel: String = "Weapon"
    }

    data class CamoResult(val camo: CamoWithChallenges, val weaponCategoryId: String?) : SearchResultItem() {
        override val id: String = camo.id
        override val title: String = camo.name
        override val subtitle: String = "${camo.weaponName} • ${camo.completedChallenges}/${camo.totalChallenges} challenges"
        override val completionPercent: Int = camo.completionPercent
        override val isFavorite: Boolean = camo.isFavorite
        override val status: ChallengeStatus = camo.status
        override val categoryId: String? = weaponCategoryId
        override val typeLabel: String = "Camo"
    }

    data class ChallengeResult(val challenge: ChallengeItem) : SearchResultItem() {
        override val id: String = challenge.id
        override val title: String = challenge.name
        override val subtitle: String = "${challenge.weaponName} • ${challenge.camoName} (${challenge.currentProgress}/${challenge.goal})"
        override val completionPercent: Int = challenge.completionPercent
        override val isFavorite: Boolean = challenge.isFavorite
        override val status: ChallengeStatus = challenge.status
        override val categoryId: String? = challenge.entity.categoryId
        override val typeLabel: String = "Challenge"
    }

    data class CategoryResult(val category: CategorySummary) : SearchResultItem() {
        override val id: String = category.id
        override val title: String = category.name
        override val subtitle: String = "${category.weaponCount} weapons • ${category.challengeCount} challenges"
        override val completionPercent: Int = category.completionPercent
        override val isFavorite: Boolean = category.isFavorite
        override val status: ChallengeStatus = when {
            category.challengeCount > 0 && category.completedChallengeCount == category.challengeCount -> ChallengeStatus.COMPLETED
            category.completedChallengeCount > 0 || category.completionPercent > 0 -> ChallengeStatus.IN_PROGRESS
            else -> ChallengeStatus.NOT_STARTED
        }
        override val categoryId: String = category.id
        override val typeLabel: String = "Category"
    }
}
