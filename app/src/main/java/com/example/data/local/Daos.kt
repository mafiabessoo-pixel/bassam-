package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, name COLLATE NOCASE ASC")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface WeaponDao {
    @Query("SELECT * FROM weapons ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WeaponEntity>>

    @Query("SELECT * FROM weapons ORDER BY updatedAt DESC")
    suspend fun getAll(): List<WeaponEntity>

    @Query("SELECT * FROM weapons WHERE id = :id")
    suspend fun getById(id: String): WeaponEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(weapon: WeaponEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(weapons: List<WeaponEntity>)

    @Update
    suspend fun update(weapon: WeaponEntity)

    @Query("UPDATE weapons SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long)

    @Query("UPDATE weapons SET categoryId = :categoryId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setCategory(id: String, categoryId: String?, updatedAt: Long)

    @Query("UPDATE weapons SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun touch(id: String, updatedAt: Long)

    @Query("DELETE FROM weapons WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM weapons")
    suspend fun deleteAll()
}

@Dao
interface CamoDao {
    @Query("SELECT * FROM camos ORDER BY sortOrder ASC, createdAt ASC")
    fun observeAll(): Flow<List<CamoEntity>>

    @Query("SELECT * FROM camos ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getAll(): List<CamoEntity>

    @Query("SELECT * FROM camos WHERE weaponId = :weaponId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getByWeaponId(weaponId: String): List<CamoEntity>

    @Query("SELECT * FROM camos WHERE id = :id")
    suspend fun getById(id: String): CamoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(camo: CamoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(camos: List<CamoEntity>)

    @Update
    suspend fun update(camo: CamoEntity)

    @Query("UPDATE camos SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long)

    @Query("DELETE FROM camos WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM camos")
    suspend fun deleteAll()
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<ChallengeEntity>>

    @Query("SELECT * FROM challenges ORDER BY createdAt ASC")
    suspend fun getAll(): List<ChallengeEntity>

    @Query("SELECT * FROM challenges WHERE id = :id")
    suspend fun getById(id: String): ChallengeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(challenge: ChallengeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(challenges: List<ChallengeEntity>)

    @Update
    suspend fun update(challenge: ChallengeEntity)

    @Query("UPDATE challenges SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long)

    @Query("UPDATE challenges SET categoryId = :categoryId, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setCategory(id: String, categoryId: String?, updatedAt: Long)

    @Query("DELETE FROM challenges WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM challenges")
    suspend fun deleteAll()
}
