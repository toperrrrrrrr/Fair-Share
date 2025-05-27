package com.fairshare.ui.viewmodel

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.fairshare.auth.AuthResult
import com.fairshare.auth.AuthUiState
import com.fairshare.auth.GoogleAuthManager
import com.fairshare.data.repository.AuthRepository
import com.fairshare.utils.CurrencyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val googleAuthManager: GoogleAuthManager = GoogleAuthManager(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        auth.currentUser?.let {
            _uiState.value = AuthUiState.Success(it)
        }
    }

    fun initGoogleSignIn(context: Context) {
        googleAuthManager.init(context)
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                when (val result = authRepository.signIn(email, password)) {
                    is AuthResult.Success -> {
                        if (result.user != null) {
                            _uiState.value = AuthUiState.Success(result.user)
                        } else {
                            _uiState.value = AuthUiState.Error("Sign in failed: No user found")
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Sign in failed")
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                when (val result = googleAuthManager.signInWithGoogle(idToken)) {
                    is AuthResult.Success -> {
                        if (result.user != null) {
                            _uiState.value = AuthUiState.Success(result.user)
                        } else {
                            _uiState.value = AuthUiState.Error("Google sign in failed: No user found")
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Google sign in failed")
            }
        }
    }

    fun signUp(email: String, password: String, displayName: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                when (val result = authRepository.signUp(email, password, displayName)) {
                    is AuthResult.Success -> {
                        if (result.user != null) {
                            _uiState.value = AuthUiState.EmailVerificationSent
                        } else {
                            _uiState.value = AuthUiState.Error("Registration failed: No user created")
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Registration failed")
            }
        }
    }

    fun sendEmailVerification() {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                when (val result = authRepository.sendEmailVerification()) {
                    is AuthResult.Success -> {
                        _uiState.value = AuthUiState.EmailVerificationSent
                    }
                    is AuthResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to send verification email")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                when (val result = authRepository.resetPassword(email)) {
                    is AuthResult.Success -> {
                        if (result.user != null) {
                            _uiState.value = AuthUiState.Success(result.user)
                        } else {
                            _uiState.value = AuthUiState.Error("Failed to reset password: No user found")
                        }
                    }
                    is AuthResult.Error -> {
                        _uiState.value = AuthUiState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to reset password")
            }
        }
    }

    fun updatePassword(
        currentPassword: String,
        newPassword: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = authRepository.updatePassword(currentPassword, newPassword)) {
                    is AuthResult.Success -> onComplete(true, null)
                    is AuthResult.Error -> onComplete(false, result.message)
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to update password")
            }
        }
    }

    fun updateProfile(
        displayName: String? = null,
        photoUrl: String? = null,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = authRepository.updateProfile(displayName, photoUrl)) {
                    is AuthResult.Success -> onComplete(true, null)
                    is AuthResult.Error -> onComplete(false, result.message)
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to update profile")
            }
        }
    }

    fun updateEmail(
        newEmail: String,
        password: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                when (val result = authRepository.updateEmail(newEmail, password)) {
                    is AuthResult.Success -> onComplete(true, null)
                    is AuthResult.Error -> onComplete(false, result.message)
                }
            } catch (e: Exception) {
                onComplete(false, e.message ?: "Failed to update email")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthManager.signOut()
            auth.signOut()
            _uiState.value = AuthUiState.Initial
        }
    }

    fun isUserSignedIn(): Boolean = auth.currentUser != null

    fun isEmailVerified(): Boolean = authRepository.isEmailVerified()

    fun initBiometric(context: Context) {
        authRepository.initBiometric(context)
    }

    fun isBiometricAvailable(): Boolean = authRepository.isBiometricAvailable()

    fun authenticateWithBiometric(activity: FragmentActivity) {
        _uiState.value = AuthUiState.BiometricAuthSuccess
    }

    fun skipLoginForDev() {
        viewModelScope.launch {
            try {
                _uiState.value = AuthUiState.Loading
                
                // Ensure we're signed out first
                auth.signOut()
                
                // Try anonymous sign in
                val result = auth.signInAnonymously().await()
                val user = result.user ?: throw Exception("Failed to create anonymous user")
                
                // Update display name for dev user
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName("Dev User")
                    .build()
                user.updateProfile(profileUpdates).await()
                
                _uiState.value = AuthUiState.Success(user)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(e.message ?: "Failed to skip login")
            }
        }
    }
} 