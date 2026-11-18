package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String = "custom",
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "weapons",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index(value = ["categoryId"])],
)
data class WeaponEntity(
    @PrimaryKey val id: String,
    val name: String,
    val categoryId: String? = null,
    val iconKey: String = "assault",
    val imageUri: String? = null,
    val notes: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "camos",
    foreignKeys = [
        ForeignKey(
            entity = WeaponEntity::class,
            parentColumns = ["id"],
            childColumns = ["weaponId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["weaponId"])],
)
data class CamoEntity(
    @PrimaryKey val id: String,
    val weaponId: String,
    val name: String,
    val tierKey: String = "custom",
    val isFavorite: Boolean = false,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "challenges",
    foreignKeys = [
        ForeignKey(
            entity = WeaponEntity::class,
            parentColumns = ["id"],
            childColumns = ["weaponId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CamoEntity::class,
            parentColumns = ["id"],
            childColumns = ["camoId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["weaponId"]),
        Index(value = ["camoId"]),
        Index(value = ["categoryId"]),
    ],
)
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val weaponId: String,
    val camoId: String,
    val categoryId: String? = null,
    val name: String,
    val goal: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
