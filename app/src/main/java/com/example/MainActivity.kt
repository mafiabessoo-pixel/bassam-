package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.GpsFixed
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.domain.CamoWithChallenges
import com.example.domain.CategorySummary
import com.example.domain.ChallengeItem
import com.example.domain.FavoriteItem
import com.example.domain.SearchResultItem
import com.example.domain.WeaponWithDetails
import com.example.ui.components.CamoFormDialog
import com.example.ui.components.CategoryFormDialog
import com.example.ui.components.ChallengeFormDialog
import com.example.ui.components.ConfirmDangerDialog
import com.example.ui.components.ProgressEditDialog
import com.example.ui.components.WeaponFormDialog
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.screens.WeaponDetailScreen
import com.example.ui.screens.WeaponsScreen
import com.example.ui.theme.CODMGrindTheme
import com.example.ui.theme.LocalTacticalColors
import com.example.ui.viewmodel.GrindViewModel

object Routes {
    const val DASHBOARD = "dashboard"
    const val WEAPONS = "weapons"
    const val FAVORITES = "favorites"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val CATEGORIES = "categories"
    const val WEAPON_DETAIL = "weapon_detail/{weaponId}"

    fun weaponDetail(weaponId: String): String = "weapon_detail/$weaponId"
}

private data class MainTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

private val mainTabs = listOf(
    MainTab(Routes.DASHBOARD, "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "tab_dashboard"),
    MainTab(Routes.WEAPONS, "Weapons", Icons.Filled.GpsFixed, Icons.Outlined.GpsFixed, "tab_weapons"),
    MainTab(Routes.FAVORITES, "Favorites", Icons.Filled.Star, Icons.Outlined.StarOutline, "tab_favorites"),
    MainTab(Routes.STATISTICS, "Stats", Icons.Filled.BarChart, Icons.Outlined.BarChart, "tab_statistics"),
    MainTab(Routes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings, "tab_settings"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CodmGrindApp()
        }
    }
}

