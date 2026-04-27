# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 工作目錄規則

所有程式碼修改必須直接在專案主目錄（`/Users/wenyi_li/AndroidStudioProjects/WorkoutLog`）進行，不可只修改 worktree（`.claude/worktrees/` 下的目錄）。若目前環境是在 worktree 內，請改以主目錄的絕對路徑操作檔案。

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

## Testing Principles

### Unit Test
- **一個測試只驗證一件事**：失敗時能立刻定位問題
- **AAA 結構**：Arrange（準備）→ Act（執行）→ Assert（驗證），三段間留空行
- **測試名稱說明情境與預期結果**：用 backtick 包裹的描述句，如 `` `getFeedback should return empty list when warnings are absent` ``
- **測試間彼此獨立**：用 `@Before` 重置狀態，執行順序不影響結果
- **只測自己寫的邏輯**：用 mock 隔離 Firebase、網路等外部依賴
- **涵蓋邊界條件**：正常輸入、空值/缺欄位、例外三種情境
- **不為覆蓋率而寫**：測試邏輯行為，而非讓每一行程式碼都被執行過

### UI Test（Instrumented）
- 測試對象為 stateless composable（直接傳入 `uiState`），避免依賴 Hilt 或 ViewModel
- 用 `createComposeRule` 渲染畫面，`onNodeWithText` 找元素，`assertIsDisplayed` / `assertDoesNotExist` 驗證顯示狀態
- 互動測試用 `performClick()` 模擬點擊，搭配旗標變數驗證 callback 是否被呼叫
- 需要 `@RunWith(AndroidJUnit4::class)`，執行指令：`./gradlew connectedAndroidTest`
- **用使用者看到的文字找元素**：`onNodeWithText("取得 AI 回饋")`，不用內部 tag/ID
- **只驗證使用者能感知的事**：畫面有沒有顯示、互動後有沒有變化，不驗證 ViewModel 內部狀態
- **測試行為，不測樣式**：不驗證顏色、字體大小、間距等視覺細節
- **優先測試有條件判斷的狀態切換**：如 Idle / Loading / Success / Error 各自應顯示與隱藏的元素
- **非同步狀態變化**：若有 coroutine 驅動的畫面更新，用 `waitUntil` 等待節點出現，避免 race condition
