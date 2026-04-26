# AI 健身教練功能架構說明

本文件說明 WorkoutLog app 中 AI 健身教練功能的完整串接流程，包含 Android 前端、Firebase Cloud Functions 後端與 Google Gemini API 之間的互動方式。

---

## 整體架構圖

```
Android App                    Firebase                    Google AI
─────────────────────────────────────────────────────────────────────
AiCoach.kt (UI)
  │ 按下按鈕
  ▼
AiCoachViewModel.kt
  │ getFeedback()
  ▼
AiCoachRepository.kt      →  Cloud Functions (index.ts)  →  Gemini API
  │ 呼叫 Firebase Functions      │ 1. 驗證身份
  │                              │ 2. 查 Firestore 快取
  │                              │ 3. 讀訓練紀錄
  │                              │ 4. 呼叫 Gemini
  │                              │ 5. 寫入快取
  ◄──────────────────────────────┘ 回傳 JSON
  │ 解析 Map → AiCoachFeedback
  ▼
AiCoachViewModel.kt
  │ _uiState = Success(feedback)
  ▼
AiCoach.kt (UI 更新)
```

---

## 逐層說明

### 1. UI 層：`AiCoach.kt`

使用者按下「取得 AI 回饋」後觸發：

```kotlin
onRequestFeedback = { viewModel.getFeedback() }
```

UI 只負責根據 `uiState` 顯示對應畫面：

| 狀態 | 畫面 |
|------|------|
| `Idle` | 說明文字 + 「取得 AI 回饋」按鈕 |
| `Loading` | 載入動畫 + 提示文字 |
| `Success` | 分析結果卡片列表 |
| `Error` | 錯誤訊息 + 「重試」按鈕 |

---

### 2. ViewModel 層：`AiCoachViewModel.kt`

管理 UI 狀態，呼叫 Repository，並攔截例外轉為 Error 狀態：

```kotlin
fun getFeedback() {
    _uiState.value = AiCoachUiState.Loading
    try {
        val feedback = aiCoachRepository.getFeedback()
        _uiState.value = AiCoachUiState.Success(feedback)
    } catch (e: Exception) {
        _uiState.value = AiCoachUiState.Error(e.message ?: "未知錯誤")
    }
}
```

---

### 3. Repository 層：`AiCoachRepository.kt`

透過 `FirebaseFunctions` SDK 呼叫 Cloud Function，並把回傳的 `Map<String, Any>` 解析成 data class：

```kotlin
val result = functions
    .getHttpsCallable("getAiCoachFeedback")   // 函式名稱對應 index.ts export
    .call(data)           // data = { appId: "workout-log-release" }
    .await()

val map = result.data as Map<String, Any>
return AiCoachFeedback(
    summary = map["summary"] as? String ?: "",
    reasoning = map["reasoning"] as? String ?: "",
    ...
)
```

Firebase Functions SDK 會自動將 Android 登入狀態（Firebase Auth token）附在每次呼叫的 header 裡，後端因此能驗證使用者身份，無需額外處理。

---

### 4. Cloud Function 層：`index.ts`

收到呼叫後依序執行以下流程：

```
① 驗證身份（request.auth）
        ↓
② 查 Firestore 快取（aiCoachCache/latest）
   ├─ 快取 < 6 小時 → 直接回傳，不呼叫 Gemini
   └─ 快取過期 → 繼續往下
        ↓
③ 讀取最近 30 筆訓練紀錄（workouts collection）
        ↓
④ 組合 prompt，呼叫 Gemini API（gemini-2.5-flash）
        ↓
⑤ 解析 Gemini 回傳的 JSON 字串
        ↓
⑥ 把結果寫入 Firestore 快取
        ↓
⑦ 回傳 JSON → Repository 接收
```

Firestore 快取路徑：
```
artifacts/{appId}/users/{uid}/aiCoachCache/latest
```

訓練紀錄路徑：
```
artifacts/{appId}/users/{uid}/workouts
```

---

### 5. 資料模型對應

Cloud Function 回傳的 JSON 格式：

```json
{
  "summary": "整體訓練表現總結",
  "reasoning": "詳細分析推理",
  "recommendedExercises": [
    {
      "exercise": "深蹲",
      "muscleGroup": "下肢",
      "intensitySuggestion": "建議強度說明"
    }
  ],
  "warnings": ["注意事項1", "注意事項2"],
  "generatedAt": 1745680000000
}
```

對應到 `AiRecommendedExercise.kt` 中的 data class：

```kotlin
data class AiCoachFeedback(
    val summary: String,
    val reasoning: String,
    val recommendedExercises: List<RecommendedExercise>,
    val warnings: List<String>,
    val generatedAt: Long       // Unix timestamp（毫秒）
)

data class RecommendedExercise(
    val exercise: String,
    val muscleGroup: String,
    val intensitySuggestion: String
)
```

---

### 6. 依賴注入（Hilt）

`AiCoachRepositoryImpl` 所需的 `FirebaseFunctions` 和 `appId` 由 `AppModule.kt` 提供：

```kotlin
@Provides
@Singleton
fun provideAiCoachRepository(
    functions: FirebaseFunctions,
    @Named("appId") appId: String
): AiCoachRepository = AiCoachRepositoryImpl(functions, appId)
```

---

## 相關檔案

| 檔案 | 說明 |
|------|------|
| `ui/view/aiCoach/AiCoach.kt` | UI Composable |
| `ui/view/aiCoach/AiCoachViewModel.kt` | UI 狀態管理 |
| `data/repository/AiCoachRepository.kt` | Firebase Functions 呼叫與資料解析 |
| `data/model/AiRecommendedExercise.kt` | `AiCoachFeedback`、`RecommendedExercise` data class |
| `di/AppModule.kt` | Hilt 依賴注入設定 |
| `functions/src/index.ts`（後端） | Firebase Cloud Function 實作 |
