\# \*\*MT MES Packing App 開發規劃 (Kotlin Android 原生版)\*\*



\*\*專案名稱：\*\* MT\_MES\_Packing\_View (Android Studio 專案名)

\*\*套件名稱：\*\* com.mtmes.packing.view (App 的唯一識別符)

\*\*開發語言：\*\* Kotlin

\*\*開發環境：\*\* Android Studio

\*\*最小 SDK 版本：\*\* API 21 (Android 5.0 Lollipop)



\*\*總目標：\*\* 打造一個功能完整的「單機版」Android App，實現介面顯示、相機掃描、手動輸入、後端資料同步、以及文件生成。



---



\## \*\*階段一：介面佈局與基礎功能\*\*



\*\*目標：\*\* 建立 App 的介面佈局，實現數據列表的顯示、手動輸入和列表操作。暫不涉及原生相機調用。此階段的目標是打造一個\*\*功能完整的「單機版」App\*\*，所有數據為寫死的假資料。



\*\*設計方案與開發細節：\*\*



1\.  \*\*專案結構理解與配置確認：\*\*

&nbsp;   \* \*\*說明：\*\* 確認 Android Studio 專案的檔案組織方式，主要包括 Kotlin 程式碼 (`app/src/main/java/com.mtmes.packing.view`) 和資源文件 (`app/src/main/res` 中的 `layout`、`drawable`、`values` 等)。

&nbsp;   \* \*\*關鍵配置：\*\*

&nbsp;       \* `build.gradle.kts (Module: app)`：設定 `compileSdk = 36`、`minSdk = 21`、`targetSdk = 36`，啟用 `viewBinding = true`，並引入必要的 AndroidX 相依性（如 `recyclerview`、`constraintlayout`、`material` 等）。



2\.  \*\*主介面佈局 (`activity\_main.xml`)：\*\*

&nbsp;   \* \*\*功能：\*\* 用戶進入 App 後的第一個畫面，顯示所有備料任務。

&nbsp;   \* \*\*UI 組件：\*\*

&nbsp;       \* `Toolbar` (標題欄)：顯示「備料任務清單」標題。

&nbsp;       \* `EditText` (搜尋框)：用戶輸入關鍵字，對任務列表進行搜尋/過濾。

&nbsp;       \* `Spinner` (排序選項)：下拉選單，讓用戶選擇列表的排序方式（如「備料日期」、「備料單號」、「完工狀態」、「掃描進度」）。預設為「備料日期 (舊到新)」。

&nbsp;       \* `Spinner` (狀態篩選)：下拉選單，讓用戶選擇按任務狀態篩選（如「所有狀態」、「待處理」、「進行中」、「已完成」）。預設為「所有狀態」。

&nbsp;       \* `RecyclerView` (任務列表)：用於高效顯示大量備料任務數據。

&nbsp;   \* \*\*設計：\*\* 使用 `ConstraintLayout` 進行彈性佈局，並結合 `LinearLayout` 或 `RelativeLayout` 組織各區域。



3\.  \*\*任務列表項目佈局 (`item\_task.xml`)：\*\*

&nbsp;   \* \*\*功能：\*\* 定義 `RecyclerView` 中每個備料任務單元的視覺化樣式。

&nbsp;   \* \*\*UI 組件：\*\*

&nbsp;       \* `CardView`：作為每個列表項目的外層容器，提供卡片效果和陰影。

&nbsp;       \* `TextView` (單號)：顯示備料單號 (e.g., "WO20250728001")。

&nbsp;       \* `TextView` (狀態)：顯示任務的完工狀態 (e.g., "進行中", "已完成")，並根據狀態顯示不同背景顏色（使用自定義 drawable）。

&nbsp;       \* `TextView` (日期及描述)：顯示備料日期和任務簡要描述。

&nbsp;       \* `TextView` (進度)：顯示已掃描數量與總數量的進度 (e.g., "15 / 20")。

&nbsp;   \* \*\*設計：\*\* 使用 `ConstraintLayout` 確保各元素相對位置和尺寸正確。



