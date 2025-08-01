package com.mtmes.packing.view.data

import com.mtmes.packing.view.model.Task
import com.mtmes.packing.view.model.ScannedItem

/**
 * 假資料提供類別
 * 用於在 App 未連接後端時填充介面
 */
object MockData {
    
    /**
     * 獲取模擬的任務列表
     */
    fun getTaskList(): List<Task> {
        return listOf(
            Task(
                taskId = "WO20250728001",
                prepareDate = "2025-07-28",
                status = Task.STATUS_IN_PROGRESS,
                description = "手機外殼組裝備料",
                itemsScanned = 15,
                totalItems = 20
            ),
            Task(
                taskId = "WO20250728002", 
                prepareDate = "2025-07-28",
                status = Task.STATUS_COMPLETED,
                description = "電池模組備料",
                itemsScanned = 25,
                totalItems = 25
            ),
            Task(
                taskId = "WO20250729001",
                prepareDate = "2025-07-29",
                status = Task.STATUS_PENDING,
                description = "螢幕總成備料",
                itemsScanned = 0,
                totalItems = 18
            ),
            Task(
                taskId = "WO20250729002",
                prepareDate = "2025-07-29", 
                status = Task.STATUS_IN_PROGRESS,
                description = "充電器套件備料",
                itemsScanned = 8,
                totalItems = 30
            ),
            Task(
                taskId = "WO20250730001",
                prepareDate = "2025-07-30",
                status = Task.STATUS_PENDING,
                description = "耳機配件備料",
                itemsScanned = 0,
                totalItems = 12
            ),
            Task(
                taskId = "WO20250730002",
                prepareDate = "2025-07-30",
                status = Task.STATUS_IN_PROGRESS,
                description = "主機板元件備料",
                itemsScanned = 22,
                totalItems = 35
            ),
            Task(
                taskId = "WO20250731001",
                prepareDate = "2025-07-31",
                status = Task.STATUS_PENDING,
                description = "記憶體模組備料",
                itemsScanned = 0,
                totalItems = 16
            ),
            Task(
                taskId = "WO20250731002",
                prepareDate = "2025-07-31",
                status = Task.STATUS_COMPLETED,
                description = "相機鏡頭備料",
                itemsScanned = 10,
                totalItems = 10
            )
        )
    }
    
    /**
     * 獲取指定任務的模擬掃描紀錄
     */
    fun getScannedItemsForTask(taskId: String): List<ScannedItem> {
        return when (taskId) {
            "WO20250728001" -> listOf(
                ScannedItem("PART001", "2025-07-28 09:15:30"),
                ScannedItem("PART002", "2025-07-28 09:16:45"),
                ScannedItem("PART003", "2025-07-28 09:18:20"),
                ScannedItem("PART004", "2025-07-28 09:20:10"),
                ScannedItem("PART005", "2025-07-28 09:22:35")
            )
            "WO20250728002" -> listOf(
                ScannedItem("BATT001", "2025-07-28 10:30:15"),
                ScannedItem("BATT002", "2025-07-28 10:31:22"),
                ScannedItem("BATT003", "2025-07-28 10:32:40")
            )
            "WO20250729002" -> listOf(
                ScannedItem("CHRG001", "2025-07-29 14:15:30"),
                ScannedItem("CHRG002", "2025-07-29 14:16:45"),
                ScannedItem("CHRG003", "2025-07-29 14:18:20")
            )
            "WO20250730002" -> listOf(
                ScannedItem("MB001", "2025-07-30 11:20:15"),
                ScannedItem("MB002", "2025-07-30 11:21:30"),
                ScannedItem("MB003", "2025-07-30 11:22:45"),
                ScannedItem("MB004", "2025-07-30 11:24:10")
            )
            else -> emptyList()
        }
    }
    
    /**
     * 獲取排序選項列表
     */
    fun getSortOptions(): List<String> {
        return listOf(
            "備料日期 (舊到新)",
            "備料日期 (新到舊)",
            "備料單號 (A-Z)",
            "備料單號 (Z-A)",
            "完工狀態",
            "掃描進度 (低到高)",
            "掃描進度 (高到低)"
        )
    }
    
    /**
     * 獲取狀態篩選選項列表
     */
    fun getStatusFilterOptions(): List<String> {
        return listOf(
            "所有狀態",
            Task.STATUS_PENDING,
            Task.STATUS_IN_PROGRESS,
            Task.STATUS_COMPLETED
        )
    }
}