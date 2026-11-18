package com.example.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.data.local.CamoEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ChallengeEntity
import com.example.data.local.GrindDatabase
import com.example.data.local.WeaponEntity
import com.example.domain.CamoWithChallenges
import com.example.domain.CategorySummary
import com.example.domain.ChallengeItem
import com.example.domain.CodmPresets
import com.example.domain.GrindOverviewStats
import com.example.domain.WeaponWithDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

data class GrindSnapshot(
    val categories: List<CategorySummary> = emptyList(),
    val weapons: List<WeaponWithDetails> = emptyList(),
    val camos: List<CamoWithChallenges> = emptyList(),
    val challenges: List<ChallengeItem> = emptyList(),
    val overview: GrindOverviewStats = GrindOverviewStats(),
)

class GrindRepository(private val database: GrindDatabase) {
    private val categoryDao = database.categoryDao()
    private val weaponDao = database.weaponDao()
    private val camoDao = database.camoDao()
    private val challengeDao = database.challengeDao()

    val snapshotFlow: Flow<GrindSnapshot> = combine(
        categoryDao.observeAll(),
        weaponDao.observeAll(),
        camoDao.observeAll(),
        challengeDao.observeAll(),
    ) { categories, weapons, camos, challenges ->
        buildSnapshot(categories, weapons, camos, challenges)
    }

    private fun buildSnapshot(
        categories: List<CategoryEntity>,
        weapons: List<WeaponEntity>,
        camos: List<CamoEntity>,
        challenges: List<ChallengeEntity>,
    ): GrindSnapshot {
        val categoryMap = categories.associateBy { it.id }
        val weaponMap = weapons.associateBy { it.id }
        val camoMap = camos.associateBy { it.id }

        val challengeItems = challenges.map { entity ->
            val weaponName = weaponMap[entity.weaponId]?.name ?: "Unknown Weapon"
            val camoName = camoMap[entity.camoId]?.name ?: "General"
            val effectiveCatId = entity.categoryId ?: weaponMap[entity.weaponId]?.categoryId
            val categoryName = effectiveCatId?.let { categoryMap[it]?.name }
            ChallengeItem(
                entity = entity,
                weaponName = weaponName,
                camoName = camoName,
                categoryName = categoryName,
            )
        }

        val challengesByCamo = challengeItems.groupBy { it.entity.camoId }
        val camosWithChallenges = camos.map { camoEntity ->
            val weaponName = weaponMap[camoEntity.weaponId]?.name ?: "Unknown Weapon"
            CamoWithChallenges(
                entity = camoEntity,
                weaponName = weaponName,
                challenges = challengesByCamo[camoEntity.id].orEmpty(),
            )
        }

        val camosByWeapon = camosWithChallenges.groupBy { it.entity.weaponId }
        val challengesByWeapon = challengeItems.groupBy { it.entity.weaponId }

        val weaponsWithDetails = weapons.map { weaponEntity ->
            WeaponWithDetails(
                entity = weaponEntity,
                category = weaponEntity.categoryId?.let { categoryMap[it] },
                camos = camosByWeapon[weaponEntity.id].orEmpty(),
                challenges = challengesByWeapon[weaponEntity.id].orEmpty(),
            )
        }

        val categorySummaries = categories.map { cat ->
            val catWeapons = weaponsWithDetails.filter { it.entity.categoryId == cat.id }
            val catChallenges = challengeItems.filter {
                (it.entity.categoryId ?: weaponMap[it.entity.weaponId]?.categoryId) == cat.id
            }
            val completedCount = catChallenges.count { it.isCompleted }
            val fraction = if (catChallenges.isEmpty()) {
                0f
            } else {
                catChallenges.map { it.completionFraction }.average().toFloat()
            }
            CategorySummary(
                entity = cat,
                weaponCount = catWeapons.size,
                challengeCount = catChallenges.size,
                completedChallengeCount = completedCount,
                completionFraction = fraction,
            )
        }

        val totalChallenges = challengeItems.size
        val completedChallenges = challengeItems.count { it.isCompleted }
        val inProgressChallenges = challengeItems.count { !it.isCompleted && it.currentProgress > 0 }
        val notStartedChallenges = challengeItems.count { !it.isCompleted && it.currentProgress == 0 }
        val remainingChallenges = (totalChallenges - completedChallenges).coerceAtLeast(0)
        val overallFraction = if (challengeItems.isEmpty()) {
            0f
        } else {
            challengeItems.map { it.completionFraction }.average().toFloat()
        }
        val overallPercent = (overallFraction * 100f).let { kotlin.math.round(it).toInt() }.coerceIn(0, 100)

        val favWeapons = weaponsWithDetails.filter { it.isFavorite }
        val favAvg = if (favWeapons.isEmpty()) 0 else favWeapons.map { it.completionPercent }.average().toInt()
        val favCompleted = favWeapons.sumOf { it.completedChallenges }

        val distribution = IntArray(6)
        weaponsWithDetails.forEach { weapon ->
            val p = weapon.completionPercent
            when {
                p == 0 -> distribution[0]++
                p in 1..25 -> distribution[1]++
                p in 26..50 -> distribution[2]++
                p in 51..75 -> distribution[3]++
                p in 76..99 -> distribution[4]++
                else -> distribution[5]++
            }
        }

        val overview = GrindOverviewStats(
            totalWeapons = weaponsWithDetails.size,
            totalCamos = camosWithChallenges.size,
            totalChallenges = totalChallenges,
            completedChallenges = completedChallenges,
            inProgressChallenges = inProgressChallenges,
            notStartedChallenges = notStartedChallenges,
            remainingChallenges = remainingChallenges,
            overallCompletionFraction = overallFraction,
            overallCompletionPercent = overallPercent,
            favoriteWeaponsCount = favWeapons.size,
            favoriteWeaponsAvgPercent = favAvg,
            favoriteWeaponsCompletedChallenges = favCompleted,
            progressDistribution = distribution.toList(),
        )

        return GrindSnapshot(
            categories = categorySummaries,
            weapons = weaponsWithDetails,
            camos = camosWithChallenges,
            challenges = challengeItems,
            overview = overview,
        )
    }

