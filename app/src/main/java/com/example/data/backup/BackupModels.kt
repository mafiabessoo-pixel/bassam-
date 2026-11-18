package com.example.data.backup

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GrindBackup(
    val app: String = "CODM Grind Tracker",
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val categories: List<BackupCategory> = emptyList(),
    val weapons: List<BackupWeapon> = emptyList(),
    val camos: List<BackupCamo> = emptyList(),
    val challenges: List<BackupChallenge> = emptyList(),
    val settings: BackupSettings = BackupSettings(),
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

@JsonClass(generateAdapter = true)
data class BackupCategory(
    val id: String,
    val name: String,
    val iconKey: String = "custom",
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@JsonClass(generateAdapter = true)
data class BackupWeapon(
    val id: String,
    val name: String,
    val categoryId: String? = null,
    val iconKey: String = "assault",
    val imageUri: String? = null,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@JsonClass(generateAdapter = true)
data class BackupCamo(
    val id: String,
    val weaponId: String,
    val name: String,
    val tierKey: String = "custom",
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@JsonClass(generateAdapter = true)
data class BackupChallenge(
    val id: String,
    val weaponId: String,
    val camoId: String,
    val categoryId: String? = null,
    val name: String,
    val goal: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)

@JsonClass(generateAdapter = true)
data class BackupSettings(
    val themeMode: String = "DARK",
    val hapticsEnabled: Boolean = true,
)

sealed class BackupValidationResult {
    data class Valid(val backup: GrindBackup) : BackupValidationResult()
    data class Invalid(val errorMessage: String) : BackupValidationResult()
}
