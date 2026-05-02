# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 套件安裝規範

禁止擅自安裝任何軟體或第三方套件，包含但不限於：

- Gradle 依賴（`build.gradle.kts` 或 `libs.versions.toml` 新增套件）
- 系統軟體（`brew install` 等）
- 任何其他工具或 CLI

若判斷需要安裝新套件，必須先向使用者說明：
1. 套件名稱
2. 安裝原因（用途是什麼）
3. 安裝方式

由使用者確認後才能執行安裝。

## 工作目錄規則

所有程式碼修改必須直接在專案主目錄（`/Users/wenyi_li/AndroidStudioProjects/WorkoutLog`）進行，不可只修改 worktree（`.claude/worktrees/` 下的目錄）。若目前環境是在 worktree 內，請改以主目錄的絕對路徑操作檔案。

## 開發規範

實作任何功能或邏輯後，須在 `app/src/test/` 中補上對應的 unit test。

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Install to connected device
./gradlew installDebug

# Lint
./gradlew lint

# Clean
./gradlew clean
```

## Architecture

**MVVM + Hilt DI + Jetpack Compose**，targeting Android 7.0+ (minSdk 24)。

### Package Structure

```
idv.wennyli.workoutlog/
├── data/
│   ├── model/       # 資料類別（Workout、UserProfile、BodyMeasurement、AiCoachModels 等）
│   └── repository/  # Repository 介面 + Impl，每個檔案同時定義 interface 和實作類別
├── di/
│   ├── AppModule.kt     # 提供 Firebase 實例、所有 Repository 綁定
│   └── ConfigModule.kt  # 提供 @Named("appId")，值來自 BuildConfig.APP_ID
├── ui/
│   ├── navigation/  # 導覽圖（Navigation.kt：頂層；BottomNavGraph.kt：底部 tab）
│   ├── theme/       # Material3 主題
│   └── view/        # 各功能頁面，每個子目錄含 Screen Composable + ViewModel
└── utils/
    ├── FirestorePaths.kt   # 集中管理所有 Firestore 路徑
    └── ResourceProvider.kt # AppResource 介面，供 ViewModel 存取字串資源（可 mock）
```

### Key Tech Stack

| Layer | Library |
|-------|---------|
| 語言 | Kotlin 2.3 / JVM 11 |
| UI | Jetpack Compose + Material3 |
| 架構 | MVVM |
| 非同步 | Coroutines + Flow |
| 依賴注入 | Hilt (KSP) |
| 後端 | Firebase Auth / Firestore / Cloud Functions |
| AI | Google Gemini API（透過 Cloud Functions 呼叫，API Key 由 Secret Manager 管理） |
| 測試 | JUnit4 + MockK + Truth + Turbine |

### Dependency Versions

統一在 `gradle/libs.versions.toml` 管理，不可在 `build.gradle.kts` 內直接寫版本號。

