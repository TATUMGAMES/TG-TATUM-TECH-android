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
package com.tatumgames.tatumtech.android.ui.components.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.tatumgames.tatumtech.android.R
import com.tatumgames.tatumtech.android.ui.components.common.BottomNavigationBar
import com.tatumgames.tatumtech.android.ui.components.common.Header
import com.tatumgames.tatumtech.android.ui.components.common.StandardText
import com.tatumgames.tatumtech.android.ui.components.screens.partners.PartnerCategoryFilters
import com.tatumgames.tatumtech.android.ui.models.Partner
import com.tatumgames.tatumtech.android.ui.theme.PartnerContactBlue
import com.tatumgames.tatumtech.android.ui.theme.PartnerDonationTeal
import com.tatumgames.tatumtech.android.ui.theme.PartnerFeaturedAccent
import com.tatumgames.tatumtech.android.ui.theme.PartnerSocialMediaPurple
import com.tatumgames.tatumtech.android.ui.theme.Purple200
import com.tatumgames.tatumtech.android.ui.theme.ScreenScaffoldLight
import com.tatumgames.tatumtech.android.ui.theme.White
import com.tatumgames.tatumtech.android.ui.utils.GameMediaResolver
import com.tatumgames.tatumtech.android.ui.utils.JsonImporter

private const val CONTACT_EMAIL_SUBJECT =
    "Got Your Contact Info From Tatum Games. I Have Some Questions"

