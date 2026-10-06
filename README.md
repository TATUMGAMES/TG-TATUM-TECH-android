# TatumTech Android App

An Android application built with Jetpack Compose for the Tatum Tech community: events and virtual speakers, digital networking, coding challenges, progress tracking, careers, games discovery, and partners. It supports Tatum Tech's mission of increasing representation of minorities and women in the video game and tech industries.

## Table of Contents

- [Overview](#overview)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Main Screen Sections](#main-screen-sections)
  - [Upcoming Events](#upcoming-events)
  - [Coding](#coding)
  - [Community](#community)
  - [Stats](#stats)
  - [Career](#career)
  - [Games](#games)
  - [Partners](#partners)
  - [Profile / Drawer](#profile--drawer)
- [Architecture & Best Practices](#architecture--best-practices)
- [Testing](#testing)
- [Documentation](#documentation)

[Back to Table of Contents](#table-of-contents)

## Overview

After signing in (email/password through the Tatum Tech API, or Google SSO), users land on a home screen with a horizontal pager of five categories (**Events, Coding, Community, Career, Games**), a collapsible Recent Notifications list, a right-side drawer, and a bottom navigation bar (Home, Learn, Timeline, Stats).

The app provides:
- Upcoming events with Luma RSVP links, virtual speaker sessions, and speaker reminders
- Digital networking with QR-shareable Tatum Tech contact cards
- Interactive coding challenges with daily limits, streaks, stats, and achievements
- Community (Discord) and donation features
- Curated career listings, learning resources, games, and partners
- Local storage (Room) for progress, timeline, contact cards, and profile data

[Back to Table of Contents](#table-of-contents)

## Getting Started

### Requirements

| Tool | Version |
|---|---|
| Android Studio | A release that supports Android Gradle Plugin 9.2 |
| JDK (to run Gradle) | 17 or newer |
| Android SDK | Platform 37 (`compileSdk`); Gradle can download it automatically |
| Gradle | 9.4.1 (wrapper included) |
| Kotlin | 2.2.10 |

The app uses `minSdk 24`, `targetSdk 35`, and `compileSdk 37`.

### Configuration

- **Tatum Tech API**: optional `tatumTech.*` settings go in the git-ignored `local.properties` or are passed as `-P` Gradle properties (`environment`, `dataSource`, `apiKey`, `connectTimeoutMs`). By default the app uses the production API. Set `tatumTech.dataSource=LOCAL_JSON` to run entirely from bundled JSON. See [docs/TATUM_TECH_API.md](docs/TATUM_TECH_API.md).
- **Firebase**: `app/google-services.json` is included for Firebase Auth, Analytics, and Crashlytics.
- Never commit API keys or other credentials to source files.

### Build & Run

```bash
./gradlew :app:installDebug            # build and install the debug app
./gradlew :app:assembleRelease         # release build (always production API)
./gradlew :app:installDebug -PtatumTech.dataSource=LOCAL_JSON   # offline JSON data
```

On Windows use `gradlew.bat`. Debug builds install alongside release builds (`applicationIdSuffix = ".debug"`).

[Back to Table of Contents](#table-of-contents)

## Project Structure

Gradle modules:
- `app` – The Tatum Tech app.
- `tatumgames-framework-android` – Shared Tatum Games infrastructure reusable by any Tatum Games app: HTTP client foundation, Google/Firebase sign-in, and logging. It contains no Tatum Tech endpoints, models, or data.

Key packages in `app` (`com.tatumgames.tatumtech.android`):
- `activity` – `AuthActivity` (launcher, auth flow) and `MainActivity` (main flow).
- `ui.components.screens` – Composables for full screens, grouped by feature.
- `ui.components.common` – Shared UI components (e.g., `Header`, `BottomNavigationBar`, `StandardAlertDialog`, buttons, text, inputs).
- `ui.components.navigation` – Navigation graphs and route constants.
- `ui.viewmodels` and feature `viewmodels` packages – State management.
- `api` – Tatum Tech API client, configuration, session management, and local JSON mode.
- `data` – Content and games repositories.
- `database` – Room database, entities, DAOs, and repositories.
- `reminders` – Virtual speaker meeting reminders (WorkManager).
- `analytics` – Firebase Analytics and Crashlytics.
- `utils`, `ui.utils` – Helpers (asset JSON importers, URL opening, validation, media resolution).
- `assets` – Curated content JSON and coding challenge banks, also used by local JSON API mode.

[Back to Table of Contents](#table-of-contents)

## Main Screen Sections

### Upcoming Events

- Loads events (Luma RSVP URL + nested virtual speakers) through the Tatum Tech API client; `assets/upcoming_events.json` backs local JSON mode. See [docs/TATUM_TECH_API.md](docs/TATUM_TECH_API.md).
- **Register** opens the event’s Luma page externally; the app does not track registered/unregistered state.
- **Virtual Speakers** opens a dedicated screen with speaker cards and Google Meet **Join** links.
- **Speaker reminders**: a couple of minutes before each virtual session starts, the app shows a banner at the top of the screen (when open) or a "Virtual Speakers" system notification (when in the background or closed); either opens that speaker's card. Reminders are scheduled with WorkManager whenever events load (code in `reminders/`); session times are computed from the event date plus the speaker's `timeZone`, never the device zone. Debug builds add a "Test reminder" button to each speaker card.
- Digital networking at the top: create/edit a Tatum Tech contact card, share via QR, and scan others’ cards.

[Back to Main Sections](#main-screen-sections)

### Coding

- Challenge banks ship as asset JSON and are imported into the Room database; quizzes read from Room.
- Coding Challenges: Kotlin, JavaScript, Python, Java, and C#, each at Beginner, Intermediate, and Advanced levels.
- Additional tracks share the same quiz experience: **AI/LLM Challenges** (Coding tab), **LeetCode Challenges** and **Mock Interview** (Career tab).
- Daily limit of 30 answers per track + language + difficulty bucket, plus streak tracking.
- Answer feedback shows animated correct/incorrect GIFs (Android 9+, static images otherwise) and a confetti burst on correct answers; both respect the system animation setting.
- Challenge results show an accurate session score and offer **Do Another Challenge** when daily allowance remains, or **Try Again Tomorrow** when the limit is reached.
- Completing a challenge writes exactly one idempotent `CHALLENGE_COMPLETION` timeline event (shared by Stats and My Timeline).

[Back to Main Sections](#main-screen-sections)

### Community

- Shows the Tatum Tech Discord server (live member/online counts from the Discord invite API) and community links.
- Displays a centered `CircularProgressIndicator` while loading.
- **Donate** offers Stripe donation tiers in an in-app web view.

[Back to Main Sections](#main-screen-sections)

### Stats

- Summarizes user activity from the shared timeline and quiz-answer stores.
- Completed challenges are counted from `TimelineType.CHALLENGE_COMPLETION` events (written once when a quiz session finishes).
- Category Breakdown groups those completions by language/track (Kotlin, Java, AI/LLM, etc.).
- Displays:
  - Events attended, challenges completed, QR scans
  - Percent correct and current streak
  - Coding Challenge Stats (questions answered vs correct)
  - Achievements (**View All** opens the full list)
- Scrollable layout with fixed top/bottom bars; progress rings use named animation step delays.

[Back to Main Sections](#main-screen-sections)

### Career

- Curated local job directory from `career_listings.json` (not live ATS scraping).
- Search across title, company, description, and technologies.
- Filter chips for Job Category and Employment Type (Games-style UI).
- Listings point at official company careers pages; no fabricated dates or salaries.

[Back to Main Sections](#main-screen-sections)

### Games

- Curated Discover Games showcase from `games.json`: **Price of Glory**, **Heroes Vs Villains: Nemesis**, **BANJAX**, **Tales of Encenia**, and **Saints Art Puzzle**.
- Featured tab: showcase cards with Featured / Coming Soon status, plus a MIKROS developer CTA (Get Your Game Discovered).
- Games tab: search plus genre and gameplay filters.
- Game details show screenshots, videos (YouTube thumbnails), store links (Google Play, App Store, Steam, website, Linktree/other), and social links.
- Local drawable logos/screenshots are referenced as `drawable:<name>`; no placeholder filler games.
- Games → Resources lists game-industry resources.

[Back to Main Sections](#main-screen-sections)

### Partners

- Curated partner directory loaded through the Tatum Tech API client (`partners.json` backs local JSON mode) with six categories (Community, Corporate, Education, Game Studios, Government, Technology).
- Category filter chips; featured partners sorted first with accent treatment.
- Data-driven CTAs: website, contact email (picker when multiple), donate, call, wishlist/product, social icons.
- Contact emails open the device mail app with subject: `Got Your Contact Info From Tatum Games. I Have Some Questions`.

[Back to Main Sections](#main-screen-sections)

### Profile / Drawer

- The home screen's hamburger icon opens a right-side drawer (75% width) over a dimmed overlay.
- Drawer contains:
  - Tatum Games logo
  - Menu items: **Profile**, **Demographic Info**, **About Tatum Games**, **FAQ**
  - App version and **Terms** / **Privacy Policy** links
- Profile screen lets users update their information (with validation) and delete their account.
- Demographic Info screen collects optional age range, sex, occupation, salary range, and school data with consent acknowledgements for event analytics.
- About screen provides information about Tatum Tech and MIKROS.

[Back to Main Sections](#main-screen-sections)

## Architecture & Best Practices

- **Jetpack Compose + Material 3** UI; MVVM with ViewModels for screens with non-trivial state. No DI framework; dependencies are wired with factories and singletons.
- **Two navigation graphs**: `AccountSetupGraph` (auth) and `MainGraph` (main app), with string route constants in `NavRoutes`.
- **Tatum Tech API**: events, speakers, partners, and authentication go through `TatumTechApiClient`, built on the shared `tatumgames-framework-android` HTTP layer, with a switchable local JSON mode. Setup, environments, sessions, and adding endpoints: [docs/TATUM_TECH_API.md](docs/TATUM_TECH_API.md).
- **Local data**: curated content (games, career, resources, achievements) ships as asset JSON; progress, timeline, profile, and networking data live in Room.
- **Consistent screens**: shared `Header` on nearly every screen; `BottomNavigationBar` on the main content screens (not on auth, scanner, detail, or form screens).
- **Feedback**: important errors use dialogs (e.g. the auth error dialog built on `StandardAlertDialog`); Snackbars confirm saves; Toasts are reserved for brief, non-blocking status.
- **Loading UX**: spinners (`CircularProgressIndicator`) rather than loading text.
- **Motion**: shared `MotionDefaults` timings that respect the system animation setting.
- **Naming & styling**: fully spelled functions and variables; named theme colors and string resources instead of literals.

[Back to Table of Contents](#table-of-contents)

## Testing

```bash
./gradlew :tatumgames-framework-android:testDebugUnitTest :app:testDebugUnitTest
./gradlew :app:lintDebug
```

- JVM unit tests cover the framework HTTP layer, the Tatum Tech API client and session manager, reminders, notification policy, auth error messages, and asset JSON validation (`app/src/test/.../assets/`).
- Instrumented tests (`app/src/androidTest`) run on a device or emulator with `./gradlew :app:connectedDebugAndroidTest`.

[Back to Table of Contents](#table-of-contents)

## Documentation

- [app/src/main/docs/APP_DESIGN.md](app/src/main/docs/APP_DESIGN.md) – Architecture, navigation, data flow, UI patterns, and design system.
- [docs/TATUM_TECH_API.md](docs/TATUM_TECH_API.md) – Tatum Tech API configuration, sessions, and adding endpoints.
- [docs/APP_FEATURES.md](docs/APP_FEATURES.md) – Non-technical feature overview.

[Back to Table of Contents](#table-of-contents)
