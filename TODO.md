# Smoke Tracker - Development TODO

> A task-by-task checklist for building the app. Work through these in order—each section builds on the previous.

---

## Phase 0: Project Setup & Migration

### 0.1 Environment Setup
- [ ] Ensure Android Studio Otter 3 Feature Drop (2025.2.3+) is installed
- [ ] Ensure Xcode is installed and updated (for iOS builds)
- [ ] Install KMP plugin in Android Studio (if not already)
- [ ] Run preflight checks in IDE

### 0.2 AGP 9.0 Migration
> Skip this section if staying on AGP 8.x for now. See SPEC.md for full migration guide.

- [ ] **Create androidApp module**
  - [ ] Create `androidApp/` directory
  - [ ] Create `androidApp/build.gradle.kts`:
    ```kotlin
    plugins {
        alias(libs.plugins.androidApplication)
        alias(libs.plugins.composeMultiplatform)
        alias(libs.plugins.composeCompiler)
    }
    ```
  - [ ] Create `androidApp/src/main/AndroidManifest.xml`
  - [ ] Move `MainActivity.kt` from `composeApp/src/androidMain/` to `androidApp/src/main/kotlin/`
  - [ ] Add dependency on shared module: `implementation(project(":shared"))`
  - [ ] Add to `settings.gradle.kts`: `include(":androidApp")`

- [ ] **Update shared module**
  - [ ] Rename `composeApp/` to `shared/` (update `settings.gradle.kts` too)
  - [ ] Update `shared/build.gradle.kts`:
    - [ ] Replace `com.android.library` → `com.android.kotlin.multiplatform.library`
    - [ ] Remove `applicationId`, `versionCode`, `versionName` (app module handles this)
    - [ ] Change `debugImplementation(...)` → `androidRuntimeClasspath(...)`
  - [ ] Add to root `build.gradle.kts`: `alias(libs.plugins.androidMultiplatformLibrary) apply false`

- [ ] **Update versions in `gradle/libs.versions.toml`**
  - [ ] AGP → `9.0.0`
  - [ ] Gradle wrapper → `9.1.0` (in `gradle/wrapper/gradle-wrapper.properties`)
  - [ ] Compose Multiplatform → `1.9.3` or `1.10.0`

- [ ] **Verify migration**
  - [ ] Sync Gradle
  - [ ] Build Android app: `./gradlew :androidApp:assembleDebug`
  - [ ] Run Android app on emulator/device
  - [ ] Build iOS app in Xcode
  - [ ] Run iOS app on simulator
  - [ ] Delete old run configurations

### 0.3 Project Structure Setup
- [ ] Create folder structure in `shared/src/commonMain/kotlin/`:
  ```
  com/example/smoketracker/  (or your package name)
  ├── App.kt
  ├── di/
  ├── data/
  │   ├── local/
  │   └── repository/
  ├── domain/
  │   ├── model/
  │   └── usecase/
  ├── ui/
  │   ├── main/
  │   ├── history/
  │   ├── settings/
  │   ├── stats/
  │   ├── components/
  │   └── theme/
  └── util/
  ```
- [ ] Create matching structure in `androidMain/` and `iosMain/` for platform-specific code

### 0.4 Add Dependencies
- [ ] Add to `libs.versions.toml`:
  - [ ] kotlinx-datetime
  - [ ] Room (or SQLDelight)
  - [ ] Koin (core + compose)
  - [ ] Navigation (compose-navigation or voyager)
  - [ ] DataStore (or multiplatform-settings)
- [ ] Add dependencies to `shared/build.gradle.kts` in appropriate source sets
- [ ] Sync and verify build still works

---

## Phase 1: Core MVP

### 1.1 Data Layer - Models
- [ ] Create `SmokeRecord` data class in `domain/model/`:
  ```kotlin
  data class SmokeRecord(
      val id: Long = 0,
      val number: Int,
      val timestamp: Instant,
      val createdAt: Instant,
      val modifiedAt: Instant? = null,
  )
  ```
