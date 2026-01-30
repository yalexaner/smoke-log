# Smoke Tracker - Project Specification

## Overview

A minimalist mobile app to track smoking habits, designed to help users become more aware of their smoking patterns and potentially reduce consumption over time.

**Platforms:** Android, iOS  
**Tech Stack:** Kotlin Multiplatform (KMP) + Compose Multiplatform  
**Architecture:** MVVM with shared ViewModels  

---

## Core Features

### 1. Main Screen

The primary interface users interact with daily.

#### Layout
```
┌─────────────────────────────────┐
│ [⚙️]                      [📊] │  ← Settings (left), Stats (right)
│                                 │
│                                 │
│              42                 │  ← Large counter (today's count)
│        "cigarettes today"       │
│                                 │
│         ⏱️ 2h 34m ago           │  ← Time since last smoke
│                                 │
│                                 │
│         [ + Log Smoke ]         │  ← Primary action button
│                                 │
│                                 │
├─────────────────────────────────┤
│ ┊ #42 • 2:34 PM • 2h 34m ago  ┊ │  ← Minimized history sheet
│ ┊         ≡ drag handle       ┊ │     (shows most recent entry)
└─────────────────────────────────┘
```

#### Components

| Component | Description | Interaction |
|-----------|-------------|-------------|
| Counter | Large, prominent number showing today's smoke count | Display only |
| Time Since Last | Shows elapsed time since most recent smoke (updates live) | Display only |
| Log Button | Primary action to record a new smoke | Tap → records smoke with current timestamp |
| Settings Button | Icon button in top-left corner | Tap → navigate to Settings screen |
| Stats Button | Icon button in top-right corner | Tap → navigate to Stats screen |
| History Sheet | Partial bottom sheet showing most recent record | Tap/swipe up → expand to full history |

#### Behavior
- Counter resets at midnight (local time)
- "Time since last" updates every minute (or more frequently when < 1 hour)
- Log button should have haptic feedback on tap
- After logging, briefly show confirmation (subtle animation or toast)

---

### 2. History Sheet / Screen

Expandable view showing all smoking records.

#### Minimized State (Bottom Sheet - Peek)
- Shows only the most recent smoke record
- Visible height: ~60-80dp
- Contains drag handle indicator
- Single tap or swipe up to expand

#### Expanded State (Full Sheet or Screen)
- List of all records grouped by day
- Smooth transition from minimized state
- Can be dismissed by swiping down or tapping outside

#### Record Display Format
```
┌─────────────────────────────────┐
│ Today, January 30                │  ← Day header (sticky)
├─────────────────────────────────┤
│ #42  •  2:34 PM  •  2h 15m      │  ← Record: number, time, gap
│ #41  •  12:19 PM •  45m         │
│ #40  •  11:34 AM •  1h 02m      │
│ ...                              │
├─────────────────────────────────┤
│ Yesterday, January 29            │  ← Previous day header
├─────────────────────────────────┤
│ #39  •  11:42 PM •  3h 20m      │
│ ...                              │
└─────────────────────────────────┘
```

#### Record Fields
| Field | Description | Format |
|-------|-------------|--------|
| Number | Sequential record number (lifetime count) | #N |
| Time | When the smoke was logged | 12-hour or 24-hour based on device settings |
| Date | Only shown if not today | "Yesterday", "Mon, Jan 28", etc. |
| Gap | Time elapsed since previous smoke | "45m", "2h 15m", "1d 3h" |

#### Record Actions
Each record can be edited or deleted:

**Option A: Swipe Actions (Recommended)**
- Swipe left → reveal Delete button (red)
- Swipe right → reveal Edit button (blue)
- Delete shows confirmation dialog

**Option B: Long Press Context Menu**
- Long press → show menu with Edit/Delete options

**Option C: Inline Icon Buttons**
- Small edit/delete icons visible on each row
- Less clean but more discoverable

> **Decision needed:** Choose Option A (swipe) for modern feel, with Option B as fallback for accessibility.

