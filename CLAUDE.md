# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SmokeLog is a minimalist smoking habit tracker built with Kotlin Multiplatform (KMP) + Compose Multiplatform, targeting Android and iOS. Uses Kotlin 2.3.0, Compose Multiplatform 1.10.0, and Material3. See `SPEC.md` for the full product specification.

## Build Commands

```bash
# Android
./gradlew :composeApp:assembleDebug        # Build Android debug APK
./gradlew :composeApp:assembleRelease       # Build Android release APK
./gradlew :composeApp:installDebug          # Install on connected Android device/emulator

# iOS (requires macOS with Xcode)
# Open iosApp/iosApp.xcodeproj in Xcode and run from there,
# or use the Kotlin/Native compile tasks:
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
./gradlew :composeApp:linkDebugFrameworkIosArm64

# Tests
./gradlew :composeApp:allTests              # Run all tests
./gradlew :composeApp:testDebugUnitTest     # Run Android unit tests only
```

## Architecture

**MVVM** with shared ViewModels. The project is currently scaffolded from the KMP wizard and will be restructured per `SPEC.md`.

**Current module structure:**
- `composeApp/` — single KMP module with all shared and platform-specific code
- `iosApp/` — thin SwiftUI wrapper hosting Compose UI via `UIViewControllerRepresentable`

**Planned module structure** (AGP 9.0 migration, see `SPEC.md`):
- `shared/` — replaces `composeApp/`, uses `com.android.kotlin.multiplatform.library` plugin
- `androidApp/` — separate Android entry point module with `com.android.application` plugin
- `iosApp/` — stays as-is

**Source sets** (`composeApp/src/`):
- `commonMain` — shared Compose UI and business logic (this is where most code lives)
- `androidMain` — Android entry point (`MainActivity`) and `actual` implementations
- `iosMain` — iOS entry point (`MainViewController`) and `actual` implementations

**Planned package layout** under `commonMain`:
```
di/          # Koin dependency injection
data/        # Database, repositories
domain/      # Use cases, business logic
ui/          # Compose screens & components
  main/      # Main screen (counter + log button)
  history/   # History bottom sheet / screen
  settings/  # Settings screen
  stats/     # Stats screen
util/        # Helpers, extensions
```

**Key patterns:**
- **Expect/actual** for platform-specific code: `expect` declarations in `commonMain`, `actual` implementations in `androidMain` and `iosMain` (see `Platform.kt`)
- **Single shared UI**: All Compose UI is defined in `commonMain` and rendered on both platforms
- **iOS bridge**: `iosApp/iosApp/ContentView.swift` wraps the Kotlin `MainViewController()` into SwiftUI via `UIViewControllerRepresentable`

**Android config:** namespace `yalexaner.smokelog`, minSdk 24, targetSdk 36, compileSdk 36, Java 11.

## Screens

Four screens connected from a main hub:
- **Main** — today's cigarette count, time since last smoke (live-updating), one-tap "Log Smoke" button, settings/stats icon buttons
- **History** — partial bottom sheet (peek shows latest record), expands to full list grouped by day. Records show sequential lifetime number, timestamp, and gap since previous. Swipe-to-edit/delete.
- **Settings** — daily goal, notifications, time format, theme (light/dark/system), CSV export, clear data
- **Stats** — summary cards (today, this week, this month, average/day, longest break). Charts planned for Phase 2.

## Data Models

```kotlin
data class SmokeRecord(
    val id: Long,              // Auto-generated PK
    val number: Int,           // Sequential lifetime count (#1, #2, ...)
    val timestamp: Instant,    // When the smoke occurred (UTC)
    val createdAt: Instant,    // Record creation time
    val modifiedAt: Instant?,  // Last edit time
)

data class UserPreferences(
    val dailyGoal: Int?,                  // null = no goal
    val notificationsEnabled: Boolean,
    val use24HourFormat: Boolean?,        // null = system default
    val theme: Theme,                     // LIGHT, DARK, SYSTEM
)
```

## Key Dependencies (Planned)

| Purpose | Library |
|---------|---------|
| UI | Compose Multiplatform |
| Navigation | Compose Navigation (or Voyager/Decompose) |
| Database | Room (KMP) or SQLDelight |
| DI | Koin |
| Date/Time | kotlinx-datetime |
| Settings | DataStore or multiplatform-settings |
| Async | Kotlin Coroutines + Flow |

## Dependency Management

All versions are centralized in `gradle/libs.versions.toml`. Reference libraries in build scripts using the `libs.*` accessor (e.g., `libs.androidx.activity.compose`).

## Design Principles

- **Minimal friction** — logging a smoke is one tap
- **Non-judgmental** — neutral language, this is a tracking tool not a lecture
- **Glanceable** — count and time-since-last visible immediately
- Log button has haptic feedback; debounce rapid taps (ignore within 2 seconds)
