package com.example.tiktak.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    val calendar = Calendar.getInstance()
    var currentMonth by remember { mutableStateOf(calendar.clone() as Calendar) }
    val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Календарь") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Навигация по месяцам
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newCalendar = currentMonth.clone() as Calendar
                    newCalendar.add(Calendar.MONTH, -1)
                    currentMonth = newCalendar
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Предыдущий")
                }

                Text(
                    text = dateFormat.format(currentMonth.time),
                    style = MaterialTheme.typography.headlineSmall
                )

                IconButton(onClick = {
                    val newCalendar = currentMonth.clone() as Calendar
                    newCalendar.add(Calendar.MONTH, 1)
                    currentMonth = newCalendar
                }) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Следующий")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Простой календарь (в разработке)
            Card(
                modifier = Modifier.fillMaxSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Календарь с отметками записей\n(В разработке)",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}