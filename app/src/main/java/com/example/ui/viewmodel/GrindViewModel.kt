package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupRepository
import com.example.data.backup.BackupValidationResult
import com.example.data.backup.GrindBackup
import com.example.data.local.AppSettings
import com.example.data.local.GrindDatabase
import com.example.data.local.PreferencesManager
import com.example.data.local.ThemeMode
import com.example.data.repository.GrindRepository
import com.example.data.repository.GrindSnapshot
import com.example.domain.ChallengeStatus
import com.example.domain.FavoriteFilterType
import com.example.domain.FavoriteItem
import com.example.domain.FavoriteSortOption
import com.example.domain.SearchFilterStatus
import com.example.domain.SearchResultItem
import com.example.domain.WeaponSortOption
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class ImportBackupUiState {
    data object Idle : ImportBackupUiState()
    data class Preview(val backup: GrindBackup) : ImportBackupUiState()
    data class Error(val message: String) : ImportBackupUiState()
}

class GrindViewModel(application: Application) : AndroidViewModel(application) {
    private val database = GrindDatabase.getInstance(application)
    private val preferencesManager = PreferencesManager(application)
    val repository = GrindRepository(database)
    private val backupRepository = BackupRepository(database, preferencesManager)

    val snapshot: StateFlow<GrindSnapshot> = repository.snapshotFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GrindSnapshot(),
        )

    val settings: StateFlow<AppSettings> = preferencesManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings(),
        )

    private val _uiMessage = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiMessage: SharedFlow<String> = _uiMessage.asSharedFlow()

    // Weapons screen filters
    private val _weaponSearch = MutableStateFlow("")
    val weaponSearch: StateFlow<String> = _weaponSearch.asStateFlow()

    private val _weaponCategoryFilter = MutableStateFlow<String?>(null) // null = All, "UNCATEGORIZED" = null categoryId
    val weaponCategoryFilter: StateFlow<String?> = _weaponCategoryFilter.asStateFlow()

    private val _weaponStatusFilter = MutableStateFlow(SearchFilterStatus.ALL)
    val weaponStatusFilter: StateFlow<SearchFilterStatus> = _weaponStatusFilter.asStateFlow()

    private val _weaponSort = MutableStateFlow(WeaponSortOption.RECENTLY_UPDATED)
    val weaponSort: StateFlow<WeaponSortOption> = _weaponSort.asStateFlow()

    // Favorites screen filters
    private val _favSearch = MutableStateFlow("")
    val favSearch: StateFlow<String> = _favSearch.asStateFlow()

    private val _favTypeFilter = MutableStateFlow(FavoriteFilterType.ALL)
    val favTypeFilter: StateFlow<FavoriteFilterType> = _favTypeFilter.asStateFlow()

    private val _favSort = MutableStateFlow(FavoriteSortOption.RECENTLY_UPDATED)
    val favSort: StateFlow<FavoriteSortOption> = _favSort.asStateFlow()

    // Global Search filters
    private val _globalQuery = MutableStateFlow("")
    val globalQuery: StateFlow<String> = _globalQuery.asStateFlow()

    private val _globalStatusFilter = MutableStateFlow(SearchFilterStatus.ALL)
    val globalStatusFilter: StateFlow<SearchFilterStatus> = _globalStatusFilter.asStateFlow()

    private val _globalCategoryFilter = MutableStateFlow<String?>(null)
    val globalCategoryFilter: StateFlow<String?> = _globalCategoryFilter.asStateFlow()

    // Import Backup preview state
    private val _importState = MutableStateFlow<ImportBackupUiState>(ImportBackupUiState.Idle)
    val importState: StateFlow<ImportBackupUiState> = _importState.asStateFlow()

    val filteredWeapons = combine(
        snapshot,
        _weaponSearch,
        _weaponCategoryFilter,
        _weaponStatusFilter,
        _weaponSort,
    ) { snap, query, catFilter, statusFilter, sort ->
        val q = query.trim().lowercase()
        snap.weapons
            .filter { weapon ->
                val matchesQuery = q.isEmpty() ||
                    weapon.name.lowercase().contains(q) ||
                    weapon.categoryName.lowercase().contains(q) ||
                    weapon.entity.notes.lowercase().contains(q)
                val matchesCat = when (catFilter) {
                    null -> true
                    "UNCATEGORIZED" -> weapon.entity.categoryId == null
                    else -> weapon.entity.categoryId == catFilter
                }
                val matchesStatus = when (statusFilter) {
                    SearchFilterStatus.ALL -> true
                    SearchFilterStatus.COMPLETED -> weapon.status == ChallengeStatus.COMPLETED
                    SearchFilterStatus.IN_PROGRESS -> weapon.status == ChallengeStatus.IN_PROGRESS
                    SearchFilterStatus.NOT_STARTED -> weapon.status == ChallengeStatus.NOT_STARTED
                    SearchFilterStatus.FAVORITES -> weapon.isFavorite
                }
                matchesQuery && matchesCat && matchesStatus
            }
            .let { list ->
                when (sort) {
                    WeaponSortOption.RECENTLY_UPDATED -> list.sortedByDescending { it.entity.updatedAt }
                    WeaponSortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
                    WeaponSortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
                    WeaponSortOption.PROGRESS_DESC -> list.sortedWith(
                        compareByDescending<com.example.domain.WeaponWithDetails> { it.completionPercent }
                            .thenBy { it.name.lowercase() }
                    )
                    WeaponSortOption.PROGRESS_ASC -> list.sortedWith(
                        compareBy<com.example.domain.WeaponWithDetails> { it.completionPercent }
                            .thenBy { it.name.lowercase() }
                    )
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allFavorites: StateFlow<List<FavoriteItem>> = combine(
        snapshot,
        _favSearch,
        _favTypeFilter,
        _favSort,
    ) { snap, query, typeFilter, sort ->
        val items = buildList {
            snap.weapons.filter { it.isFavorite }.forEach { add(FavoriteItem.WeaponFav(it)) }
            snap.camos.filter { it.isFavorite }.forEach { add(FavoriteItem.CamoFav(it)) }
            snap.challenges.filter { it.isFavorite }.forEach { add(FavoriteItem.ChallengeFav(it)) }
            snap.categories.filter { it.isFavorite }.forEach { add(FavoriteItem.CategoryFav(it)) }
        }
        val q = query.trim().lowercase()
        items
            .filter { item ->
                val matchesType = when (typeFilter) {
                    FavoriteFilterType.ALL -> true
                    FavoriteFilterType.WEAPONS -> item is FavoriteItem.WeaponFav
                    FavoriteFilterType.CAMOS -> item is FavoriteItem.CamoFav
                    FavoriteFilterType.CHALLENGES -> item is FavoriteItem.ChallengeFav
                    FavoriteFilterType.CATEGORIES -> item is FavoriteItem.CategoryFav
                }
                val matchesQuery = q.isEmpty() ||
                    item.title.lowercase().contains(q) ||
                    item.subtitle.lowercase().contains(q) ||
                    item.typeLabel.lowercase().contains(q)
                matchesType && matchesQuery
            }
            .let { filtered ->
                when (sort) {
                    FavoriteSortOption.RECENTLY_UPDATED -> filtered.sortedByDescending { it.updatedAt }
                    FavoriteSortOption.NAME_ASC -> filtered.sortedBy { it.title.lowercase() }
                    FavoriteSortOption.PROGRESS_DESC -> filtered.sortedByDescending { it.completionPercent }
                    FavoriteSortOption.TYPE -> filtered.sortedWith(
                        compareBy<FavoriteItem> { it.typeLabel }.thenBy { it.title.lowercase() }
                    )
                }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val globalSearchResults: StateFlow<List<SearchResultItem>> = combine(
        snapshot,
        _globalQuery,
        _globalStatusFilter,
        _globalCategoryFilter,
    ) { snap, query, statusFilter, categoryFilter ->
        val q = query.trim().lowercase()
        val weaponCatMap = snap.weapons.associate { it.id to it.entity.categoryId }
        val results = buildList {
            snap.weapons.forEach { add(SearchResultItem.WeaponResult(it)) }
            snap.camos.forEach { add(SearchResultItem.CamoResult(it, weaponCatMap[it.entity.weaponId])) }
            snap.challenges.forEach { add(SearchResultItem.ChallengeResult(it)) }
            snap.categories.forEach { add(SearchResultItem.CategoryResult(it)) }
        }
        results.filter { item ->
            val matchesQuery = q.isEmpty() ||
                item.title.lowercase().contains(q) ||
                item.subtitle.lowercase().contains(q) ||
                item.typeLabel.lowercase().contains(q)

            val matchesStatus = when (statusFilter) {
                SearchFilterStatus.ALL -> true
                SearchFilterStatus.COMPLETED -> item.status == ChallengeStatus.COMPLETED
                SearchFilterStatus.IN_PROGRESS -> item.status == ChallengeStatus.IN_PROGRESS
                SearchFilterStatus.NOT_STARTED -> item.status == ChallengeStatus.NOT_STARTED
                SearchFilterStatus.FAVORITES -> item.isFavorite
            }

            val matchesCategory = categoryFilter == null || item.categoryId == categoryFilter
            matchesQuery && matchesStatus && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // --- Filter setters ---
    fun setWeaponSearch(query: String) { _weaponSearch.value = query }
    fun setWeaponCategoryFilter(categoryId: String?) { _weaponCategoryFilter.value = categoryId }
    fun setWeaponStatusFilter(status: SearchFilterStatus) { _weaponStatusFilter.value = status }
    fun setWeaponSort(sort: WeaponSortOption) { _weaponSort.value = sort }

    fun setFavSearch(query: String) { _favSearch.value = query }
    fun setFavTypeFilter(filter: FavoriteFilterType) { _favTypeFilter.value = filter }
    fun setFavSort(sort: FavoriteSortOption) { _favSort.value = sort }

    fun setGlobalQuery(query: String) { _globalQuery.value = query }
    fun setGlobalStatusFilter(status: SearchFilterStatus) { _globalStatusFilter.value = status }
    fun setGlobalCategoryFilter(categoryId: String?) { _globalCategoryFilter.value = categoryId }

    // --- Weapon actions ---
    fun addWeapon(
        name: String,
        categoryId: String?,
        iconKey: String,
        pickedImageUri: Uri?,
        notes: String,
        includeStandardCamos: Boolean,
        onCreated: (String) -> Unit = {},
    ) {
        viewModelScope.launch {
            runCatching {
                val persistedUri = pickedImageUri?.let {
                    repository.persistPickedImage(getApplication(), it)
                }
                repository.createWeapon(
                    name = name,
                    categoryId = categoryId,
                    iconKey = iconKey,
                    imageUri = persistedUri,
                    notes = notes,
                    includeStandardCamos = includeStandardCamos,
                )
            }.onSuccess { id ->
                _uiMessage.tryEmit("Weapon '${name.trim()}' added")
                onCreated(id)
            }.onFailure {
                _uiMessage.tryEmit(it.message ?: "Failed to add weapon")
            }
        }
    }

    fun updateWeapon(
        id: String,
        name: String,
        categoryId: String?,
        iconKey: String,
        existingImageUri: String?,
        newPickedImageUri: Uri?,
        clearImage: Boolean,
        notes: String,
    ) {
        viewModelScope.launch {
            runCatching {
                val resolvedUri = when {
                    clearImage -> null
                    newPickedImageUri != null -> repository.persistPickedImage(getApplication(), newPickedImageUri) ?: existingImageUri
                    else -> existingImageUri
                }
                repository.updateWeapon(id, name, categoryId, iconKey, resolvedUri, notes)
            }.onSuccess {
                _uiMessage.tryEmit("Weapon updated")
            }.onFailure {
                _uiMessage.tryEmit(it.message ?: "Failed to update weapon")
            }
        }
    }

    fun deleteWeapon(id: String) {
        viewModelScope.launch {
            repository.deleteWeapon(id)
            _uiMessage.tryEmit("Weapon deleted")
        }
    }

    fun toggleWeaponFavorite(id: String) {
        viewModelScope.launch { repository.toggleWeaponFavorite(id) }
    }

    fun moveWeaponToCategory(weaponId: String, categoryId: String?) {
        viewModelScope.launch {
            repository.moveWeaponToCategory(weaponId, categoryId)
            _uiMessage.tryEmit("Weapon moved")
        }
    }

    // --- Camo actions ---
    fun addCamo(weaponId: String, name: String, tierKey: String) {
        viewModelScope.launch {
            runCatching {
                repository.createCamo(weaponId, name, tierKey)
            }.onSuccess {
                _uiMessage.tryEmit("Camo '${name.trim()}' added")
            }.onFailure {
                _uiMessage.tryEmit(it.message ?: "Failed to add camo")
            }
        }
    }

    fun updateCamo(camoId: String, name: String, tierKey: String) {
        viewModelScope.launch {
            runCatching {
                repository.updateCamo(camoId, name, tierKey)
            }.onSuccess {
                _uiMessage.tryEmit("Camo updated")
            }.onFailure {
                _uiMessage.tryEmit(it.message ?: "Failed to update camo")
            }
        }
    }

    fun deleteCamo(camoId: String) {
        viewModelScope.launch {
            repository.deleteCamo(camoId)
            _uiMessage.tryEmit("Camo deleted")
        }
    }

    fun toggleCamoFavorite(camoId: String) {
        viewModelScope.launch { repository.toggleCamoFavorite(camoId) }
    }

    // --- Challenge actions ---
    fun addChallenge(
        weaponId: String,
        camoId: String?,
        categoryId: String?,
        name: String,
        goal: Int,
        initialProgress: Int,
    ) {
        viewModelScope.launch {
            runCatching {
                repository.createChallenge(weaponId, camoId, categoryId, name, goal, initialProgress)
            }.onSuccess {
                _uiMessage.tryEmit("Challenge added")
            }.onFailure {
                _uiMessage.tryEmit(it.message ?: "Failed to add challenge")
            }
        }
    }

    fun updateChallengeDetails(
        challengeId: String,
        name: String,
        goal: Int,
        camoId: String,
        categoryId: String?,
    ) {
        viewModelScope.launch {
            runCatching {
                repository.updateChallengeDetails(challengeId, name, goal, camoId, categoryId)
            }.onSuccess {
                _uiMessage.tryEmit("Challenge updated")
            }.onFailure {
                _uiMessage.tryEmit(it.message ?: "Failed to update challenge")
            }
        }
    }

    fun adjustChallengeProgress(challengeId: String, delta: Int) {
        viewModelScope.launch { repository.adjustChallengeProgress(challengeId, delta) }
    }

    fun setChallengeProgress(challengeId: String, value: Int) {
        viewModelScope.launch { repository.setChallengeProgress(challengeId, value) }
    }

    fun markChallengeCompleted(challengeId: String) {
        viewModelScope.launch {
            repository.markChallengeCompleted(challengeId)
            _uiMessage.tryEmit("Challenge marked completed")
        }
    }

    fun resetChallengeProgress(challengeId: String) {
        viewModelScope.launch {
            repository.resetChallengeProgress(challengeId)
            _uiMessage.tryEmit("Challenge progress reset")
        }
    }

    fun deleteChallenge(challengeId: String) {
        viewModelScope.launch {
            repository.deleteChallenge(challengeId)
            _uiMessage.tryEmit("Challenge deleted")
        }
    }

    fun toggleChallengeFavorite(challengeId: String) {
        viewModelScope.launch { repository.toggleChallengeFavorite(challengeId) }
    }

    fun moveChallengeToCategory(challengeId: String, categoryId: String?) {
        viewModelScope.launch {
            repository.moveChallengeToCategory(challengeId, categoryId)
            _uiMessage.tryEmit("Challenge category updated")
        }
    }

    // --- Categories ---
    fun addCategory(name: String, iconKey: String = "custom") {
        viewModelScope.launch {
            runCatching {
                repository.addCategory(name, iconKey)
            }.onSuccess {
                _uiMessage.tryEmit("Category '${name.trim()}' created")
            }.onFailure {
                _uiMessage.tryEmit(it.message ?: "Failed to create category")
            }
        }
    }

    fun renameCategory(id: String, newName: String) {
        viewModelScope.launch {
            runCatching {
                repository.renameCategory(id, newName)
            }.onSuccess {
                _uiMessage.tryEmit("Category renamed")
            }.onFailure {
                _uiMessage.tryEmit(it.message ?: "Failed to rename category")
            }
        }
    }

    fun deleteCategory(id: String) {
        viewModelScope.launch {
            repository.deleteCategory(id)
            if (_weaponCategoryFilter.value == id) _weaponCategoryFilter.value = null
            if (_globalCategoryFilter.value == id) _globalCategoryFilter.value = null
            _uiMessage.tryEmit("Category deleted (items moved to Uncategorized)")
        }
    }

    fun toggleCategoryFavorite(id: String) {
        viewModelScope.launch { repository.toggleCategoryFavorite(id) }
    }

    // --- Settings & Backup ---
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setHapticsEnabled(enabled) }
    }

    fun exportBackupToUri(uri: Uri) {
        viewModelScope.launch {
            backupRepository.exportToUri(getApplication(), uri)
                .onSuccess { _uiMessage.tryEmit("Backup exported successfully") }
                .onFailure { _uiMessage.tryEmit("Export failed: ${it.message ?: "Unknown error"}") }
        }
    }

    fun prepareImportFromUri(uri: Uri) {
        viewModelScope.launch {
            when (val result = backupRepository.readAndValidateUri(getApplication(), uri)) {
                is BackupValidationResult.Valid -> {
                    _importState.value = ImportBackupUiState.Preview(result.backup)
                }
                is BackupValidationResult.Invalid -> {
                    _importState.value = ImportBackupUiState.Error(result.errorMessage)
                }
            }
        }
    }

    fun confirmRestoreBackup(backup: GrindBackup) {
        viewModelScope.launch {
            backupRepository.restoreBackup(backup)
                .onSuccess {
                    _importState.value = ImportBackupUiState.Idle
                    _uiMessage.tryEmit("Backup restored (${backup.weapons.size} weapons, ${backup.challenges.size} challenges)")
                }
                .onFailure {
                    _importState.value = ImportBackupUiState.Error("Restore failed: ${it.message ?: "Unknown error"}")
                }
        }
    }

    fun dismissImportState() {
        _importState.value = ImportBackupUiState.Idle
    }

    fun resetAllData(restoreDefaultCategories: Boolean = true) {
        viewModelScope.launch {
            repository.resetAllData(restoreDefaultCategories)
            _uiMessage.tryEmit("All tracking data has been reset")
        }
    }
}
