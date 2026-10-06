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
package com.tatumgames.tatumtech.android.ui.components.screens.networking.models

import com.tatumgames.tatumtech.android.database.entity.ContactCardEntity
import java.security.MessageDigest

/**
 * Encodes / parses Tatum Tech Card data as **vCard 3.0** for universal QR interoperability
 * (stock Android Camera, iPhone Camera, and the in-app scanner).
 *
 * Generation omits internal app state except [ContactCardEntity.cardId] as the standard
 * `UID` property (required for duplicate connection detection and own-card checks).
 *
 * [NOTE] (description) is capped to keep QR density practical for dense cards.
 */
object ContactCardVCardCodec {

    private const val MAX_NOTE_CHARS = 280
    private val CRLF = "\r\n"

    fun isVCard(raw: String): Boolean {
        val trimmed = raw.trimStart()
        return trimmed.startsWith("BEGIN:VCARD", ignoreCase = true)
    }

    /**
     * Builds a vCard 3.0 string suitable for QR encoding.
     * Returns null when there is no display name to share (incomplete card).
     *
     * @param firstName Profile first name when available (for structured `N`); not username.
     * @param lastName Profile last name when available.
     */
    fun encode(
        card: ContactCardEntity,
        firstName: String? = null,
        lastName: String? = null
    ): String? {
        val lines = mutableListOf<String>()
        lines += "BEGIN:VCARD"
        lines += "VERSION:3.0"

        val displayName = ContactCardQrCodec.blankToNull(card.name)
            ?: listOf(firstName, lastName)
                .mapNotNull { ContactCardQrCodec.blankToNull(it) }
                .joinToString(" ")
                .ifBlank { null }
        if (displayName.isNullOrBlank()) return null

        lines += "FN:${escape(displayName)}"

        val given = ContactCardQrCodec.blankToNull(firstName)
        val family = ContactCardQrCodec.blankToNull(lastName)
        when {
            given != null || family != null ->
                lines += "N:${escape(family.orEmpty())};${escape(given.orEmpty())};;;"
            else ->
                // Unstructured legacy display name — put in Given Name; do not invent a split.
                lines += "N:;${escape(displayName)};;;"
        }

        ContactCardQrCodec.blankToNull(card.jobTitle)?.let {
            lines += "TITLE:${escape(it)}"
        }
        ContactCardQrCodec.blankToNull(card.company)?.let {
            lines += "ORG:${escape(it)}"
        }
        ContactCardQrCodec.blankToNull(card.phone)?.let {
            lines += "TEL;TYPE=CELL:${escape(it)}"
        }
        ContactCardQrCodec.blankToNull(card.email)?.let {
            lines += "EMAIL;TYPE=INTERNET:${escape(it)}"
        }
        ContactCardQrCodec.blankToNull(card.alternateEmail)?.let {
            lines += "EMAIL;TYPE=INTERNET:${escape(it)}"
        }

        // URLs in stable order for round-trip classification by host / sequence.
        ContactCardQrCodec.blankToNull(card.website)?.let { lines += "URL:${escape(it)}" }
        ContactCardQrCodec.blankToNull(card.linkedin)?.let { lines += "URL:${escape(it)}" }
        ContactCardQrCodec.blankToNull(card.twitter)?.let { lines += "URL:${escape(it)}" }
        ContactCardQrCodec.blankToNull(card.customLink)?.let { lines += "URL:${escape(it)}" }
        ContactCardQrCodec.blankToNull(card.calendly)?.let { lines += "URL:${escape(it)}" }

        ContactCardQrCodec.blankToNull(card.description)?.let { desc ->
            val clipped = if (desc.length <= MAX_NOTE_CHARS) {
                desc
            } else {
                desc.take(MAX_NOTE_CHARS - 1).trimEnd() + "…"
            }
            lines += "NOTE:${escape(clipped)}"
        }

        // Standard UID — preserves Tatum Tech card identity without custom JSON.
        lines += "UID:${escape(card.cardId)}"

        lines += "END:VCARD"
        return foldLines(lines).joinToString(CRLF) + CRLF
    }

    /**
     * Parses a vCard 3.0 (or compatible) payload into [ContactCardQrPayload] for the existing
     * scan → preview → connection pipeline.
     */
    fun parse(raw: String): ContactCardQrParseResult {
        if (!isVCard(raw)) return ContactCardQrParseResult.Invalid
        return try {
            val props = parseProperties(raw)
            if (props.isEmpty()) return ContactCardQrParseResult.Invalid

            val fn = firstValue(props, "FN")
            val nParts = firstValue(props, "N")?.split(";", limit = 5)
            val family = nParts?.getOrNull(0)?.takeIf { it.isNotBlank() }
            val given = nParts?.getOrNull(1)?.takeIf { it.isNotBlank() }
            val displayName = when {
                !fn.isNullOrBlank() -> fn
                given != null || family != null ->
                    listOfNotNull(given, family).joinToString(" ")
                else -> null
            }
            if (displayName.isNullOrBlank()) return ContactCardQrParseResult.Invalid

            val emails = allValues(props, "EMAIL")
            val urls = allValues(props, "URL")
            val classified = classifyUrls(urls)

            val uid = firstValue(props, "UID")
            val cardId = when {
                !uid.isNullOrBlank() -> uid
                else -> syntheticCardId(displayName, emails.firstOrNull(), firstTel(props))
            }

            ContactCardQrParseResult.Success(
                ContactCardQrPayload(
                    cardId = cardId,
                    userId = 0L,
                    anonymousId = null,
                    name = displayName,
                    jobTitle = firstValue(props, "TITLE"),
                    company = firstValue(props, "ORG"),
                    description = firstValue(props, "NOTE"),
                    email = emails.getOrNull(0),
                    phone = firstTel(props),
                    alternateEmail = emails.getOrNull(1),
                    website = classified.website,
                    linkedin = classified.linkedin,
                    twitter = classified.twitter,
                    customLink = classified.customLink,
                    calendly = classified.calendly
                )
            )
        } catch (_: Exception) {
            ContactCardQrParseResult.Invalid
        }
    }

