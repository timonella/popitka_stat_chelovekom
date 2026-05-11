package com.example.dnevnik.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dnevnik.data.local.entity.JournalEntryEntity
import com.example.dnevnik.domain.repository.JournalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: JournalRepository
) : ViewModel() {

    private val _entries = MutableStateFlow<List<JournalEntryEntity>>(emptyList())
    val entries: StateFlow<List<JournalEntryEntity>> = _entries

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            repository.getAllEntries().collectLatest { entryList ->
                _entries.value = entryList
            }
        }
    }

    fun searchEntries(query: String) {
        viewModelScope.launch {
            if (query.isEmpty()) {
                loadEntries()
            } else {
                repository.searchEntries(query).collectLatest { results ->
                    _entries.value = results
                }
            }
        }
    }
}