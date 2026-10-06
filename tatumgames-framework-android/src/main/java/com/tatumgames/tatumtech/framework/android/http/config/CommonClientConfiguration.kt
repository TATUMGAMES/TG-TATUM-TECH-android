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

import java.net.URI

/**
 * Immutable, validated [BaseClientConfiguration] produced by a [ConfigurationBuilder].
 *
 * Applications that need extra settings subclass this and their own builder:
 * ```
 * class MyConfiguration private constructor(values: Values) : CommonClientConfiguration(values) {
 *     class Builder : ConfigurationBuilder<Builder, MyConfiguration>() {
 *         override fun self() = this
 *         override fun build() = MyConfiguration(values())
 *     }
 * }
 * ```
 */
open class CommonClientConfiguration protected constructor(
    values: Values
) : BaseClientConfiguration() {

    final override val apiKey: String? = values.apiKey
    final override val jwtAccessToken: String? = values.jwtAccessToken
    final override val baseUrl: String? = values.baseUrl
    final override val connectTimeout: Long? = values.connectTimeout
    final override val refreshToken: String? = values.refreshToken
    final override val tokenExpiration: Long? = values.tokenExpiration
    final override val debugMode: Boolean = values.debugMode

    /**
     * Secrets are redacted so configurations are safe to log.
     */
    override fun toString(): String =
        "${this::class.simpleName}(baseUrl=$baseUrl, connectTimeout=$connectTimeout, " +
            "apiKey=${redact(apiKey)}, jwtAccessToken=${redact(jwtAccessToken)}, " +
            "refreshToken=${redact(refreshToken)}, tokenExpiration=$tokenExpiration, " +
            "debugMode=$debugMode)"

    /**
     * Normalized values handed from a builder to a configuration constructor.
     */
    class Values internal constructor(
        val apiKey: String?,
        val jwtAccessToken: String?,
        val baseUrl: String?,
        val connectTimeout: Long?,
        val refreshToken: String?,
        val tokenExpiration: Long?,
        val debugMode: Boolean
    )

    /**
     * Builder for applications that need no settings beyond the common ones.
     */
    class Builder : ConfigurationBuilder<Builder, CommonClientConfiguration>() {
        override fun self(): Builder = this
        override fun build(): CommonClientConfiguration = CommonClientConfiguration(values())
    }

    private fun redact(secret: String?): String = if (secret == null) "null" else "<redacted>"
}

/**
 * Self-typed builder so subclasses keep their own type through chained setter calls.
 *
 * @param B The concrete builder type.
 * @param C The configuration type produced by [build].
 */
abstract class ConfigurationBuilder<B : ConfigurationBuilder<B, C>, C : BaseClientConfiguration> {

    private var apiKey: String? = null
    private var jwtAccessToken: String? = null
    private var baseUrl: String? = null
    private var connectTimeout: Long? = null
    private var refreshToken: String? = null
    private var tokenExpiration: Long? = null
    private var debugMode: Boolean = false

    protected abstract fun self(): B

    abstract fun build(): C

    /**
     * Blank values are treated as absent.
     */
    fun setApiKey(apiKey: String?): B {
        this.apiKey = apiKey.trimToNull()
        return self()
    }

    /**
     * Accepts a raw token or one already prefixed with `Bearer` (any case).
     */
    fun setJwtAccessToken(token: String?): B {
        jwtAccessToken = BearerToken.normalize(token)
        return self()
    }

    /**
     * @throws IllegalArgumentException if [baseUrl] is not an absolute `http`/`https` URL.
     */
    fun setBaseUrl(baseUrl: String?): B {
        this.baseUrl = BaseUrl.normalize(baseUrl)
        return self()
    }

    /**
     * @param timeoutMillis Connect timeout in milliseconds; `null` restores the executor default.
     * @throws IllegalArgumentException if [timeoutMillis] is zero or negative.
     */
    fun setHttpClientTimeout(timeoutMillis: Long?): B {
        require(timeoutMillis == null || timeoutMillis > 0) {
            "HTTP client timeout must be positive, was $timeoutMillis"
        }
        connectTimeout = timeoutMillis
        return self()
    }

    fun setRefreshToken(refreshToken: String?): B {
        this.refreshToken = refreshToken.trimToNull()
        return self()
    }

    /**
     * @param epochMillis Time at which the access token expires.
     */
    fun setTokenExpiration(epochMillis: Long?): B {
        tokenExpiration = epochMillis
        return self()
    }

    fun setDebugMode(debugMode: Boolean): B {
        this.debugMode = debugMode
        return self()
    }

    /**
     * Copies every common value from [configuration], e.g. to rebuild it with a new token.
     */
    fun from(configuration: BaseClientConfiguration): B {
        apiKey = configuration.apiKey
        jwtAccessToken = configuration.jwtAccessToken
        baseUrl = configuration.baseUrl
        connectTimeout = configuration.connectTimeout
        refreshToken = configuration.refreshToken
        tokenExpiration = configuration.tokenExpiration
        debugMode = configuration.debugMode
        return self()
    }

    protected fun values(): CommonClientConfiguration.Values = CommonClientConfiguration.Values(
        apiKey = apiKey,
        jwtAccessToken = jwtAccessToken,
        baseUrl = baseUrl,
        connectTimeout = connectTimeout,
        refreshToken = refreshToken,
        tokenExpiration = tokenExpiration,
        debugMode = debugMode
    )
}

/**
 * Normalizes access tokens to a single `Bearer <token>` form.
 */
object BearerToken {
    const val PREFIX = "Bearer "

    /**
     * Returns `Bearer <token>`, or `null` when [token] is blank or only a prefix.
     */
    fun normalize(token: String?): String? = strip(token)?.let { PREFIX + it }

    /**
     * Returns the bare token without any `Bearer` prefix, or `null` when blank.
     */
    fun strip(token: String?): String? {
        val trimmed = token.trimToNull() ?: return null
        return trimmed.replaceFirst(PREFIX_PATTERN, "").trimToNull()
    }

    private val PREFIX_PATTERN = Regex("^bearer(\\s+|$)", RegexOption.IGNORE_CASE)
}

internal object BaseUrl {
    fun normalize(url: String?): String? {
        val trimmed = url.trimToNull()?.trimEnd('/') ?: return null
        val uri = try {
            URI(trimmed)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid base URL: $trimmed", e)
        }
        val scheme = uri.scheme?.lowercase()
        require((scheme == "http" || scheme == "https") && !uri.host.isNullOrBlank()) {
            "Base URL must be an absolute http(s) URL: $trimmed"
        }
        require(uri.rawQuery == null && uri.rawFragment == null) {
            "Base URL must not contain a query or fragment: $trimmed"
        }
        return trimmed
    }
}

private fun String?.trimToNull(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
