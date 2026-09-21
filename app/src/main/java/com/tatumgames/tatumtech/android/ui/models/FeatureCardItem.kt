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
package com.tatumgames.tatumtech.android.ui.models

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

/**
 * Data class representing a feature card item for the home pager.
 * Used to display FeatureCard components in the horizontal pager.
 *
 * @param imageResId Drawable resource ID for the card icon/image
 * @param titleResId String resource ID for the card title (resolved in the UI layer)
 * @param route Navigation route when the card is clicked
 */
data class FeatureCardItem(
    @DrawableRes val imageResId: Int,
    @StringRes val titleResId: Int,
    val route: String
)
