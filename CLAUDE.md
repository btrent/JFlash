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
- **Screens**: Jetpack Compose UI (`ReviewScreen`)
- **ViewModels**: State management (`ReviewViewModel`)
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

**Navigation**: Currently single-screen app, ready for Navigation Compose expansion

## Technology Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material Design 3
- **Database**: Room + bundled SQLite
- **DI**: Dagger Hilt
- **Async**: Coroutines + Flow
- **Testing**: JUnit + Mockito
- **Build**: Gradle Kotlin DSL

## Development Notes

- Target SDK: 34, Min SDK: 21
- Uses kapt for annotation processing (Room, Hilt)
- Compose BOM manages Compose library versions
- Java 17 compatibility