package com.tatumgames.tatumtech.android.ui.models

/**
 * Data class representing a game in the games catalog.
 * 
 * @param appId Unique identifier for the game
 * @param appName Display name of the game
 * @param companyName Company that developed the game
 * @param title Full title of the game
 * @param shortDesc Short description for cards and previews
 * @param fullDesc Full description for game details screen
 * @param androidPackageName Android package name for Play Store
 * @param iOSBundleId iOS bundle identifier for App Store
 * @param website Official website URL
 * @param appCategory Category of the game (Racing, Action, RPG, etc.)
 * @param appStore Primary app store (Android, iOS, Steam)
 * @param userSubscriptionType Subscription type (Enterprise, Startup, Free)
 * @param releaseStatus Release status (Released, Coming Soon, Not Released)
 * @param images Game images including feature graphics and screenshots
 * @param videos Game videos including promotional and other content
 * @param marketingCampaignActive Whether the game is currently featured
 * @param appGameId Game ID in the platform (optional, from API)
 * @param gameGenre Genre of the game (optional, from API)
 * @param gameplayType Type of gameplay - Casual or Non-Casual (optional, from API)
 * @param contentGenre Content genre classification (optional, from API)
 * @param contentTheme Theme of the content (optional, from API)
 * @param campaign Marketing campaign information (optional, from API)
 */
data class GameModel(
    val appId: String,
    val appName: String,
    val companyName: String,
    val title: String,
    @com.google.gson.annotations.SerializedName("shortDescription")
    val shortDesc: String,
    @com.google.gson.annotations.SerializedName("longDescription")
    val fullDesc: String,
    val androidPackageName: String?,
    val iOSBundleId: String?,
    val website: String?,
    val appCategory: String,
    val appStore: String,
    val userSubscriptionType: String,
    val releaseStatus: String,
    val images: GameImages,
    val videos: GameVideos,
    val marketingCampaignActive: Boolean = false,
    val appGameId: String? = null,
    val gameGenre: String? = null,
    val gameplayType: String? = null,
    val contentGenre: String? = null,
    val contentTheme: String? = null,
    val campaign: GameCampaign? = null,
    /** When true, game may appear in hero/sponsored; gated by featured date window. */
    val isFeatured: Boolean = false,
    /** Higher values sort first in featured carousels. */
    val featuredPriority: Int = 0,
    /** ISO-8601 instant string, e.g. 2026-01-01T00:00:00Z; null with [isFeatured] uses legacy [marketingCampaignActive] behavior in repository. */
    val featuredStartDate: String? = null,
    val featuredEndDate: String? = null,
    /** Higher values rank higher for Top 10 style rows. */
    val popularityScore: Int = 0,
    /** e.g. Trending, NewRelease — used for discovery rows. */
    val discoveryTags: List<String>? = null,
    /** Extra genre labels for filtering; falls back to [gameGenre] / [appCategory] when null. */
    val genres: List<String>? = null
)

/**
 * Data class representing game images.
 * 
 * @param featureGraphics List of feature graphic URLs
 * @param screenshots List of screenshot URLs
 */
data class GameImages(
    val featureGraphics: List<String>,
    val screenshots: List<String>
)

/**
 * Data class representing game videos.
 *
 * Prefer tracked Appspot URLs in [GameVideoLink.url] for outbound clicks.
 * Optional [GameVideoLink.thumbnailUrl] may point at a YouTube thumbnail for display.
 */
data class GameVideos(
    val promotional: List<GameVideoLink> = emptyList(),
    val others: List<GameVideoLink> = emptyList()
)

/**
 * Outbound video action with optional thumbnail source for UI.
 */
data class GameVideoLink(
    val url: String,
    val thumbnailUrl: String? = null
)

