# JFlash - Japanese Flashcard App

JFlash is an offline Android app that helps English-speaking learners study Japanese using spaced repetition flashcards. It implements the FSRS v6 algorithm for optimal learning scheduling.

## Features

- **Spaced Repetition**: Uses FSRS v6 algorithm for scientifically-proven learning intervals
- **Fully Offline**: No internet connection required
- **Import from Japanese App**: Compatible with exports from the Japanese Android app
- **Multiple Card Types**: Automatically generates 2-4 cards per item based on available data
- **Text-to-Speech**: Hear Japanese pronunciations
- **Clean UI**: Material Design 3 with Jetpack Compose

## Requirements

- Android 4.1 (API 16) or higher
- Japanese4.db file (bundled in assets)

## Architecture

The app follows Clean Architecture principles with:

- **Presentation Layer**: Jetpack Compose UI with ViewModels
- **Domain Layer**: Use cases and business logic
- **Data Layer**: Room database with repositories

## Database Schema

### Lists Table
- `id`: Primary key
- `title`: List name
- `createdAt`: Creation timestamp
- `updatedAt`: Last update timestamp

### Cards Table
- `id`: Primary key
- `listId`: Foreign key to Lists
- `japanese`: Japanese word/phrase
- `reading`: Pronunciation (hiragana/katakana)
- `meaning`: English translation
- `cardType`: Type of prompt/answer format
- `nextDueAt`: Next review timestamp
- FSRS state fields for scheduling

### Daily Stats Table
- `date`: Primary key (date only)
- `cardsReviewedToday`: Review count

## Card Types

Based on the imported data, cards are generated as follows:

**For tuples (Japanese, English):**
1. Japanese → English
2. English → Japanese

**For triples (Japanese, Reading, English):**
1. Japanese → Reading
2. Japanese → English  
3. English → Japanese + Reading
4. Reading → Japanese + English

## FSRS Implementation

### Grade Mapping
- **Forgot** (Red) → FSRS Grade 1 (Again)
- **Almost** (Orange) → FSRS Grade 2 (Hard)
- **Recalled** (Green) → FSRS Grade 3 (Good)
- **Easy** (Blue) → FSRS Grade 4 (Easy)

### Percent Learned Calculation
The "percent learned" is derived from FSRS state:
- **New cards**: 0%
- **Learning cards**: min(25%, reps × 5%)
- **Relearning cards**: max(50%, 75% - lapses × 5%)
- **Review cards**: 75% + (25% × stability_factor × lapse_penalty)

Where:
- `stability_factor = min(1.0, stability / 90.0)`
- `lapse_penalty = max(0.0, 1.0 - lapses × 0.05)`

## Import Format

JFlash accepts gzipped JSON files with the following structure:

```json
{
  "meta": { "version": "1.0" },
  "lists": [
    {
      "id": "uuid",
      "title": "List Name",
      "updatedAt": "2024-01-01T00:00:00Z",
      "entries": [
        {
          "ref": 12345,
          "updatedAt": "2024-01-01T00:00:00Z"
        }
      ]
    }
  ]
}
```

The `ref` field maps to ROWID in Japanese4.db entries table.

## Import Behavior

- **New List**: Creates all cards from entries
- **Existing List** (matched by title): 
  - Adds new cards for new refs
  - Removes cards for deleted refs
  - Preserves FSRS state for existing cards

## How to Build

1. Clone the repository
2. Place `Japanese4.db` in `app/src/main/assets/`
3. Open in Android Studio
4. Build and run

## How to Import

1. Via Share Intent:
   - In the Japanese app, export a list
   - Choose "Share" and select JFlash

2. Via File Picker:
   - Tap menu → Import list
   - Select the exported .gz file

## Testing

Run tests with:
```bash
./gradlew test
```

Key test coverage:
- FSRS algorithm scheduling
- Import JSON parsing
- Card generation from entries

## Technologies Used

- Kotlin
- Jetpack Compose
- Room Database
- Hilt (Dependency Injection)
- Coroutines & Flow
- FSRS v6 Algorithm
- Android TTS