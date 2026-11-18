package com.example

import com.example.data.local.CamoEntity
import com.example.data.local.CategoryEntity
import com.example.data.local.ChallengeEntity
import com.example.data.local.WeaponEntity
import com.example.domain.CamoWithChallenges
import com.example.domain.ChallengeItem
import com.example.domain.ChallengeStatus
import com.example.domain.WeaponWithDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun `challenge percentage and status calculate accurately`() {
        val item = ChallengeItem(
            entity = ChallengeEntity(
                id = "c1",
                weaponId = "w1",
                camoId = "cam1",
                name = "Long Shots",
                goal = 100,
                currentProgress = 65,
                isCompleted = false,
            ),
            weaponName = "AK-47",
            camoName = "Splinter",
            categoryName = "Assault Rifles",
        )

        assertEquals(65, item.completionPercent)
        assertEquals(0.65f, item.completionFraction, 0.001f)
        assertEquals(ChallengeStatus.IN_PROGRESS, item.status)
        assertFalse(item.isCompleted)
    }

    @Test
    fun `weapon and camo percentages aggregate child challenges`() {
        val ch1 = ChallengeItem(
            entity = ChallengeEntity("c1", "w1", "cam1", name = "Kills", goal = 100, currentProgress = 100, isCompleted = true),
            weaponName = "Krig 6",
            camoName = "Sand",
            categoryName = "Assault Rifles",
        )
        val ch2 = ChallengeItem(
            entity = ChallengeEntity("c2", "w1", "cam2", name = "Headshots", goal = 100, currentProgress = 50, isCompleted = false),
            weaponName = "Krig 6",
            camoName = "Jungle",
            categoryName = "Assault Rifles",
        )
        val camo1 = CamoWithChallenges(CamoEntity("cam1", "w1", "Sand"), "Krig 6", listOf(ch1))
        val camo2 = CamoWithChallenges(CamoEntity("cam2", "w1", "Jungle"), "Krig 6", listOf(ch2))

        val weapon = WeaponWithDetails(
            entity = WeaponEntity("w1", "Krig 6", "cat_ar"),
            category = CategoryEntity("cat_ar", "Assault Rifles"),
            camos = listOf(camo1, camo2),
            challenges = listOf(ch1, ch2),
        )

        assertEquals(100, camo1.completionPercent)
        assertEquals(50, camo2.completionPercent)
        assertEquals(75, weapon.completionPercent)
        assertEquals(1, weapon.completedChallenges)
        assertEquals(1, weapon.remainingChallenges)
        assertEquals(ChallengeStatus.IN_PROGRESS, weapon.status)
        assertTrue(weapon.camos.first().status == ChallengeStatus.COMPLETED)
    }
}
