package com.tatumgames.tatumtech.android.ui.viewmodels.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tatumgames.tatumtech.android.ui.components.screens.main.ProfileUpdateManager
import com.tatumgames.tatumtech.android.ui.components.screens.main.SignOutManager
import com.tatumgames.tatumtech.android.ui.viewmodels.UserProfileViewModel

class UserProfileViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            val appContext = context.applicationContext
            return UserProfileViewModel(
                signOut = { SignOutManager.signOut(appContext) },
                saveProfile = { firstName, lastName, email ->
                    ProfileUpdateManager.save(appContext, firstName, lastName, email)
                }
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