- [ ] Create `UserPreferences` data class:
  ```kotlin
  data class UserPreferences(
      val dailyGoal: Int? = null,
      val notificationsEnabled: Boolean = false,
      val use24HourFormat: Boolean? = null,
      val theme: Theme = Theme.SYSTEM,
  )
  enum class Theme { LIGHT, DARK, SYSTEM }
  ```

### 1.2 Data Layer - Database
- [ ] **Setup Room (or SQLDelight)**
  - [ ] Create `SmokeRecordEntity` with Room annotations
  - [ ] Create `SmokeDao` interface with queries:
    - [ ] `insertRecord(record)`
    - [ ] `getAllRecords(): Flow<List<SmokeRecord>>`
    - [ ] `getRecordsForDate(date): Flow<List<SmokeRecord>>`
    - [ ] `getLatestRecord(): Flow<SmokeRecord?>`
    - [ ] `getRecordCount(): Flow<Int>`
    - [ ] `getTodayCount(): Flow<Int>`
    - [ ] `deleteRecord(id)`
    - [ ] `updateRecord(record)`
  - [ ] Create `SmokeDatabase` class
  - [ ] Create platform-specific database builders (`androidMain/`, `iosMain/`)

- [ ] **Test database**
  - [ ] Write unit tests for DAO operations
  - [ ] Verify migrations work (if any)

### 1.3 Data Layer - Repository
- [ ] Create `SmokeRepository` interface in `data/repository/`:
  ```kotlin
  interface SmokeRepository {
      fun getAllRecords(): Flow<List<SmokeRecord>>
      fun getTodayRecords(): Flow<List<SmokeRecord>>
      fun getLatestRecord(): Flow<SmokeRecord?>
      fun getTodayCount(): Flow<Int>
      fun getTotalCount(): Flow<Int>
      suspend fun logSmoke(): SmokeRecord
      suspend fun deleteRecord(id: Long)
      suspend fun updateRecord(record: SmokeRecord)
  }
  ```
- [ ] Create `SmokeRepositoryImpl` implementing the interface
- [ ] Implement `logSmoke()`:
  - [ ] Get current total count
  - [ ] Create new record with number = count + 1
  - [ ] Insert and return

### 1.4 Dependency Injection
- [ ] Create `dataModule` in `di/`:
  - [ ] Provide Database instance
  - [ ] Provide DAO
  - [ ] Provide Repository
- [ ] Create `viewModelModule` (will add ViewModels here later)
- [ ] Create `appModule` combining all modules
- [ ] Initialize Koin in Android `Application` class
- [ ] Initialize Koin in iOS entry point

### 1.5 UI Theme Setup
- [ ] Create `Theme.kt` in `ui/theme/`:
  - [ ] Define color schemes (light/dark)
  - [ ] Define typography
  - [ ] Create `SmokeTrackerTheme` composable wrapper
- [ ] Create `Dimens.kt` for spacing constants
- [ ] Apply theme in root `App.kt`

### 1.6 Main Screen - Basic Layout
- [ ] Create `MainScreen.kt` in `ui/main/`
- [ ] Create `MainViewModel.kt`:
  - [ ] Inject `SmokeRepository`
  - [ ] Expose `todayCount: StateFlow<Int>`
  - [ ] Expose `latestRecord: StateFlow<SmokeRecord?>`
  - [ ] Implement `fun logSmoke()`
- [ ] Add ViewModel to Koin module
- [ ] **Build Main Screen UI:**
  - [ ] Scaffold with top app bar area
  - [ ] Settings icon button (top-left) - no navigation yet, just placeholder
  - [ ] Stats icon button (top-right) - no navigation yet, just placeholder
  - [ ] Large counter text (centered)
  - [ ] "cigarettes today" label
  - [ ] Log Smoke button (prominent, centered below counter)
- [ ] Connect UI to ViewModel:
  - [ ] Display `todayCount` in counter
  - [ ] Wire button to `viewModel.logSmoke()`
- [ ] **Test:** Tap button → counter increments

