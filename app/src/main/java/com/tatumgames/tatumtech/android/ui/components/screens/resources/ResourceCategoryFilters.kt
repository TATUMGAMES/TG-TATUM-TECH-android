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
package com.tatumgames.tatumtech.android.ui.components.screens.resources

import com.tatumgames.tatumtech.android.ui.models.ResourceLink

/**
 * Technology filter chips for Coding → Resources.
 * Aligned with coding quiz languages plus task-specified C++ / PHP / AI/ML.
 */
object ResourceCategoryFilters {

    const val FILTER_ALL = "All"

    val categories: List<String> = listOf(
        FILTER_ALL,
        "Java",
        "Kotlin",
        "C++",
        "C#",
        "Python",
        "AI/ML",
        "JavaScript",
        "PHP"
    )

    fun matches(resource: ResourceLink, selectedCategory: String): Boolean {
        if (selectedCategory == FILTER_ALL) return true
        return resource.categories.any { it.equals(selectedCategory, ignoreCase = true) }
    }

    fun filter(resources: List<ResourceLink>, selectedCategory: String): List<ResourceLink> =
        resources.filter { matches(it, selectedCategory) }
}