4\.  \*\*自定義 UI 資源文件 (Drawable XMLs)：\*\*

&nbsp;   \* `rounded\_edittext\_bg.xml`：定義搜尋框和手動輸入框的圓角白色背景帶邊框樣式。

&nbsp;   \* `rounded\_spinner\_bg.xml`：定義排序和狀態篩選下拉選單的圓角白色背景帶邊框樣式，並包含一個向下箭頭。

&nbsp;   \* `rounded\_status\_bg\_default.xml`：狀態標籤的預設背景（灰色圓角）。

&nbsp;   \* `rounded\_status\_bg\_in\_progress.xml`：狀態標籤「進行中」的背景（藍色圓角）。

&nbsp;   \* `rounded\_status\_bg\_completed.xml`：狀態標籤「已完成」的背景（綠色圓角）。

&nbsp;   \* `rounded\_status\_bg\_pending.xml`：狀態標籤「待處理」的背景（橙色圓角）。

&nbsp;   \* \*\*設計：\*\* 這些是 XML 形狀資源，用於定義可重複使用的背景樣式，提高 UI 的一致性。



5\.  \*\*數據模型 (`Task.kt`)：\*\*

&nbsp;   \* \*\*功能：\*\* 定義備料任務的數據結構。

&nbsp;   \* \*\*設計：\*\* Kotlin `data class`，包含 `taskId` (String)、`prepareDate` (String)、`status` (String)、`description` (String)、`itemsScanned` (Int)、`totalItems` (Int) 等屬性。



6\.  \*\*假資料提供 (`MockData.kt`)：\*\*

&nbsp;   \* \*\*功能：\*\* 提供寫死的 `Task` 列表數據，用於在 App 未連接後端時填充介面。

&nbsp;   \* \*\*設計：\*\* Kotlin `object` 類，包含一個 `getTaskList(): List<Task>` 方法，返回預定義的任務列表。



7\.  \*\*列表適配器 (`TaskListAdapter.kt`)：\*\*

&nbsp;   \* \*\*功能：\*\* 作為 `RecyclerView` 和 `List<Task>` 數據之間的橋樑，負責將數據綁定到 `item\_task.xml` 佈局。

&nbsp;   \* \*\*設計：\*\* 繼承 `RecyclerView.Adapter`，內部包含 `TaskViewHolder` 類來持有列表項目的視圖引用。實現 `onCreateViewHolder`、`onBindViewHolder` 和 `getItemCount` 方法。提供 `updateTasks` 方法來動態更新列表數據。



8\.  \*\*掃描作業介面佈局 (`activity\_scan.xml`)：\*\*

&nbsp;   \* \*\*功能：\*\* 用戶點擊任務後進入的掃描操作介面。

&nbsp;   \* \*\*UI 組件：\*\*

&nbsp;       \* `Toolbar` (頂部工具欄)：包含「返回」按鈕、「掃描作業」標題和「結束作業」按鈕。

&nbsp;       \* `TextView` (備料單號顯示)：顯示當前正在操作的備料單號。

&nbsp;       \* `TextView` (相機預覽佔位符)：在第二階段才整合相機，此處為提示文字的佔位區域。

&nbsp;       \* `Button` (啟動原生掃描)：用戶點擊此按鈕啟動相機掃描。

&nbsp;       \* `EditText` (手動輸入框)：用戶手動輸入料號。

&nbsp;       \* `Button` (新增)：將手動輸入的料號添加到列表中。

&nbsp;       \* `SwitchCompat` (重複掃描檢查開關)：控制是否在添加料號時檢查重複。

&nbsp;       \* `RecyclerView` (掃描清單)：顯示已掃描或手動添加的料號列表。

&nbsp;   \* \*\*設計：\*\* 類似 `activity\_main.xml`，使用 `ConstraintLayout` 結合 `LinearLayout` 佈局。



9\.  \*\*掃描列表項目佈局 (`item\_scanned\_item.xml`)：\*\*

