# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install to connected device
./gradlew installDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run a single test class
./gradlew testDebugUnitTest --tests "idv.wennyli.workoutlog.YourTestClass"

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Clean
./gradlew clean
```

## Architecture

**MVVM + Hilt DI + Jetpack Compose**, targeting Android 7.0+ (minSdk 24).

### Package Structure

```
idv.wennyli.workoutlog/
├── data/
│   └── model/       # Data classes (e.g. AiRecommendedExercise)
├── ui/
│   ├── navigation/  # Navigation graphs (BottomNavGraph, etc.)
│   ├── view/        # Feature screens, each with ViewModel + Composable
│   └── theme/       # Material3 color, typography, theme
└── utils/           # Shared utilities (DateUtils, etc.)
```

### Key Tech Stack

| Layer | Library |
|-------|---------|
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose |
| DI | Hilt (KSP) |
| Backend | Firebase (Analytics; Firestore ready but commented out) |
| Language | Kotlin 2.0 / JVM 11 |

### Data Flow

- ViewModels expose `StateFlow` / `UiState` consumed by Composable screens
- Repositories abstract data sources (Firebase Firestore expected as the remote source)
- Hilt provides constructor injection throughout; `WorkoutApplication` is the `@HiltAndroidApp` root

### Dependency Versions

Managed centrally in `gradle/libs.versions.toml`. Always update versions there, not inline in `build.gradle.kts`.
