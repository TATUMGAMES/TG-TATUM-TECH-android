# TatumTech Android App - Architecture & UI/UX Design

This document describes how the app is structured today. Paths are relative to
`app/src/main/java/com/tatumgames/tatumtech/android/` unless stated otherwise.
API setup, environments, sessions, and adding endpoints are covered separately in
[docs/TATUM_TECH_API.md](../../../../docs/TATUM_TECH_API.md).

## Table of Contents
1. [Modules](#modules)
2. [App Architecture](#app-architecture)
3. [Activities & App Flow](#activities--app-flow)
4. [Authentication Flow](#authentication-flow)
5. [Navigation Architecture](#navigation-architecture)
6. [Data & Network Architecture](#data--network-architecture)
7. [Features](#features)
8. [Header Component](#header-component)
9. [Home Screen Design](#home-screen-design)
10. [FeatureCard Component](#featurecard-component)
11. [Bottom Navigation](#bottom-navigation)
12. [Screen Patterns](#screen-patterns)
13. [Navigation Drawer](#navigation-drawer)
14. [Reusable Components](#reusable-components)
15. [Feedback, Errors & Dialogs](#feedback-errors--dialogs)
16. [Motion](#motion)
17. [Design System](#design-system)
18. [Resource Conventions](#resource-conventions)

---

## Modules

| Module | Purpose |
|---|---|
| `:app` | The Tatum Tech app: UI, navigation, Room database, Tatum Tech API client, reminders, analytics. |
| `:tatumgames-framework-android` | Shared Tatum Games infrastructure with no Tatum Tech endpoints or data: HTTP client foundation (`http/client`, `http/executor`, `http/response`, `http/logging`, `http/serialization`, `http/config`), Google/Firebase sign-in (`auth/`), the HTTP-error `AnalyticsClient` interface, and `Logger`. |

Both modules use `compileSdk 37` and `minSdk 24`; the app targets SDK 35. Java/Kotlin bytecode targets JVM 11, and core library desugaring provides `java.time` on API 24–25.

---

## App Architecture

- **UI**: 100% Jetpack Compose with Material 3. One composable per screen under `ui/components/screens/<feature>/`.
- **State**: MVVM where a screen has non-trivial state. ViewModels live in `ui/viewmodels/` (`HomePagerViewModel`, `GamesViewModel`) and next to their feature (`ui/components/screens/coding/viewmodels/CodingChallengesViewModel`). Simpler screens hold state with `remember`/`mutableStateOf` and call repositories from a `rememberCoroutineScope`.
- **Dependency wiring**: Manual; there is no DI framework. ViewModels are created through `ViewModelProvider.Factory` classes (`ui/viewmodels/factory/`), the database via `AppDatabase.getInstance`, and the API via the `TatumTechApiProvider` singleton.
- **Startup** (`application/TatumTechApplication`): initializes analytics/Crashlytics, the Tatum Tech API provider and session refresh, and meeting-reminder sync.

### Package overview

| Package | Contents |
|---|---|
| `activity/` | `AuthActivity`, `MainActivity` |
| `analytics/` | Firebase Analytics/Crashlytics service, event names, `FirebaseAnalyticsClient`, `TrackNavigationAnalytics` |
| `api/` | `TatumTechApiClient`, provider, configuration, data-source selection, local JSON executor, models, session (`TatumTechSessionManager`, `KeystoreSessionStore`) |
| `data/` | `content/TatumTechContentRepository` (events, speakers, partners), `games/` (`LocalGameRepository`) |
| `database/` | Room `AppDatabase` (`tatum_tech.db`), entities, DAOs, repositories |
| `enums/` | `HomePagerCategory`, `TimelineType`, `TimelineFilter`, `NotificationType`, etc. |
| `reminders/` | Virtual-speaker meeting reminders (WorkManager) |
| `ui/` | `components/` (common, layout, navigation, screens), `models/`, `theme/`, `utils/` (`JsonImporter`, `GameMediaResolver`), `viewmodels/` |
| `utils/` | `Utils` (URL opening, validation, anonymous IDs), `CodingChallengesImporter`, `QrCodeBitmapGenerator` |

---

## Activities & App Flow

```
AuthActivity (launcher, splash screen)
  ├─ existing session? ──yes──> MainActivity (task cleared)
  └─ no ──> AccountSetupGraph ──sign-in success──> MainActivity (task cleared)

MainActivity
  ├─ MainGraph (start: HOME_PAGER_SCREEN)
  ├─ MeetingReminderBannerHost (top-aligned overlay)
  ├─ NotificationPermissionPrompt (one-time, Android 13+)
  └─ on fresh start: navigate to a reminder destination from the intent,
     otherwise record an app open (may show RATING_SCREEN)
```

- **`AuthActivity`** installs the AndroidX splash screen. If the Tatum Tech session manager is signed in, or a Google (Firebase) user is still signed in, it opens `MainActivity` immediately (forwarding any reminder destination) and finishes.
- **`openMainScreen`** (`ui/components/screens/auth/AuthNavigation.kt`) starts `MainActivity` with `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`, so the auth flow is removed from the back stack.
- **`MainActivity`** hosts the main graph plus app-wide overlays. A `null` saved-state bundle is treated as a new app open (rotation/process restore is not).

---

## Authentication Flow

- **AuthScreen** (`auth/splash/AuthScreen.kt`): Sign In, Sign Up, and Google SSO.
  - Google SSO uses the framework `GoogleAuthClient`, which requests a Google ID token through Credential Manager (`GetSignInWithGoogleOption` with the Web OAuth client ID as the server client ID), parses it with `GoogleIdTokenCredential`, and signs in to Firebase Auth. On success the ID token is exchanged with the Tatum Tech API (10 s timeout). Exchange failure is logged but does not block entry.
  - Google failures are reported as typed `GoogleAuthError` values and shown in a dialog with friendly copy (`GoogleAuthErrorDialog`). A user cancel shows nothing. Raw exceptions are never shown.
  - Google sign-in requires the app's package name and signing certificate SHA-1 to be registered as an Android OAuth client in the Firebase project; otherwise Google rejects the request (formerly surfaced as `ApiException: 10`). Each variant (`.debug` and release) and each signing key (debug, upload, Play App Signing) needs its own fingerprint.
  - Account deletion removes the Firebase user, signs out of Firebase, and calls `CredentialManager.clearCredentialState()`.
- **SignInScreen / SignUpScreen / ForgotPasswordScreen** call `TatumTechSessionManager` (`signIn`, `signUp`, `forgotPassword`) and handle the `ApiResponse` result:
  - Success: sign-in/sign-up open the main flow; forgot-password shows a short confirmation Toast.
  - Failure: an `AuthErrorDialog` is shown (see [Feedback, Errors & Dialogs](#feedback-errors--dialogs)).
  - While a request runs, the submit button is replaced by its disabled outlined variant.
- Inline validation (email format, password rules, password match) is shown as persistent red labels under the fields, using `Utils.isEmailValid` / `Utils.isPasswordValid`.
- Session tokens are stored with `KeystoreSessionStore`. Account deletion (on `UserProfileScreen`, via `AccountDeletionManager`) also signs out. There is no standalone sign-out action.
- `ChangePasswordScreen` exists but is not registered in either navigation graph.

---

## Navigation Architecture

### Overview
Jetpack Compose Navigation with two `NavHost` graphs (`ui/components/navigation/graph/NavGraph.kt`), one per activity. Routes are string constants in `NavRoutes` (`ui/components/navigation/routes/NavRoutes.kt`); routes with arguments have builder functions (`gameDetailsRoute`, `virtualSpeakersRoute`, `ratingRoute`). Both graphs call `TrackNavigationAnalytics` to log screen views.

### AccountSetupGraph (AuthActivity)
- **Start Destination**: `AUTH_SCREEN`
- `AUTH_SCREEN`, `SIGN_IN_SCREEN`, `SIGN_UP_SCREEN`, `FORGOT_PASSWORD_SCREEN`

### MainGraph (MainActivity)
- **Start Destination**: `HOME_PAGER_SCREEN`
- Transitions come from `MotionDefaults` (fade + slight horizontal slide; instant when system animations are off).

| Area | Routes |
|---|---|
| Home | `HOME_PAGER_SCREEN` |
| Events & networking | `UPCOMING_EVENTS_SCREEN`, `VIRTUAL_SPEAKERS_SCREEN` (`{eventId}?speakerId=`), `SCANNER_SCREEN`, `SCANNER_FROM_UPCOMING_EVENTS`, `CONTACT_CARD_EDITOR_SCREEN`, `MY_CONTACT_CARD_QR_SCREEN`, `SCANNED_CONTACT_PREVIEW`, `PARTNERS_SCREEN` |
| Coding | `CODING_CHALLENGES_SCREEN`, `AI_LLM_CHALLENGES_SCREEN`, `LEET_CODE_CHALLENGES_SCREEN`, `MOCK_INTERVIEW_CHALLENGES_SCREEN`, `RESOURCES_SCREEN` |
| Progress | `MY_TIMELINE_SCREEN`, `STATS_SCREEN`, `ACHIEVEMENTS_SCREEN` |
| Community | `COMMUNITY_SCREEN`, `DONATE_SCREEN` |
| Career | `CAREER_SCREEN` |
| Games | `GAMES_SCREEN`, `GAME_DETAILS_SCREEN` (`{gameId}`), `GAMES_RESOURCES_SCREEN`, `GET_YOUR_GAME_DISCOVERED_SCREEN` |
| Account | `USER_PROFILE_SCREEN`, `DEMOGRAPHIC_SCREEN`, `about_screen/{contentType}` (About / FAQ) |
| Prompts | `RATING_SCREEN` (`{trigger}`) |

### Navigation Flow

```
AccountSetupGraph
  └─> AUTH_SCREEN
      ├─> SIGN_IN_SCREEN
      ├─> SIGN_UP_SCREEN
      └─> FORGOT_PASSWORD_SCREEN

MainGraph (after authentication)
  └─> HOME_PAGER_SCREEN (start destination)
      ├─> Feature screens (from FeatureCards in each pager tab)
      ├─> Drawer screens (Profile, Demographic Info, About, FAQ)
      └─> Bottom Navigation
          ├─> HOME_PAGER_SCREEN (Home)
          ├─> CODING_CHALLENGES_SCREEN (Learn)
          ├─> MY_TIMELINE_SCREEN (Timeline)
          └─> STATS_SCREEN (Stats)
```

Meeting reminders (banner tap or system notification) navigate to `virtualSpeakersRoute(eventId, speakerId)`. Recent notifications on the home screen navigate to the route stored with each notification.

---

## Data & Network Architecture

### Tatum Tech API
- `TatumTechApiClient` extends the framework `BaseApiClient`. Every call returns `ApiResponse<T>`: `Success`, or `Failure(ApiError)` where `ApiError` is `Http`, `Network`, `Serialization`, or `Unexpected`.
- `TatumTechDataSources` selects the executor: **NETWORK** (`OkHttpRequestExecutor`) or **LOCAL_JSON** (`LocalJsonRequestExecutor`, serving bundled `assets/*.json`). The data source and environment (PRODUCTION / STAGE) are build properties; release builds always use NETWORK + PRODUCTION.
- Debug builds can log HTTP traffic (tags `RQ`/`RS`) through the framework `HttpTrafficLogger`; release builds never log.
- HTTP failures are reported to Firebase Analytics as `api_error` events via `FirebaseAnalyticsClient`.

### Where each feature's data comes from

| Feature | Source |
|---|---|
| Events, virtual speakers | `TatumTechContentRepository` → API (`upcoming_events.json` in LOCAL_JSON mode) |
| Partners | `TatumTechContentRepository` → API (`partners.json` in LOCAL_JSON mode) |
| Auth / session | `TatumTechSessionManager` → API |
| Games | `assets/games.json` via `LocalGameRepository` |
| Career | `assets/career_listings.json` via `JsonImporter` |
| Resources | `assets/resources.json` via `JsonImporter` |
| Games resources | `assets/games_resources.json` combined with API partners |
| Coding challenges | Asset JSON imported into Room by `CodingChallengesImporter`, then read from Room |
| Achievements | `assets/achievements.json` definitions + Room engagement counters |
| Timeline, stats, contact cards, connections, profile, demographics, recent notifications | Room |
| Community | Discord invite API via OkHttp (`screens/community/apiclient/DiscordApiClient`) |

### Room database
`AppDatabase` (`tatum_tech.db`, destructive migration) holds 12 tables: EventRegistration, Timeline, CodingChallenge, CodingQuestion, QuizProgress, QuizAnswerEvent, User, DemographicData, ContactCard, Connection, EngagementCounter, RecentNotification.

### Meeting reminders
Whenever events load, `reminders/` schedules WorkManager jobs for each virtual session a couple of minutes before it starts. Session times come from the event date plus the speaker's `timeZone`, never the device zone. When the app is in the foreground a banner is shown (`MeetingReminderBannerHost`); otherwise a system notification is posted. Debug builds add a "Test reminder" button on speaker cards.

---

## Features

- **Upcoming Events**: event list with Luma RSVP links, Virtual Speakers screen with Google Meet Join links, and digital networking (Tatum Tech contact card editor, My QR, scanner, scanned-contact preview).
- **Coding**: four challenge tracks share `ChallengeQuizScreen` (`screens/coding/CodingChallengesScreen.kt`):
  - Coding: Kotlin, JavaScript, Python, Java, C#
  - AI/LLM, LeetCode (adds pattern badge and code block), Mock Interview
  - Levels: Beginner, Intermediate, Advanced. 10-question sessions; 30 answers per day per track + language + level bucket. Answer feedback uses an overlay with native animated GIFs (API 28+) and a confetti burst on correct answers.
- **Stats / Achievements / My Timeline**: computed from Room timeline events, quiz answers, and engagement counters.
- **Community**: Discord server info (live counts) and links. **Donate**: Stripe tiers opened in an in-app WebView.
- **Career**: curated job listings with search and filter chips.
- **Games**: `GamesScreen` with **Featured** (showcase cards sorted by `featuredPriority`, plus a MIKROS CTA to Get Your Game Discovered) and **Games** (search, genre/gameplay filters) tabs; `GameDetailsScreen` with media, store links, and social links. Local media use `drawable:<name>` strings resolved by `GameMediaResolver`.
- **Partners**: category filter chips, featured partners first, data-driven CTAs (website, contact email picker, call, donate, product, social links).
- **Resources / Games Resources**: curated learning and industry-resource directories.
- **Profile / Demographic Info / About / FAQ**: opened from the drawer.
- **Rating prompt**: `RATING_SCREEN` on every 20th app open or when a coding daily limit is reached; 4+ stars opens the Play Store listing.

---

## Header Component

### Location
`ui/components/common/Header.kt`

### Overview
The `Header` component is the standardized top bar for nearly every screen, including the auth screens. It provides consistent back navigation and title display.

### Implementation Details
- **Layout**: `ConstraintLayout`, full width, fixed 80dp height, `statusBarsPadding()`.
- **Back Button** (start-aligned): 32dp `Image` with 4dp padding using `back_arrow`; optional `backArrowTint`; shown when `isBackButtonVisible` (default `true`).
- **Title** (centered): `TitleText` with `headlineSmall`, bold, `R.color.black`, centered alignment.
- **Divider**: 0.5dp black `HorizontalDivider` below the title.

### Customization Options
- `modifier: Modifier`
- `text: String` (default `""`)
- `isBackButtonVisible: Boolean` (default `true`)
- `onBackClick: () -> Unit` (default no-op)
- `backArrowTint: Color?` (default `null`)

**Exception**: `HomePagerScreen` uses its own title row (app name + hamburger menu).

---

## Home Screen Design

### Location
- Screen: `ui/components/screens/HomePagerScreen.kt`
- Pager: `ui/components/layout/HorizontalPagerLayout.kt`
- Page content: `ui/components/layout/PagerPageContent.kt`
- Tab bar: `ui/components/common/PagerTabBar.kt`
- ViewModel: `ui/viewmodels/HomePagerViewModel.kt` (factory in `ui/viewmodels/factory/`)
- Card model: `ui/models/FeatureCardItem.kt` (`imageResId`, `titleResId`, `route`)

### Overview
The home screen is the main entry point. Static elements surround a horizontal pager of categories; each page is a two-column grid of `FeatureCard`s. On entry it syncs coding questions from assets into Room.

```
┌──────────────────────────────────────────────┐
│ STATIC: App Name + Hamburger Menu            │
├──────────────────────────────────────────────┤
│ STATIC: Greeting Text                        │
├──────────────────────────────────────────────┤
│ STATIC: Pager Tab Bar (horizontally scrolls) │
│ Events | Coding | Community | Career | Games │
├──────────────────────────────────────────────┤
│  DYNAMIC: HorizontalPager                    │
│  ┌──────────────────────────────┐            │
│  │ Two-column grid of cards     │            │
│  └──────────────────────────────┘            │
├──────────────────────────────────────────────┤
│ STATIC: Recent Notifications (collapsible)   │
└──────────────────────────────────────────────┘
│ Bottom Navigation                            │
```

### Static Elements
1. **Title Row**: `R.string.app_name` (`headlineSmall`, bold) and a menu icon that opens the drawer; space-between, 8dp vertical padding.
2. **Greeting**: `headlineMedium`, bold, 12dp top padding. Uses the stored user's name (`greeting_with_name`) or `greeting_generic`.
3. **Pager Tab Bar**: one tab per `HomePagerCategory`, in a horizontally scrolling row (4dp spacing). Selected tab: `titleMedium` bold in the primary color; others at 70% opacity. Tapping a tab animates the pager.
4. **Recent Notifications**: `titleSmall` bold header with expand/collapse; when expanded, a scrollable list (max 200dp) or an empty state. Tapping an item marks it read and navigates to its route.

### Pager & Grid
- `androidx.compose.foundation.pager.HorizontalPager`, synced with the tab bar.
- Items are laid out with `chunked(2)`: two cards per row with `weight(1f)`, 16dp gaps, 16dp horizontal padding; an odd last card spans the full width.

### Category Content

| Tab | Cards → Route |
|---|---|
| Events | Upcoming Events → `UPCOMING_EVENTS_SCREEN`; Scanner → `SCANNER_SCREEN`; Partners → `PARTNERS_SCREEN` |
| Coding | Coding → `CODING_CHALLENGES_SCREEN`; AI/LLM Challenges → `AI_LLM_CHALLENGES_SCREEN`; Stats → `STATS_SCREEN`; Resources → `RESOURCES_SCREEN` |
| Community | Community → `COMMUNITY_SCREEN`; Donate → `DONATE_SCREEN` |
| Career | Apply for Jobs → `CAREER_SCREEN`; LeetCode Challenges → `LEET_CODE_CHALLENGES_SCREEN`; Mock Interview → `MOCK_INTERVIEW_CHALLENGES_SCREEN` |
| Games | Discover → `GAMES_SCREEN`; Resources → `GAMES_RESOURCES_SCREEN` |

### Recent notifications
Generated by `RecentNotificationDatabaseRepository.refreshAndLoad` with stable IDs (no duplicates on refresh): a daily coding-challenge item, up to three upcoming events, and daily Career / Community / Games spotlights. `RecentNotificationPolicy` builds IDs, keeps read items for 14 days, and resolves destination routes.

### Design Rationale
1. **Scalability**: new categories or cards only need a `HomePagerCategory` entry and `FeatureCardItem`s.
2. **Organization**: content grouped by theme.
3. **Consistency**: greeting and notifications stay visible regardless of the selected tab.

---

## FeatureCard Component

### Location
`ui/components/screens/main/FeatureCard.kt`

### Overview
Reusable card used in the home pager grid. Shows an icon/image with a title and navigates on click.

```
┌─────────────────────────┐
│ ┌──────────┐            │
│ │   Icon   │            │ 36dp container
│ └──────────┘            │
│ Card Title Text         │ 16sp, Medium
└─────────────────────────┘
     100dp height
```

### Dimensions and Styling
- **Height**: fixed 100dp; width from the grid (weight or full width)
- **Shape**: 12dp rounded corners
- **Elevation**: 4dp default
- **Background**: `Grey200` default
- **Padding**: 16dp; content start-aligned and vertically centered

### Icon
- 36dp container, 8dp corners, 6dp padding, `NotificationLavender` (`#EDE7F6`) background by default.
- Accepts an `ImageVector` (`icon`) or a `Painter` (`image`); one is required (`require` check). Both are drawn with `Icon` and `iconTint` (default `Color.Unspecified`).

### Text
16sp, `FontWeight.Medium`, `Black` by default, 8dp below the icon.

### Parameters
`modifier`, `icon: ImageVector?`, `image: Painter?`, `text: String`, `onClick: (() -> Unit)?` (not clickable when null), `elevation: Dp` (4dp), `backgroundColor: Color` (`Grey200`), `iconTint: Color` (`Unspecified`), `iconBackground: Color` (`NotificationLavender`), `textColor: Color` (`Black`).

---

## Bottom Navigation

### Location
`ui/components/common/BottomNavigationBar.kt` (items modeled in `ui/components/screens/main/models/BottomNavigation.kt`)

### Items

| Item | Icon | Route | Label |
|---|---|---|---|
| Home | `Icons.Default.Home` | `HOME_PAGER_SCREEN` | `R.string.home` |
| Learn | `Icons.Default.Face` | `CODING_CHALLENGES_SCREEN` | `R.string.learn` |
| Timeline | `Icons.Default.DateRange` | `MY_TIMELINE_SCREEN` | `R.string.timeline` |
| Stats | `Icons.Default.Star` | `STATS_SCREEN` | `R.string.stats` |

### Visual Design
Material 3 `NavigationBar`, `White` container, 8dp tonal elevation, labels always shown, icon `contentDescription` set to the label.

### Navigation Behavior
- Tapping the current item does nothing.
- **Home** pops back to `HOME_PAGER_SCREEN`.
- Other items:

```kotlin
navController.navigate(item.route) {
    popUpTo(NavRoutes.HOME_PAGER_SCREEN) {
        inclusive = false
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
```

This keeps the back stack shallow (back from a tab returns home) and preserves each tab's state.

### Where It Appears
Home, Upcoming Events, Virtual Speakers, all coding challenge screens, My Timeline, Stats, Achievements, Community, Donate, About/FAQ, Partners, Resources, Games Resources, Games, Get Your Game Discovered, Career.

Not shown on: auth screens, Scanner, Game Details, User Profile, Demographic Info, Rating, and the networking screens (contact card editor, My QR, scanned contact preview).

---

## Screen Patterns

### Standard Screen Structure

```kotlin
Scaffold(
    topBar = {
        Header(
            text = stringResource(R.string.screen_title),
            onBackClick = { navController.popBackStack() }
        )
    },
    bottomBar = {
        BottomNavigationBar(navController = navController)
    },
    containerColor = ScreenScaffoldLight
) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Content
    }
}
```

### Background Colors
- **`ScreenScaffoldLight`** (`#F0F0F0`): content/list screens (events, speakers, coding, timeline, stats, achievements, community, donate, about, partners, resources, games, career, contact card editor, scanned contact preview).
- **`White`**: home, auth, form/account screens (user profile, demographic info), rating, My QR.
- **`Black`**: scanner (camera preview).

### Layout
- Scaffold `paddingValues` are always applied to content.
- Long content scrolls with `verticalScroll(rememberScrollState())` or `LazyColumn`.
- Typical spacing: 16dp horizontal padding; 8/12/16/24dp vertical rhythm.

### Screen Variants
- **Auth screens**: `Header` (no bottom bar), white background, 20dp padding, terms/privacy text pinned to the bottom.
- **Forms** (profile, demographic info, contact card editor): `Header`, scrollable `Column`, Snackbar host for save confirmations.
- **Detail screens** (Game Details, Virtual Speakers): `Header` with back, scrollable content.
- **Home**: custom title row instead of `Header`.

---

## Navigation Drawer

### Location
`ui/components/screens/main/UserProfileDrawer.kt`, hosted by `HomePagerScreen`.

### Structure
- **Drawer**: 75% of screen width, anchored to the end (right), white background, scrollable content.
- **Overlay**: full-screen `Black` at 30% alpha behind the drawer; tapping it closes the drawer.
- **Visibility**: `HomePagerScreen` toggles `isDrawerOpen` (`remember { mutableStateOf(false) }`) from the hamburger icon and shows the drawer with `AnimatedVisibility` (fade, 300ms).

### Content
1. `tatumgames_logo`
2. Menu items, each navigating and then closing the drawer:
   - Profile → `USER_PROFILE_SCREEN`
   - Demographic Info → `DEMOGRAPHIC_SCREEN`
   - About Tatum Games → `about_screen/about`
   - FAQ → `about_screen/faq`
3. Footer: divider, app version, Terms & Privacy links.

### Closing
Overlay tap, or selecting a menu item.

---

## Reusable Components

All in `ui/components/common/` unless noted.

| Component | Purpose |
|---|---|
| `Header` | Standard top bar (see above) |
| `BottomNavigationBar` | Four-item bottom navigation |
| `PagerTabBar` | Home pager category tabs |
| `RoundedButton`, `OutlinedButton` (`Buttons.kt`) | Primary filled button (Purple200) and outlined/disabled variant (2dp purple border, 8dp corners) |
| `OutlinedInputField` (`EditTexts.kt`) | Standard text input |
| `StandardText`, `TitleText`, `LinkifyText`, `ClickableText`, `TermsAndPrivacyText` (`TextViews.kt`, `ClickableText.kt`) | Text primitives; `StandardText` defaults to `bodyMedium` / `R.color.black` |
| `StandardAlertDialog` | Shared Material 3 dialog: title, scrollable description, primary button, optional secondary button |
| `MeetingReminderBannerHost` | App-wide reminder banner that slides in from the top |
| `NotificationPermissionPrompt` | One-time explainer dialog before requesting `POST_NOTIFICATIONS` (Android 13+) |
| `MotionDefaults` | Shared animation timings and nav transitions |
| `SectionCard` | Square section card (currently unused) |
| `FeatureCard` (`screens/main/`) | Home grid card |
| `AnimatedGifImage`, `ConfettiBurst` (`screens/coding/FeedbackAnimations.kt`) | Native GIF playback (API 28+, static fallback) and Canvas confetti for answer feedback |

---

## Feedback, Errors & Dialogs

Choose the mechanism by how much attention the message needs:

| Mechanism | Used for | Examples |
|---|---|---|
| **Dialog** (`StandardAlertDialog`) | Errors or decisions that need acknowledgement | `AuthErrorDialog`, demographic age confirmation |
| **Dialog** (Material 3 `AlertDialog`) | Feature-specific dialogs with custom content | Partner details, contact picker, delete-account confirmation, notification permission explainer |
| **Snackbar** | Save confirmations and recoverable issues on a screen with a `SnackbarHost` | Profile/demographic saved, contact card editor, scanner results |
| **Toast** | Brief, non-blocking status that needs no acknowledgement | Reset email sent, "couldn't open link/email/dialer" fallbacks, debug-only reminder scheduled |
| **Inline label** | Form validation | Red `Red300` text under auth fields |

### Auth error dialog
`AuthErrorDialog(error: ApiError, onDismiss)` in `ui/components/screens/auth/AuthNavigation.kt`:
- Title `R.string.auth_error_title`, message from `authErrorMessage`, single `R.string.ok` button.
- Dismissed only by OK (back press and outside taps are ignored).
- Message mapping:
  - `ApiError.Http`: first non-blank server message, trimmed and capped at 500 characters; otherwise `something_went_wrong`.
  - `ApiError.Network`: `error_network_unavailable`.
  - `ApiError.Serialization` / `ApiError.Unexpected`: `something_went_wrong` (technical details are never shown).
- Screens keep the error in Compose state (`var authError by remember { mutableStateOf<ApiError?>(null) }`) and render the dialog from composition. Because it is not created from a `Context`, it is bound to the hosting Activity's window and disappears with the screen, so it cannot leak a window during navigation.

### Long dialog text
`StandardAlertDialog` makes its description scrollable, so long messages never push the button off screen.

---

## Motion

`MotionDefaults` centralizes timings (`NAV_MS` 220, `CONTENT_MS` 200, feedback enter/exit 280/180) and `MainGraph` transitions. `animationsEnabled()` reads the system animator duration scale; when animations are off, durations become 0 and decorative effects (GIF playback, confetti) are skipped.

---

## Design System

### Theme
`ui/theme/Theme.kt` defines `TatumTechTheme`: a static light `ColorScheme` (no dynamic color, no dark theme) with `primary = Purple200`, `secondary = Teal200`, white background/surface, black on-colors. Typography is the Material 3 default.

### Color Palette (`ui/theme/Color.kt`)
- **Base**: `Purple200/500/700`, `Teal200/700`, `Black`, `White`, `ScreenScaffoldLight` (`#F0F0F0`), `Grey200–500`
- **Semantic**: `SuccessGreen`, `DestructiveRed`, `Red300` (validation), `Gold`
- **Feature accents**: `NotificationLavender`, `SpringPurple*`, `FallDeepOrange*`, `Partner*` (partner CTAs), `SteamStoreDark`, `Discord*`

Use the named colors rather than hex literals.

### Typography
- **App name / Header title**: `headlineSmall`, bold
- **Greeting**: `headlineMedium`, bold
- **Pager tabs**: `titleMedium`
- **Section titles**: `titleSmall`/`titleMedium`, bold
- **Card text**: 16sp, Medium
- **Body**: `bodyMedium`/`bodyLarge`

### Spacing, Elevation, Shapes
- **Spacing**: 4 / 8 / 12 / 16 / 24dp
- **Elevation**: FeatureCard 4dp; bottom navigation 8dp tonal
- **Corners**: FeatureCard 12dp; icon containers and buttons 8dp
- **Sizes**: Header 80dp tall, back button 32dp, FeatureCard 100dp tall, icon container 36dp

---

## Resource Conventions

- **Strings**: all user-facing text lives in `res/values/strings.xml` (English only), grouped by feature comments; composables use `stringResource`, non-composable code uses `context.getString`.
- **Drawables**: feature-prefixed names (e.g. `pog_*`, `saint_art_puzzle_*`, partner logos by name). JSON assets reference drawables as `"drawable:<name>"` (games) or plain names (partner logos, achievement badges), resolved at runtime.
- **Assets** (`app/src/main/assets/`): curated content JSON (`games.json`, `partners.json`, `upcoming_events.json`, `career_listings.json`, `resources.json`, `games_resources.json`, `achievements.json`) and coding-challenge banks named `coding_challenges_<track>_<level>.json`. Asset validation tests live in `app/src/test/.../assets/`.
- **Configuration**: Tatum Tech API settings come from `-PtatumTech.*` Gradle properties or the git-ignored `local.properties`, exposed through `BuildConfig`. Never put credentials in source files.
