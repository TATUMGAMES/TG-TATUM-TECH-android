/**
 * Copyright 2013-present Tatum Games, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tatumgames.tatumtech.framework.android.auth

import android.content.Context
import com.google.firebase.FirebaseApp
import com.tatumgames.tatumtech.framework.android.auth.FirebaseInitializerMessages.FIREBASE_ALREADY_INITIALIZED
import com.tatumgames.tatumtech.framework.android.auth.FirebaseInitializerMessages.FIREBASE_FAILED_TO_INITIALIZE
import com.tatumgames.tatumtech.framework.android.auth.FirebaseInitializerMessages.FIREBASE_INITIALIZED_SUCCESSFULLY
import com.tatumgames.tatumtech.framework.android.auth.FirebaseInitializerMessages.TAG
import com.tatumgames.tatumtech.framework.android.logger.Logger

object FirebaseInitializerMessages {
    internal const val TAG = "FirebaseInitializer"
    internal const val FIREBASE_INITIALIZED_SUCCESSFULLY = "Firebase initialized successfully"
    internal const val FIREBASE_FAILED_TO_INITIALIZE = "Failed to initialize Firebase"
    internal const val FIREBASE_ALREADY_INITIALIZED = "Firebase already initialized"
}

/**
 * Handles Firebase initialization for the framework layer.
 * This ensures Firebase is properly initialized before any authentication operations.
 */
object FirebaseInitializer {

    private var isInitialized = false
    
    /**
     * Initializes Firebase if not already initialized.
     * This should be called early in the application lifecycle.
     * 
     * @param context The application context.
     */
    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                FirebaseApp.initializeApp(context)
                isInitialized = true
                Logger.d(TAG, FIREBASE_INITIALIZED_SUCCESSFULLY)
            } catch (e: Exception) {
                Logger.e(TAG, FIREBASE_FAILED_TO_INITIALIZE, e)
                GoogleAuthError.FirebaseInitializationFailed
                throw e
            }
        } else {
            Logger.d(TAG, FIREBASE_ALREADY_INITIALIZED)
        }
    }
    
    /**
     * Checks if Firebase is initialized.
     * 
     * @return true if Firebase is initialized, false otherwise.
     */
    fun isInitialized(): Boolean {
        return isInitialized
    }
} 