### 1.7 Main Screen - Time Since Last
- [ ] Add `timeSinceLastSmoke: StateFlow<Duration?>` to ViewModel
- [ ] Implement timer that updates every minute:
  - [ ] Use `kotlinx-datetime` for time calculations
  - [ ] Handle null case (no records yet)
- [ ] Display formatted duration on Main Screen:
  - [ ] "2h 34m ago" format
  - [ ] "Just now" if < 1 minute
  - [ ] Hide if no records

### 1.8 Navigation Setup
- [ ] Add navigation dependency (Compose Navigation or Voyager)
- [ ] Create `Navigation.kt` with sealed class/enum for destinations:
  ```kotlin
  sealed class Screen {
      object Main : Screen()
      object Settings : Screen()
      object Stats : Screen()
  }
  ```
- [ ] Setup NavHost in `App.kt`
- [ ] Wire Settings button → navigate to Settings
- [ ] Wire Stats button → navigate to Stats
- [ ] Create placeholder `SettingsScreen.kt` (just text for now)
- [ ] Create placeholder `StatsScreen.kt` (just text for now)
- [ ] **Test:** Navigation works both directions

### 1.9 History - Basic List
- [ ] Create `HistoryViewModel.kt`:
  - [ ] Expose `allRecords: StateFlow<List<SmokeRecord>>`
- [ ] Create `HistoryList.kt` composable:
  - [ ] LazyColumn showing all records
  - [ ] Each item shows: number, time, gap since previous
  - [ ] Format time based on device locale
- [ ] Create helper function to calculate gap between records
- [ ] **Test:** Records appear in list after logging

### 1.10 History - Bottom Sheet Integration
- [ ] Add Material3 BottomSheet to Main Screen
- [ ] Create `HistorySheet.kt` composable:
  - [ ] Peek state: shows only latest record + drag handle
  - [ ] Expanded state: shows full `HistoryList`
- [ ] Wire sheet state (collapsed/expanded)
- [ ] **Test:** Sheet expands/collapses smoothly

---

## Phase 2: Polish

### 2.1 History - Day Grouping
- [ ] Create `GroupedRecords` data class:
  ```kotlin
  data class DayGroup(
      val date: LocalDate,
      val records: List<SmokeRecord>
  )
  ```
- [ ] Add grouping logic in ViewModel or UseCase
- [ ] Update `HistoryList` to show sticky day headers
- [ ] Format headers: "Today", "Yesterday", "Mon, Jan 28"
- [ ] **Test:** Records grouped correctly across multiple days

### 2.2 History - Edit Record
- [ ] Create `EditRecordDialog.kt`:
  - [ ] Date picker
  - [ ] Time picker
  - [ ] Save/Cancel buttons
- [ ] Add `updateRecord()` to ViewModel
- [ ] Wire swipe-to-reveal Edit action (or long-press menu)
- [ ] **Test:** Can edit timestamp, change persists

### 2.3 History - Delete Record
- [ ] Create `DeleteConfirmationDialog.kt`
- [ ] Add `deleteRecord()` to ViewModel
- [ ] Wire swipe-to-reveal Delete action
- [ ] Implement swipe-to-delete animation
- [ ] **Test:** Delete works, list updates, confirmation shown

### 2.4 Settings Screen
- [ ] Create `SettingsViewModel.kt`:
  - [ ] Expose `preferences: StateFlow<UserPreferences>`
  - [ ] Functions to update each preference
- [ ] Create preferences storage (DataStore or multiplatform-settings)
- [ ] Build Settings UI:
  - [ ] Daily Goal setting (number input or disable)
  - [ ] Theme selector (Light/Dark/System)
  - [ ] 24-hour time toggle
  - [ ] About section (version info)
- [ ] Wire theme changes to actually update app theme
- [ ] **Test:** Preferences persist across app restarts

### 2.5 Dark Mode Support
- [ ] Ensure all colors use theme values (no hardcoded colors)
- [ ] Test light mode appearance
- [ ] Test dark mode appearance
- [ ] Test system theme switching
- [ ] Fix any contrast issues