@Composable
fun CodmGrindApp(
    viewModel: GrindViewModel = viewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val snapshot by viewModel.snapshot.collectAsStateWithLifecycle()
    val filteredWeapons by viewModel.filteredWeapons.collectAsStateWithLifecycle()
    val favorites by viewModel.allFavorites.collectAsStateWithLifecycle()
    val searchResults by viewModel.globalSearchResults.collectAsStateWithLifecycle()
    val importState by viewModel.importState.collectAsStateWithLifecycle()

    val weaponSearch by viewModel.weaponSearch.collectAsStateWithLifecycle()
    val weaponCatFilter by viewModel.weaponCategoryFilter.collectAsStateWithLifecycle()
    val weaponStatusFilter by viewModel.weaponStatusFilter.collectAsStateWithLifecycle()
    val weaponSort by viewModel.weaponSort.collectAsStateWithLifecycle()

    val favSearch by viewModel.favSearch.collectAsStateWithLifecycle()
    val favTypeFilter by viewModel.favTypeFilter.collectAsStateWithLifecycle()
    val favSort by viewModel.favSort.collectAsStateWithLifecycle()

    val globalQuery by viewModel.globalQuery.collectAsStateWithLifecycle()
    val globalStatusFilter by viewModel.globalStatusFilter.collectAsStateWithLifecycle()
    val globalCategoryFilter by viewModel.globalCategoryFilter.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        viewModel.uiMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Shared Modal Dialog States
    var showWeaponForm by remember { mutableStateOf(false) }
    var editingWeapon by remember { mutableStateOf<WeaponWithDetails?>(null) }
    var deletingWeapon by remember { mutableStateOf<WeaponWithDetails?>(null) }

    var camoFormWeaponId by remember { mutableStateOf<String?>(null) }
    var editingCamo by remember { mutableStateOf<CamoWithChallenges?>(null) }
    var deletingCamo by remember { mutableStateOf<CamoWithChallenges?>(null) }

    var challengeFormTarget by remember { mutableStateOf<Pair<String, String?>?>(null) } // weaponId to defaultCamoId
    var editingChallenge by remember { mutableStateOf<ChallengeItem?>(null) }
    var deletingChallenge by remember { mutableStateOf<ChallengeItem?>(null) }
    var manualProgressChallenge by remember { mutableStateOf<ChallengeItem?>(null) }

    var showCategoryForm by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategorySummary?>(null) }
    var deletingCategory by remember { mutableStateOf<CategorySummary?>(null) }

    CODMGrindTheme(themeMode = settings.themeMode) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val currentRoute = currentDestination?.route
        val isTopLevelTab = mainTabs.any { it.route == currentRoute }

        val navigateToTab: (String) -> Unit = { route ->
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        val toggleFavoriteItem: (FavoriteItem) -> Unit = { item ->
            when (item) {
                is FavoriteItem.WeaponFav -> viewModel.toggleWeaponFavorite(item.id)
                is FavoriteItem.CamoFav -> viewModel.toggleCamoFavorite(item.id)
                is FavoriteItem.ChallengeFav -> viewModel.toggleChallengeFavorite(item.id)
                is FavoriteItem.CategoryFav -> viewModel.toggleCategoryFavorite(item.id)
            }
        }

        val toggleSearchFavorite: (SearchResultItem) -> Unit = { item ->
            when (item) {
                is SearchResultItem.WeaponResult -> viewModel.toggleWeaponFavorite(item.id)
                is SearchResultItem.CamoResult -> viewModel.toggleCamoFavorite(item.id)
                is SearchResultItem.ChallengeResult -> viewModel.toggleChallengeFavorite(item.id)
                is SearchResultItem.CategoryResult -> viewModel.toggleCategoryFavorite(item.id)
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavRail = maxWidth >= 600.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (isTopLevelTab && !useNavRail) {
                        val tactical = LocalTacticalColors.current
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp,
                        ) {
                            mainTabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    modifier = Modifier.testTag(tab.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label,
                                        )
                                    },
                                    label = { Text(tab.label, maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = if (tab.route == Routes.FAVORITES) tactical.gold else MaterialTheme.colorScheme.primary,
                                        selectedTextColor = if (tab.route == Routes.FAVORITES) tactical.gold else MaterialTheme.colorScheme.onSurface,
                                        indicatorColor = if (tab.route == Routes.FAVORITES) tactical.goldContainer else MaterialTheme.colorScheme.primaryContainer,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (isTopLevelTab && useNavRail) {
                        val tactical = LocalTacticalColors.current
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ) {
                            mainTabs.forEach { tab ->
                                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    modifier = Modifier.testTag(tab.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.label,
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = if (tab.route == Routes.FAVORITES) tactical.gold else MaterialTheme.colorScheme.primary,
                                        indicatorColor = if (tab.route == Routes.FAVORITES) tactical.goldContainer else MaterialTheme.colorScheme.primaryContainer,
                                    ),
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = Routes.DASHBOARD,
                        ) {
                            composable(Routes.DASHBOARD) {
                                DashboardScreen(
                                    snapshot = snapshot,
                                    favorites = favorites,
                                    hapticsEnabled = settings.hapticsEnabled,
                                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                                    onAddWeaponClick = {
                                        editingWeapon = null
                                        showWeaponForm = true
                                    },
                                    onOpenWeaponDetail = { weaponId ->
                                        navController.navigate(Routes.weaponDetail(weaponId))
                                    },
                                    onOpenCategoryWeapons = { categoryId ->
                                        viewModel.setWeaponCategoryFilter(categoryId)
                                        navigateToTab(Routes.WEAPONS)
                                    },
                                    onOpenFavoritesTab = { navigateToTab(Routes.FAVORITES) },
                                    onOpenWeaponsTab = { navigateToTab(Routes.WEAPONS) },
                                    onAdjustChallengeProgress = viewModel::adjustChallengeProgress,
                                    onToggleWeaponFavorite = viewModel::toggleWeaponFavorite,
                                    onToggleFavoriteItem = toggleFavoriteItem,
                                )
                            }

                            composable(Routes.WEAPONS) {
                                WeaponsScreen(
                                    weapons = filteredWeapons,
                                    categories = snapshot.categories,
                                    searchQuery = weaponSearch,
                                    selectedCategoryFilter = weaponCatFilter,
                                    selectedStatusFilter = weaponStatusFilter,
                                    selectedSort = weaponSort,
                                    hapticsEnabled = settings.hapticsEnabled,
                                    onSearchChange = viewModel::setWeaponSearch,
                                    onCategoryFilterChange = viewModel::setWeaponCategoryFilter,
                                    onStatusFilterChange = viewModel::setWeaponStatusFilter,
                                    onSortChange = viewModel::setWeaponSort,
                                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                                    onAddWeapon = {
                                        editingWeapon = null
                                        showWeaponForm = true
                                    },
                                    onEditWeapon = { weapon ->
                                        editingWeapon = weapon
                                        showWeaponForm = true
                                    },
                                    onDeleteWeapon = { weapon -> deletingWeapon = weapon },
                                    onOpenWeaponDetail = { weaponId ->
                                        navController.navigate(Routes.weaponDetail(weaponId))
                                    },
                                    onToggleFavorite = viewModel::toggleWeaponFavorite,
                                )
                            }

                            composable(Routes.FAVORITES) {
                                FavoritesScreen(
                                    favorites = favorites,
                                    searchQuery = favSearch,
                                    typeFilter = favTypeFilter,
                                    sortOption = favSort,
                                    hapticsEnabled = settings.hapticsEnabled,
                                    onSearchChange = viewModel::setFavSearch,
                                    onTypeFilterChange = viewModel::setFavTypeFilter,
                                    onSortChange = viewModel::setFavSort,
                                    onToggleFavoriteItem = toggleFavoriteItem,
                                    onOpenWeaponDetail = { weaponId ->
                                        navController.navigate(Routes.weaponDetail(weaponId))
                                    },
                                    onOpenCategoryWeapons = { categoryId ->
                                        viewModel.setWeaponCategoryFilter(categoryId)
                                        navigateToTab(Routes.WEAPONS)
                                    },
                                )
                            }

                            composable(Routes.STATISTICS) {
                                StatisticsScreen(
                                    snapshot = snapshot,
                                    onOpenWeaponDetail = { weaponId ->
                                        navController.navigate(Routes.weaponDetail(weaponId))
                                    },
                                )
                            }

                            composable(Routes.SETTINGS) {
                                SettingsScreen(
                                    settings = settings,
                                    snapshot = snapshot,
                                    importState = importState,
                                    onThemeModeChange = viewModel::setThemeMode,
                                    onHapticsChange = viewModel::setHapticsEnabled,
                                    onOpenCategories = { navController.navigate(Routes.CATEGORIES) },
                                    onExportBackupUri = viewModel::exportBackupToUri,
                                    onImportBackupUri = viewModel::prepareImportFromUri,
                                    onConfirmRestoreBackup = viewModel::confirmRestoreBackup,
                                    onDismissImportState = viewModel::dismissImportState,
                                    onResetAllData = { viewModel.resetAllData(restoreDefaultCategories = true) },
                                )
                            }

                            composable(Routes.SEARCH) {
                                SearchScreen(
                                    query = globalQuery,
                                    statusFilter = globalStatusFilter,
                                    categoryFilter = globalCategoryFilter,
                                    categories = snapshot.categories,
                                    results = searchResults,
                                    hapticsEnabled = settings.hapticsEnabled,
                                    onBack = { navController.popBackStack() },
                                    onQueryChange = viewModel::setGlobalQuery,
                                    onStatusFilterChange = viewModel::setGlobalStatusFilter,
                                    onCategoryFilterChange = viewModel::setGlobalCategoryFilter,
                                    onToggleFavorite = toggleSearchFavorite,
                                    onOpenWeaponDetail = { weaponId ->
                                        navController.navigate(Routes.weaponDetail(weaponId))
                                    },
                                    onOpenCategoryWeapons = { categoryId ->
                                        viewModel.setWeaponCategoryFilter(categoryId)
                                        navigateToTab(Routes.WEAPONS)
                                    },
                                )
                            }

                            composable(Routes.CATEGORIES) {
                                CategoriesScreen(
                                    snapshot = snapshot,
                                    hapticsEnabled = settings.hapticsEnabled,
                                    onBack = { navController.popBackStack() },
                                    onAddCategory = {
                                        editingCategory = null
                                        showCategoryForm = true
                                    },
                                    onRenameCategory = { cat ->
                                        editingCategory = cat
                                        showCategoryForm = true
                                    },
                                    onDeleteCategory = { cat -> deletingCategory = cat },
                                    onToggleCategoryFavorite = viewModel::toggleCategoryFavorite,
                                    onMoveWeaponToCategory = viewModel::moveWeaponToCategory,
                                    onMoveChallengeToCategory = viewModel::moveChallengeToCategory,
                                    onOpenWeaponDetail = { weaponId ->
                                        navController.navigate(Routes.weaponDetail(weaponId))
                                    },
                                )
                            }

                            composable(
                                route = Routes.WEAPON_DETAIL,
                                arguments = listOf(navArgument("weaponId") { type = NavType.StringType }),
                            ) { backStackEntry ->
                                val weaponId = backStackEntry.arguments?.getString("weaponId") ?: ""
                                val weapon = snapshot.weapons.firstOrNull { it.id == weaponId }
                                WeaponDetailScreen(
                                    weapon = weapon,
                                    categories = snapshot.categories,
                                    hapticsEnabled = settings.hapticsEnabled,
                                    onBack = { navController.popBackStack() },
                                    onEditWeapon = { w ->
                                        editingWeapon = w
                                        showWeaponForm = true
                                    },
                                    onDeleteWeapon = { w -> deletingWeapon = w },
                                    onToggleWeaponFavorite = viewModel::toggleWeaponFavorite,
                                    onMoveWeaponCategory = viewModel::moveWeaponToCategory,
                                    onAddCamo = { wId ->
                                        editingCamo = null
                                        camoFormWeaponId = wId
                                    },
                                    onEditCamo = { camo ->
                                        camoFormWeaponId = camo.entity.weaponId
                                        editingCamo = camo
                                    },
                                    onDeleteCamo = { camo -> deletingCamo = camo },
                                    onToggleCamoFavorite = viewModel::toggleCamoFavorite,
                                    onAddChallenge = { wId, cId ->
                                        editingChallenge = null
                                        challengeFormTarget = wId to cId
                                    },
                                    onEditChallenge = { ch ->
                                        editingChallenge = ch
                                        challengeFormTarget = ch.entity.weaponId to ch.entity.camoId
                                    },
                                    onDeleteChallenge = { ch -> deletingChallenge = ch },
                                    onToggleChallengeFavorite = viewModel::toggleChallengeFavorite,
                                    onAdjustProgress = viewModel::adjustChallengeProgress,
                                    onEditManualProgress = { ch -> manualProgressChallenge = ch },
                                    onMarkCompleted = viewModel::markChallengeCompleted,
                                    onResetChallenge = viewModel::resetChallengeProgress,
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Modal Dialogs ---
        if (showWeaponForm) {
            WeaponFormDialog(
                categories = snapshot.categories,
                existingWeapon = editingWeapon,
                onDismiss = {
                    showWeaponForm = false
                    editingWeapon = null
                },
                onSave = { name, catId, iconKey, pickedUri, clearImg, notes, includeCamos ->
                    val existing = editingWeapon
                    if (existing == null) {
                        viewModel.addWeapon(
                            name = name,
                            categoryId = catId,
                            iconKey = iconKey,
                            pickedImageUri = pickedUri,
                            notes = notes,
                            includeStandardCamos = includeCamos,
                        )
                    } else {
                        viewModel.updateWeapon(
                            id = existing.id,
                            name = name,
                            categoryId = catId,
                            iconKey = iconKey,
                            existingImageUri = existing.entity.imageUri,
                            newPickedImageUri = pickedUri,
                            clearImage = clearImg,
                            notes = notes,
                        )
                    }
                    showWeaponForm = false
                    editingWeapon = null
                },
            )
        }

        deletingWeapon?.let { weapon ->
            ConfirmDangerDialog(
                title = "Delete ${weapon.name}?",
                message = "This will permanently delete '${weapon.name}' along with its ${weapon.camos.size} camos and ${weapon.totalChallenges} challenges.",
                onConfirm = {
                    viewModel.deleteWeapon(weapon.id)
                    deletingWeapon = null
                },
                onDismiss = { deletingWeapon = null },
            )
        }

        camoFormWeaponId?.let { weaponId ->
            CamoFormDialog(
                existingCamo = editingCamo,
                onDismiss = {
                    camoFormWeaponId = null
                    editingCamo = null
                },
                onSave = { name, tierKey ->
                    val existing = editingCamo
                    if (existing == null) {
                        viewModel.addCamo(weaponId, name, tierKey)
                    } else {
                        viewModel.updateCamo(existing.id, name, tierKey)
                    }
                    camoFormWeaponId = null
                    editingCamo = null
                },
            )
        }

        deletingCamo?.let { camo ->
            ConfirmDangerDialog(
                title = "Delete Camo '${camo.name}'?",
                message = "All ${camo.totalChallenges} challenges inside '${camo.name}' will also be deleted.",
                onConfirm = {
                    viewModel.deleteCamo(camo.id)
                    deletingCamo = null
                },
                onDismiss = { deletingCamo = null },
            )
        }

        challengeFormTarget?.let { (weaponId, defaultCamoId) ->
            val weaponCamos = snapshot.weapons.firstOrNull { it.id == weaponId }?.camos.orEmpty()
            val weaponCategoryId = snapshot.weapons.firstOrNull { it.id == weaponId }?.entity?.categoryId
            ChallengeFormDialog(
                camos = weaponCamos,
                categories = snapshot.categories,
                defaultCamoId = defaultCamoId,
                defaultCategoryId = weaponCategoryId,
                existingChallenge = editingChallenge,
                onDismiss = {
                    challengeFormTarget = null
                    editingChallenge = null
                },
                onSave = { name, goal, initialProgress, camoId, catId ->
                    val existing = editingChallenge
                    if (existing == null) {
                        viewModel.addChallenge(
                            weaponId = weaponId,
                            camoId = camoId,
                            categoryId = catId,
                            name = name,
                            goal = goal,
                            initialProgress = initialProgress,
                        )
                    } else {
                        viewModel.updateChallengeDetails(
                            challengeId = existing.id,
                            name = name,
                            goal = goal,
                            camoId = camoId ?: existing.entity.camoId,
                            categoryId = catId,
                        )
                    }
                    challengeFormTarget = null
                    editingChallenge = null
                },
            )
        }

        deletingChallenge?.let { ch ->
            ConfirmDangerDialog(
                title = "Delete Challenge?",
                message = "Remove '${ch.name}' (${ch.currentProgress}/${ch.goal})?",
                onConfirm = {
                    viewModel.deleteChallenge(ch.id)
                    deletingChallenge = null
                },
                onDismiss = { deletingChallenge = null },
            )
        }

        manualProgressChallenge?.let { ch ->
            ProgressEditDialog(
                challenge = ch,
                onDismiss = { manualProgressChallenge = null },
                onSave = { value ->
                    viewModel.setChallengeProgress(ch.id, value)
                    manualProgressChallenge = null
                },
            )
        }

        if (showCategoryForm) {
            CategoryFormDialog(
                existingCategory = editingCategory,
                onDismiss = {
                    showCategoryForm = false
                    editingCategory = null
                },
                onSave = { name, iconKey ->
                    val existing = editingCategory
                    if (existing == null) {
                        viewModel.addCategory(name, iconKey)
                    } else {
                        viewModel.renameCategory(existing.id, name)
                    }
                    showCategoryForm = false
                    editingCategory = null
                },
            )
        }

        deletingCategory?.let { cat ->
            ConfirmDangerDialog(
                title = "Delete Category '${cat.name}'?",
                message = "Weapons and challenges in '${cat.name}' will be preserved and moved to Uncategorized.",
                onConfirm = {
                    viewModel.deleteCategory(cat.id)
                    deletingCategory = null
                },
                onDismiss = { deletingCategory = null },
            )
        }
    }
}