    // --- Categories ---
    suspend fun addCategory(name: String, iconKey: String = "custom"): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Category name cannot be empty." }
        val now = System.currentTimeMillis()
        val existing = categoryDao.getAll()
        val id = "cat_${UUID.randomUUID().toString().take(8)}"
        categoryDao.insert(
            CategoryEntity(
                id = id,
                name = trimmed,
                iconKey = iconKey,
                isFavorite = false,
                sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1,
                createdAt = now,
                updatedAt = now,
            )
        )
        return id
    }

    suspend fun renameCategory(id: String, newName: String) {
        val trimmed = newName.trim()
        require(trimmed.isNotEmpty()) { "Category name cannot be empty." }
        val existing = categoryDao.getById(id) ?: return
        categoryDao.update(existing.copy(name = trimmed, updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteCategory(id: String) {
        categoryDao.deleteById(id)
    }

    suspend fun toggleCategoryFavorite(id: String) {
        val existing = categoryDao.getById(id) ?: return
        categoryDao.setFavorite(id, !existing.isFavorite, System.currentTimeMillis())
    }

    suspend fun moveWeaponToCategory(weaponId: String, categoryId: String?) {
        weaponDao.setCategory(weaponId, categoryId, System.currentTimeMillis())
    }

    suspend fun moveChallengeToCategory(challengeId: String, categoryId: String?) {
        challengeDao.setCategory(challengeId, categoryId, System.currentTimeMillis())
    }

    // --- Weapons ---
    suspend fun createWeapon(
        name: String,
        categoryId: String?,
        iconKey: String,
        imageUri: String?,
        notes: String,
        includeStandardCamos: Boolean,
    ): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Weapon name cannot be empty." }
        val now = System.currentTimeMillis()
        val weaponId = "wpn_${UUID.randomUUID().toString().take(8)}"
        val categoryIcon = categoryId?.let { categoryDao.getById(it)?.iconKey } ?: iconKey

        database.withTransaction {
            weaponDao.insert(
                WeaponEntity(
                    id = weaponId,
                    name = trimmed,
                    categoryId = categoryId,
                    iconKey = iconKey,
                    imageUri = imageUri,
                    notes = notes.trim(),
                    isFavorite = false,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            if (includeStandardCamos) {
                val templates = CodmPresets.standardTemplatesForCategory(categoryIcon)
                templates.forEachIndexed { index, tmpl ->
                    val camoId = "cam_${UUID.randomUUID().toString().take(8)}"
                    camoDao.insert(
                        CamoEntity(
                            id = camoId,
                            weaponId = weaponId,
                            name = tmpl.camoName,
                            tierKey = tmpl.tierKey,
                            isFavorite = false,
                            sortOrder = index,
                            createdAt = now + index,
                            updatedAt = now + index,
                        )
                    )
                    challengeDao.insert(
                        ChallengeEntity(
                            id = "chl_${UUID.randomUUID().toString().take(8)}",
                            weaponId = weaponId,
                            camoId = camoId,
                            categoryId = categoryId,
                            name = tmpl.challengeName,
                            goal = tmpl.goal,
                            currentProgress = 0,
                            isCompleted = false,
                            isFavorite = false,
                            createdAt = now + index,
                            updatedAt = now + index,
                        )
                    )
                }
            }
        }
        return weaponId
    }

    suspend fun updateWeapon(
        id: String,
        name: String,
        categoryId: String?,
        iconKey: String,
        imageUri: String?,
        notes: String,
    ) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Weapon name cannot be empty." }
        val existing = weaponDao.getById(id) ?: return
        weaponDao.update(
            existing.copy(
                name = trimmed,
                categoryId = categoryId,
                iconKey = iconKey,
                imageUri = imageUri,
                notes = notes.trim(),
                updatedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun deleteWeapon(id: String) {
        weaponDao.deleteById(id)
    }

    suspend fun toggleWeaponFavorite(id: String) {
        val existing = weaponDao.getById(id) ?: return
        weaponDao.setFavorite(id, !existing.isFavorite, System.currentTimeMillis())
    }

    // --- Camos ---
    suspend fun createCamo(weaponId: String, name: String, tierKey: String): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Camo name cannot be empty." }
        val now = System.currentTimeMillis()
        val existing = camoDao.getByWeaponId(weaponId)
        val camoId = "cam_${UUID.randomUUID().toString().take(8)}"
        database.withTransaction {
            camoDao.insert(
                CamoEntity(
                    id = camoId,
                    weaponId = weaponId,
                    name = trimmed,
                    tierKey = tierKey,
                    isFavorite = false,
                    sortOrder = (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            weaponDao.touch(weaponId, now)
        }
        return camoId
    }

    suspend fun updateCamo(camoId: String, name: String, tierKey: String) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Camo name cannot be empty." }
        val existing = camoDao.getById(camoId) ?: return
        val now = System.currentTimeMillis()
        database.withTransaction {
            camoDao.update(existing.copy(name = trimmed, tierKey = tierKey, updatedAt = now))
            weaponDao.touch(existing.weaponId, now)
        }
    }

    suspend fun deleteCamo(camoId: String) {
        val existing = camoDao.getById(camoId) ?: return
        database.withTransaction {
            camoDao.deleteById(camoId)
            weaponDao.touch(existing.weaponId, System.currentTimeMillis())
        }
    }

    suspend fun toggleCamoFavorite(camoId: String) {
        val existing = camoDao.getById(camoId) ?: return
        camoDao.setFavorite(camoId, !existing.isFavorite, System.currentTimeMillis())
    }

    // --- Challenges ---
    suspend fun createChallenge(
        weaponId: String,
        camoId: String?,
        categoryId: String?,
        name: String,
        goal: Int,
        initialProgress: Int = 0,
    ): String {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Challenge name cannot be empty." }
        val safeGoal = goal.coerceAtLeast(1)
        val safeProgress = initialProgress.coerceIn(0, safeGoal)
        val now = System.currentTimeMillis()

        val resolvedCamoId = if (camoId != null) {
            camoId
        } else {
            val existingCamos = camoDao.getByWeaponId(weaponId)
            existingCamos.firstOrNull()?.id ?: run {
                val newCamoId = "cam_${UUID.randomUUID().toString().take(8)}"
                camoDao.insert(
                    CamoEntity(
                        id = newCamoId,
                        weaponId = weaponId,
                        name = "Custom",
                        tierKey = "custom",
                        isFavorite = false,
                        sortOrder = 0,
                        createdAt = now,
                        updatedAt = now,
                    )
                )
                newCamoId
            }
        }

        val id = "chl_${UUID.randomUUID().toString().take(8)}"
        database.withTransaction {
            challengeDao.insert(
                ChallengeEntity(
                    id = id,
                    weaponId = weaponId,
                    camoId = resolvedCamoId,
                    categoryId = categoryId,
                    name = trimmed,
                    goal = safeGoal,
                    currentProgress = safeProgress,
                    isCompleted = safeProgress >= safeGoal,
                    isFavorite = false,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            weaponDao.touch(weaponId, now)
        }
        return id
    }

    suspend fun updateChallengeDetails(
        challengeId: String,
        name: String,
        goal: Int,
        camoId: String,
        categoryId: String?,
    ) {
        val trimmed = name.trim()
        require(trimmed.isNotEmpty()) { "Challenge name cannot be empty." }
        val existing = challengeDao.getById(challengeId) ?: return
        val safeGoal = goal.coerceAtLeast(1)
        val clampedProgress = existing.currentProgress.coerceIn(0, safeGoal)
        val now = System.currentTimeMillis()
        database.withTransaction {
            challengeDao.update(
                existing.copy(
                    name = trimmed,
                    goal = safeGoal,
                    camoId = camoId,
                    categoryId = categoryId,
                    currentProgress = clampedProgress,
                    isCompleted = clampedProgress >= safeGoal,
                    updatedAt = now,
                )
            )
            weaponDao.touch(existing.weaponId, now)
        }
    }

    suspend fun setChallengeProgress(challengeId: String, newProgress: Int) {
        val existing = challengeDao.getById(challengeId) ?: return
        val safeGoal = existing.goal.coerceAtLeast(1)
        val clamped = newProgress.coerceIn(0, safeGoal)
        val now = System.currentTimeMillis()
        database.withTransaction {
            challengeDao.update(
                existing.copy(
                    currentProgress = clamped,
                    isCompleted = clamped >= safeGoal,
                    updatedAt = now,
                )
            )
            camoDao.getById(existing.camoId)?.let {
                camoDao.update(it.copy(updatedAt = now))
            }
            weaponDao.touch(existing.weaponId, now)
        }
    }

    suspend fun adjustChallengeProgress(challengeId: String, delta: Int) {
        val existing = challengeDao.getById(challengeId) ?: return
        setChallengeProgress(challengeId, existing.currentProgress + delta)
    }

    suspend fun markChallengeCompleted(challengeId: String) {
        val existing = challengeDao.getById(challengeId) ?: return
        setChallengeProgress(challengeId, existing.goal)
    }

    suspend fun resetChallengeProgress(challengeId: String) {
        setChallengeProgress(challengeId, 0)
    }

    suspend fun deleteChallenge(challengeId: String) {
        val existing = challengeDao.getById(challengeId) ?: return
        database.withTransaction {
            challengeDao.deleteById(challengeId)
            weaponDao.touch(existing.weaponId, System.currentTimeMillis())
        }
    }

    suspend fun toggleChallengeFavorite(challengeId: String) {
        val existing = challengeDao.getById(challengeId) ?: return
        challengeDao.setFavorite(challengeId, !existing.isFavorite, System.currentTimeMillis())
    }

    // --- Reset All Data ---
    suspend fun resetAllData(restoreDefaultCategories: Boolean = true) {
        database.withTransaction {
            challengeDao.deleteAll()
            camoDao.deleteAll()
            weaponDao.deleteAll()
            categoryDao.deleteAll()
            if (restoreDefaultCategories) {
                categoryDao.insertAll(GrindDatabase.defaultCategories())
            }
        }
    }

    // --- Copy picked weapon image into app private files so URI stays valid offline ---
    suspend fun persistPickedImage(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val imagesDir = File(context.filesDir, "weapon_images").apply { mkdirs() }
            val destFile = File(imagesDir, "wpn_${UUID.randomUUID().toString().take(8)}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return@runCatching null
            Uri.fromFile(destFile).toString()
        }.getOrNull()
    }
}
