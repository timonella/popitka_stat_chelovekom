package com.example.tiktak.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatrioticTopAppBar(
    title: String,
    navController: NavController? = null,
    showBackButton: Boolean = false,
    isZaNashikhTheme: Boolean = false,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Column {
        if (isZaNashikhTheme) {
            StGeorgeRibbonHeader()
        }

        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                if (showBackButton && navController != null) {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = if (isZaNashikhTheme)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp),
                titleContentColor = if (isZaNashikhTheme)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )
        )

        if (isZaNashikhTheme) {
            StGeorgeRibbon()
        }
    }
}