package com.mtmes.packing.view.model

/**
 * 備料任務數據模型
 */
data class Task(
    val taskId: String,           // 備料單號
    val prepareDate: String,      // 備料日期
    val status: String,           // 任務狀態 (待處理/進行中/已完成)
    val description: String,      // 任務描述
    val itemsScanned: Int,        // 已掃描數量
    val totalItems: Int           // 總數量
) {
    /**
     * 獲取任務進度百分比
     */
    fun getProgressPercentage(): Float {
        return if (totalItems > 0) {
            (itemsScanned.toFloat() / totalItems.toFloat()) * 100
        } else {
            0f
        }
    }
    
    /**
     * 檢查任務是否已完成
     */
    fun isCompleted(): Boolean {
        return itemsScanned >= totalItems && totalItems > 0
    }
    
    companion object {
        const val STATUS_PENDING = "待處理"
        const val STATUS_IN_PROGRESS = "進行中"
        const val STATUS_COMPLETED = "已完成"
    }
}