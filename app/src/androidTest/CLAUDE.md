# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## UI Test（Instrumented）規範

執行指令：`./gradlew connectedAndroidTest`（需連接裝置或模擬器）

使用 **Compose UI Test（`createComposeRule`）+ JUnit4**，需標註 `@RunWith(AndroidJUnit4::class)`。

- **測試對象為 stateless composable**：直接傳入 `uiState`，避免依賴 Hilt 或 ViewModel
- **用使用者看到的文字找元素**：`onNodeWithText("取得 AI 回饋")`，不用內部 tag/ID
- **只驗證使用者能感知的事**：畫面有沒有顯示、互動後有沒有變化，不驗證 ViewModel 內部狀態
- **測試行為，不測樣式**：不驗證顏色、字體大小、間距等視覺細節
- **優先測試有條件判斷的狀態切換**：如 Idle / Loading / Success / Error 各自應顯示與隱藏的元素
- **互動測試**：用 `performClick()` 模擬點擊，搭配旗標變數驗證 callback 是否被呼叫
- **非同步狀態變化**：若有 coroutine 驅動的畫面更新，用 `waitUntil` 等待節點出現，避免 race condition
