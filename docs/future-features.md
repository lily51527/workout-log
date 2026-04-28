# 未來可實作功能規劃

本文件記錄 WorkoutLog App 未來可延伸的功能方向，依實作難度與優先度分為三個階段。

---

## 短期（邏輯延伸現有功能）

### 訓練週報 / 月報
- 定期彙總使用者的訓練資料，產生圖表數據
- 使用 Cloud Functions `onSchedule` 排程觸發，每週自動產生，不需使用者手動觸發
- App 端新增報表頁面呈現彙總結果

### 推播通知
- 結合 Firebase Cloud Messaging（FCM），偵測使用者幾天未訓練時發送提醒
- 例如：「你已經 3 天沒有訓練了，今天來一組？」
- 後端使用 `onSchedule` 定期掃描，觸發推播

---

## 中期（需要後端才能做的功能）

### 個人化訓練計畫產生
- 使用者輸入目標（增肌 / 減脂 / 提升耐力），AI 產生一份週訓練計畫
- 與目前 AI 教練功能類似，差異在於從「分析過去」變成「主動規劃未來」
- Cloud Functions 新增一個 function，串接 Gemini API 產生計畫 JSON

### 動作影片 / 教學內容推薦
- 根據使用者訓練的部位，推薦對應的動作教學資源
- 後端負責過濾與排序邏輯，避免直接在 App 端暴露資料來源

---

## 長期（社群或進階功能）

### 多使用者資料比較
- 提供匿名的同年齡 / 同性別訓練量比較基準
- 需要後端彙總跨使用者資料，不可直接讓 App 讀取其他使用者的 Firestore

### 教練帳號 / 學員管理
- 教練可查看學員的訓練紀錄並給予回饋
- 跨使用者的資料存取需透過後端做權限控管，不能由 App 直接存取

---

## 跨平台計畫（KMP）

### 目標

將 WorkoutLog 擴展為 Android + iOS 雙平台 App，採用 **Kotlin Multiplatform（KMP）共享邏輯層、各平台原生 UI** 的架構。

### 架構方向

```
共享（commonMain）          Android             iOS
─────────────────────────────────────────────────────
data/model/             Jetpack Compose     SwiftUI
data/repository/        Hilt → Koin         原生 DI
ViewModel 層            ────────────────────────────
```

- **共享層**：`data/model/`、`data/repository/`、ViewModel 邏輯
- **Android UI**：維持 Jetpack Compose，不動
- **iOS UI**：用 SwiftUI 重新撰寫，對應相同 ViewModel 狀態

### 主要遷移工作

| 項目 | 說明 |
|------|------|
| Hilt → Koin | Hilt 不支援 KMP，需改用 Koin 做依賴注入 |
| Firebase SDK 替換 | 改用 `firebase-kotlin-sdk`（GitLive），支援 Auth / Firestore / Functions |
| ViewModel 共享 | 移至 `commonMain`，邏輯幾乎不需改動 |
| iOS UI 撰寫 | 用 SwiftUI 實作各頁面，消費同一套 ViewModel |

### 不採用 Compose Multiplatform 的理由

Compose Multiplatform 的 iOS 渲染目前尚未成熟，效能與平台整合風險較高，因此 UI 層各自用原生框架，確保最佳使用者體驗。

---

## 最容易延伸的起點

目前已串接 Gemini API，以下兩個功能只需新增 Cloud Function 並在 App 端加對應的 Repository 即可完成：

1. **訓練計畫產生** — 使用者輸入目標，AI 輸出週計畫 JSON
2. **動作姿勢建議** — 針對特定動作給予詳細的技術指導與注意事項
