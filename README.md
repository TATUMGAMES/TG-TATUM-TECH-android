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

- Renders a `LazyColumn` displaying upcoming marketing or coding events.
- Uses local JSON via `/assets` as fallback, or eventual API.
- Handles `Under Review`, `Waiting Assignment`, `Campaign Assigned`, and other statuses with time-based logic.
- Applies smooth scroll optimization and proper state hoisting.
- Features attendee management with friend functionality and registration status tracking.

[Back to Main Sections](#main-screen-sections)

### Coding

- Displays coding challenges fetched from assets or DB.
- On first load, it imports from JSON into the local database if empty.
- Composable reads from a ViewModel backed by Room.
- Select language & difficulty, then drills into challenge details.
- Supports multiple programming languages (Kotlin, JavaScript, Python) and difficulty levels (Beginner, Intermediate).
- Features daily question limits and streak tracking for user engagement.

[Back to Main Sections](#main-screen-sections)

### Community

- Shows community-related information like server status or chat links.
- Currently displays a loading spinner (not text!) during data fetch.
- Replaced legacy "Loading server info..." with a `CircularProgressIndicator` centered.
- Provides Discord integration and community engagement features.

[Back to Main Sections](#main-screen-sections)

### Stats

- Summarizes user activity or performance.
- Previously crashed due to a missing ViewModel initializer (`CodingChallengesViewModel`).
- Now fixed via correct ViewModel injection or removal of unnecessary dependencies.
- Displays comprehensive user statistics including:
  - Events attended
  - Coding challenges completed
  - QR codes scanned
  - Achievement progress
  - Current streaks
- Features animated progress rings and achievement tracking.

[Back to Main Sections](#main-screen-sections)

### Profile / Drawer

- MainScreen includes a hamburger icon that opens a right-side drawer (75% width).
- Drawer contains:
  - Logo (from drawable `logo_text.png`)
  - Menu items: **Profile**, **About Tatum Games**, **FAQ**
  - Bottom links: **Terms**, **Privacy Policy**
- Implemented using `AnimatedVisibility` (or `ModalDrawer`) with clean, modular Composables.
- Profile screen allows users to update their information with proper form validation.
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




