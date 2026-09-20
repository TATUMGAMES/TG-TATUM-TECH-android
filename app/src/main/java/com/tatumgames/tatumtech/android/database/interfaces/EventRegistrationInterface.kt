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
package com.tatumgames.tatumtech.android.database.interfaces

/**
 * Interface defining event registration-related database operations.
 * 
 * Provides a contract for event registration management operations including
 * registration, unregistration, and retrieval of registered events.
 */
interface EventRegistrationInterface {

    /**
     * Get all registered event IDs.
     * 
     * @return List of registered event IDs.
     */
    suspend fun getRegisteredEvents(): List<Long>

    /**
     * Register for an event.
     * 
     * @param id The ID of the event to register for.
     */
    suspend fun registerEvent(id: Long)

    /**
     * Unregister from an event.
     * 
     * @param id The ID of the event to unregister from.
     */
    suspend fun unregisterEvent(id: Long)
}
