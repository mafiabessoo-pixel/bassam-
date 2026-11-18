package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.backup.GrindBackup
import com.example.data.local.AppSettings
import com.example.data.local.ThemeMode
import com.example.data.repository.GrindSnapshot
import com.example.ui.components.ConfirmDangerDialog
import com.example.ui.components.TacticalCard
import com.example.ui.theme.LocalTacticalColors
import com.example.ui.viewmodel.ImportBackupUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: AppSettings,
    snapshot: GrindSnapshot,
    importState: ImportBackupUiState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onOpenCategories: () -> Unit,
    onExportBackupUri: (Uri) -> Unit,
    onImportBackupUri: (Uri) -> Unit,
    onConfirmRestoreBackup: (GrindBackup) -> Unit,
    onDismissImportState: () -> Unit,
    onResetAllData: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tactical = LocalTacticalColors.current
    var showResetConfirm by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportBackupUri(uri)
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onImportBackupUri(uri)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column {
                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        text = "Appearance, Categories & Local Backup",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Theme Settings
            item {
                TacticalCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Theme Appearance", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(8.dp))
                    ThemeMode.entries.forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.themeMode == mode,
                                onClick = { onThemeModeChange(mode) },
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // Tactical Feedback & Organization
            item {
                TacticalCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Filled.Vibration, contentDescription = null, tint = tactical.silver)
                            Column {
                                Text("Haptic Feedback", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Tactile pulse when starring or updating kills",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Switch(
                            checked = settings.hapticsEnabled,
                            onCheckedChange = onHapticsChange,
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onOpenCategories,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Filled.Category, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Manage Weapon Categories (${snapshot.categories.size})")
                    }
                }
            }

            // Backup & Data Management
            item {
                TacticalCard {
                    Text("Backup & Data Management", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "All changes save automatically to your local SQLite database. Export a JSON backup anytime to safeguard or transfer your grind.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
                            exportLauncher.launch("codm_grind_backup_$stamp.json")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("export_backup_button"),
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Export JSON Backup")
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            importLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("import_backup_button"),
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Import JSON Backup")
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { showResetConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("reset_all_data_button"),
                    ) {
                        Icon(Icons.Filled.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset All Data")
                    }
                }
            }

            // Application & Version Info
            item {
                TacticalCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = tactical.silver)
                        Text("Application Information", style = MaterialTheme.typography.titleLarge)
                    }
                    Spacer(Modifier.height(8.dp))
                    InfoRow("Application", "CODM Grind Tracker")
                    InfoRow("Version", "1.0.0")
                    InfoRow("Storage Engine", "Offline Local Room SQLite + DataStore")
                    InfoRow("Tracked Items", "${snapshot.weapons.size} weapons • ${snapshot.camos.size} camos • ${snapshot.challenges.size} challenges")
                    InfoRow("Account Required", "None (100% Offline, Ad-Free)")
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (showResetConfirm) {
        ConfirmDangerDialog(
            title = "Reset All Data?",
            message = "This will permanently delete all ${snapshot.weapons.size} weapons, ${snapshot.camos.size} camos, ${snapshot.challenges.size} challenges, and favorites. Default weapon categories will be restored. Consider exporting a JSON backup first.",
            confirmLabel = "Reset Everything",
            onConfirm = {
                showResetConfirm = false
                onResetAllData()
            },
            onDismiss = { showResetConfirm = false },
        )
    }

    when (importState) {
        is ImportBackupUiState.Preview -> {
            val b = importState.backup
            AlertDialog(
                onDismissRequest = onDismissImportState,
                title = { Text("Restore Backup?", style = MaterialTheme.typography.headlineSmall) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "This backup is valid and ready to restore. Importing will replace your current tracking data.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("• Categories: ${b.categories.size}", style = MaterialTheme.typography.bodySmall)
                        Text("• Weapons: ${b.weapons.size}", style = MaterialTheme.typography.bodySmall)
                        Text("• Camos: ${b.camos.size}", style = MaterialTheme.typography.bodySmall)
                        Text("• Challenges: ${b.challenges.size}", style = MaterialTheme.typography.bodySmall)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirmRestoreBackup(b) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Text("Replace & Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissImportState) {
                        Text("Cancel")
                    }
                },
            )
        }
        is ImportBackupUiState.Error -> {
            AlertDialog(
                onDismissRequest = onDismissImportState,
                title = { Text("Invalid Backup File", style = MaterialTheme.typography.headlineSmall) },
                text = {
                    Text(
                        text = importState.message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    Button(onClick = onDismissImportState) {
                        Text("OK")
                    }
                },
            )
        }
        ImportBackupUiState.Idle -> Unit
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