@Composable
fun PartnersScreen(navController: NavController) {
    val context = LocalContext.current
    var partners by remember { mutableStateOf<List<Partner>>(emptyList()) }
    var loadFinished by remember { mutableStateOf(false) }
    var selectedChip by remember { mutableStateOf(PartnerCategoryFilters.FILTER_ALL) }
    var detailPartner by remember { mutableStateOf<Partner?>(null) }
    var contactPickerPartner by remember { mutableStateOf<Partner?>(null) }

    LaunchedEffect(Unit) {
        partners = JsonImporter.loadPartners(context)
        loadFinished = true
    }

    val filtered = remember(partners, selectedChip) {
        PartnerCategoryFilters.filter(partners, selectedChip)
    }

    Scaffold(
        topBar = {
            Header(
                text = stringResource(R.string.title_partners),
                onBackClick = { navController.popBackStack() }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController = navController)
        },
        containerColor = ScreenScaffoldLight
    ) { paddingValues ->
        when {
            !loadFinished -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = stringResource(R.string.partners_loading),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            partners.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    StandardText(
                        text = stringResource(R.string.partners_empty),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "chips") {
                        PartnerCategoryChipRow(
                            selectedChip = selectedChip,
                            onSelected = { selectedChip = it }
                        )
                    }

                    if (filtered.isEmpty()) {
                        item(key = "empty_filter") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                StandardText(
                                    text = stringResource(R.string.partners_no_match),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(filtered, key = { it.id }) { partner ->
                            PartnerCard(
                                partner = partner,
                                modifier = Modifier.padding(horizontal = 16.dp),
                                onOpenDetails = { detailPartner = partner },
                                onContact = {
                                    val emails = partner.emails()
                                    when {
                                        emails.size > 1 -> contactPickerPartner = partner
                                        emails.size == 1 -> openPartnerEmail(
                                            context,
                                            emails.first().email.orEmpty()
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    detailPartner?.let { partner ->
        PartnerDetailDialog(
            partner = partner,
            onDismiss = { detailPartner = null },
            onContact = {
                val emails = partner.emails()
                when {
                    emails.size > 1 -> {
                        detailPartner = null
                        contactPickerPartner = partner
                    }

                    emails.size == 1 -> openPartnerEmail(context, emails.first().email.orEmpty())
                }
            }
        )
    }

    contactPickerPartner?.let { partner ->
        ContactPickerDialog(
            partner = partner,
            onDismiss = { contactPickerPartner = null },
            onSelect = { email ->
                contactPickerPartner = null
                openPartnerEmail(context, email)
            }
        )
    }
}

@Composable
private fun PartnerCategoryChipRow(
    selectedChip: String,
    onSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PartnerCategoryFilters.chips.forEach { chip ->
                FilterChip(
                    selected = selectedChip == chip.filterKey,
                    onClick = { onSelected(chip.filterKey) },
                    label = {
                        StandardText(
                            text = if (chip.filterKey == PartnerCategoryFilters.FILTER_ALL) {
                                stringResource(R.string.filter_all)
                            } else {
                                chip.filterKey
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun PartnerCard(
    partner: Partner,
    onOpenDetails: () -> Unit,
    onContact: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val logoData = partner.logo?.let { GameMediaResolver.resolve(context, "drawable:$it") }
        ?: R.drawable.partners

    Card(
        onClick = onOpenDetails,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        border = if (partner.featured) {
            BorderStroke(2.dp, PartnerFeaturedAccent)
        } else {
            null
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(logoData).crossfade(200).build(),
                    contentDescription = stringResource(R.string.partners_logo_cd, partner.name),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.partners),
                    error = painterResource(R.drawable.partners)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    StandardText(
                        text = partner.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    StandardText(
                        text = PartnerCategoryFilters.shortLabelForCategory(partner.category),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    if (partner.featured) {
                        StandardText(
                            text = stringResource(R.string.partners_featured_badge),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = PartnerFeaturedAccent
                            )
                        )
                    }
                }
            }

            partner.description?.takeIf { it.isNotBlank() }?.let { desc ->
                StandardText(
                    text = desc,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            partner.productName?.takeIf { it.isNotBlank() }?.let { product ->
                StandardText(
                    text = stringResource(R.string.partners_product_label, product),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
            }

            PartnerPrimaryActions(
                partner = partner,
                onContact = onContact,
                compact = true
            )

            PartnerSocialIconRow(partner = partner)
        }
    }
}

@Composable
private fun PartnerPrimaryActions(
    partner: Partner,
    onContact: () -> Unit,
    compact: Boolean
) {
    val context = LocalContext.current
    val hasWebsite = !partner.websiteUrl.isNullOrBlank()
    val hasContact = partner.emails().isNotEmpty()
    val hasDonate = !partner.donationUrl.isNullOrBlank()
    val hasDownload = !partner.downloadUrl.isNullOrBlank()
    val hasProduct = !partner.productUrl.isNullOrBlank()
    val hasPhone = partner.contactList().any { !it.phone.isNullOrBlank() }

    if (!hasWebsite && !hasContact && !hasDonate && !hasDownload && !hasProduct && !hasPhone &&
        partner.linkList().isEmpty()
    ) {
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasWebsite) {
                PartnerActionButton(
                    label = stringResource(R.string.partners_visit_website),
                    containerColor = Purple200,
                    modifier = Modifier.weight(1f),
                    onClick = { openUrl(context, partner.websiteUrl) }
                )
            }
            if (hasContact) {
                PartnerActionButton(
                    label = stringResource(R.string.partners_contact),
                    containerColor = PartnerContactBlue,
                    modifier = Modifier.weight(1f),
                    onClick = onContact
                )
            }
        }

        if (hasDonate) {
            PartnerActionButton(
                label = stringResource(R.string.partners_donate),
                containerColor = PartnerDonationTeal,
                modifier = Modifier.fillMaxWidth(),
                onClick = { openUrl(context, partner.donationUrl) }
            )
        }

        if (hasDownload) {
            val label = partner.productName?.let {
                stringResource(R.string.partners_wishlist_named, it)
            } ?: stringResource(R.string.partners_wishlist)
            PartnerActionButton(
                label = label,
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.fillMaxWidth(),
                onClick = { openUrl(context, partner.downloadUrl) }
            )
        }

        if (hasProduct) {
            val label = partner.productName?.let {
                stringResource(R.string.partners_view_product, it)
            } ?: stringResource(R.string.partners_view_product_generic)
            OutlinedButton(
                onClick = { openUrl(context, partner.productUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                StandardText(text = label)
            }
        }

        if (!compact) {
            partner.linkList().forEach { link ->
                OutlinedButton(
                    onClick = { openUrl(context, link.url) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StandardText(text = link.label)
                }
            }
            partner.contactList().firstOrNull { !it.phone.isNullOrBlank() }?.phone?.let { phone ->
                PartnerActionButton(
                    label = stringResource(R.string.partners_call),
                    containerColor = PartnerContactBlue,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { dialPhone(context, phone) }
                )
            }
        } else {
            // Compact card: show at most one additional link label row via first additional link
            partner.linkList().take(1).forEach { link ->
                TextButton(onClick = { openUrl(context, link.url) }) {
                    StandardText(
                        text = link.label,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PartnerSocialMediaPurple
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun PartnerActionButton(
    label: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        StandardText(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = White
        )
    }
}

@Composable
private fun PartnerSocialIconRow(partner: Partner) {
    val context = LocalContext.current
    val social = partner.socialLinks ?: return
    val hasAny = listOf(
        social.x, social.linkedin, social.tiktok, social.instagram,
        social.meta, social.discord, social.youtube
    ).any { !it.isNullOrBlank() }
    if (!hasAny) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        social.x?.let { url ->
            SocialIconButton(R.drawable.social_media_x, R.string.partners_social_x, url)
        }
        social.linkedin?.let { url ->
            SocialIconButton(
                R.drawable.social_media_linkedin,
                R.string.partners_social_linkedin,
                url
            )
        }
        social.tiktok?.let { url ->
            SocialIconButton(R.drawable.social_media_tiktok, R.string.partners_social_tiktok, url)
        }
        social.instagram?.let { url ->
            SocialIconButton(
                R.drawable.social_media_instragram,
                R.string.partners_social_instagram,
                url
            )
        }
        social.meta?.let { url ->
            SocialIconButton(R.drawable.social_media_meta, R.string.partners_social_meta, url)
        }
        social.discord?.let { url ->
            SocialIconButton(R.drawable.social_media_discord, R.string.partners_social_discord, url)
        }
        social.youtube?.let { url ->
            TextButton(onClick = { openUrl(context, url) }) {
                StandardText(
                    text = stringResource(R.string.partners_social_youtube),
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun SocialIconButton(drawableRes: Int, labelRes: Int, url: String) {
    val context = LocalContext.current
    val label = stringResource(labelRes)
    IconButton(
        onClick = { openUrl(context, url) },
        modifier = Modifier
            .size(44.dp)
            .semantics { contentDescription = label }
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(drawableRes),
            contentDescription = label,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun PartnerDetailDialog(
    partner: Partner,
    onDismiss: () -> Unit,
    onContact: () -> Unit
) {
    val context = LocalContext.current
    val logoData = partner.logo?.let { GameMediaResolver.resolve(context, "drawable:$it") }
        ?: R.drawable.partners

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                StandardText(text = stringResource(R.string.close))
            }
        },
        title = {
            StandardText(
                text = partner.name,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context).data(logoData).crossfade(200).build(),
                    contentDescription = stringResource(R.string.partners_logo_cd, partner.name),
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                    error = painterResource(R.drawable.partners)
                )
                if (partner.featured) {
                    StandardText(
                        text = stringResource(R.string.partners_featured_badge),
                        color = PartnerFeaturedAccent,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                StandardText(
                    text = partner.category,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                partner.description?.let {
                    StandardText(text = it, style = MaterialTheme.typography.bodyMedium)
                }
                partner.contactList().forEach { contact ->
                    Column {
                        StandardText(
                            text = contact.name,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        contact.title?.let {
                            StandardText(
                                text = it,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        contact.email?.let {
                            StandardText(
                                text = it,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        contact.phone?.let {
                            StandardText(
                                text = it,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                PartnerPrimaryActions(
                    partner = partner,
                    onContact = onContact,
                    compact = false
                )
                PartnerSocialIconRow(partner = partner)
            }
        },
        containerColor = White
    )
}

@Composable
private fun ContactPickerDialog(
    partner: Partner,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            StandardText(
                text = stringResource(R.string.partners_contact_picker_title, partner.name),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                partner.emails().forEach { contact ->
                    OutlinedButton(
                        onClick = { onSelect(contact.email.orEmpty()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StandardText(
                                text = contact.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            StandardText(
                                text = contact.email.orEmpty(),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                StandardText(text = stringResource(R.string.close))
            }
        },
        containerColor = White
    )
}

private fun openUrl(context: android.content.Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }.onFailure {
        Toast.makeText(
            context,
            context.getString(R.string.partners_open_failed),
            Toast.LENGTH_SHORT
        )
            .show()
    }
}

private fun openPartnerEmail(context: android.content.Context, email: String) {
    if (email.isBlank()) return
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        putExtra(Intent.EXTRA_SUBJECT, CONTACT_EMAIL_SUBJECT)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.partners_email_failed),
            Toast.LENGTH_SHORT
        )
            .show()
    }
}

private fun dialPhone(context: android.content.Context, phone: String) {
    val digits = phone.filter { it.isDigit() || it == '+' }
    runCatching {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
    }.onFailure {
        Toast.makeText(
            context,
            context.getString(R.string.partners_call_failed),
            Toast.LENGTH_SHORT
        )
            .show()
    }
}
