package com.example.tiktak.presentation.screens.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.data.datastore.SettingsDataStore
import com.example.tiktak.domain.repository.AuthRepository
import com.example.tiktak.domain.repository.DiaryRepository
import com.example.tiktak.presentation.theme.ThemeType
import com.example.tiktak.utils.PdfExportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore,
    private val diaryRepository: DiaryRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentTheme = MutableStateFlow(ThemeType.SYSTEM)
    val currentTheme = _currentTheme.asStateFlow()

    private val _zaNashikhAdsEnabled = MutableStateFlow(true)
    val zaNashikhAdsEnabled = _zaNashikhAdsEnabled.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting = _isExporting.asStateFlow()

    private val _exportResult = MutableStateFlow<Uri?>(null)
    val exportResult = _exportResult.asStateFlow()

    private val _exportError = MutableStateFlow<String?>(null)
    val exportError = _exportError.asStateFlow()

    private val _user = MutableStateFlow(authRepository.getCurrentUser())
    val user = _user.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsDataStore.themeFlow.collect { theme ->
                _currentTheme.value = theme
            }
        }

        viewModelScope.launch {
            settingsDataStore.zaNashikhAdsEnabledFlow.collect { enabled ->
                _zaNashikhAdsEnabled.value = enabled
            }
        }
    }

    fun updateTheme(themeType: ThemeType) {
        viewModelScope.launch {
            settingsDataStore.saveTheme(themeType)
        }
    }

    fun updateZaNashikhAdsEnabled(enabled: Boolean) {
        _zaNashikhAdsEnabled.value = enabled
        viewModelScope.launch {
            settingsDataStore.saveZaNashikhAdsEnabled(enabled)
        }
    }

    suspend fun exportToPdf(context: Context): Result<Uri> {
        _isExporting.value = true
        _exportError.value = null

        val result = try {
            val entriesFlow = diaryRepository.getAllEntries()
            val entries = entriesFlow.first()

            if (entries.isEmpty()) {
                return Result.failure(Exception("Нет записей для экспорта"))
            }

            val pdfManager = PdfExportManager(context)
            pdfManager.exportEntriesToPdf(entries)
        } catch (e: Exception) {
            Result.failure(e)
        }

        _isExporting.value = false

        if (result.isSuccess) {
            _exportResult.value = result.getOrNull()
        } else {
            _exportError.value = result.exceptionOrNull()?.message ?: "Ошибка экспорта"
        }

        return result
    }

    fun clearExportResult() {
        _exportResult.value = null
        _exportError.value = null
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.logout()
            if (result.isSuccess) {
                settingsDataStore.clearPin()
                onSuccess()
            }
            _isLoading.value = false
        }
    }
}
