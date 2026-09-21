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
package com.tatumgames.tatumtech.android.assets

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tatumgames.tatumtech.android.ui.components.screens.resources.ResourceCategoryFilters
import com.tatumgames.tatumtech.android.ui.models.ResourceLink
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ResourcesJsonValidationTest {

    private fun loadResources(): List<ResourceLink> {
        val json = File("src/main/assets/resources.json").readText()
        val type = object : TypeToken<List<ResourceLink>>() {}.type
        return Gson().fromJson(json, type)
    }

    @Test
    fun resourcesJson_parsesWithoutDuplicates() {
        val resources = loadResources()
        assertTrue(resources.size >= 10)
        assertEquals(resources.size, resources.map { it.id }.toSet().size)
        assertEquals(resources.size, resources.map { it.url }.toSet().size)
        assertTrue(resources.all { it.url.startsWith("https://") })
        assertTrue(resources.all { it.categories.isNotEmpty() })
    }

    @Test
    fun filterAll_returnsEverything() {
        val resources = loadResources()
        assertEquals(
            resources.size,
            ResourceCategoryFilters.filter(resources, ResourceCategoryFilters.FILTER_ALL).size
        )
    }

    @Test
    fun filterByCategory_usesStructuredCategories() {
        val resources = loadResources()
        ResourceCategoryFilters.categories
            .filter { it != ResourceCategoryFilters.FILTER_ALL }
            .forEach { category ->
                val filtered = ResourceCategoryFilters.filter(resources, category)
                assertTrue("$category should have resources", filtered.isNotEmpty())
                assertTrue(filtered.all { it.categories.any { c -> c.equals(category, true) } })
            }
    }

    @Test
    fun multiCategoryResource_appearsUnderEachFilter() {
        val resources = loadResources()
        val pytorch = resources.first { it.id == "aiml_pytorch_tutorials" }
        assertTrue(pytorch.categories.containsAll(listOf("AI/ML", "Python")))
        assertTrue(ResourceCategoryFilters.matches(pytorch, "Python"))
        assertTrue(ResourceCategoryFilters.matches(pytorch, "AI/ML"))
        assertFalse(ResourceCategoryFilters.matches(pytorch, "Java"))
    }

    @Test
    fun emptyCategoryFilter_detectedForSyntheticData() {
        val resources = loadResources()
        val filtered = ResourceCategoryFilters.filter(resources, "COBOL")
        assertTrue(filtered.isEmpty())
    }
}
