package com.tatumgames.tatumtech.android.data.games

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.tatumgames.tatumtech.android.ui.models.GameModel
import com.tatumgames.tatumtech.android.ui.models.GamesCatalogResponse
import java.io.IOException

/**
 * Loads [GameModel] list from assets/games.json (GamesCatalogResponse shape).
 */
class LocalGameRepository(
    private val context: Context,
    private val gson: Gson = Gson()
) : GameRepository {

    override fun loadCatalog(): Result<List<GameModel>> {
        return try {
            val json = context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
            val response = gson.fromJson(json, GamesCatalogResponse::class.java)
            val apps = response?.data?.apps
            if (apps == null) {
                Result.failure(IllegalStateException("Invalid catalog: missing data.apps"))
            } else {
                Result.success(apps)
            }
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: JsonSyntaxException) {
            Result.failure(e)
        } catch (e: RuntimeException) {
            // Gson can throw various parsing errors
            Result.failure(e)
        }
    }

    companion object {
        const val ASSET_NAME = "games.json"
    }
}
