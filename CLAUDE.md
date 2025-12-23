# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

**Build the project:**
```bash
./gradlew build
```

**Run tests:**
```bash
./gradlew test
```

**Run tests with coverage:**
```bash
./gradlew testDebugUnitTest
```

**Install debug APK:**
```bash
./gradlew installDebug
```

**Clean build:**
```bash
./gradlew clean
```

## Project Architecture

This is an Android flashcard app following Clean Architecture principles with these layers:

### Data Layer (`app/src/main/java/com/jflash/data/`)
- **Database**: Room database with entities (`CardEntity`, `ListEntity`, `DailyStatsEntity`)
- **DAOs**: Data access objects for database operations (`CardDao`, `ListDao`, `DailyStatsDao`)
- **Repositories**: Abstract data access (`CardRepository`, `ListRepository`, `StatsRepository`)
- **Models**: Data transfer objects (`ImportData`, `CardType`)
- **External DB**: `JapaneseDbHelper` manages the bundled Japanese4.db file

### Domain Layer (`app/src/main/java/com/jflash/domain/`)
- **Models**: Core business entities (`Card`, `List`, `FSRSState`, `FSRSGrade`, `FSRSParameters`)
- **Use Cases**: Business logic (`ImportUseCase`, `FSRSAlgorithm`)

### Presentation Layer (`app/src/main/java/com/jflash/ui/`)
- **Screens**: Jetpack Compose UI (`ReviewScreen`, `ViewListScreen`)
- **ViewModels**: State management (`ReviewViewModel`, `ViewListViewModel`)
- **Components**: Reusable UI components (`BackgroundImageLayout`)
- **Theme**: Material Design 3 theming

### Dependency Injection (`app/src/main/java/com/jflash/di/`)
- Uses Dagger Hilt for dependency injection
- `AppModule` provides database and repository instances

## Key Implementation Details

**FSRS Algorithm**: The app implements FSRS v6 spaced repetition algorithm in `FSRSAlgorithm.kt`

**Import System**: Accepts gzipped JSON files from the "Japanese" Android app via:
- Share intent handling in `MainActivity`
- File picker for manual import
- Processing logic in `ImportUseCase`

**Card Generation**: Based on Japanese4.db entries, generates 2-4 cards per item depending on available data (Japanese, Reading, English)

**Database Setup**: 
- Room database (`JFlashDatabase`) for user data
- External SQLite database (`Japanese4.db`) bundled in assets for reference data

**Navigation**: Multi-screen app with Navigation Compose:
- ReviewScreen: Main spaced repetition interface with background images
- ViewListScreen: Word list viewer with progress tracking

**Background Images**: Dynamic kanji background images from `app/src/main/assets/bg/` using Coil image loading

## Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material Design 3
- **Database**: Room + bundled SQLite
- **DI**: Dagger Hilt
- **Async**: Coroutines + Flow
- **Images**: Coil for image loading and background scaling
- **Testing**: JUnit + Mockito
- **Build**: Gradle Kotlin DSL

## Development Notes

- Target SDK: 34, Min SDK: 21
- Uses kapt for annotation processing (Room, Hilt)
- Compose BOM manages Compose library versions
- Java 17 compatibility

## Recent Updates

### Session 2024-12-23: View List Screen & Progress Tracking

**Fixed Percentage Learned Display Issue:**
- Issue: Percentage learned always showed 0% even after grading cards as "Easy"
- Root cause: `ViewListViewModel` was using `.first()` instead of collecting Flow updates
- Solution: Updated to use `collect` for live database updates in `ViewListViewModel.kt:54`

**Implemented Word Grouping:**
- Problem: Duplicate entries for same word/reading/meaning combinations (2-4 cards per word)
- Solution: Added `WordGroup` data class and `groupCardsByWord()` function
- Feature: Shows average percentage learned across all cards for each word
- Display: "Progress (X cards): Y% learned" format with card count

**Updated App Icon:**
- Replaced default Android icon with custom `icon.png`
- Generated all required density variants (mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi)
- Created both standard and round icon variants
- Updated `AndroidManifest.xml` with icon references
- Icon shows Android character with Japanese flashcards

**File Changes:**
- `app/src/main/java/com/jflash/ui/screen/ViewListViewModel.kt`: Fixed Flow collection, added word grouping
- `app/src/main/java/com/jflash/ui/screen/ViewListScreen.kt`: Updated to use WordGroup instead of Card
- `app/src/main/AndroidManifest.xml`: Added icon configuration
- `app/src/main/res/mipmap-*/`: Created icon files for all densities

**Technical Details:**
- WordGroup calculation uses `groupBy { Triple(japanese, reading, meaning) }`
- Average percentage: `groupedCards.map { it.percentLearned }.average()`
- Real-time updates via Flow collection ensure UI stays synchronized with database
- Icon generation used macOS `sips` command for batch resizing