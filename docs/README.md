# WorkoutLog - 健身紀錄
這是一款使用 Kotlin 和 Jetpack Compose 打造的現代化 Android 健身日誌應用程式。
使用者可以輕鬆記錄每日的訓練內容、追蹤身體測量數據，並使用內建的組間計時器輔助訓練。
所有資料皆透過 Firebase 進行雲端同步，並整合 AI 教練功能提供個人化訓練回饋。

## 主要功能

* 健身紀錄 (Workout Log):
  * 日期篩選: 可依據特定日期查看該日的訓練紀錄。
  * 新增/編輯/刪除紀錄: 直覺地新增訓練項目，包含動作、組數、次數/時間、重量等詳細資訊。
  * 動作建議: 輸入訓練動作時，提供即時的建議清單。

* 計時器 (Timer):
  * 自訂設定: 可自行設定動作名稱、總組數及組間休息時間。
  * 狀態追蹤: 清晰顯示目前狀態（進行中、休息中、已完成）。
  * 一鍵儲存: 完成訓練後，可直接將該項目儲存至當日的健身日誌中。

* AI 教練 (AI Coach):
  * 訓練分析: 透過 Google Gemini API，根據最近 30 筆訓練紀錄產生個人化回饋。
  * 整體評估與推理: 提供訓練表現總結及詳細的分析推理。
  * 建議動作: 根據訓練模式推薦補強動作與強度建議。
  * 注意事項: 提示潛在的訓練風險（如肌群失衡、休息不足）。
  * 智慧快取: 分析結果快取 6 小時，避免重複呼叫 API。

* 設定 (Settings):
  * 身體數據追蹤: 記錄並查看身高、體重、體脂肪的歷史變化。
  * 個人資料設定: 設定性別、出生年月日等基本資料。
  * 使用者登出: 提供安全的登出功能。

* 後端整合:
  * 使用者認證: 使用 Firebase Authentication 進行使用者註冊、登入與匿名登入。
  * 雲端同步: 所有訓練與個人數據皆儲存於 Cloud Firestore，實現資料的即時同步與備份。
  * AI 後端: 使用 Firebase Cloud Functions 串接 Google Gemini API，API 金鑰透過 Secret Manager 安全管理。

## 技術棧與架構

本專案採用了業界推薦的現代 Android 開發技術，以確保程式碼的穩定性、可測試性與可維護性。
* 語言: [Kotlin](https://kotlinlang.org/)
* UI: [Jetpack Compose](https://developer.android.com/jetpack/compose) - Google 用於建構原生 Android UI 的現代化工具包。
* 架構: MVVM (Model-View-ViewModel)
* 非同步處理: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://developer.android.com/kotlin/flow)
* 後端服務: [Firebase](https://firebase.google.com/) (Authentication, Cloud Firestore, Cloud Functions)
* AI 模型: [Google Generative AI (Gemini)](https://ai.google.dev/) - 透過 Cloud Functions 串接
* 依賴注入: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)

### 架構說明

* View (UI 層): 由 Jetpack Compose Screen 組成（`WorkoutLogScreen`, `TimerScreen`, `AiCoachScreen`, `SettingsScreen`），負責顯示 UI 並將使用者操作事件傳遞給 ViewModel。
* ViewModel: (`WorkoutLogViewModel`, `TimerViewModel`, `AiCoachViewModel`, `SettingsViewModel`) 存放 UI 狀態並處理所有業務邏輯。它從 Repository 取得資料，並將其轉換成 UI 可直接使用的狀態。
* Model (資料層):
  * Data Classes: 定義資料結構（`Workout`, `BodyMeasurement`, `UserProfile`, `AiCoachFeedback`, `RecommendedExercise`）。
  * Repository: `WorkoutRepository`, `BodyMeasurementRepository`, `UserProfileRepository`, `AiCoachRepository` 作為資料來源，將資料來源的實作細節抽象化。
* Cloud Functions (後端層): 使用 TypeScript 撰寫，負責串接 Gemini API、管理 AI 回饋快取、控制 Firestore 跨使用者資料存取權限。

## 設定與執行

1. Firebase 設定:
    * 在 [Firebase Console](https://console.firebase.google.com/) 建立一個新的 Android 專案。
    * 將產生的 `google-services.json` 檔案下載並放置到專案的 `app/` 目錄下。
    * 在 Firebase Console 中啟用 **Authentication**（電子郵件/密碼及匿名登入）和 **Cloud Firestore**。
    * 設定 Firestore 安全規則，確保使用者只能存取自己的資料。

2. Cloud Functions 設定:
    * 安裝 Firebase CLI：`npm install -g firebase-tools`
    * 至 [Google AI Studio](https://aistudio.google.com/) 取得 Gemini API 金鑰。
    * 透過 Secret Manager 設定金鑰：`firebase functions:secrets:set GEMINI_API_KEY`
    * 部署 Cloud Functions：`firebase deploy --only functions`

3. 建置與執行:
   * 使用 Android Studio 開啟專案。
   * 等待 Gradle 同步完成。
   * 在 `debug` 與 `release` 版本之間切換，`appId` 會自動變更以區分開發與正式資料庫。
   * 點擊 "Run 'app'"。

## 未來展望

* 訓練統計與圖表化進度。
* 建立與儲存個人化的訓練菜單。
* 離線支援。
