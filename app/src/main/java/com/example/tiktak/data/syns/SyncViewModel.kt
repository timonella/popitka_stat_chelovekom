package com.example.tiktak.presentation.screens.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tiktak.data.sync.SyncManager
import com.example.tiktak.data.sync.SyncState
import com.example.tiktak.data.sync.SyncStatusType
import com.example.tiktak.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SyncViewModel(
    private val diaryRepository: DiaryRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _syncState = MutableStateFlow<SyncState?>(null)
    val syncState = _syncState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    private val _syncEnabled = MutableStateFlow(true)
    val syncEnabled = _syncEnabled.asStateFlow()

    private val _syncInterval = MutableStateFlow(30) // минут
    val syncInterval = _syncInterval.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<String?>(null)
    val lastSyncTime = _lastSyncTime.asStateFlow()

    init {
        observeSyncState()
        loadSyncSettings()
    }

    private fun observeSyncState() {
        viewModelScope.launch {
            syncManager.getSyncState().collect { state ->
                _syncState.value = state
                _isSyncing.value = state.status == SyncStatusType.SYNCING
            }
        }
    }

    private fun loadSyncSettings() {
        // Загрузка настроек синхронизации из DataStore
        viewModelScope.launch {
            // Здесь можно загрузить настройки
        }
    }

    fun startSync() {
        viewModelScope.launch {
            syncManager.syncEntries()
            _lastSyncTime.value = java.text.SimpleDateFormat(
                "HH:mm:ss dd.MM.yyyy",
                java.util.Locale.getDefault()
            ).format(java.util.Date())
        }
    }

    fun updateSyncEnabled(enabled: Boolean) {
        _syncEnabled.value = enabled
        // Сохраняем настройку
    }

    fun updateSyncInterval(minutes: Int) {
        _syncInterval.value = minutes
        // Сохраняем настройку
    }

    fun formatProgress(progress: Int): String {
        return "$progress%"
    }
}