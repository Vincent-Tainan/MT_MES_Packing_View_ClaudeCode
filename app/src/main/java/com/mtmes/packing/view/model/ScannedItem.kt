package com.mtmes.packing.view.model

/**
 * 已掃描料號數據模型
 */
data class ScannedItem(
    val partNumber: String,       // 料號
    val scannedTime: String       // 掃描時間
) {
    companion object {
        /**
         * 創建當前時間的掃描項目
         */
        fun createWithCurrentTime(partNumber: String): ScannedItem {
            val currentTime = java.text.SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                java.util.Locale.getDefault()
            ).format(java.util.Date())
            
            return ScannedItem(partNumber, currentTime)
        }
    }
}