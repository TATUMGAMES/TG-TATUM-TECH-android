package com.tatumgames.tatumtech.android.data.games

import com.tatumgames.tatumtech.android.ui.models.GameModel

/**
 * Loads the games catalog. Swap implementation for a remote API later.
 */
interface GameRepository {
    fun loadCatalog(): Result<List<GameModel>>
}
