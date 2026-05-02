# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository 模式

- 介面與實作定義在同一個檔案（如 `WorkoutRepository.kt` 同時含 `WorkoutRepository` 介面和 `WorkoutRepositoryImpl`）
- 所有需要 Firestore 的 Repository 注入 `FirebaseAuth`、`FirebaseFirestore`、`@Named("appId")`
- 即時資料（如 `getWorkouts()`）使用 `callbackFlow` + `addSnapshotListener`，回傳 `Flow<List<T>>`
- 單次讀寫操作使用 `suspend fun` + `.await()`