&nbsp;   \* \*\*功能：\*\* 定義 `RecyclerView` 中每個已掃描料號單元的視覺化樣式。

&nbsp;   \* \*\*UI 組件：\*\*

&nbsp;       \* `CardView`：作為外層容器。

&nbsp;       \* `TextView` (料號)：顯示已掃描的料號。

&nbsp;       \* `TextView` (掃描時間)：顯示料號的掃描時間。

&nbsp;       \* `Button` (刪除)：用於從列表中刪除該料號。



10\. \*\*掃描數據模型 (`ScannedItem.kt`)：\*\*

&nbsp;   \* \*\*功能：\*\* 定義已掃描料號的數據結構。

&nbsp;   \* \*\*設計：\*\* Kotlin `data class`，包含 `partNumber` (String) 和 `scannedTime` (String) 屬性。



11\. \*\*掃描列表適配器 (`ScannedItemsAdapter.kt`)：\*\*

&nbsp;   \* \*\*功能：\*\* 作為 `RecyclerView` 和 `MutableList<ScannedItem>` 數據之間的橋樑。

&nbsp;   \* \*\*設計：\*\* 繼承 `RecyclerView.Adapter`，內部包含 `ScannedItemViewHolder`。提供 `addItem` 和 `removeItem` 方法來動態管理列表數據。



12\. \*\*主活動邏輯 (`MainActivity.kt`)：\*\*

&nbsp;   \* \*\*功能：\*\* 管理主螢幕的業務邏輯和用戶交互。

&nbsp;   \* \*\*設計：\*\*

&nbsp;       \* 使用 `ViewBinding` 訪問 UI 元素。

&nbsp;       \* 在 `onCreate` 中初始化介面、載入假資料、設定 `RecyclerView`、`Spinner` 和搜尋框的監聽器。

&nbsp;       \* 實現 `loadTasks()`：從 `MockData` 獲取數據。

&nbsp;       \* 實現 `setupRecyclerView()`：設置 `TaskListAdapter`，並處理點擊事件，通過 `Intent` 跳轉到 `ScanActivity` 並傳遞 `taskId`。

&nbsp;       \* 實現 `setupSpinners()`：設定排序和狀態篩選下拉選單的數據和選擇監聽。

&nbsp;       \* 實現 `setupSearch()`：設定搜尋框的文字變化監聽。

&nbsp;       \* 實現 `applyFiltersAndSort()`：根據搜尋文字、選中的篩選和排序選項，對 `originalTasks` 進行過濾和排序，然後更新 `taskListAdapter`。



13\. \*\*掃描頁面邏輯 (`ScanActivity.kt`)：\*\*

&nbsp;   \* \*\*功能：\*\* 管理掃描作業螢幕的業務邏輯和用戶交互。

&nbsp;   \* \*\*設計：\*\*

&nbsp;       \* 使用 `ViewBinding` 訪問 UI 元素。

&nbsp;       \* 在 `onCreate` 中初始化介面、接收 `taskId`、設定 `Toolbar`、`RecyclerView`、手動輸入和開關的監聽器。

&nbsp;       \* 實現 `setupToolbar()`：處理返回和結束作業按鈕的點擊事件（返回 `MainActivity`）。

&nbsp;       \* 實現 `setupRecyclerView()`：設置 `ScannedItemsAdapter`，處理刪除按鈕點擊事件。

&nbsp;       \* 實現 `setupInputAndButtons()`：處理手動輸入框的文本變化和新增按鈕點擊事件。

&nbsp;       \* 實現 `setupDuplicateCheckSwitch()`：處理重複檢查開關的狀態變化。

&nbsp;       \* 實現 `addItem(partNumber: String)`：核心方法，處理料號的重複檢查，將新料號添加到 `scannedItemsAdapter` 並刷新列表。

&nbsp;       \* 實現 `showEndOperationDialog()` 和 `showDeleteItemDialog()`：顯示確認對話框。



---



\## \*\*階段二：原生掃描整合\*\*



