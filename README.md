# TatumTech Android App

An Android application built with Jetpack Compose that provides a comprehensive platform for Tatum Tech community events, coding challenges, and user engagement. The app features a modular, clean-architecture approach with offline-first capabilities and seamless user experience.

## Table of Contents

- [Overview](#overview)
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

[Back to Table of Contents](#table-of-contents)

## Overview

This Android app utilizes Jetpack Compose with a modular, clean-architecture approach. The `MainScreen.kt` contains multiple sections accessible via the bottom navigation or drawer. Each section has specific responsibilities, UI logic, and data sources.

The app is designed to support Tatum Tech's mission of increasing representation of minorities and women in the video game industry by providing:
- Event registration and management
- Interactive coding challenges
- Community engagement features
- Progress tracking and achievements
- Offline-first functionality with local database storage

[Back to Table of Contents](#table-of-contents)

## Project Structure

- `ui.components.screens` – Composables for full-screen UI like `MainScreen`, `UpcomingEventsScreen`, etc.
- `ui.components.common` – Shared UI components (e.g., `BottomNavigationBar`, `Drawer`, `LoadingOverlay`).
- `viewmodels` – State management logic and business logic.
- `models` – Data models for requests, responses, and entities.
- `assets` – Local JSON files used for documentation and offline mock data.
- `utils` – Helper classes (e.g., asset JSON importers, anonymizer, date formatters).

The file and folder structure is modular and corresponds to app features. Screens and components are separated for clarity and scalability.

[Back to Table of Contents](#table-of-contents)

## Main Screen Sections

### Upcoming Events

- Loads API-shaped event data from `assets/upcoming_events.json` (Luma RSVP URL + nested virtual speakers).
- **Register** opens the event’s Luma page externally; the app does not track registered/unregistered state.
- **Virtual Speakers** opens a dedicated screen with speaker cards and Google Meet **Join** links.
- Digital networking at the top: create/edit a Tatum Tech contact card, share via QR, and scan others’ cards.

[Back to Main Sections](#main-screen-sections)

### Coding

- Displays coding challenges fetched from assets or DB.
- On first load, it imports from JSON into the local database if empty.
- Composable reads from a ViewModel backed by Room.
- Select language & difficulty, then drills into challenge details.
- Supports multiple programming languages (Kotlin, JavaScript, Python) and difficulty levels (Beginner, Intermediate).
- Features daily question limits (30 per language + difficulty bucket) and streak tracking for user engagement.
- Challenge results show an accurate session score and offer **Do Another Challenge** when daily allowance remains, or **Try Again Tomorrow** when the limit is reached.
- Completing a challenge writes exactly one idempotent `CHALLENGE_COMPLETION` timeline event (shared by Stats and My Timeline).

[Back to Main Sections](#main-screen-sections)

### Community

- Shows community-related information like server status or chat links.
- Currently displays a loading spinner (not text!) during data fetch.
- Replaced legacy "Loading server info..." with a `CircularProgressIndicator` centered.
- Provides Discord integration and community engagement features.

[Back to Main Sections](#main-screen-sections)

### Stats

- Summarizes user activity from the shared timeline and quiz-answer stores.
- Completed challenges are counted from `TimelineType.CHALLENGE_COMPLETION` events (written once when a quiz session finishes).
- Category Breakdown groups those completions by language/track (Kotlin, Java, AI/LLM, etc.).
- Displays:
  - Events attended, challenges completed, QR scans
  - Percent correct and current streak
  - Coding Challenge Stats (questions answered vs correct)
  - Achievements
- Scrollable layout with fixed top/bottom bars; progress rings use named animation step delays.

[Back to Main Sections](#main-screen-sections)

### Career

- Curated local job directory from `career_listings.json` (not live ATS scraping).
- Search across title, company, description, and technologies.
- Filter chips for Job Category and Employment Type (Games-style UI).
- Listings point at official company careers pages; no fabricated dates or salaries.

[Back to Main Sections](#main-screen-sections)

### Games

- Curated Discover Games showcase (currently **Price of Glory** and **Heroes Vs Villains: Nemesis**).
- Featured tab: showcase cards with Featured / Coming Soon status, plus a MIKROS developer CTA.
- Games tab: search/filter and Mikros catalog sections (intentionally sparse until more titles are added).
- Outbound video, store, website, and Discord actions prefer tracked Appspot URLs; YouTube thumbnails are used for video previews.
- Local drawable logos/screenshots (`pog_*`, `hvn_*`); no placeholder filler games.

[Back to Main Sections](#main-screen-sections)

### Partners

- Curated partner directory from `partners.json` with six categories (Community, Corporate, Education, Game Studios, Government, Technology).
- Category filter chips; featured partners sorted first with accent treatment.
- Data-driven CTAs: website, contact email (picker when multiple), donate, call, wishlist/product, social icons.
- Contact emails open the device mail app with subject: `Got Your Contact Info From Tatum Games. I Have Some Questions`.

[Back to Main Sections](#main-screen-sections)

### Profile / Drawer

- MainScreen includes a hamburger icon that opens a right-side drawer (75% width).
- Drawer contains:
  - Logo (from drawable `logo_text.png`)
  - Menu items: **Profile**, **Demographic Info**, **About Tatum Games**, **FAQ**
  - Bottom links: **Terms**, **Privacy Policy**
- Implemented using `AnimatedVisibility` (or `ModalDrawer`) with clean, modular Composables.
- Profile screen allows users to update their information with proper form validation.
- Demographic Info screen collects optional age range, sex, occupation, salary range, and school data with consent acknowledgements for event analytics.
- About screen provides comprehensive information about Tatum Tech and MIKROS.

[Back to Main Sections](#main-screen-sections)

## Architecture & Best Practices

- **MVVM architecture** with ViewModels for state and business logic.
- **Lazy loading & fallback**: assets loaded into DB if empty, with future API integration.
- **Scalable data models**: JSON schema with pagination, IDs, nested data where appropriate.
- **Loading UX**: spinners rather than text for better UI expectations (Jetpack Compose `CircularProgressIndicator`).
- **Modular UI**: One Composable per screen/component, reusable, maintainable.
- **Naming & styling**: Fully spelled functions and variables; consistent naming.
- **Loading overlay**: Full-screen spinner with dim background and touch blocking context (using techniques like pointerInput overlays).
- **Database-first approach**: Room database with asset fallback for offline functionality.
- **Type-safe navigation**: Enum-based navigation routes for better maintainability.
- **Empty state handling**: Proper empty state messages and UI for better user experience.
- **Consistent navigation**: Bottom navigation bar present on all screens for seamless navigation.

[Back to Table of Contents](#table-of-contents)





