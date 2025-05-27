package com.fairshare.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fairshare.utils.CurrencyUtils
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val selectedCurrency: String = CurrencyUtils.CurrencyCodes.PHP,
    val pushNotificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    private val auth = FirebaseAuth.getInstance()

    fun setDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun setCurrency(code: String) {
        _uiState.value = _uiState.value.copy(selectedCurrency = code)
    }

    fun setPushNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
    }

    fun setEmailNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(emailNotificationsEnabled = enabled)
    }

    fun logout() {
        viewModelScope.launch {
            try {
                auth.signOut()
            } catch (e: Exception) {
                // Handle logout error if needed
                e.printStackTrace()
            }
        }
    }
} 