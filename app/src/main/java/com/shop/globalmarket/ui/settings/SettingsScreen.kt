package com.shop.globalmarket.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkModeEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            SettingsToggleItem(
                title = "Push Notifications",
                icon = Icons.Default.Notifications,
                state = notificationsEnabled,
                onStateChange = { notificationsEnabled = it }
            )
            
            SettingsToggleItem(
                title = "Dark Mode",
                icon = Icons.Default.Palette,
                state = darkModeEnabled,
                onStateChange = { darkModeEnabled = it }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            ListItem(
                headlineContent = { Text("Security") },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Privacy Policy") },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Terms of Service") },
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun SettingsToggleItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    state: Boolean,
    onStateChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge)
        }
        Switch(checked = state, onCheckedChange = onStateChange)
    }
}
