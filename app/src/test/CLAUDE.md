# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Unit Test 規範

執行指令：`./gradlew testDebugUnitTest`
執行單一測試類別：`./gradlew testDebugUnitTest --tests "idv.wennyli.workoutlog.YourTestClass"`

使用 **JUnit4 + MockK + Truth + Turbine + kotlinx-coroutines-test**。

- **一個測試只驗證一件事**：失敗時能立刻定位問題
- **AAA 結構**：Arrange（準備）→ Act（執行）→ Assert（驗證），三段間留空行
- **測試名稱說明情境與預期結果**：用 backtick 包裹的描述句，如 `` `getFeedback should return empty list when warnings are absent` ``
- **測試間彼此獨立**：用 `@Before` 重置狀態，執行順序不影響結果
- **只測自己寫的邏輯**：用 MockK mock 掉 Firebase、Repository 等外部依賴
- **涵蓋邊界條件**：正常輸入、空值/缺欄位、例外三種情境
- **不為覆蓋率而寫**：測試邏輯行為，而非讓每一行程式碼都被執行過
- **StateFlow 測試**：用 Turbine 的 `test { }` 驗證 Flow 發射序列；ViewModel 測試需搭配 `MainDispatcherRule` 替換 coroutine dispatcher
