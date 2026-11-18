package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.backup.BackupRepository
import com.example.data.backup.BackupValidationResult
import com.example.data.local.GrindDatabase
import com.example.data.local.PreferencesManager
import com.example.data.repository.GrindRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: GrindDatabase
    private lateinit var repo: GrindRepository
    private lateinit var prefs: PreferencesManager
    private lateinit var backupRepo: BackupRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, GrindDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = GrindRepository(db)
        prefs = PreferencesManager(context)
        backupRepo = BackupRepository(db, prefs)
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun `app name matches CODM Grind Tracker`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("CODM Grind Tracker", appName)
    }

    @Test
    fun `challenge progress clamps to goal and auto completes`() = runBlocking {
        val catId = repo.addCategory("Assault Rifles", "assault")
        val weaponId = repo.createWeapon("M4", catId, "assault", null, "", includeStandardCamos = false)
        val camoId = repo.createCamo(weaponId, "Jungle", "jungle")
        val challengeId = repo.createChallenge(weaponId, camoId, catId, "Headshots", goal = 100, initialProgress = 65)

        var snap = repo.snapshotFlow.first()
        var challenge = snap.challenges.first { it.id == challengeId }
        assertEquals(65, challenge.completionPercent)
        assertFalse(challenge.isCompleted)

        // Attempt to exceed goal -> clamped to 100 and auto-marked completed
        repo.adjustChallengeProgress(challengeId, 50)
        snap = repo.snapshotFlow.first()
        challenge = snap.challenges.first { it.id == challengeId }
        assertEquals(100, challenge.currentProgress)
        assertEquals(100, challenge.completionPercent)
        assertTrue(challenge.isCompleted)

        // Attempt to go below 0 -> clamped to 0 and not completed
        repo.setChallengeProgress(challengeId, -10)
        snap = repo.snapshotFlow.first()
        challenge = snap.challenges.first { it.id == challengeId }
        assertEquals(0, challenge.currentProgress)
        assertFalse(challenge.isCompleted)
    }

    @Test
    fun `favorites operate independently for weapons camos and challenges`() = runBlocking {
        val catId = repo.addCategory("SMGs", "smg")
        val weaponId = repo.createWeapon("CBR4", catId, "smg", null, "", includeStandardCamos = true)

        // Favorite ONLY the weapon
        repo.toggleWeaponFavorite(weaponId)
        var snap = repo.snapshotFlow.first()
        val weapon = snap.weapons.first { it.id == weaponId }
        assertTrue(weapon.isFavorite)
        // Camos & challenges must remain unfavorited!
        assertTrue(weapon.camos.all { !it.isFavorite })
        assertTrue(weapon.challenges.all { !it.isFavorite })

        // Favorite one camo and one challenge independently
        val camoId = weapon.camos.first().id
        val challengeId = weapon.challenges.last().id
        repo.toggleCamoFavorite(camoId)
        repo.toggleChallengeFavorite(challengeId)

        // Unfavorite the weapon -> must NOT delete weapon or affect camo/challenge favorites
        repo.toggleWeaponFavorite(weaponId)
        snap = repo.snapshotFlow.first()
        val updatedWeapon = snap.weapons.first { it.id == weaponId }
        assertFalse(updatedWeapon.isFavorite)
        assertTrue(updatedWeapon.camos.first { it.id == camoId }.isFavorite)
        assertTrue(updatedWeapon.challenges.first { it.id == challengeId }.isFavorite)
    }

    @Test
    fun `backup export validates and restores full state`() = runBlocking {
        val catId = repo.addCategory("Sniper Rifles", "sniper")
        val weaponId = repo.createWeapon("DL Q33", catId, "sniper", null, "Quickscope build", includeStandardCamos = false)
        val camoId = repo.createCamo(weaponId, "Diamond", "diamond")
        val chId = repo.createChallenge(weaponId, camoId, catId, "10+ Kills in Matches", goal = 120, initialProgress = 60)
        repo.toggleWeaponFavorite(weaponId)

        val json = backupRepo.createBackupJson()
        val validation = backupRepo.validateBackupJson(json)
        assertTrue(validation is BackupValidationResult.Valid)

        // Wipe DB then restore
        repo.resetAllData(restoreDefaultCategories = false)
        assertEquals(0, repo.snapshotFlow.first().weapons.size)

        val validBackup = (validation as BackupValidationResult.Valid).backup
        val restoreResult = backupRepo.restoreBackup(validBackup)
        assertTrue(restoreResult.isSuccess)

        val restoredSnap = repo.snapshotFlow.first()
        assertEquals(1, restoredSnap.weapons.size)
        assertEquals("DL Q33", restoredSnap.weapons.first().name)
        assertTrue(restoredSnap.weapons.first().isFavorite)
        assertEquals(60, restoredSnap.challenges.first { it.id == chId }.currentProgress)
        assertEquals(50, restoredSnap.challenges.first { it.id == chId }.completionPercent)
    }

    @Test
    fun `corrupted backup JSON is rejected gracefully`() {
        val badJson = """{ "app": "Other App", "schemaVersion": 99 }"""
        val result = backupRepo.validateBackupJson(badJson)
        assertTrue(result is BackupValidationResult.Invalid)

        val malformed = """{ not valid json"""
        assertTrue(backupRepo.validateBackupJson(malformed) is BackupValidationResult.Invalid)
    }
}
