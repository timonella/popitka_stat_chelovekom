package com.example.tiktak.presentation.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tiktak.domain.model.Emotion
import com.example.tiktak.presentation.common.components.EmotionSelector
import com.example.tiktak.presentation.common.components.LoadingSpinner
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryScreen(
    navController: NavController,
    entryId: String
) {
    val context = LocalContext.current

    val viewModel: EntryViewModel = viewModel(
        factory = EntryViewModelFactory(context, entryId)
    )

    val isLoading by viewModel.isLoading.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val title by viewModel.title.collectAsState()
    val content by viewModel.content.collectAsState()
    val selectedEmotion by viewModel.selectedEmotion.collectAsState()
    val images by viewModel.images.collectAsState()
    val videos by viewModel.videos.collectAsState()
    val audioFiles by viewModel.audioFiles.collectAsState()
    val documents by viewModel.documents.collectAsState()

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var currentPhotoPath by remember { mutableStateOf<String?>(null) }

    // Проверка разрешений
    val hasCameraPermission = ContextCompat.checkSelfPermission(
        context, Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    val hasStoragePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    } else {
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    // Лаунчеры для разрешений
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Нужно разрешение для камеры", Toast.LENGTH_SHORT).show()
        }
    }

    val storagePermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Нужно разрешение для доступа к файлам", Toast.LENGTH_SHORT).show()
        }
    }

    // Лаунчер для камеры (фото)
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            viewModel.addImage(currentPhotoPath!!)
            currentPhotoPath = null
        }
    }

    // Лаунчер для выбора видео с камеры
    val videoCaptureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && currentPhotoPath != null) {
            viewModel.addVideo(currentPhotoPath!!)
            currentPhotoPath = null
        }
    }

    // Лаунчеры для выбора файлов из галереи
    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addImage(it.toString())
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addVideo(it.toString())
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addAudio(it.toString())
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.addDocument(it.toString())
        }
    }

    // Создание временного файла для фото/видео
    fun createTempFile(type: String): File? {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir(null)
        return try {
            File.createTempFile(
                "JPEG_${timeStamp}_",
                if (type == "image") ".jpg" else ".mp4",
                storageDir
            ).apply {
                currentPhotoPath = absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun takePhoto() {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            val file = createTempFile("image")
            if (file != null) {
                val photoUri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",
                    file
                )
                cameraLauncher.launch(photoUri)
            } else {
                Toast.makeText(context, "Не удалось создать файл", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun takeVideo() {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            val file = createTempFile("video")
            if (file != null) {
                val videoUri = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".fileprovider",
                    file
                )
                videoCaptureLauncher.launch(videoUri)
            } else {
                Toast.makeText(context, "Не удалось создать файл", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkAndRequestStoragePermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (hasStoragePermission) {
                action()
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        } else {
            if (hasStoragePermission) {
                action()
            } else {
                storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
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

                // Изображения
                if (images.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📷 Фотографии",
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
                                            model = File(imagePath),
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
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Видео
                if (videos.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🎬 Видео",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(videos) { videoPath ->
                                    Box(
                                        modifier = Modifier.size(100.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.fillMaxSize(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = "Видео",
                                                    modifier = Modifier.size(48.dp)
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.removeVideo(videoPath) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Аудио
                if (audioFiles.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🎵 Аудиозаписи",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            audioFiles.forEach { audioPath ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.Audiotrack,
                                                contentDescription = "Аудио",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = audioPath.substringAfterLast("/"),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(onClick = { viewModel.removeAudio(audioPath) }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Документы
                if (documents.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📄 Документы",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            documents.forEach { documentPath ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                Icons.Default.Description,
                                                contentDescription = "Документ",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = documentPath.substringAfterLast("/"),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }
                                        IconButton(onClick = { viewModel.removeDocument(documentPath) }) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Удалить",
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Кнопка добавления медиа
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Добавить вложение",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { takePhoto() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Снять фото")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Снять")
                            }

                            OutlinedButton(
                                onClick = { takeVideo() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = "Записать видео")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Записать")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { checkAndRequestStoragePermission { imagePickerLauncher.launch("image/*") } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Выбрать фото")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Фото")
                            }

                            OutlinedButton(
                                onClick = { checkAndRequestStoragePermission { videoPickerLauncher.launch("video/*") } },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Videocam, contentDescription = "Выбрать видео")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Видео")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = "Аудио")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Аудио")
                            }

                            OutlinedButton(
                                onClick = { documentPickerLauncher.launch("*/*") },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Description, contentDescription = "Файл")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Файл")
                            }
                        }
                    }
                }
            }
        }
    }
}