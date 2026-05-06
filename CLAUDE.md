# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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

