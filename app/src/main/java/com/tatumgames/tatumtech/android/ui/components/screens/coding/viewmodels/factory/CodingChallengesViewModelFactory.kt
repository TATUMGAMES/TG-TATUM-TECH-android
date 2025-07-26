package com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tatumgames.tatumtech.android.ui.components.screens.coding.viewmodels.CodingChallengesViewModel

class CodingChallengesViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CodingChallengesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CodingChallengesViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
