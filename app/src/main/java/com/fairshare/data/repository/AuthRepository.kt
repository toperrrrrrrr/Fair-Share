package com.fairshare.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException
import com.google.firebase.auth.UserProfileChangeRequest
import com.fairshare.data.model.FirebaseUser as AppUser
import com.google.firebase.Timestamp
import android.content.Context
import androidx.fragment.app.FragmentActivity
import com.fairshare.auth.BiometricManager
import com.fairshare.auth.AuthResult

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    private val firebaseRepository = FirebaseRepository()
    private lateinit var biometricManager: BiometricManager

    fun initBiometric(context: Context) {
        biometricManager = BiometricManager(context)
    }

    fun isBiometricAvailable(): Boolean {
        return ::biometricManager.isInitialized && biometricManager.isBiometricAvailable()
    }

    fun authenticateWithBiometric(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!::biometricManager.isInitialized) {
            onError("Biometric authentication not initialized")
            return
        }

        biometricManager.showBiometricPrompt(
            activity = activity,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { AuthResult.Success(it) } ?: AuthResult.Error("Authentication failed")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Authentication failed", e)
        }
    }

    suspend fun signUp(email: String, password: String, displayName: String): AuthResult {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                // Update display name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                firebaseUser.updateProfile(profileUpdates).await()

                // Send email verification
                firebaseUser.sendEmailVerification().await()

                // Create user document in Firestore
                val user = AppUser(
                    id = firebaseUser.uid,
                    email = email,
                    displayName = displayName,
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )
                firebaseRepository.createUser(user)

                AuthResult.Success(firebaseUser)
            } ?: AuthResult.Error("User creation failed")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Registration failed", e)
        }
    }

    suspend fun resetPassword(email: String): AuthResult {
        return try {
            auth.sendPasswordResetEmail(email).await()
            auth.currentUser?.let { AuthResult.Success(it) } ?: AuthResult.Error("No user logged in")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Failed to reset password", e)
        }
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String): AuthResult {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val email = user.email ?: throw Exception("User email not found")

            // Re-authenticate user before changing password
            signIn(email, currentPassword)
            
            user.updatePassword(newPassword).await()
            AuthResult.Success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Failed to update password", e)
        }
    }

    suspend fun updateProfile(
        displayName: String? = null,
        photoUrl: String? = null
    ): AuthResult {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            
            val profileUpdates = UserProfileChangeRequest.Builder().apply {
                displayName?.let { setDisplayName(it) }
                photoUrl?.let { setPhotoUri(android.net.Uri.parse(it)) }
            }.build()

            user.updateProfile(profileUpdates).await()

            // Update Firestore user document
            val updates = mutableMapOf<String, Any>("updatedAt" to Timestamp.now())
            displayName?.let { updates["displayName"] = it }
            photoUrl?.let { updates["photoUrl"] = it }
            
            firebaseRepository.updateUser(user.uid, updates)
            
            AuthResult.Success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Failed to update profile", e)
        }
    }

    suspend fun updateEmail(newEmail: String, password: String): AuthResult {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            val currentEmail = user.email ?: throw Exception("User email not found")

            // Re-authenticate user before changing email
            signIn(currentEmail, password)
            
            user.updateEmail(newEmail).await()

            // Update Firestore user document
            firebaseRepository.updateUser(
                user.uid,
                mapOf(
                    "email" to newEmail,
                    "updatedAt" to Timestamp.now()
                )
            )

            AuthResult.Success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Failed to update email", e)
        }
    }

    suspend fun sendEmailVerification(): AuthResult {
        return try {
            val user = auth.currentUser ?: throw Exception("No user logged in")
            user.sendEmailVerification().await()
            AuthResult.Success(user)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            AuthResult.Error(e.message ?: "Failed to send verification email", e)
        }
    }

    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isUserSignedIn(): Boolean = auth.currentUser != null
} 