\*\*目標：\*\* 將手機原生相機功能與 App 介面整合，實現條碼掃描。



\*\*設計方案與開發細節：\*\*



1\.  \*\*權限管理：\*\*

&nbsp;   \* \*\*`AndroidManifest.xml`：\*\* 聲明 `android.permission.CAMERA` 和 `android.permission.WRITE\_EXTERNAL\_STORAGE` (如果需要保存掃描歷史到本地文件) 權限。

&nbsp;   \* \*\*`ScanActivity.kt`：\*\* 在啟動掃描前，實現運行時權限請求（Android 6.0 Marshmallow (API 23) 及以上版本需要動態請求權限）。使用 `ActivityCompat.requestPermissions`。



2\.  \*\*掃描庫整合：\*\*

&nbsp;   \* \*\*選擇掃描庫：\*\* 推薦使用 Google 的 \[ML Kit Barcode Scanning](https://developers.google.com/ml-kit/vision/barcode-scanning) 或開源的 \[ZXing](https://github.com/journeyapps/zxing-android-embedded)。ML Kit 更現代且性能好。

&nbsp;   \* \*\*`build.gradle.kts (Module: app)`：\*\* 添加所選掃描庫的相依性。

&nbsp;       \* 例如 ML Kit: `implementation 'com.google.mlkit:barcode-scanning:17.2.0'`

&nbsp;       \* 例如 ZXing: `implementation 'com.journeyapps:zxing-android-embedded:4.3.0'` (需額外處理一些配置)

&nbsp;   \* \*\*重要：\*\* 為了實現快速可視化，我會先採用 `ZXing-android-embedded`，它相對更容易快速集成並啟動相機介面。



3\.  \*\*相機掃描邏輯 (`ScanActivity.kt`)：\*\*

&nbsp;   \* \*\*綁定事件：\*\* 將 `startScanButton` 的點擊事件與掃描邏輯綁定。

&nbsp;   \* \*\*啟動掃描：\*\* 檢查權限後，呼叫掃描庫的 API 啟動相機介面。

&nbsp;       \* 例如，使用 `IntentIntegrator` (ZXing) 或 `BarcodeScanner` (ML Kit) 啟動掃描 Activity。

&nbsp;   \* \*\*接收結果：\*\* 覆寫 `onActivityResult` 方法，接收掃描 Activity 返回的結果，解析出條碼內容。

&nbsp;   \* \*\*處理結果：\*\* 將掃描到的條碼通過 `addItem()` 函式加入到掃描清單中。

&nbsp;   \* \*\*錯誤處理：\*\* 處理掃描失敗或使用者取消的邏輯，給予相應提示。

&nbsp;   \* \*\*介面優化：\*\* 調整掃描庫的選項，使其相機預覽範圍較小，便於用戶使用。



---



\## \*\*階段三：後端 API 整合與資料同步\*\*



\*\*目標：\*\* 用真實的後端資料，取代 App 中的所有假資料。



\*\*設計方案與開發細節：\*\*



1\.  \*\*網絡請求庫整合：\*\*

&nbsp;   \* \*\*選擇庫：\*\* 推薦使用 `Retrofit` (基於 `OkHttp`)，它是 Android 上最流行的類型安全的 HTTP 客戶端。

&nbsp;   \* \*\*`build.gradle.kts (Module: app)`：\*\* 添加 `Retrofit`、`OkHttp` 和 JSON 轉換器（如 `Gson` 或 ` kotlinx.serialization`）的相依性。

&nbsp;   \* \*\*`AndroidManifest.xml`：\*\* 聲明 `android.permission.INTERNET` 網絡權限。



2\.  \*\*數據模型調整：\*\*

&nbsp;   \* 調整 `Task.kt` 和 `ScannedItem.kt` 數據模型，以匹配後端 API 返回的真實資料結構。可能需要添加 `Serializable` 或 `Parcelable` 接口以便在組件間傳遞。



3\.  \*\*API 服務接口 (`ApiService.kt`)：\*\*

&nbsp;   \* \*\*功能：\*\* 定義與後端 API 交互的接口。

&nbsp;   \* \*\*設計：\*\* Kotlin `interface`，使用 Retrofit 的註解定義 `GET /api/workorders` (獲取任務清單)、`GET /api/workorders/{單號}` (獲取已掃描明細)、`POST /api/scan` (提交掃描數據) 等 API 方法。



4\.  \*\*載入任務清單 (`MainActivity.kt`)：\*\*

&nbsp;   \* \*\*修改 `loadTasks()`：\*\* 不再從 `MockData.getTaskList()` 獲取數據。

&nbsp;   \* \*\*網絡請求：\*\* 使用 `Retrofit` 呼叫 `GET /api/workorders` API。

&nbsp;   \* \*\*非同步處理：\*\* 使用 Kotlin Coroutines 或 Callback 處理網絡請求的非同步操作，避免阻塞 UI 線程。

&nbsp;   \* \*\*數據更新：\*\* 收到後端數據後，更新 `taskListAdapter`。

&nbsp;   \* \*\*錯誤處理：\*\* 處理網絡請求失敗（如網絡不穩、服務器錯誤）的情況，向用戶顯示錯誤提示。



5\.  \*\*載入已掃描明細 (`ScanActivity.kt`)：\*\*

&nbsp;   \* \*\*修改數據載入：\*\* 當從 `MainActivity` 跳轉到 `ScanActivity` 時，接收 `taskId`。

&nbsp;   \* \*\*網絡請求：\*\* 呼叫 `GET /api/workorders/{單號}` API，根據單號獲取已存在的掃描紀錄。

&nbsp;   \* \*\*數據更新：\*\* 收到數據後，填充 `scannedItemsAdapter`。

&nbsp;   \* \*\*錯誤處理：\*\* 處理網絡請求失敗。



---



\## \*\*階段四：P檔 (交換檔) 生成 (即時同步)\*\*



\*\*目標：\*\* 實現核心的「即時同步」功能，每掃描一筆料號， App 就立刻將數據發送到後端。



\*\*設計方案與開發細節：\*\*



1\.  \*\*即時同步邏輯 (`ScanActivity.kt`)：\*\*

&nbsp;   \* \*\*修改 `addItem()` 函式：\*\*

&nbsp;       \* 在料號成功添加到本地 `scannedItemsAdapter` 後，立即觸發網絡請求。

&nbsp;       \* \*\*網絡請求：\*\* 呼叫 `POST /api/scan` 後端 API。

&nbsp;       \* \*\*請求參數：\*\* 將當前的「備料單號 (`currentTaskId`)」和「新掃描的料號 (`partNumber`)」作為請求體發送。

&nbsp;       \* \*\*非同步處理：\*\* 確保網絡請求是非同步的，不阻塞 UI。

&nbsp;   \* \*\*響應處理：\*\*

&nbsp;       \* \*\*成功：\*\* 如果 API 返回成功，給予用戶提示（例如「同步成功」）。

&nbsp;       \* \*\*失敗：\*\* 如果 API 返回失敗（如網絡問題、服務器錯誤），給予用戶提示（例如「同步失敗，請檢查網絡或稍後重試」）。

&nbsp;       \* \*\*VFP 下載器驗證：\*\* 呼應需求，此階段後即可透過 VFP 下載器去驗證 P檔 的內容是否與 PDA 畫面同步。



2\.  \*\*錯誤重試/離線緩存 (可選但推薦)：\*\*

&nbsp;   \* \*\*功能：\*\* 增強 App 的健壯性，防止數據丟失。

&nbsp;   \* \*\*設計：\*\*

&nbsp;       \* \*\*失敗重試：\*\* 如果 `POST /api/scan` 請求失敗，可以將該數據暫存到本地（例如使用 Room 數據庫或簡單的檔案），並在網絡恢復或 App 重新啟動時嘗試重試發送。

&nbsp;       \* \*\*離線模式：\*\* App 可以在離線狀態下繼續掃描，數據暫存在本地，待聯網後再批量同步。



---

