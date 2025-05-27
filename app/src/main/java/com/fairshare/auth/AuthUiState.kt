package com.fairshare.auth

import com.google.firebase.auth.FirebaseUser

sealed class AuthUiState {
    object Initial : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: FirebaseUser) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object EmailVerificationSent : AuthUiState()
    object BiometricAuthSuccess : AuthUiState()
} 