/**
 * Data class representing a marketing campaign for a game.
 * 
 * @param campaignId Unique identifier for the campaign
 * @param campaignName Display name of the campaign
 * @param startDate Campaign start date as Unix timestamp
 * @param endDate Campaign end date as Unix timestamp
 * @param images Campaign images (contains appLogo)
 * @param ctas Call-to-action links for various stores
 * @param screenshotUrls URLs for campaign screenshots
 * @param videoUrls URLs for campaign videos
 * @param socialMedia Social media links for the campaign
 */
data class GameCampaign(
    val campaignId: String,
    val campaignName: String,
    val startDate: Long,
    val endDate: Long,
    val images: CampaignImages,
    val ctas: CampaignCtas,
    val screenshotUrls: List<String>,
    val videoUrls: List<String>,
    val socialMedia: CampaignSocialMedia
)

/**
 * Data class representing campaign images.
 * 
 * @param appLogo URL for the app logo
 */
data class CampaignImages(
    val appLogo: String
)

/**
 * Data class representing campaign call-to-action links.
 * 
 * @param googleStore Google Play Store link
 * @param appleStore Apple App Store link
 * @param steamStore Steam Store link
 * @param samsungStore Samsung Store link
 * @param amazonStore Amazon Store link
 * @param website Website link
 * @param other Other link
 */
data class CampaignCtas(
    @com.google.gson.annotations.SerializedName("google_store")
    val googleStore: String? = null,
    @com.google.gson.annotations.SerializedName("apple_store")
    val appleStore: String? = null,
    @com.google.gson.annotations.SerializedName("steam_store")
    val steamStore: String? = null,
    @com.google.gson.annotations.SerializedName("samsung_store")
    val samsungStore: String? = null,
    @com.google.gson.annotations.SerializedName("amazon_store")
    val amazonStore: String? = null,
    val website: String? = null,
    val other: String? = null
)

/**
 * Data class representing campaign social media links.
 * 
 * @param facebook Facebook page URL
 * @param x Twitter/X URL
 * @param instagram Instagram URL
 * @param linkedin LinkedIn URL
 * @param tiktok TikTok URL
 * @param youtube YouTube URL
 * @param discord Discord invite URL
 * @param twitch Twitch URL
 */
data class CampaignSocialMedia(
    val facebook: String? = null,
    val x: String? = null,
    val instagram: String? = null,
    val linkedin: String? = null,
    val tiktok: String? = null,
    val youtube: String? = null,
    val discord: String? = null,
    val twitch: String? = null
)

/**
 * Data class representing the games catalog response.
 * 
 * @param data The games data container
 * @param status Response status information
 */
data class GamesCatalogResponse(
    val data: GamesData,
    val status: GamesStatus
)

/**
 * Data class containing the games data.
 * 
 * @param apps List of game applications (direct GameModel objects in API format)
 */
data class GamesData(
    val apps: List<GameModel>
)

/**
 * Data class representing the response status.
 * 
 * @param statusCode HTTP status code
 * @param statusMessage Status message
 */
data class GamesStatus(
    val statusCode: Int,
    val statusMessage: String
)

/**
 * Enum representing game categories for filtering.
 */
enum class GameCategory(val displayName: String) {
    FEATURED("Featured"),
    JUST_TOO_FUN("Just Too Fun"),
    TATUM_GAMES_FAVORITES("Tatum Games Favorites"),
    APPS_IN_DEVELOPMENT("Apps in Development"),
    CASUAL_GAMER("Casual Gamer"),
    HARDCORE_GAMER("Hardcore Gamer"),
    COMING_SOON("Coming Soon")
}

/**
 * Enum representing subscription types.
 */
enum class SubscriptionType(val displayName: String) {
    ENTERPRISE("Enterprise"),
    STARTUP("Startup")
}

/**
 * Enum representing release status.
 */
enum class ReleaseStatus(val displayName: String) {
    RELEASED("Released"),
    COMING_SOON("Coming Soon"),
    NOT_RELEASED("Not Released")
}