#### Edit Functionality
- Can modify the timestamp (date and time)
- Cannot change the record number (it's sequential)
- Show date/time pickers native to each platform

---

### 3. Settings Screen

App configuration and preferences.

#### Settings Options

| Setting | Type | Description | Default |
|---------|------|-------------|---------|
| Daily Goal | Number input | Target max cigarettes per day (optional) | Off |
| Notifications | Toggle | Remind if exceeding goal | Off |
| Time Format | Toggle | 12-hour vs 24-hour display | System default |
| Theme | Selector | Light / Dark / System | System |
| Export Data | Button | Export history as CSV | - |
| Clear All Data | Button | Delete all records (with confirmation) | - |
| About | Info | Version, credits, links | - |

#### Navigation
- Back button/gesture returns to Main screen
- Settings are persisted immediately (no save button)

---

### 4. Stats Screen

Analytics and insights about smoking patterns.

#### Statistics to Display

**Summary Cards**
| Stat | Description |
|------|-------------|
| Today | Count for current day |
| This Week | Count for current week (Mon-Sun or Sun-Sat based on locale) |
| This Month | Count for current calendar month |
| Average per Day | Calculated over last 30 days |
| Longest Break | Maximum gap between two smokes |
| Current Streak | If implementing "smoke-free" tracking |

**Charts (Phase 2)**
- Bar chart: daily counts for last 7 or 14 days
- Line chart: trend over last 30 days
- Heat map: hourly distribution (when do you smoke most?)

#### Design Notes
- Keep it simple initially (just summary cards)
- Charts can be added in Phase 2
- Consider using positive framing where possible

---

## Data Model

### SmokeRecord

```kotlin
data class SmokeRecord(
    val id: Long,                    // Auto-generated primary key
    val number: Int,                 // Sequential lifetime count (#1, #2, #3...)
    val timestamp: Instant,          // When the smoke was logged (UTC)
    val createdAt: Instant,          // When record was created (for sync purposes)
    val modifiedAt: Instant?,        // When record was last edited
)
```

### UserPreferences

```kotlin
data class UserPreferences(
    val dailyGoal: Int?,             // null = no goal set
    val notificationsEnabled: Boolean,
    val use24HourFormat: Boolean?,   // null = use system default
    val theme: Theme,                // LIGHT, DARK, SYSTEM
)

enum class Theme { LIGHT, DARK, SYSTEM }
```

---

## Technical Architecture

### Module Structure (AGP 9.0 Compatible)

```
smoke-tracker/
├── androidApp/                      # Android entry point
│   ├── build.gradle.kts             # com.android.application
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── kotlin/.../MainActivity.kt
│
├── iosApp/                          # iOS entry point (Xcode)
│   └── iosApp/
│       ├── iOSApp.swift
│       └── ContentView.swift
│
├── shared/                          # All shared code
│   ├── build.gradle.kts             # kotlin.multiplatform + com.android.kotlin.multiplatform.library
│   └── src/
│       ├── commonMain/              # Shared code
│       │   └── kotlin/.../
│       │       ├── App.kt           # Root composable
│       │       ├── di/              # Dependency injection (Koin)
│       │       ├── data/            # Database, repositories
│       │       ├── domain/          # Use cases, business logic
│       │       ├── ui/              # Compose screens & components
│       │       │   ├── main/
│       │       │   ├── history/
│       │       │   ├── settings/
│       │       │   ├── stats/
│       │       │   └── components/
│       │       └── util/            # Helpers, extensions
│       │
│       ├── androidMain/             # Android-specific
│       │   └── kotlin/.../
│       │       └── Platform.android.kt
│       │
│       └── iosMain/                 # iOS-specific
│           └── kotlin/.../
│               └── Platform.ios.kt
│
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml
```

### Dependencies

| Purpose | Library | Notes |
|---------|---------|-------|
| UI | Compose Multiplatform | Shared UI for Android & iOS |
| Navigation | Compose Navigation | Or Voyager/Decompose |
| Database | Room (KMP) or SQLDelight | Local persistence |
| DI | Koin | Multiplatform dependency injection |
| Date/Time | kotlinx-datetime | Multiplatform date/time handling |
| Settings | DataStore or multiplatform-settings | Key-value preferences |
| Async | Kotlin Coroutines + Flow | Reactive data streams |

### Suggested libs.versions.toml

```toml
[versions]
kotlin = "2.1.0"
agp = "9.0.0"
compose-multiplatform = "1.7.3"
koin = "4.0.0"
room = "2.7.0-alpha12"
kotlinx-datetime = "0.6.1"
datastore = "1.1.2"

[libraries]
# Compose
compose-ui = { module = "org.jetbrains.compose.ui:ui" }
compose-material3 = { module = "org.jetbrains.compose.material3:material3" }

# Room (KMP)
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }

# Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }

# Utilities
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "kotlinx-datetime" }

[plugins]
kotlinMultiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
androidApplication = { id = "com.android.application", version.ref = "agp" }
androidMultiplatformLibrary = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
composeMultiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
composeCompiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
room = { id = "androidx.room", version.ref = "room" }
```

---

## AGP 9.0 Migration Checklist

Since the JetBrains wizard currently generates AGP 8.x projects, you need to migrate:

### Step 1: Create androidApp module
- [ ] Create `androidApp/` directory
- [ ] Create `androidApp/build.gradle.kts`
- [ ] Create `androidApp/src/main/AndroidManifest.xml`
- [ ] Move `MainActivity.kt` from `composeApp` to `androidApp`
- [ ] Add `include(":androidApp")` to `settings.gradle.kts`

### Step 2: Update shared module (composeApp → shared)
- [ ] Rename `composeApp` to `shared` (optional but cleaner)
- [ ] Replace `com.android.library` with `com.android.kotlin.multiplatform.library`
- [ ] Remove `org.jetbrains.kotlin.android` plugin (not needed)
- [ ] Update `debugImplementation` → `androidRuntimeClasspath` for tooling deps
- [ ] Remove Android app configuration (applicationId, etc.) from shared module

### Step 3: Update versions
- [ ] Update AGP to 9.0.0 in `libs.versions.toml`
- [ ] Ensure Gradle is 9.1.0+
- [ ] Ensure Compose Multiplatform is 1.9.3+ (AGP 9 compatible)

### Step 4: Update root build.gradle.kts
- [ ] Add `alias(libs.plugins.androidMultiplatformLibrary) apply false`

### Step 5: Verify
- [ ] Run Android app
- [ ] Run iOS app
- [ ] Delete old run configurations

---

## Screen Flow

```
                    ┌─────────────┐
                    │   Launch    │
                    └──────┬──────┘
                           │
                           ▼
┌──────────────┐    ┌─────────────┐    ┌──────────────┐
│   Settings   │◄───│    Main     │───►│    Stats     │
│    Screen    │    │   Screen    │    │    Screen    │
└──────────────┘    └──────┬──────┘    └──────────────┘
                           │
                           │ expand sheet
                           ▼
                    ┌─────────────┐
                    │   History   │
                    │   (Sheet)   │
                    └──────┬──────┘
                           │
                      ┌────┴────┐
                      ▼         ▼
               ┌──────────┐  ┌──────────┐
               │  Edit    │  │  Delete  │
               │  Dialog  │  │  Confirm │
               └──────────┘  └──────────┘
```

---

## UI/UX Guidelines

### Design Principles
1. **Minimal friction** - Logging a smoke should be one tap
2. **Non-judgmental** - Avoid negative language; this is a tracking tool
3. **Glanceable** - Key info (count, time since last) visible immediately
4. **Native feel** - Use platform conventions where possible

### Color Palette (Suggestion)

| Element | Light Mode | Dark Mode |
|---------|------------|-----------|
| Background | #FAFAFA | #121212 |
| Surface | #FFFFFF | #1E1E1E |
| Primary | #5C6BC0 (Indigo) | #7986CB |
| On Primary | #FFFFFF | #000000 |
| Counter Text | #212121 | #EEEEEE |
| Secondary Text | #757575 | #AAAAAA |
| Destructive | #E53935 | #EF5350 |

### Typography
- Counter: Large display font (48-64sp)
- Time since: Medium (16-18sp)
- List items: Regular (14-16sp)
- Use system font (Roboto on Android, SF Pro on iOS)

### Animations
- Counter increment: Subtle scale pulse
- Sheet expand/collapse: Spring animation
- Record delete: Slide out with fade

---

## Development Phases

### Phase 1: MVP (Current Focus)
- [x] Project setup with KMP
- [ ] AGP 9.0 migration
- [ ] Main screen with counter and log button
- [ ] Basic history list (no grouping yet)
- [ ] Local database (Room or SQLDelight)
- [ ] Simple bottom sheet

### Phase 2: Polish
- [ ] Day grouping in history
- [ ] Time since last (live updating)
- [ ] Edit/delete records
- [ ] Settings screen (basic)
- [ ] Dark mode support

### Phase 3: Insights
- [ ] Stats screen with summary cards
- [ ] Daily goal setting
- [ ] Simple charts/graphs
- [ ] Export to CSV

### Phase 4: Engagement (Optional)
- [ ] Notifications (goal exceeded)
- [ ] Widgets (Android/iOS)
- [ ] Apple Watch / Wear OS companion
- [ ] Cloud sync

---

## Open Questions

1. **Sequential numbering**: Should record numbers be lifetime (#1, #2, #3...) or reset daily (#1 today, #1 tomorrow)?
   > Recommendation: Lifetime - more meaningful for tracking progress

2. **Backdating**: Can users log a smoke for a past time (e.g., "I smoked 30 min ago but forgot to log")?
   > Recommendation: Yes, via edit or "Log for earlier" option

3. **Multiple logs**: What if user taps log button multiple times quickly?
   > Recommendation: Debounce (ignore taps within 2 seconds) + show brief confirmation

4. **Midnight rollover**: If user logs at 11:59 PM, does it count for today or tomorrow?
   > Recommendation: Use exact timestamp; "today" is based on local device time

5. **Sheet vs Screen**: Should expanded history be a modal bottom sheet or a full screen?
   > Recommendation: Start with bottom sheet (more modern), can add full-screen option later

---

## Resources

- [KMP Documentation](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [AGP 9.0 Migration Guide](https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html)
- [Room KMP](https://developer.android.com/kotlin/multiplatform/room)
- [Material 3 Design](https://m3.material.io/)

---

*Last updated: January 30, 2026*
