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
- **Repositories**: Abstract data access (`CardRepository`, `ListRepository`, `StatsRepository`, `SentenceRepository`)
- **Models**: Data transfer objects (`ImportData`, `CardType`)
- **External DB**: `JapaneseDbHelper` manages the bundled Japanese4.db file with example sentences

### Domain Layer (`app/src/main/java/com/jflash/domain/`)
- **Models**: Core business entities (`Card`, `List`, `Sentence`, `FSRSState`, `FSRSGrade`, `FSRSParameters`)
- **Use Cases**: Business logic (`ImportUseCase`, `FSRSAlgorithm`)
- **Utilities**: Helper functions (`SentenceSearchUtil` for extracting search terms from Japanese text)

### Presentation Layer (`app/src/main/java/com/jflash/ui/`)
- **Screens**: Jetpack Compose UI (`ReviewScreen`, `ViewListScreen`, `MoreDetailsScreen`)
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
- External SQLite database (`Japanese4.db`) bundled in assets for reference data and example sentences

**Navigation**: Multi-screen app with Navigation Compose:
- ReviewScreen: Main spaced repetition interface with background images and swipeable sentence examples
- ViewListScreen: Word list viewer with progress tracking
- MoreDetailsScreen: Example sentences display with sepia theme

**Example Sentences**: 58,660 Japanese example sentences with English translations:
- Automatic search based on card's Japanese text using intelligent extraction
- Swipe navigation with dot indicators when sentences are available
- Furigana-formatted pronunciation guide for better readability

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

### Session 2025-01-03: Example Sentences Feature

**Implemented Example Sentences System:**
- Added 58,660 Japanese example sentences from existing database to `klc_sentences` table
- Sentences include Japanese text, furigana pronunciation, and English translations
- Intelligent search logic extracts appropriate search terms from card Japanese text:
  - Pure kanji (e.g., 油断) → exact substring search
  - Mixed kanji+hiragana (e.g., 慣れる) → removes trailing hiragana (search for "慣")
  - Pure hiragana/katakana (e.g., もったいない) → exact substring search

**Enhanced Review Interface:**
- Added `MoreDetailsScreen` with sepia background (RGB: 237, 223, 201) and dark text (RGB: 97, 78, 55)
- Implemented horizontal pager with swipe navigation between answer and example sentences
- Added dot indicators showing current page (bright) and available pages (dim)
- Grade buttons only appear on main answer screen

**Architecture Updates:**
- Created `Sentence` domain model and `SentenceRepository` with implementation
- Added `SentenceSearchUtil` for Japanese text processing logic
- Enhanced `ReviewViewModel` to automatically load sentences for each card
- Updated dependency injection to provide sentence repository

**File Changes:**
- `app/src/main/java/com/jflash/domain/model/Sentence.kt`: New sentence data model
- `app/src/main/java/com/jflash/domain/util/SentenceSearchUtil.kt`: Search term extraction logic
- `app/src/main/java/com/jflash/data/repository/SentenceRepository.kt`: Repository interface
- `app/src/main/java/com/jflash/data/repository/SentenceRepositoryImpl.kt`: Repository implementation
- `app/src/main/java/com/jflash/data/database/JapaneseDbHelper.kt`: Added sentence search functionality
- `app/src/main/java/com/jflash/ui/screen/MoreDetailsScreen.kt`: Sentences display UI
- `app/src/main/java/com/jflash/ui/screen/ReviewScreen.kt`: Updated with pager and navigation
- `app/src/main/java/com/jflash/ui/screen/ReviewViewModel.kt`: Added sentence loading logic
- `app/src/main/java/com/jflash/di/AppModule.kt`: Added sentence repository injection

**Technical Implementation:**
- Converted 58,660 sentences from `examples` table to `klc_sentences` format with proper furigana
- Used Foundation's `HorizontalPager` with `ExperimentalFoundationApi` for swipe navigation
- Implemented real-time sentence loading when cards change via Flow collections
- Background images continue to work with new pager layout

