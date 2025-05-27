package com.fairshare.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fairshare.utils.CurrencyUtils
import com.fairshare.ui.viewmodel.SettingsViewModel
import com.fairshare.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val uiState by viewModel.uiState.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        // Set default currency to PHP if not already set
        if (uiState.selectedCurrency != CurrencyUtils.CurrencyCodes.PHP) {
            viewModel.setCurrency(CurrencyUtils.CurrencyCodes.PHP)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Account Section
            SettingsSectionHeader(title = "Account")
            SettingsItem(
                title = "Profile Settings",
                subtitle = "Manage your account details",
                icon = Icons.Default.Person,
                onClick = { navController.navigate(Screen.Profile.route) }
            )
            SettingsItem(
                title = "Pro Features",
                subtitle = "Coming soon",
                icon = Icons.Default.Star,
                enabled = false,
                onClick = { }
            )
            SettingsItem(
                title = "Logout",
                subtitle = "Sign out of your account",
                icon = Icons.Default.Logout,
                onClick = { showLogoutDialog = true }
            )
            Divider()

            // Preferences Section
            SettingsSectionHeader(title = "Preferences")
            SettingsItem(
                title = "Currency",
                subtitle = "Selected: ${CurrencyUtils.getCurrencySymbol(uiState.selectedCurrency)} (${uiState.selectedCurrency})",
                icon = Icons.Default.AttachMoney,
                onClick = { showCurrencyDialog = true }
            )
            SettingsSwitch(
                title = "Push Notifications",
                subtitle = "Get instant updates about expenses and settlements",
                icon = Icons.Default.Notifications,
                checked = uiState.pushNotificationsEnabled,
                enabled = false, // Disabled as requested
                onCheckedChange = { viewModel.setPushNotifications(it) }
            )
            SettingsSwitch(
                title = "Email Notifications",
                subtitle = "Receive email updates about important activities",
                icon = Icons.Default.Email,
                checked = uiState.emailNotificationsEnabled,
                enabled = false, // Disabled as requested
                onCheckedChange = { viewModel.setEmailNotifications(it) }
            )
            SettingsSwitch(
                title = "Dark Mode",
                subtitle = "Toggle dark theme",
                icon = Icons.Default.DarkMode,
                checked = uiState.isDarkMode,
                onCheckedChange = { viewModel.setDarkMode(it) }
            )
            Divider()

            // Feedback Section
            SettingsSectionHeader(title = "Feedback")
            SettingsItem(
                title = "Rate App",
                subtitle = "Coming soon to Play Store",
                icon = Icons.Default.Star,
                enabled = false,
                onClick = { }
            )
            SettingsItem(
                title = "Contact Support",
                subtitle = "Get help with Fair Share",
                icon = Icons.Default.Support,
                enabled = false,
                onClick = { }
            )
            Divider()

            // About Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "© 2025 Persiv Studio\nAll rights reserved",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showCurrencyDialog) {
            CurrencySelectionDialog(
                onDismiss = { showCurrencyDialog = false },
                onCurrencySelected = { 
                    viewModel.setCurrency(it)
                    showCurrencyDialog = false
                }
            )
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Logout") },
                text = { Text("Are you sure you want to logout?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Text("Logout")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun CurrencySelectionDialog(
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Currency") },
        text = {
            LazyColumn {
                items(CurrencyUtils.getAllCurrencies()) { currency ->
                    Surface(
                        onClick = { onCurrencySelected(currency) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${CurrencyUtils.getCurrencySymbol(currency)} ($currency)",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = CurrencyUtils.getCurrencyDisplayName(currency),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (enabled) 1f else 0.38f
                        )
                    )
                }
            }
            if (onClick != {}) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (enabled) 1f else 0.38f
                    )
                )
            }
        }
    }
}

@Composable
private fun SettingsSwitch(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true
) {
    val textColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        onClick = { if (enabled) onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = if (enabled) 1f else 0.38f
                        )
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = { if (enabled) onCheckedChange(it) },
                enabled = enabled
            )
        }
    }
} 