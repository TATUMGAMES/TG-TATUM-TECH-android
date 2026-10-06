package com.tatumgames.tatumtech.android.ui.viewmodels.factory

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.tatumgames.tatumtech.android.database.AppDatabase
import com.tatumgames.tatumtech.android.database.repository.RecentNotificationDatabaseRepository
import com.tatumgames.tatumtech.android.database.repository.UserDatabaseRepository
import com.tatumgames.tatumtech.android.ui.viewmodels.HomePagerViewModel

class HomePagerViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomePagerViewModel::class.java)) {
            val app = context.applicationContext as Application
            val db = AppDatabase.getInstance(app)
            val userRepository = UserDatabaseRepository(db.userDao())
            val notificationRepository = RecentNotificationDatabaseRepository(
                notificationDao = db.recentNotificationDao(),
                codingQuestionDao = db.codingQuestionDao()
            )
            return HomePagerViewModel(app, userRepository, notificationRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
