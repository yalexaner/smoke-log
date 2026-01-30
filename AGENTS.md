# AGENTS

Guidance for coding agents working in this repository.
Align changes with SPEC and existing conventions.

## Project at a glance
- Product: minimalist smoking habit tracker.
- Platforms: Android + iOS via Kotlin Multiplatform.
- UI: Compose Multiplatform, shared UI in `composeApp`.
- Architecture: MVVM with shared ViewModels.
- Current module: `composeApp` (Android app + shared code).
- Planned migration (SPEC): split into `shared` + `androidApp` for AGP 9.

## Repository layout (current)
- `composeApp/src/commonMain`: shared UI + domain.
- `composeApp/src/androidMain`: Android entry point + actuals.
- `composeApp/src/iosMain`: iOS entry point + actuals.
- `iosApp/iosApp`: SwiftUI host for Compose UI.

## Build and run (Gradle)
- Windows Android debug APK: `.\gradlew.bat :composeApp:assembleDebug`
- macOS/Linux Android debug APK: `./gradlew :composeApp:assembleDebug`
- Android release APK: `./gradlew :composeApp:assembleRelease`
- Install on device/emulator: `./gradlew :composeApp:installDebug`

## iOS build (macOS only)
- Open `iosApp/iosApp.xcodeproj` in Xcode and run.
- Or build frameworks:
- `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`
- `./gradlew :composeApp:linkDebugFrameworkIosArm64`

## Tests
- All tests: `./gradlew :composeApp:allTests`
- Android unit tests: `./gradlew :composeApp:testDebugUnitTest`
- Single JVM test (class): `./gradlew :composeApp:testDebugUnitTest --tests "pkg.ClassName"`
- Single JVM test (method): `./gradlew :composeApp:testDebugUnitTest --tests "pkg.ClassName.testName"`
- If adding tests, place in `composeApp/src/commonTest` or `composeApp/src/androidTest`.

## Lint
- Android lint (Debug): `./gradlew :composeApp:lintDebug`
- No ktlint/detekt config present; follow Kotlin official style.

## Product principles (SPEC)
- Minimal friction: logging a smoke is one tap.
- Non-judgmental language; neutral UI copy.
- Glanceable: count and time-since visible immediately.
- Native feel: platform conventions where possible.

## Main screen behavior (SPEC)
- Counter resets at local midnight.
- Time since last smoke updates every minute (or faster < 1h).
- Log button provides haptic feedback.
- Debounce rapid taps; ignore within 2 seconds.
- After logging, show a brief confirmation/animation.

## History behavior (SPEC)
- Bottom sheet peek shows most recent record.
- Expanded history is grouped by day; sticky headers.
- Record shows lifetime number, timestamp, and gap.
- Preferred actions: swipe left delete, swipe right edit.
- Delete must confirm; edit adjusts timestamp only.

## Settings + Stats (SPEC)
- Settings are applied immediately (no save button).
- Theme: Light/Dark/System; time format 12/24/system.
- Stats show today/week/month, average/day, longest break.
- Charts are Phase 2; keep initial UI simple.

## Data model rules (SPEC)
- `SmokeRecord.number` is lifetime sequential count.
- Timestamps stored in UTC; display in local time.
- `UserPreferences` supports nullable daily goal and time format.

## Architecture notes
- MVVM with shared ViewModels; keep UI logic minimal.
- Favor shared code in `commonMain`; use expect/actual for platform APIs.

## Kotlin/Compose style
- Kotlin official style (`kotlin.code.style=official`).
- Indent 4 spaces; no tabs.
- Use trailing commas in multiline argument lists and calls.
- Compose: prefer small composables; avoid giant functions.
- State hoisting: pass state + events down, keep sources up.
- Use `remember` and `LaunchedEffect` for side effects.
- Use `@Preview` for focused UI previews (Android).

## Imports
- Group imports with blank lines: stdlib, AndroidX/Compose, project.
- Remove unused imports.
- Wildcard imports are acceptable for `androidx.compose.runtime.*` if consistent.

## Naming
- Composables/classes/objects: PascalCase.
- Functions/vars: lowerCamel.
- Packages: lower_snake.
- Constants: UPPER_SNAKE.

## Types and mutability
- Prefer `val` and immutable data structures.
- Use `data class` for models.
- Use `var` only for Compose state or unavoidable mutation.

## Error handling
- Use `require`/`check` for invariants.
- Map recoverable failures into domain/UI state.
- Do not swallow exceptions; log or surface via state.

## Compose UI patterns
- Use `Modifier` chaining vertically, one per line.
- Keep layout parameters (padding/spacing) in constants.
- Avoid heavy work in composables; move to ViewModels/use cases.
- Prefer Material 3 components unless platform-specific UI is required.

## Platform specifics
- `expect` declarations live in `commonMain`.
- `actual` implementations in `androidMain` and `iosMain`.
- Keep platform-specific dependencies out of `commonMain`.

## Dependencies and versions
- Versions are centralized in `gradle/libs.versions.toml`.
- Use `libs.*` accessors in Gradle build scripts.

## Planned migration (SPEC, not implemented yet)
- Target AGP 9.x; split into `shared` + `androidApp` modules.
- Do not assume new modules exist until migration is done.

## Cursor/Copilot rules
- No `.cursor/rules/**`, `.cursorrules`, or `.github/copilot-instructions.md` found.

## Contribution hygiene
- Keep edits scoped to requested work; avoid unrelated refactors.
- Respect existing file structure and Compose patterns.
- Update tests when behavior changes.
- Document new commands or conventions in `AGENTS.md`.

## Quick reference
- Android debug build: `./gradlew :composeApp:assembleDebug`
- Android unit tests: `./gradlew :composeApp:testDebugUnitTest`
- Single test: `./gradlew :composeApp:testDebugUnitTest --tests "pkg.Class.test"`
- iOS framework: `./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64`

## Notes
- The app is intentionally minimalist; avoid adding complexity without SPEC support.
- Use neutral tone for any user-facing strings.
- Keep UI glanceable; prioritize key metrics on the main screen.
- Debounce the log action when implementing the log button.
- Prefer lifetime sequential numbering for records.
- Local date boundaries drive "today" calculations.

End of file.
