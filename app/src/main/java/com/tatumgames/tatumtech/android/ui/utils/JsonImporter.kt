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
package com.tatumgames.tatumtech.android.ui.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tatumgames.tatumtech.android.ui.components.screens.events.models.Event
import com.tatumgames.tatumtech.android.ui.models.Achievement
import com.tatumgames.tatumtech.android.ui.models.CareerListing
import com.tatumgames.tatumtech.android.ui.models.GameModel
import com.tatumgames.tatumtech.android.ui.models.GamesResourceCategory
import com.tatumgames.tatumtech.android.ui.models.NotificationItem
import com.tatumgames.tatumtech.android.ui.models.Partner
import com.tatumgames.tatumtech.android.ui.models.ResourceLink
import java.io.IOException

/**
 * Utility class for importing JSON data from the app's assets folder.
 * 
 * This class provides a centralized way to load various data models from local JSON files
 * stored in the assets directory. It handles JSON parsing, error handling, and type conversion
 * for different data models used throughout the Tatum Tech app.
 * 
 * @author Tatum Games, LLC
 * @since 1.0.0
 */
object JsonImporter {

    /**
     * Gson instance for JSON parsing operations.
     * Configured with default settings for optimal performance.
     */
    private val gson = Gson()

    /**
     * Loads partners data from assets/partners.json
     * 
     * @param context Android context
     * @return List of Partner objects
     */
    fun loadPartners(context: Context): List<Partner> {
        return try {
            val json = context.assets.open("partners.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Partner>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: IOException) {
            emptyList()
        }
    }

    /**
     * Loads career listings data from assets/career_listings.json
     * 
     * @param context Android context
     * @return List of CareerListing objects
     */
    fun loadCareerListings(context: Context): List<CareerListing> {
        return try {
            val json =
                context.assets.open("career_listings.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<CareerListing>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: IOException) {
            emptyList()
        }
    }

    /**
     * Loads resources data from assets/resources.json
     * 
     * @param context Android context
     * @return List of ResourceLink objects
     */
    fun loadResources(context: Context): List<ResourceLink> {
        return try {
            val json = context.assets.open("resources.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<ResourceLink>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: IOException) {
            emptyList()
        }
    }

    /**
     * Loads notifications data from assets/mock_notifications.json
     * 
     * @param context Android context
     * @return List of NotificationItem objects
     */
    fun loadNotifications(context: Context): List<NotificationItem> {
        return try {
            val json = context.assets.open("mock_notifications.json").bufferedReader()
                .use { it.readText() }
            val listType = object : TypeToken<List<NotificationItem>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: IOException) {
            emptyList()
        }
    }

    /**
     * Loads achievements data from assets/achievements.json
     * 
     * @param context Android context
     * @return List of Achievement objects
     */
    fun loadAchievements(context: Context): List<Achievement> {
        return try {
            val json =
                context.assets.open("achievements.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Achievement>>() {}.type
            gson.fromJson(json, listType)
        } catch (e: IOException) {
            emptyList()
        }
    }

    /**
     * Loads upcoming events from assets/upcoming_events.json (API-shaped local catalog).
     */
    fun loadUpcomingEvents(context: Context): List<Event> {
        return try {
            val json =
                context.assets.open("upcoming_events.json").bufferedReader().use { it.readText() }
            val listType = object : TypeToken<List<Event>>() {}.type
            gson.fromJson<List<Event>>(json, listType).orEmpty()
        } catch (e: IOException) {
            emptyList()
        }
    }

    fun loadUpcomingEventById(context: Context, eventId: Long): Event? =
        loadUpcomingEvents(context).firstOrNull { it.id == eventId }

    /**
     * Loads Games → Resources category → partnerId mappings from assets/games_resources.json.
     */
    fun loadGamesResourceCategories(context: Context): List<GamesResourceCategory> {
        return try {
            val json = context.assets.open("games_resources.json").bufferedReader()
                .use { it.readText() }
            val listType = object : TypeToken<List<GamesResourceCategory>>() {}.type
            gson.fromJson<List<GamesResourceCategory>>(json, listType).orEmpty()
        } catch (e: IOException) {
            emptyList()
        }
    }

    /**
     * Loads games data from assets/games.json via [com.tatumgames.tatumtech.android.data.games.LocalGameRepository].
     *
     * @param context Android context
     * @return List of GameModel objects
     */
    fun loadGames(context: Context): List<GameModel> {
        return com.tatumgames.tatumtech.android.data.games.LocalGameRepository(context)
            .loadCatalog()
            .getOrElse { emptyList() }
    }
}
