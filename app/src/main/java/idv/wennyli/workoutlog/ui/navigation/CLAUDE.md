# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Navigation 結構

兩層導覽：
1. **`MainNavHost`**（`Navigation.kt`）：頂層，管理 `login_screen` ↔ `main_screen` 切換，起始畫面由 `MainScreenViewModel` 根據 Firebase 登入狀態決定
2. **`BottomNavGraph`**（`BottomNavGraph.kt`）：底部 tab，管理四個主功能頁面（健身紀錄、計時器、AI 教練、設定）

新增/編輯訓練記錄使用 `WorkoutDestinations`，以 optional argument `?workoutId=` 區分新增與編輯模式。
