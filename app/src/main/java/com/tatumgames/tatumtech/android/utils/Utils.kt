package com.tatumgames.tatumtech.android.utils

import android.util.Patterns

object Utils {

    fun isPasswordValid(password: String): Boolean {
        val minLength = 6
        val hasNoSpaces = !password.contains(" ")
        val hasUppercase = password.any { it.isUpperCase() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return password.length >= minLength && hasNoSpaces && hasUppercase && hasSpecialChar
    }

    fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
