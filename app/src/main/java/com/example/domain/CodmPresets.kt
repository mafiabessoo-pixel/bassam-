package com.example.domain

data class CamoChallengeTemplate(
    val camoName: String,
    val tierKey: String,
    val challengeName: String,
    val goal: Int,
)

data class WeaponIconOption(
    val key: String,
    val label: String,
)

object CodmPresets {
    val weaponIcons = listOf(
        WeaponIconOption("assault", "Assault Rifle"),
        WeaponIconOption("smg", "SMG"),
        WeaponIconOption("sniper", "Sniper"),
        WeaponIconOption("lmg", "LMG"),
        WeaponIconOption("shotgun", "Shotgun"),
        WeaponIconOption("marksman", "Marksman"),
        WeaponIconOption("pistol", "Pistol"),
        WeaponIconOption("launcher", "Launcher"),
        WeaponIconOption("melee", "Melee"),
        WeaponIconOption("custom", "Tactical"),
    )

    val camoTiers = listOf(
        "sand" to "Sand",
        "dragon" to "Dragon",
        "splinter" to "Splinter",
        "tiger" to "Tiger",
        "jungle" to "Jungle",
        "reptile" to "Reptile",
        "gold" to "Gold",
        "platinum" to "Platinum",
        "damascus" to "Damascus",
        "diamond" to "Diamond",
        "custom" to "Custom",
    )

    fun standardTemplatesForCategory(categoryIconKey: String?): List<CamoChallengeTemplate> {
        return when (categoryIconKey) {
            "sniper", "marksman" -> listOf(
                CamoChallengeTemplate("Sand", "sand", "Total Kills", 350),
                CamoChallengeTemplate("Dragon", "dragon", "Crouch Kills", 45),
                CamoChallengeTemplate("Splinter", "splinter", "Long Shot Kills", 45),
                CamoChallengeTemplate("Tiger", "tiger", "Kills with Full Attachments", 125),
                CamoChallengeTemplate("Jungle", "jungle", "Headshot Kills", 45),
                CamoChallengeTemplate("Reptile", "reptile", "Kills with No Attachments", 60),
                CamoChallengeTemplate("Diamond", "diamond", "10+ Kills in Completed Matches", 120),
            )
            "shotgun" -> listOf(
                CamoChallengeTemplate("Sand", "sand", "Total Kills", 300),
                CamoChallengeTemplate("Dragon", "dragon", "Hip Fire Kills", 45),
                CamoChallengeTemplate("Splinter", "splinter", "Long Shot Kills", 30),
                CamoChallengeTemplate("Tiger", "tiger", "Kills with Full Attachments", 200),
                CamoChallengeTemplate("Jungle", "jungle", "Headshot Kills", 40),
                CamoChallengeTemplate("Reptile", "reptile", "Kills with No Attachments", 60),
                CamoChallengeTemplate("Diamond", "diamond", "10+ Kills in Completed Matches", 120),
            )
            "pistol" -> listOf(
                CamoChallengeTemplate("Sand", "sand", "Total Kills", 200),
                CamoChallengeTemplate("Dragon", "dragon", "Hip Fire Kills", 25),
                CamoChallengeTemplate("Splinter", "splinter", "Long Shot Kills", 35),
                CamoChallengeTemplate("Tiger", "tiger", "Kills with Full Attachments", 100),
                CamoChallengeTemplate("Jungle", "jungle", "Headshot Kills", 40),
                CamoChallengeTemplate("Reptile", "reptile", "Kills with No Attachments", 50),
                CamoChallengeTemplate("Diamond", "diamond", "10+ Kills in Completed Matches", 80),
            )
            "launcher" -> listOf(
                CamoChallengeTemplate("Sand", "sand", "Scorestreaks Destroyed", 50),
                CamoChallengeTemplate("Dragon", "dragon", "UAVs Destroyed", 30),
                CamoChallengeTemplate("Splinter", "splinter", "Double Kills", 10),
                CamoChallengeTemplate("Tiger", "tiger", "Ground Scorestreaks Destroyed", 15),
                CamoChallengeTemplate("Jungle", "jungle", "Transform Shields / Sentries", 10),
                CamoChallengeTemplate("Diamond", "diamond", "UAVs Destroyed (100 Matches)", 100),
            )
            "melee" -> listOf(
                CamoChallengeTemplate("Sand", "sand", "Total Kills", 100),
                CamoChallengeTemplate("Dragon", "dragon", "Kills from Behind", 20),
                CamoChallengeTemplate("Splinter", "splinter", "Crouch / Slide Kills", 30),
                CamoChallengeTemplate("Tiger", "tiger", "2 Kills Without Dying", 15),
                CamoChallengeTemplate("Jungle", "jungle", "3 Kills Without Dying", 10),
                CamoChallengeTemplate("Diamond", "diamond", "500 Total Melee Kills", 500),
            )
            else -> listOf( // Assault, SMG, LMG, Custom
                CamoChallengeTemplate("Sand", "sand", "Total Kills", 600),
                CamoChallengeTemplate("Dragon", "dragon", "Hip Fire Kills", 50),
                CamoChallengeTemplate("Splinter", "splinter", "Long Shot Kills", 80),
                CamoChallengeTemplate("Tiger", "tiger", "Kills with Full Attachments", 125),
                CamoChallengeTemplate("Jungle", "jungle", "Headshot Kills", 100),
                CamoChallengeTemplate("Reptile", "reptile", "Kills with No Attachments", 80),
                CamoChallengeTemplate("Diamond", "diamond", "10+ Kills in Completed Matches", 150),
            )
        }
    }
}
