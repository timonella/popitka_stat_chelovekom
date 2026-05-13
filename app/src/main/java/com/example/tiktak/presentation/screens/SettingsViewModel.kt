package com.example.tiktak.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.data.datastore.SettingsDataStore
import com.example.tiktak.domain.repository.AuthRepository
import com.example.tiktak.presentation.theme.ThemeType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentTheme = MutableStateFlow(ThemeType.SYSTEM)
    val currentTheme = _currentTheme.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled = _biometricEnabled.asStateFlow()

    private val _zaNashikhAdsEnabled = MutableStateFlow(true)
    val zaNashikhAdsEnabled = _zaNashikhAdsEnabled.asStateFlow()

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
            settingsDataStore.notificationsFlow.collect { enabled ->
                _notificationsEnabled.value = enabled
            }
        }

        viewModelScope.launch {
            settingsDataStore.biometricFlow.collect { enabled ->
                _biometricEnabled.value = enabled
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

    fun updateNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        viewModelScope.launch {
            settingsDataStore.saveNotifications(enabled)
        }
    }

    fun updateBiometric(enabled: Boolean) {
        _biometricEnabled.value = enabled
        viewModelScope.launch {
            settingsDataStore.saveBiometric(enabled)
        }
    }

    fun updateZaNashikhAdsEnabled(enabled: Boolean) {
        _zaNashikhAdsEnabled.value = enabled
        viewModelScope.launch {
            settingsDataStore.saveZaNashikhAdsEnabled(enabled)
        }
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = authRepository.logout()
            _isLoading.value = false

            if (result.isSuccess) {
                onSuccess()
            }
        }
    }
}