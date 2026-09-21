# TatumTech Android App - UI/UX Design Documentation

## Table of Contents
1. [Navigation Architecture](#navigation-architecture)
2. [Header Component](#header-component)
3. [Home Screen Design](#home-screen-design)
4. [FeatureCard Component](#featurecard-component)
5. [Bottom Navigation](#bottom-navigation)
6. [Screen Patterns](#screen-patterns)
7. [Navigation Drawer](#navigation-drawer)
8. [Design System](#design-system)

---

## Navigation Architecture

### Overview
The app uses Jetpack Compose Navigation with two separate navigation graphs to handle authentication flow and main app flow separately.

### Navigation Graphs

#### 1. AccountSetupGraph
Handles all authentication-related screens before users enter the main app:
- **Start Destination**: `AUTH_SCREEN`
- **Screens**:
  - `AUTH_SCREEN` - Authentication splash/landing screen
  - `SIGN_IN_SCREEN` - User sign-in
  - `SIGN_UP_SCREEN` - User registration
  - `FORGOT_PASSWORD_SCREEN` - Password recovery

#### 2. MainGraph
Main application navigation graph for authenticated users:
- **Start Destination**: `HOME_PAGER_SCREEN`
- **Primary Screens**:
  - `HOME_PAGER_SCREEN` - Main home screen with horizontal pager
  - `MAIN_SCREEN` - Alternative main screen (legacy)
  - `UPCOMING_EVENTS_SCREEN` - Events listing
  - `CODING_CHALLENGES_SCREEN` - Coding challenges/quiz
  - `MY_TIMELINE_SCREEN` - User activity timeline
  - `COMMUNITY_SCREEN` - Community features
  - `SCANNER_SCREEN` - QR code scanner
  - `DONATE_SCREEN` - Donation interface
  - `STATS_SCREEN` - User statistics and achievements
  - `USER_PROFILE_SCREEN` - User profile management

- **Secondary Screens**:
  - `PARTNERS_SCREEN` - Partners listing
  - `RESOURCES_SCREEN` - Resources library
  - `CAREER_SCREEN` - Job listings
  - `GAMES_SCREEN` - Games listing
  - `GAME_DETAILS_SCREEN` - Individual game details (with `gameId` parameter)
  - `ATTENDEES_SCREEN` - Event attendees list (with `eventId` parameter)
  - `about_screen/{contentType}` - About/FAQ content (with `contentType` parameter)

### Navigation Flow

```
AccountSetupGraph
  └─> AUTH_SCREEN
      ├─> SIGN_IN_SCREEN
      ├─> SIGN_UP_SCREEN
      └─> FORGOT_PASSWORD_SCREEN

MainGraph (after authentication)
  └─> HOME_PAGER_SCREEN (start destination)
      ├─> Various feature screens (from FeatureCards)
      └─> Bottom Navigation screens
          ├─> MAIN_SCREEN (Home)
          ├─> CODING_CHALLENGES_SCREEN (Learn)
          ├─> MY_TIMELINE_SCREEN (Timeline)
          └─> STATS_SCREEN (Stats)
```

### Navigation Implementation
- Uses `NavHostController` for navigation state management
- Route-based navigation with string constants defined in `NavRoutes` object
- Parameter passing via route arguments (e.g., `eventId`, `gameId`, `contentType`)
- Back stack management: `popUpTo(MainRoutes.MAIN_SCREEN)` with `inclusive = false` to prevent deep back stacks
- `launchSingleTop = true` to prevent duplicate instances of the same screen

---

## Header Component

### Location
`app/src/main/java/com/tatumgames/tatumtech/android/ui/components/common/Header.kt`

### Overview
The `Header` component is a standardized, reusable header used across **almost all screens** in the app. It provides consistent navigation and title display.

### Implementation Details

#### Structure
- **Layout**: ConstraintLayout for precise positioning
- **Height**: Fixed 80dp
- **Status Bar Padding**: Automatically handles system status bar insets

#### Components

1. **Back Button** (Left-aligned)
   - 32dp × 32dp clickable area
   - Uses `back_arrow` drawable resource
   - Positioned at start of header
   - 4dp padding around icon
   - Optional color tint via `backArrowTint` parameter
   - Visibility controlled by `isBackButtonVisible` (default: `true`)

2. **Title Text** (Center-aligned)
   - Centered horizontally and vertically
   - Uses `StandardText` component (custom text component)
   - Black color (`R.color.black`)
   - Typography: `MaterialTheme.typography.headlineSmall`
   - Bold font weight
   - Center text alignment

3. **Horizontal Divider** (Bottom)
   - 0.5dp thickness
   - Black color
   - Spans full width at bottom of header
   - Provides visual separation between header and content

### Usage Pattern

**Standard Implementation:**
```kotlin
Scaffold(
    topBar = {
        Header(
            text = "Screen Title",
            onBackClick = { navController.popBackStack() }
        )
    },
    bottomBar = {
        BottomNavigationBar(navController = navController)
    },
    containerColor = Color(0xFFF0F0F0)
) { paddingValues ->
    // Screen content
}
```

### Customization Options

- **`text: String`** - Header title text (default: empty string)
- **`isBackButtonVisible: Boolean`** - Show/hide back button (default: `true`)
- **`onBackClick: () -> Unit`** - Back button click handler (default: empty lambda)
- **`backArrowTint: Color?`** - Optional color tint for back arrow (default: `null`)
- **`modifier: Modifier`** - Additional modifier for layout customization

### Screens Using Header

Almost all screens in the app use the Header component, including:
- `CodingChallengesScreen`
- `MyTimelineScreen`
- `StatsScreen`
- `UpcomingEventsScreen`
- `CommunityScreen`
- `DonateScreen`
- `ScannerScreen`
- `AboutScreen`
- `UserProfileScreen`
- And more...

**Exception**: `HomePagerScreen` uses a custom header implementation with app name and hamburger menu (not the standard Header component).

---

## Home Screen Design

### Location
`app/src/main/java/com/tatumgames/tatumtech/android/ui/components/screens/HomePagerScreen.kt`

### Overview
The Home Screen (`HOME_PAGER_SCREEN`) is the primary entry point of the app. It features a **horizontal pager** that allows users to swipe between different categories, each containing a **vertical gridview** of feature cards. This design provides scalability for adding more categories and items without cluttering the interface.

### Screen Structure

The Home Screen is divided into **static elements** (that don't scroll with pager) and **dynamic pager content**:

```
┌─────────────────────────────────────┐
│ STATIC: App Name + Hamburger Menu  │
├─────────────────────────────────────┤
│ STATIC: Greeting Text               │
├─────────────────────────────────────┤
│ STATIC: Pager Tab Bar               │
│ (Events | Coding | Community | Career) │
├─────────────────────────────────────┤
│                                     │
│  DYNAMIC: Horizontal Pager          │
│  ┌─────────────────────────────┐   │
│  │ Vertical GridView of Cards  │   │
│  │ [Card] [Card]               │   │
│  │ [Card] [Card]               │   │
│  │ [Card]                      │   │
│  └─────────────────────────────┘   │
│                                     │
├─────────────────────────────────────┤
│ STATIC: Notifications Title         │
├─────────────────────────────────────┤
│ STATIC: Notifications List          │
└─────────────────────────────────────┘
```

### Static Elements (Outside Pager)

1. **Top Title Row**
   - App name on the left (`R.string.app_name`)
   - Hamburger menu icon on the right
   - Typography: `headlineSmall` with bold weight
   - 8dp vertical padding
   - Full width with space-between arrangement

2. **Greeting Text**
   - Dynamic greeting with user name (or generic greeting)
   - Typography: `headlineMedium` with bold weight
   - 12dp top padding
   - Personalized: "Hello, [Name]!" or generic greeting

3. **Pager Tab Bar**
   - Four category tabs: "Events", "Coding", "Community", "Career"
   - Horizontal arrangement with `SpaceEvenly` distribution
   - Active tab: Bold text with primary color
   - Inactive tabs: Regular text with 70% opacity
   - Clicking a tab animates to that pager page
   - 8dp vertical padding

4. **Notifications Section** (Below Pager)
   - Static "Recent Notifications" title
   - List of notification items
   - Always visible regardless of pager page

### Dynamic Pager Content

#### Horizontal Pager Implementation
- Uses `HorizontalPager` from Compose Foundation
- `PagerState` manages current page index
- Synced with `PagerTabBar` - selecting a tab navigates to that page
- Smooth animation between pages
- Four pages corresponding to four categories

#### Vertical GridView Pattern

Each pager page contains a **vertical scrollable gridview** of `FeatureCard` components:

- **Layout**: Column with `verticalScroll` modifier
- **Grid Pattern**: Rows of 2 cards side-by-side
- **Spacing**: 16dp between rows, 16dp between cards in a row
- **Odd Item Handling**: If there's an odd number of items, the last item spans full width
- **Padding**: 16dp horizontal padding
- **Scalability**: Can handle any number of items - automatically wraps to new rows

**Example Grid Layout:**
```
┌──────────┬──────────┐
│  Card 1  │  Card 2  │
├──────────┼──────────┤
│  Card 3  │  Card 4  │
├──────────┴──────────┤
│      Card 5         │  (full width if odd)
└─────────────────────┘
```

### Category Content

Each category in the pager displays different feature cards:

#### Events Category
- Upcoming Events → `UPCOMING_EVENTS_SCREEN`
- Scanner → `SCANNER_SCREEN`
- Partners → `PARTNERS_SCREEN`
- Resources → `RESOURCES_SCREEN`

#### Coding Category
- Coding Challenges → `CODING_CHALLENGES_SCREEN`
- Stats → `STATS_SCREEN`
- Resources → `RESOURCES_SCREEN`

#### Community Category
- Community → `COMMUNITY_SCREEN`
- Donate → `DONATE_SCREEN`
- Resources → `RESOURCES_SCREEN`

#### Career Category
- Apply for Jobs → `CAREER_SCREEN`
- Resources → `RESOURCES_SCREEN`

### Implementation Files

- **Main Screen**: `HomePagerScreen.kt`
- **Pager Layout**: `HorizontalPagerLayout.kt`
- **Page Content**: `PagerPageContent.kt`
- **Tab Bar**: `PagerTabBar.kt`
- **ViewModel**: `HomePagerViewModel.kt`
- **Feature Card Model**: `FeatureCardItem.kt`

### Design Rationale

1. **Scalability**: Easy to add new categories or items to categories without redesign
2. **Organization**: Content grouped by theme (Events, Coding, Community, Career)
3. **Discoverability**: Horizontal pager allows users to discover different content areas
4. **Consistency**: Static elements (greeting, notifications) always visible for context
5. **Performance**: Only visible page content is rendered (with pager caching)

---

## FeatureCard Component

### Location
`app/src/main/java/com/tatumgames/tatumtech/android/ui/components/screens/main/FeatureCard.kt`

### Overview
`FeatureCard` is a reusable card component used throughout the app, primarily in the Home Screen's pager gridview. It displays an icon/image with text and handles navigation when clicked.

### Component Structure

```
┌─────────────────────────┐
│ ┌──────────┐            │
│ │   Icon   │            │ 36dp × 36dp
│ │  (36dp)  │            │ Rounded background
│ └──────────┘            │
│                         │
│ Card Title Text         │ 16sp, Medium weight
└─────────────────────────┘
     100dp height
```

### Dimensions and Styling

- **Height**: Fixed 100dp
- **Width**: Flexible (weight-based in grid, or full width)
- **Shape**: Rounded corners (12dp radius)
- **Elevation**: 4dp default (configurable)
- **Background Color**: White (configurable via `backgroundColor` parameter)
- **Padding**: 16dp internal padding

### Icon/Image Placement

#### Icon Container
- **Size**: 36dp × 36dp square
- **Background**: Light purple (`Color(0xFFEDE7F6)`) by default
- **Shape**: Rounded corners (8dp radius)
- **Internal Padding**: 6dp
- **Position**: Top-left of card
- **Content**: Centered icon or image

#### Icon Types
The component accepts either:
1. **ImageVector** (`icon: ImageVector?`) - Material Icons
2. **Painter** (`image: Painter?`) - Drawable resources/images

One of these must be provided (enforced via `require` check).

#### Icon Styling
- **Tint**: Configurable via `iconTint` parameter (default: `Color.Unspecified`)
- **Background Color**: Configurable via `iconBackground` parameter (default: `Color(0xFFEDE7F6)`)

### Text Content

- **Position**: Below icon, left-aligned
- **Typography**: 
  - Font size: 16sp
  - Font weight: Medium (`FontWeight.Medium`)
- **Color**: Black by default (configurable via `textColor` parameter)
- **Spacing**: 8dp gap between icon and text

### Layout Implementation

The card uses a `Column` layout with:
- `fillMaxSize()` modifier
- `padding(16.dp)` for internal spacing
- `horizontalAlignment = Alignment.Start` (left-aligned content)
- `verticalArrangement = Arrangement.Center` (centered vertically)

### Grid Layout Pattern

When used in the Home Screen's pager, FeatureCards are arranged in a specific grid pattern:

1. **Two Cards Per Row**: Cards are grouped into rows of 2
2. **Equal Width**: Each card in a row gets `weight(1f)` modifier
3. **Row Spacing**: 16dp gap between cards horizontally
4. **Column Spacing**: 16dp gap between rows vertically
5. **Odd Item Handling**: If there's an odd number of items, the last card spans full width (`fillMaxWidth()`)

**Implementation Logic** (from `PagerPageContent.kt`):
```kotlin
while (index < items.size) {
    if (index < items.size - 1) {
        // Two cards side-by-side
        Row {
            FeatureCard(...) // weight(1f)
            FeatureCard(...) // weight(1f)
        }
        index += 2
    } else {
        // Single card full width
        FeatureCard(...) // fillMaxWidth()
        index += 1
    }
}
```

### Click Handling

- **Clickable**: Entire card is clickable (when `onClick` is provided)
- **Navigation**: Typically navigates to a route via `navController.navigate(route)`
- **Disabled State**: If `onClick` is `null`, card is not clickable

### Customization Parameters

- `icon: ImageVector?` - Material icon (optional)
- `image: Painter?` - Image/drawable (optional)
- `text: String` - Card title text (required)
- `modifier: Modifier` - Additional layout modifiers
- `onClick: (() -> Unit)?` - Click handler (optional)
- `elevation: Dp` - Card elevation (default: 4dp)
- `backgroundColor: Color` - Card background (default: White)
- `iconTint: Color` - Icon color tint (default: Unspecified)
- `iconBackground: Color` - Icon background (default: `0xFFEDE7F6`)
- `textColor: Color` - Text color (default: Black)

---

## Bottom Navigation

### Location
`app/src/main/java/com/tatumgames/tatumtech/android/ui/components/common/BottomNavigationBar.kt`

### Overview
The Bottom Navigation Bar provides persistent navigation access to the four main sections of the app. It appears at the bottom of almost all screens using a Material 3 `NavigationBar` component.

### Navigation Items

The bottom navigation contains **4 items**:

1. **Home** 
   - Icon: `Icons.Default.Home`
   - Route: `NavRoutes.MAIN_SCREEN`
   - Label: "Home" (from `R.string.home`)

2. **Learn**
   - Icon: `Icons.Default.Face`
   - Route: `NavRoutes.CODING_CHALLENGES_SCREEN`
   - Label: "Learn" (from `R.string.learn`)

3. **Timeline**
   - Icon: `Icons.Default.DateRange`
   - Route: `NavRoutes.MY_TIMELINE_SCREEN`
   - Label: "Timeline" (from `R.string.timeline`)

4. **Stats**
   - Icon: `Icons.Default.Star`
   - Route: `NavRoutes.STATS_SCREEN`
   - Label: "Stats" (from `R.string.stats`)

### Visual Design

- **Container Color**: White (`Color.White`)
- **Elevation**: 8dp tonal elevation
- **Always Show Labels**: `alwaysShowLabel = true` (labels always visible, not just when selected)
- **Icons**: Material Icons (filled style)
- **Selected State**: Highlighted icon and text when current route matches
- **Unselected State**: Normal icon and text styling

### Navigation Behavior

#### Route Matching
- Uses `currentBackStackEntryAsState()` to determine current route
- Compares current route with each item's route
- Highlights matching item as selected

#### Navigation Actions
When an item is clicked:
- Checks if current route differs from target route
- If different, navigates using `navController.navigate(item.route)`
- Uses `popUpTo(NavRoutes.MAIN_SCREEN)` with `inclusive = false`
- Sets `launchSingleTop = true` to prevent duplicate instances
- This ensures the back stack doesn't grow too deep

#### Navigation Stack Management
```kotlin
navController.navigate(item.route) {
    popUpTo(NavRoutes.MAIN_SCREEN) {
        inclusive = false
    }
    launchSingleTop = true
}
```

This means:
- When navigating to any bottom nav item, the back stack pops to (but doesn't include) `MAIN_SCREEN`
- Prevents deep navigation stacks
- Ensures single instance of each screen
- Back button from bottom nav screens goes to home

### Usage Pattern

**Standard Implementation:**
```kotlin
Scaffold(
    topBar = {
        Header(...)
    },
    bottomBar = {
        BottomNavigationBar(navController = navController)
    },
    containerColor = Color(0xFFF0F0F0)
) { paddingValues ->
    // Screen content
}
```

### Icon Placement and Styling

- **Icon Size**: Default Material 3 `NavigationBarItem` icon size (24dp)
- **Icon Position**: Above label text
- **Icon Color**: Material theme colors (primary for selected, default for unselected)
- **Label Position**: Below icon
- **Label Typography**: Material 3 default navigation bar label style

### Accessibility

- All icons have `contentDescription` set to the label text
- Labels are always visible for clarity
- Touch targets meet Material Design guidelines (minimum 48dp)

---

## Screen Patterns

### Standard Screen Structure

Almost all screens in the app follow a consistent pattern using Jetpack Compose's `Scaffold` component:

```kotlin
Scaffold(
    topBar = {
        Header(
            text = "Screen Title",
            onBackClick = { navController.popBackStack() }
        )
    },
    bottomBar = {
        BottomNavigationBar(navController = navController)
    },
    containerColor = Color(0xFFF0F0F0)
) { paddingValues ->
    // Screen content with padding
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Content here
    }
}
```

### Common Elements

#### 1. Scaffold Structure
- **`topBar`**: Contains `Header` component (except HomePagerScreen)
- **`bottomBar`**: Contains `BottomNavigationBar` component
- **`containerColor`**: Light gray background (`Color(0xFFF0F0F0)`)
- **`content`**: Screen-specific content with padding from `paddingValues`

#### 2. Background Color
- Consistent light gray background: `Color(0xFFF0F0F0)`
- Provides visual consistency across all screens
- Soft, non-distracting background for content

#### 3. Padding and Spacing
- **Scaffold Padding**: Automatically handled by `paddingValues` from Scaffold
- **Content Padding**: Typically 16dp horizontal, 10-16dp vertical
- **Element Spacing**: Consistent 8dp, 12dp, 16dp, 24dp spacing used throughout

#### 4. Content Layout
- Most screens use `Column` for vertical content
- Scrollable content uses `verticalScroll(rememberScrollState())`
- Grid layouts use `LazyVerticalGrid` for performance

### Screen-Specific Patterns

#### Screens with Lists
- Use `LazyColumn` or `LazyVerticalGrid` for efficient rendering
- Example: `UpcomingEventsScreen`, `CodingChallengesScreen`

#### Screens with Forms
- Use `Column` with `verticalScroll`
- Form fields with consistent spacing
- Example: `SignInScreen`, `SignUpScreen`

#### Detail Screens
- Header with back button
- Content area with scrollable content
- Example: `GameDetailsScreen`, `AttendeesScreen`

### Exceptions to Standard Pattern

#### HomePagerScreen
- Does **not** use `Header` component in `topBar`
- Uses custom header with app name and hamburger menu
- Has horizontal pager instead of standard content
- Still uses `BottomNavigationBar`

#### Authentication Screens
- Do **not** use `BottomNavigationBar`
- Do **not** use `Header` component
- Custom layouts for sign-in, sign-up, etc.

---

## Navigation Drawer

### Location
`app/src/main/java/com/tatumgames/tatumtech/android/ui/components/screens/main/UserProfileDrawer.kt`

### Overview
The Navigation Drawer is a right-side sliding panel that provides access to user profile options, settings, and additional navigation links. It's accessed via the hamburger menu icon in the Home Screen's top bar.

### Drawer Structure

```
┌─────────────────────────────────────┐
│                                     │
│  [Overlay - 25% width]              │
│  Semi-transparent black (30% alpha) │
│                                     │
│  [Drawer - 75% width]               │
│  ┌─────────────────────────────┐   │
│  │ White Background             │   │
│  │                              │   │
│  │ - Logo                       │   │
│  │ - Menu Items                 │   │
│  │ - User Profile               │   │
│  │ - Settings                   │   │
│  │ - About/FAQ                  │   │
│  │ - Terms & Privacy            │   │
│  │                              │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

### Implementation Details

#### Drawer Dimensions
- **Drawer Width**: 75% of screen width (`screenWidth * 0.75f`)
- **Overlay Width**: 25% of screen width (remaining space)
- **Drawer Position**: Right side of screen (`Alignment.CenterEnd`)
- **Full Height**: Spans entire screen height

#### Overlay
- **Color**: Black with 30% opacity (`Color.Black.copy(alpha = 0.3f)`)
- **Clickable**: Clicking overlay closes the drawer
- **Coverage**: Spans full screen behind drawer
- **Purpose**: Provides visual separation and focus on drawer content

#### Animation
- **Enter Animation**: Slide in from right (`slideInHorizontally`)
- **Exit Animation**: Slide out to right (`slideOutHorizontally`)
- **Duration**: 500ms animation
- **Easing**: Default easing curve

### Drawer Content

The drawer contains several sections:

1. **Logo Section**
   - Tatum Games logo/logo text at top
   - Provides brand identity

2. **Menu Items**
   - Navigation links to various screens
   - User Profile
   - Settings
   - About Tatum Games
   - FAQ
   - Terms & Privacy

3. **User Information**
   - User profile details
   - Account-related information

4. **Footer**
   - Terms and Privacy text at bottom
   - Scrollable content area

### Trigger Mechanism

The drawer is opened from the **Home Screen** (`HomePagerScreen`):

1. **Hamburger Menu Icon**: Top-right corner of screen
2. **Click Action**: Sets `isDrawerOpen = true`
3. **State Management**: Uses `remember { mutableStateOf(false) }` for visibility

### Drawer State Management

```kotlin
var isDrawerOpen by remember { mutableStateOf(false) }

// Hamburger menu click
Icon(
    modifier = Modifier.clickable { isDrawerOpen = true }
)

// Drawer visibility
AnimatedVisibility(
    visible = isDrawerOpen,
    enter = fadeIn(...),
    exit = fadeOut(...)
) {
    // Drawer content with overlay
}
```

### Closing the Drawer

The drawer can be closed by:
1. **Overlay Click**: Clicking the semi-transparent overlay area
2. **Close Button**: If present in drawer content
3. **Navigation**: Navigating to a new screen (drawer closes automatically)
4. **Back Button**: System back button closes drawer

### Drawer Content Implementation

The drawer uses `UserProfileDrawer` composable:
- **Scrollable**: Uses `verticalScroll` for content that exceeds screen height
- **Layout**: Column with spacing between sections
- **Padding**: 16dp internal padding
- **Alignment**: Top-aligned content with footer at bottom

### Design Rationale

1. **Right-Side Placement**: Follows Material Design guidelines for navigation drawers
2. **75/25 Split**: Provides enough space for content while maintaining overlay visibility
3. **Smooth Animation**: Professional slide-in animation enhances user experience
4. **Overlay Interaction**: Clickable overlay makes closing intuitive
5. **Scrollable Content**: Handles varying amounts of content gracefully

---

## Design System

### Color Palette

#### Primary Colors
- **Background**: `Color(0xFFF0F0F0)` - Light gray used across all screens
- **Card Background**: `Color.White` - Standard card background
- **Text**: `Color.Black` (`R.color.black`) - Primary text color
- **Primary Theme**: Material Theme primary color (purple tones)

#### Icon Backgrounds
- **Default Icon Background**: `Color(0xFFEDE7F6)` - Light purple for FeatureCard icons
- **Icon Tint**: Configurable, defaults to `Color.Unspecified`

#### Navigation
- **Bottom Nav Background**: `Color.White`
- **Bottom Nav Elevation**: 8dp
- **Header Divider**: Black, 0.5dp

### Typography

#### Text Styles
- **App Name/Title**: `headlineSmall` with bold weight
- **Greeting**: `headlineMedium` with bold weight
- **Section Titles**: `titleMedium` with bold weight
- **Card Text**: 16sp, Medium weight
- **Body Text**: Material Theme default body styles
- **Button Text**: Material Theme button styles

#### Font Weights
- **Bold**: Used for titles, headings, selected states
- **Medium**: Used for card text, labels
- **Regular**: Default body text

### Spacing System

Consistent spacing values used throughout:
- **4dp**: Tight spacing (icon padding)
- **8dp**: Small spacing (between icon and text in cards, tab padding)
- **12dp**: Medium-small spacing
- **16dp**: Standard spacing (card padding, grid gaps, content padding)
- **24dp**: Medium spacing (section separators)
- **30dp**: Large spacing (major section breaks)

### Component Elevations

- **FeatureCard**: 4dp default elevation
- **Bottom Navigation**: 8dp tonal elevation
- **Drawer Overlay**: No elevation (background layer)

### Icon Sizes

- **Header Back Button**: 32dp × 32dp
- **FeatureCard Icon Container**: 36dp × 36dp
- **Hamburger Menu Icon**: Default Material icon size
- **Bottom Navigation Icons**: Default Material 3 NavigationBar icon size (24dp)

### Border Radius

- **FeatureCard**: 12dp rounded corners
- **Icon Container in Cards**: 8dp rounded corners
- **Other Cards**: Varies by component

### Layout Guidelines

#### Screen Padding
- **Horizontal**: 16dp standard
- **Vertical**: 10-16dp standard
- **Scaffold Padding**: Handled automatically via `paddingValues`

#### Grid Layouts
- **Columns**: 2 columns standard (FeatureCard grid, Section grid)
- **Gaps**: 16dp horizontal and vertical spacing

#### Card Dimensions
- **FeatureCard Height**: Fixed 100dp
- **FeatureCard Width**: Flexible (weight-based or full width)
- **Header Height**: Fixed 80dp

---

## Summary

This design documentation covers the key UI/UX patterns and components used throughout the TatumTech Android app:

1. **Consistent Navigation**: Two navigation graphs handle auth and main app flows
2. **Standardized Header**: Header component used across all screens for consistency
3. **Scalable Home Screen**: Horizontal pager with vertical gridview allows easy expansion
4. **Reusable Components**: FeatureCard and other components promote consistency
5. **Persistent Navigation**: Bottom navigation provides constant access to main sections
6. **Standard Patterns**: Scaffold-based screen structure ensures consistency
7. **User Access**: Right-side drawer provides additional navigation and settings

The design prioritizes **scalability**, **consistency**, and **user experience** through reusable components and established patterns.

