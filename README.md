# WorkoutLog - 健身紀錄
這是一款使用 Kotlin 和 Jetpack Compose 打造的現代化 Android 健身日誌應用程式。
使用者可以輕鬆記錄每日的訓練內容、追蹤身體測量數據，並使用內建的組間計時器輔助訓練。
所有資料皆透過 Firebase 進行雲端同步。

## 主要功能

* 健身紀錄 (Workout Log):
  * 日期篩選: 可依據特定日期查看該日的訓練紀錄。 
  * 新增/刪除紀錄: 直覺地新增訓練項目，包含動作、組數、次數/時間、重量等詳細資訊。 
  * 動作建議: 輸入訓練動作時，提供即時的建議清單。 
  * ~~AI 訓練建議: 透過 Google Gemini API，根據您的歷史紀錄，產生下一次的訓練建議。~~
  
* 計時器 (Timer):
  * 自訂設定: 可自行設定動作名稱、總組數及組間休息時間。 
  * 狀態追蹤: 清晰顯示目前狀態（進行中、休息中、已完成）。 
  * 一鍵儲存: 完成訓練後，可直接將該項目儲存至當日的健身日誌中。
  
* 設定 (Settings):
  * 身體數據追蹤: 記錄並查看身高、體重、體脂肪的歷史變化。 
  * 個人資料設定: 設定性別、出生年月日等基本資料。 
  * 使用者登出: 提供安全的登出功能。

* 後端整合:
  * 使用者認證: 使用 Firebase Authentication 進行使用者註冊與登入。 
  * 雲端同步: 所有訓練與個人數據皆儲存於 Cloud Firestore，實現資料的即時同步與備份。

## 技術棧與架構

本專案採用了業界推薦的現代 Android 開發技術，以確保程式碼的穩定性、可測試性與可維護性。
* 語言: [Kotlin](https://kotlinlang.org/)
* UI: [Jetpack Compose](https://developer.android.com/jetpack/compose) - Google 用於建構原生 Android UI 的現代化工具包。
* 架構: MVVM (Model-View-ViewModel)
* 非同步處理: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://developer.android.com/kotlin/flow)
* 後端服務: [Firebase](https://firebase.google.com/) (Authentication, Cloud Firestore)
* 依賴注入: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
* ~~AI 模型: [Google Generative AI (Gemini)](https://ai.google.dev/)~~

### 架構說明

* View (UI 層): 由 Jetpack Compose Screen 組成 (`WorkoutLogScreen`, `TimerScreen`, `SettingsScreen`)，負責顯示 UI 並將使用者操作事件傳遞給 ViewModel。
* ViewModel: (`WorkoutViewModel`, `TimerViewModel`, `SettingsViewModel`) 存放 UI 狀態並處理所有業務邏輯。它從 Repository 取得資料，並將其轉換成 UI 可直接使用的狀態。
* Model (資料層):
  * Data Classes: 定義資料結構 (`Workout`, `BodyMeasurement`, `UserProfile`)。
  * Repository: `WorkoutRepository`, `BodyMeasurementRepository`, `UserProfileRepository`作為資料來源，將資料來源（Firestore）的實作細節抽象化。

## 設定與執行

1. Firebase 設定:
    * 在 [Firebase Console](https://console.firebase.google.com/) 建立一個新的 Android 專案。 
    * 將產生的 `google-services.json` 檔案下載並放置到專案的 `app/` 目錄下。
    * 在 Firebase Console 中啟用 **Authentication** (電子郵件/密碼) 和 **Cloud Firestore**。
    * 設定 Firestore 安全規則，確保使用者只能存取自己的資料。
   
2. ~~API 金鑰:
   * 若要使用 AI 建議功能，請至 Google AI Studio 取得您的 API 金鑰。
   * 將金鑰存放至 local.properties 檔案中，格式如下：
    ```
    GEMINI_API_KEY="YOUR_API_KEY"
    ```~~
   
3. 建置與執行:
   * 使用 Android Studio 開啟專案。
   * 等待 Gradle 同步完成。
   * 在 `debug` 與 `release` 版本之間切換，`appId` 會自動變更以區分開發與正式資料庫。
   * 點擊 "Run 'app'"。

## 未來展望

* 新增 Gemini AI 訓練建議功能
* 訓練統計與圖表化進度。
* 建立與儲存個人化的訓練菜單。
* ~~更完整的訓練動作資料庫（包含示範圖/影片）。~~
* 離線支援。