package com.mtmes.packing.view.security

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.mtmes.packing.view.R
import kotlinx.coroutines.launch
import java.io.File

/**
 * 更新執行管理器
 * 負責執行版本更新策略
 */
class UpdateExecutor(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateExecutor"
    }
    
    private val versionControlManager = VersionControlManager(context)
    private var downloadId: Long = -1
    
    /**
     * 執行版本檢查和更新流程
     */
    fun executeUpdateCheck() {
        // 使用協程執行非同步版本檢查
        if (context is androidx.lifecycle.LifecycleOwner) {
            context.lifecycleScope.launch {
                val result = versionControlManager.checkVersionUpdate()
                result.fold(
                    onSuccess = { policy -> handleUpdatePolicy(policy) },
                    onFailure = { error -> handleUpdateError(error) }
                )
            }
        }
    }
    
    /**
     * 處理不同的更新策略
     */
    private fun handleUpdatePolicy(policy: VersionControlManager.UpdatePolicy) {
        when (policy) {
            VersionControlManager.UpdatePolicy.NO_UPDATE_NEEDED -> {
                Log.i(TAG, "App is up to date")
            }
            
            VersionControlManager.UpdatePolicy.OPTIONAL_UPDATE -> {
                showOptionalUpdateDialog()
            }
            
            VersionControlManager.UpdatePolicy.RECOMMENDED_UPDATE -> {
                showRecommendedUpdateDialog()
            }
            
            VersionControlManager.UpdatePolicy.FORCE_UPDATE -> {
                showForceUpdateDialog()
            }
            
            VersionControlManager.UpdatePolicy.SECURITY_UPDATE -> {
                showSecurityUpdateDialog()
            }
            
            VersionControlManager.UpdatePolicy.UNSUPPORTED_VERSION -> {
                showUnsupportedVersionDialog()
            }
        }
    }
    
    /**
     * 顯示可選更新對話框
     */
    private fun showOptionalUpdateDialog() {
        AlertDialog.Builder(context)
            .setTitle("應用程式更新")
            .setMessage("發現新版本，是否要立即更新？")
            .setPositiveButton("立即更新") { _, _ ->
                startUpdateDownload()
            }
            .setNegativeButton("稍後提醒") { dialog, _ ->
                dialog.dismiss()
                scheduleNextUpdateCheck(24 * 60 * 60 * 1000) // 24小時後再提醒
            }
            .setCancelable(true)
            .show()
    }
    
    /**
     * 顯示建議更新對話框
     */
    private fun showRecommendedUpdateDialog() {
        AlertDialog.Builder(context)
            .setTitle("重要更新")
            .setMessage("發現重要版本更新，建議立即更新以獲得最佳體驗。")
            .setPositiveButton("立即更新") { _, _ ->
                startUpdateDownload()
            }
            .setNegativeButton("稍後更新") { dialog, _ ->
                dialog.dismiss()
                scheduleNextUpdateCheck(4 * 60 * 60 * 1000) // 4小時後再提醒
            }
            .setCancelable(true)
            .show()
    }
    
    /**
     * 顯示強制更新對話框
     */
    private fun showForceUpdateDialog() {
        AlertDialog.Builder(context)
            .setTitle("必要更新")
            .setMessage("必須更新到最新版本才能繼續使用應用程式。")
            .setPositiveButton("立即更新") { _, _ ->
                startUpdateDownload()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 顯示安全更新對話框
     */
    private fun showSecurityUpdateDialog() {
        AlertDialog.Builder(context)
            .setTitle("🔒 安全更新")
            .setMessage("發現重要安全更新，為保護您的資料安全，請立即更新。")
            .setPositiveButton("立即更新") { _, _ ->
                startUpdateDownload()
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 顯示不支援版本對話框
     */
    private fun showUnsupportedVersionDialog() {
        AlertDialog.Builder(context)
            .setTitle("版本過舊")
            .setMessage("您的應用程式版本過舊，已不再支援。請更新到最新版本。")
            .setPositiveButton("立即更新") { _, _ ->
                startUpdateDownload()
            }
            .setNegativeButton("退出應用") { _, _ ->
                if (context is android.app.Activity) {
                    context.finishAffinity()
                }
            }
            .setCancelable(false)
            .show()
    }
    
    /**
     * 開始下載更新
     */
    private fun startUpdateDownload() {
        try {
            // 檢查存儲權限
            if (!hasStoragePermission()) {
                requestStoragePermission()
                return
            }
            
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadUri = Uri.parse("https://your-server.com/apk/latest.apk")
            
            val request = DownloadManager.Request(downloadUri).apply {
                setTitle("MT MES Packing App 更新")
                setDescription("正在下載最新版本...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "MTMESPacking_update.apk")
                setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            }
            
            downloadId = downloadManager.enqueue(request)
            
            // 註冊下載完成監聽器
            registerDownloadReceiver()
            
            Toast.makeText(context, "開始下載更新...", Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start download", e)
            Toast.makeText(context, "下載失敗，請稍後重試", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 註冊下載完成廣播接收器
     */
    private fun registerDownloadReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (id == downloadId) {
                    handleDownloadComplete()
                    context?.unregisterReceiver(this)
                }
            }
        }
        
        context.registerReceiver(
            receiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        )
    }
    
    /**
     * 處理下載完成
     */
    private fun handleDownloadComplete() {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = downloadManager.query(query)
            
            if (cursor.moveToFirst()) {
                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                val localUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                
                if (status == DownloadManager.STATUS_SUCCESSFUL && localUri != null) {
                    // 驗證APK完整性
                    val apkPath = Uri.parse(localUri).path
                    if (apkPath != null && verifyDownloadedApk(apkPath)) {
                        installApk(apkPath)
                    } else {
                        showError("下載的文件驗證失敗，請重新下載")
                    }
                } else {
                    showError("下載失敗，請檢查網絡連接")
                }
            }
            cursor.close()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle download complete", e)
            showError("安裝失敗：${e.message}")
        }
    }
    
    /**
     * 驗證下載的APK
     */
    private fun verifyDownloadedApk(apkPath: String): Boolean {
        // TODO: 從服務器獲取預期的校驗和
        val expectedChecksum = "expected_checksum_from_server"
        return versionControlManager.verifyApkIntegrity(apkPath, expectedChecksum)
    }
    
    /**
     * 安裝APK
     */
    private fun installApk(apkPath: String) {
        try {
            val apkFile = File(apkPath)
            val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
            } else {
                Uri.fromFile(apkFile)
            }
            
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(installIntent)
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install APK", e)
            showError("安裝失敗：${e.message}")
        }
    }
    
    /**
     * 檢查存儲權限
     */
    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            // 對於較舊的Android版本，檢查寫入權限
            true // 簡化處理，實際專案中需要檢查WRITE_EXTERNAL_STORAGE權限
        }
    }
    
    /**
     * 請求存儲權限
     */
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = Uri.parse("package:${context.packageName}")
            context.startActivity(intent)
        }
    }
    
    /**
     * 安排下次更新檢查
     */
    private fun scheduleNextUpdateCheck(delayMillis: Long) {
        // TODO: 使用WorkManager或AlarmManager安排下次檢查
        Log.i(TAG, "Next update check scheduled in ${delayMillis}ms")
    }
    
    /**
     * 顯示錯誤信息
     */
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        Log.e(TAG, message)
    }
    
    /**
     * 處理更新錯誤
     */
    private fun handleUpdateError(error: Throwable) {
        Log.e(TAG, "Update check failed", error)
        
        when (error) {
            is SecurityException -> {
                showError("安全驗證失敗，請稍後重試或聯繫技術支援")
            }
            is java.net.UnknownHostException -> {
                showError("網絡連接失敗，請檢查網絡設定")
            }
            else -> {
                showError("版本檢查失敗：${error.message}")
            }
        }
    }
}