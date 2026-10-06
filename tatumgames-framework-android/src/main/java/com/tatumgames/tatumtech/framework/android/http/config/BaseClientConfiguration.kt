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
package com.tatumgames.tatumtech.framework.android.http.config

/**
 * HTTP configuration shared by every API client built on the framework.
 *
 * Every property is optional except [debugMode]; an application only supplies what its API needs.
 * Application-specific configurations extend this class (usually via [CommonClientConfiguration]).
 */
abstract class BaseClientConfiguration {

    /**
     * Key sent with every request when present; the header name is chosen by the API client.
     */
    abstract val apiKey: String?

    /**
     * Access token, always normalized to the `Bearer <token>` form, or `null`.
     */
    abstract val jwtAccessToken: String?

    /**
     * Absolute `http`/`https` base URL without a trailing slash, or `null`.
     */
    abstract val baseUrl: String?

    /**
     * Connect timeout in milliseconds; `null` uses the executor default.
     */
    abstract val connectTimeout: Long?

    /**
     * Refresh token when session is invalidated.
     */
    abstract val refreshToken: String?

    /**
     * Epoch milliseconds at which [jwtAccessToken] expires, or `null` when unknown.
     */
    abstract val tokenExpiration: Long?

    /**
     * Development diagnostics only. Never use it to select a data source.
     */
    abstract val debugMode: Boolean
}