    // --- escaping / folding -------------------------------------------------

    internal fun escape(value: String): String = buildString(value.length) {
        value.forEach { ch ->
            when (ch) {
                '\\' -> append("\\\\")
                ';' -> append("\\;")
                ',' -> append("\\,")
                '\n' -> append("\\n")
                '\r' -> Unit
                else -> append(ch)
            }
        }
    }

    internal fun unescape(value: String): String {
        val out = StringBuilder(value.length)
        var i = 0
        while (i < value.length) {
            val ch = value[i]
            if (ch == '\\' && i + 1 < value.length) {
                when (val next = value[i + 1]) {
                    'n', 'N' -> out.append('\n')
                    '\\', ';', ',' -> out.append(next)
                    else -> {
                        out.append(ch)
                        out.append(next)
                    }
                }
                i += 2
            } else {
                out.append(ch)
                i++
            }
        }
        return out.toString()
    }

    /**
     * RFC 2425 line folding: soft-break at 75 octets with CRLF + space continuation.
     */
    internal fun foldLines(lines: List<String>): List<String> {
        val folded = mutableListOf<String>()
        for (line in lines) {
            if (line.toByteArray(Charsets.UTF_8).size <= 75) {
                folded += line
                continue
            }
            var remaining = line
            var first = true
            while (remaining.isNotEmpty()) {
                val budget = if (first) 75 else 74 // continuation lines start with a space
                var cut = remaining.length
                while (cut > 0 && remaining.take(cut).toByteArray(Charsets.UTF_8).size > budget) {
                    cut--
                }
                if (cut <= 0) cut = 1
                val chunk = remaining.take(cut)
                folded += if (first) chunk else " $chunk"
                remaining = remaining.drop(cut)
                first = false
            }
        }
        return folded
    }

    // --- parse helpers ------------------------------------------------------

    private data class Prop(val name: String, val params: String, val value: String)

    private fun parseProperties(raw: String): List<Prop> {
        val unfolded = unfold(raw)
        val props = mutableListOf<Prop>()
        for (line in unfolded) {
            if (line.isBlank()) continue
            val upper = line.uppercase()
            if (upper == "BEGIN:VCARD" || upper == "END:VCARD") continue
            if (upper.startsWith("VERSION:")) continue
            val colon = line.indexOf(':')
            if (colon <= 0) continue
            val left = line.substring(0, colon)
            val value = unescape(line.substring(colon + 1).trim())
            val semi = left.indexOf(';')
            val name = (if (semi >= 0) left.substring(0, semi) else left).uppercase()
            val params = if (semi >= 0) left.substring(semi + 1) else ""
            // Ignore group prefixes like item1.URL
            val bareName = name.substringAfterLast('.')
            props += Prop(bareName, params, value)
        }
        return props
    }

    private fun unfold(raw: String): List<String> {
        val normalized = raw.replace("\r\n", "\n").replace('\r', '\n')
        val out = mutableListOf<String>()
        for (line in normalized.lines()) {
            if (line.startsWith(" ") || line.startsWith("\t")) {
                if (out.isNotEmpty()) {
                    out[out.lastIndex] = out.last() + line.drop(1)
                }
            } else {
                out += line
            }
        }
        return out
    }

    private fun firstValue(props: List<Prop>, name: String): String? =
        props.firstOrNull { it.name.equals(name, ignoreCase = true) && it.value.isNotBlank() }
            ?.value

    private fun allValues(props: List<Prop>, name: String): List<String> =
        props.filter { it.name.equals(name, ignoreCase = true) && it.value.isNotBlank() }
            .map { it.value }

    private fun firstTel(props: List<Prop>): String? = firstValue(props, "TEL")

    private data class ClassifiedUrls(
        val website: String?,
        val linkedin: String?,
        val twitter: String?,
        val customLink: String?,
        val calendly: String?
    )

    private fun classifyUrls(urls: List<String>): ClassifiedUrls {
        var website: String? = null
        var linkedin: String? = null
        var twitter: String? = null
        var customLink: String? = null
        var calendly: String? = null
        val leftovers = mutableListOf<String>()

        for (url in urls) {
            val lower = url.lowercase()
            when {
                linkedin == null && "linkedin.com" in lower -> linkedin = url
                twitter == null && (
                    "twitter.com" in lower ||
                        Regex("""(^|[/.])x\.com([/:?]|$)""").containsMatchIn(lower)
                    ) -> twitter = url
                calendly == null && "calendly.com" in lower -> calendly = url
                else -> leftovers += url
            }
        }
        if (leftovers.isNotEmpty()) website = leftovers[0]
        if (leftovers.size > 1) customLink = leftovers[1]
        // Extra leftovers beyond website+custom are dropped to keep model 1:1 with Card fields.
        return ClassifiedUrls(website, linkedin, twitter, customLink, calendly)
    }

    /**
     * Local-only fallback when a third-party vCard has no UID.
     * Deterministic so re-scans update the same connection row.
     */
    private fun syntheticCardId(name: String, email: String?, phone: String?): String {
        val material = listOf(name.trim().lowercase(), email?.trim()?.lowercase().orEmpty(), phone?.trim().orEmpty())
            .joinToString("|")
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(material.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
            .take(32)
        return "vcard:$digest"
    }
}
