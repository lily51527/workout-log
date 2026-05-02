# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Firestore 路徑規則

所有路徑集中定義在 `FirestorePaths.kt`，格式為：

```
artifacts/{appId}/users/{uid}/workouts
artifacts/{appId}/users/{uid}/measurements
artifacts/{appId}/users/{uid}/profile/data
artifacts/{appId}/users/{uid}/aiCoachCache/latest  ← 後端 Cloud Functions 使用
```

`appId` 統一透過 `ConfigModule` 注入，值為 `BuildConfig.APP_ID`，不可在各處硬編碼。新增路徑時須在此檔案集中定義，不可在 Repository 內散落路徑字串。
