package com.fairshare.utils

import android.util.Patterns

enum class PasswordStrength {
    WEAK,
    MEDIUM,
    STRONG
}

object ValidationUtils {
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && getPasswordStrength(password) != PasswordStrength.WEAK
    }

    fun getPasswordStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK

        var hasLetter = false
        var hasDigit = false
        var hasSpecialChar = false

        for (char in password) {
            when {
                char.isLetter() -> hasLetter = true
                char.isDigit() -> hasDigit = true
                !char.isLetterOrDigit() -> hasSpecialChar = true
            }
        }

        return when {
            hasLetter && hasDigit && hasSpecialChar -> PasswordStrength.STRONG
            hasLetter && hasDigit -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
} 