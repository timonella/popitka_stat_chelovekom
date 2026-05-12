package com.example.tiktak.presentation.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.presentation.common.components.EmotionSelector
import com.example.tiktak.presentation.common.components.LoadingSpinner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    navController: NavController,
    entryId: String
) {
    val context = LocalContext.current

    // Создаем ViewModel с фабрикой
    val viewModel: EntryViewModel = viewModel(
        factory = EntryViewModelFactory(context, entryId)
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val images by viewModel.images.collectAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addImage(it.toString())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (entryId == "new") "Новая запись" else "Редактирование") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (viewModel.saveEntry()) {
                                    navController.navigateUp()
                                }
                            }
                        },
                        enabled = !isSaving && title.isNotBlank() && content.isNotBlank()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text("Сохранить")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            LoadingSpinner(modifier = Modifier.fillMaxSize())
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EmotionSelector(
                    selectedEmotion = selectedEmotion,
                    onEmotionSelected = { emotion -> viewModel.updateEmotion(emotion) }
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.updateTitle(it) },
                    label = { Text("Заголовок") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("О чем вы думаете?") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { viewModel.updateContent(it) },
                    label = { Text("Содержание") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Поделитесь своими мыслями...") },
                    minLines = 8,
                    maxLines = 15
                )

                if (images.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Фотографии",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(images) { imagePath ->
                                Box(
                                    modifier = Modifier.size(100.dp)
                                ) {
                                    AsyncImage(
                                        model = imagePath,
                                        contentDescription = "Фото",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    IconButton(
                                        onClick = { viewModel.removeImage(imagePath) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить фото")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Добавить фотографию")
                }
            }
        }
    }
}