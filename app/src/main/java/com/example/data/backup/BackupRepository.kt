package com.example.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.example.data.local.AppSettings
import com.example.data.local.CamoEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ChallengeEntity
import com.example.data.local.GrindDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.ThemeMode
import com.example.data.local.WeaponEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class BackupRepository(
    private val database: GrindDatabase,
    private val preferencesManager: PreferencesManager,
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(GrindBackup::class.java).indent("  ")

    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val categories = database.categoryDao().getAll().map {
            BackupCategory(
                id = it.id,
                name = it.name,
                iconKey = it.iconKey,
                isFavorite = it.isFavorite,
                sortOrder = it.sortOrder,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
        val weapons = database.weaponDao().getAll().map {
            BackupWeapon(
                id = it.id,
                name = it.name,
                categoryId = it.categoryId,
                iconKey = it.iconKey,
                imageUri = it.imageUri,
                notes = it.notes,
                isFavorite = it.isFavorite,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
        val camos = database.camoDao().getAll().map {
            BackupCamo(
                id = it.id,
                weaponId = it.weaponId,
                name = it.name,
                tierKey = it.tierKey,
                isFavorite = it.isFavorite,
                sortOrder = it.sortOrder,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
        val challenges = database.challengeDao().getAll().map {
            BackupChallenge(
                id = it.id,
                weaponId = it.weaponId,
                camoId = it.camoId,
                categoryId = it.categoryId,
                name = it.name,
                goal = it.goal,
                currentProgress = it.currentProgress,
                isCompleted = it.isCompleted,
                isFavorite = it.isFavorite,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
        val settings = preferencesManager.getCurrentSettings()
        val payload = GrindBackup(
            categories = categories,
            weapons = weapons,
            camos = camos,
            challenges = challenges,
            settings = BackupSettings(
                themeMode = settings.themeMode.name,
                hapticsEnabled = settings.hapticsEnabled,
            ),
        )
        adapter.toJson(payload)
    }

    suspend fun exportToUri(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val json = createBackupJson()
            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(json.toByteArray(Charsets.UTF_8))
                stream.flush()
            } ?: error("Unable to open destination file for writing.")
        }
    }

    suspend fun readAndValidateUri(context: Context, uri: Uri): BackupValidationResult = withContext(Dispatchers.IO) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).readText()
            } ?: return@withContext BackupValidationResult.Invalid("Could not read the selected file.")
            validateBackupJson(content)
        } catch (e: Exception) {
            BackupValidationResult.Invalid("Failed to read backup file: ${e.message ?: "Unknown error"}")
        }
    }

    fun validateBackupJson(rawJson: String): BackupValidationResult {
        if (rawJson.isBlank()) {
            return BackupValidationResult.Invalid("The selected backup file is empty.")
        }
        val parsed = try {
            adapter.fromJson(rawJson)
        } catch (e: Exception) {
            return BackupValidationResult.Invalid("Corrupted or incompatible JSON format: ${e.message ?: "Parse error"}")
        } ?: return BackupValidationResult.Invalid("Backup file could not be parsed.")

        if (parsed.app != "CODM Grind Tracker") {
            return BackupValidationResult.Invalid("This file does not appear to be a CODM Grind Tracker backup.")
        }
        if (parsed.schemaVersion !in 1..GrindBackup.CURRENT_SCHEMA_VERSION) {
            return BackupValidationResult.Invalid("Unsupported backup schema version: ${parsed.schemaVersion}")
        }

        val categoryIds = parsed.categories.map { it.id }
        if (categoryIds.any { it.isBlank() } || categoryIds.size != categoryIds.toSet().size) {
            return BackupValidationResult.Invalid("Backup contains invalid or duplicate category IDs.")
        }
        if (parsed.categories.any { it.name.isBlank() }) {
            return BackupValidationResult.Invalid("Backup contains a category with an empty name.")
        }

        val weaponIds = parsed.weapons.map { it.id }
        if (weaponIds.any { it.isBlank() } || weaponIds.size != weaponIds.toSet().size) {
            return BackupValidationResult.Invalid("Backup contains invalid or duplicate weapon IDs.")
        }
        if (parsed.weapons.any { it.name.isBlank() }) {
            return BackupValidationResult.Invalid("Backup contains a weapon with an empty name.")
        }
        val weaponIdSet = weaponIds.toSet()

        val camoIds = parsed.camos.map { it.id }
        if (camoIds.any { it.isBlank() } || camoIds.size != camoIds.toSet().size) {
            return BackupValidationResult.Invalid("Backup contains invalid or duplicate camo IDs.")
        }
        if (parsed.camos.any { it.name.isBlank() || it.weaponId !in weaponIdSet }) {
            return BackupValidationResult.Invalid("Backup contains camos referencing missing weapons.")
        }
        val camoIdSet = camoIds.toSet()

        val challengeIds = parsed.challenges.map { it.id }
        if (challengeIds.any { it.isBlank() } || challengeIds.size != challengeIds.toSet().size) {
            return BackupValidationResult.Invalid("Backup contains invalid or duplicate challenge IDs.")
        }
        for (ch in parsed.challenges) {
            if (ch.name.isBlank()) {
                return BackupValidationResult.Invalid("Backup contains a challenge with an empty name.")
            }
            if (ch.goal < 1) {
                return BackupValidationResult.Invalid("Challenge '${ch.name}' has an invalid goal (${ch.goal}).")
            }
            if (ch.currentProgress < 0) {
                return BackupValidationResult.Invalid("Challenge '${ch.name}' has negative progress.")
            }
            if (ch.weaponId !in weaponIdSet || ch.camoId !in camoIdSet) {
                return BackupValidationResult.Invalid("Challenge '${ch.name}' references a missing weapon or camo.")
            }
        }

        return BackupValidationResult.Valid(parsed)
    }

    suspend fun restoreBackup(backup: GrindBackup): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val now = System.currentTimeMillis()
            val validCatIds = backup.categories.map { it.id }.toSet()

            val categories = backup.categories.map {
                CategoryEntity(
                    id = it.id,
                    name = it.name.trim(),
                    iconKey = it.iconKey,
                    isFavorite = it.isFavorite,
                    sortOrder = it.sortOrder,
                    createdAt = if (it.createdAt > 0) it.createdAt else now,
                    updatedAt = if (it.updatedAt > 0) it.updatedAt else now,
                )
            }
            val weapons = backup.weapons.map {
                WeaponEntity(
                    id = it.id,
                    name = it.name.trim(),
                    categoryId = it.categoryId?.takeIf { cid -> cid in validCatIds },
                    iconKey = it.iconKey,
                    imageUri = it.imageUri,
                    notes = it.notes,
                    isFavorite = it.isFavorite,
                    createdAt = if (it.createdAt > 0) it.createdAt else now,
                    updatedAt = if (it.updatedAt > 0) it.updatedAt else now,
                )
            }
            val camos = backup.camos.map {
                CamoEntity(
                    id = it.id,
                    weaponId = it.weaponId,
                    name = it.name.trim(),
                    tierKey = it.tierKey,
                    isFavorite = it.isFavorite,
                    sortOrder = it.sortOrder,
                    createdAt = if (it.createdAt > 0) it.createdAt else now,
                    updatedAt = if (it.updatedAt > 0) it.updatedAt else now,
                )
            }
            val challenges = backup.challenges.map {
                val safeGoal = it.goal.coerceAtLeast(1)
                val safeProgress = it.currentProgress.coerceIn(0, safeGoal)
                ChallengeEntity(
                    id = it.id,
                    weaponId = it.weaponId,
                    camoId = it.camoId,
                    categoryId = it.categoryId?.takeIf { cid -> cid in validCatIds },
                    name = it.name.trim(),
                    goal = safeGoal,
                    currentProgress = safeProgress,
                    isCompleted = safeProgress >= safeGoal,
                    isFavorite = it.isFavorite,
                    createdAt = if (it.createdAt > 0) it.createdAt else now,
                    updatedAt = if (it.updatedAt > 0) it.updatedAt else now,
                )
            }

            database.withTransaction {
                database.challengeDao().deleteAll()
                database.camoDao().deleteAll()
                database.weaponDao().deleteAll()
                database.categoryDao().deleteAll()

                database.categoryDao().insertAll(categories)
                database.weaponDao().insertAll(weapons)
                database.camoDao().insertAll(camos)
                database.challengeDao().insertAll(challenges)
            }

            preferencesManager.applySettings(
                AppSettings(
                    themeMode = ThemeMode.fromName(backup.settings.themeMode),
                    hapticsEnabled = backup.settings.hapticsEnabled,
                )
            )
        }
    }
}