### 2.6 Visual Polish
- [ ] Add haptic feedback on Log button tap (platform-specific)
- [ ] Add subtle animation when counter increments
- [ ] Add loading states where appropriate
- [ ] Add empty states (no records yet)
- [ ] Refine spacing and typography
- [ ] Test on different screen sizes

### 2.7 Error Handling
- [ ] Add error handling in Repository
- [ ] Show error messages to user (Snackbar or Dialog)
- [ ] Handle edge cases:
  - [ ] Rapid button taps (debounce)
  - [ ] Database errors
  - [ ] Invalid date/time edits

---

## Phase 3: Insights

### 3.1 Stats Screen - Summary Cards
- [ ] Create `StatsViewModel.kt`:
  - [ ] `todayCount: StateFlow<Int>`
  - [ ] `weekCount: StateFlow<Int>`
  - [ ] `monthCount: StateFlow<Int>`
  - [ ] `averagePerDay: StateFlow<Float>`
  - [ ] `longestBreak: StateFlow<Duration?>`
- [ ] Create queries/use cases for each stat
- [ ] Build Stats Screen UI:
  - [ ] Card grid layout
  - [ ] Each card: icon, value, label
- [ ] **Test:** Stats calculate correctly

### 3.2 Stats - Detailed Queries
- [ ] Implement `getCountForDateRange(start, end)` in Repository
- [ ] Calculate week count (handle week start preference)
- [ ] Calculate monthly count
- [ ] Calculate 30-day rolling average
- [ ] Find longest gap between any two records

### 3.3 Daily Goal Feature
- [ ] Add goal progress indicator to Main Screen
- [ ] Show "X of Y" or progress bar when goal is set
- [ ] Visual indicator when goal exceeded
- [ ] Add goal status to Stats screen

### 3.4 Data Export
- [ ] Create `ExportUseCase`:
  - [ ] Format records as CSV
  - [ ] Include headers: number, timestamp, date, time, gap
- [ ] Add "Export Data" button to Settings
- [ ] Implement platform-specific file saving:
  - [ ] Android: Share intent or save to Downloads
  - [ ] iOS: Share sheet
- [ ] **Test:** Exported CSV is valid and importable to spreadsheet

### 3.5 Charts (Optional)
- [ ] Add charting library (if desired)
- [ ] Create daily bar chart (last 7 days)
- [ ] Create trend line chart (last 30 days)
- [ ] Add charts to Stats screen

---

## Phase 4: Engagement (Future)

### 4.1 Notifications
- [ ] Add notification permissions handling
- [ ] Implement goal exceeded notification
- [ ] Add notification preferences to Settings
- [ ] Test notifications on both platforms

### 4.2 Widgets
- [ ] Android: Create Glance widget showing today's count
- [ ] iOS: Create WidgetKit widget
- [ ] Widget shows count + log button (if possible)

### 4.3 Watch Apps
- [ ] Wear OS companion app
- [ ] Apple Watch companion app

### 4.4 Cloud Sync
- [ ] Design sync strategy
- [ ] Add authentication
- [ ] Implement sync logic
- [ ] Handle conflicts

---

## Ongoing Tasks

### Testing
- [ ] Unit tests for ViewModels
- [ ] Unit tests for UseCases
- [ ] Unit tests for Repository
- [ ] Integration tests for Database
- [ ] UI tests for critical flows
- [ ] Manual testing on multiple devices

### Documentation
- [ ] Keep SPEC.md updated with changes
- [ ] Document any architectural decisions
- [ ] Add code comments for complex logic
- [ ] Create README.md for the repository

### Maintenance
- [ ] Update dependencies regularly
- [ ] Monitor for KMP/Compose updates
- [ ] Address deprecation warnings
- [ ] Performance profiling

---

## Decision Log

Track decisions made during development:

| Date | Decision | Reasoning |
|------|----------|-----------|
| | | |

---

## Notes

- Check off tasks as you complete them
- Feel free to reorder within a phase if it makes sense
- Add sub-tasks as needed when you discover complexity
- Reference SPEC.md for detailed requirements
- Commit after completing each numbered section (1.1, 1.2, etc.)

---

*Created: January 30, 2026*
