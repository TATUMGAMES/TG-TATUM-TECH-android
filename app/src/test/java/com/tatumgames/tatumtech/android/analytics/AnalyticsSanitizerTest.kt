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
package com.tatumgames.tatumtech.android.analytics

import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException

class AnalyticsSanitizerTest {

    @Test
    fun screenNameFromRoute_stripsArgsAndNormalizes() {
        assertEquals("home_pager", AnalyticsSanitizer.screenNameFromRoute("home_pager"))
        assertEquals(
            "game_details_screen",
            AnalyticsSanitizer.screenNameFromRoute("game_details_screen/abc123")
        )
        assertEquals(
            "about_screen",
            AnalyticsSanitizer.screenNameFromRoute("about_screen/about")
        )
        assertEquals("unknown", AnalyticsSanitizer.screenNameFromRoute(null))
        assertEquals("unknown", AnalyticsSanitizer.screenNameFromRoute("  "))
    }

    @Test
    fun sanitizeEndpoint_removesInviteCodeAndQuery() {
        assertEquals(
            "/api/v9/invites",
            AnalyticsSanitizer.sanitizeEndpoint(
                "https://discord.com/api/v9/invites/6FzqSUDRXQ?with_counts=true"
            )
        )
    }

    @Test
    fun classifyThrowable_mapsNetworkFailures() {
        assertEquals(ApiErrorTypes.TIMEOUT, AnalyticsSanitizer.classifyThrowable(SocketTimeoutException()))
        assertEquals(ApiErrorTypes.CONNECTION, AnalyticsSanitizer.classifyThrowable(ConnectException()))
    }
}