### Session 2026-01-18: Duplicate Card Prevention & Import Testing

**Fixed Duplicate Card Creation on Re-Import:**
- Issue: Re-importing a list with the same name created duplicate cards for all words
- Root cause: Original ref-based detection failed when `japaneseDbRef` was NULL (old imports)
- Solution: Added content-based duplicate detection as fallback

**Duplicate Detection Logic (`ImportUseCase.syncList()`):**
1. First checks by `japaneseDbRef` (ref-based detection)
2. For new refs, also checks if card content already exists (japanese + reading + meaning + cardType)
3. Only creates cards that pass both checks

**Architecture Changes for Testability:**
- Created `JapaneseEntryProvider` interface to abstract database access
- `JapaneseDbHelper` now implements this interface
- `ImportUseCase` accepts injectable `entryProviderFactory` for testing
- Made `importLists()` method internal for test access

**New Test File (`ImportUseCaseSyncTest.kt`):**
- Tests ref-based duplicate skipping
- Tests content-based duplicate skipping (same content, different ref)
- Tests that new cards are properly added
- Tests that removed words are deleted from list
- Uses mockito-kotlin for cleaner mock syntax

**File Changes:**
- `app/src/main/java/com/jflash/data/database/JapaneseEntryProvider.kt`: New interface
- `app/src/main/java/com/jflash/data/database/JapaneseDbHelper.kt`: Implements interface
- `app/src/main/java/com/jflash/data/database/dao/CardDao.kt`: Added `cardExists()` query, `DISTINCT` on `getExistingRefs()`
- `app/src/main/java/com/jflash/data/repository/CardRepository.kt`: Added `cardExists()` method
- `app/src/main/java/com/jflash/domain/usecase/ImportUseCase.kt`: Content-based detection, testable factory
- `app/src/main/java/com/jflash/di/AppModule.kt`: Provides `JapaneseEntryProvider` factory
- `app/src/test/java/com/jflash/ImportUseCaseSyncTest.kt`: New test file (6 tests)
- `app/build.gradle.kts`: Added mockito-kotlin dependency

**Device Database Cleanup Procedure:**
If duplicate cards exist on a device, clean them up with:
```bash
# Pull database with WAL
adb shell "run-as com.jflash cat /data/data/com.jflash/databases/jflash.db" > /tmp/jflash.db
adb shell "run-as com.jflash cat /data/data/com.jflash/databases/jflash.db-wal" > /tmp/jflash.db-wal
adb shell "run-as com.jflash cat /data/data/com.jflash/databases/jflash.db-shm" > /tmp/jflash.db-shm

# Delete duplicates keeping best learning progress
sqlite3 /tmp/jflash.db "
WITH ranked AS (
  SELECT id, ROW_NUMBER() OVER (
    PARTITION BY japanese, reading, meaning, cardType
    ORDER BY fsrsStability DESC, fsrsReps DESC, id ASC
  ) as rn FROM cards
)
DELETE FROM cards WHERE id IN (SELECT id FROM ranked WHERE rn > 1);
PRAGMA wal_checkpoint(TRUNCATE);
"

# Push cleaned database back
adb shell am force-stop com.jflash
adb push /tmp/jflash.db /data/local/tmp/jflash_clean.db
adb shell "run-as com.jflash sh -c 'cat /data/local/tmp/jflash_clean.db > /data/data/com.jflash/databases/jflash.db'"
adb shell "run-as com.jflash rm -f /data/data/com.jflash/databases/jflash.db-wal"
adb shell "run-as com.jflash rm -f /data/data/com.jflash/databases/jflash.db-shm"
```

**Technical Notes:**
- Room uses WAL (Write-Ahead Logging) - must pull all 3 files (db, db-wal, db-shm) to see uncommitted changes
- Cards without `japaneseDbRef` are from older imports before refs were stored
- Content-based detection uses exact match on (listId, japanese, reading, meaning, cardType)