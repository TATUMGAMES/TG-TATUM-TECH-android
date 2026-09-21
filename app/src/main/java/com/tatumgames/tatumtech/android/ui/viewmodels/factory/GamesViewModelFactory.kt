package com.tatumgames.tatumtech.android.ui.viewmodels.factory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tatumgames.tatumtech.android.data.games.LocalGameRepository
import com.tatumgames.tatumtech.android.ui.viewmodels.GamesViewModel

class GamesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamesViewModel::class.java)) {
            val appContext = context.applicationContext
            val repository = LocalGameRepository(appContext)
            return GamesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
