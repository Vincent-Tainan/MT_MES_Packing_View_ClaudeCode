package com.mtmes.packing.view.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import com.mtmes.packing.view.BuildConfig

/**
 * 版本更新管控管理器
 * 負責檢查應用版本、驗證更新安全性
 */
class VersionControlManager(private val context: Context) {
    
    companion object {
        private const val TAG = "VersionControl"
        private const val VERSION_CHECK_URL = "https://your-server.com/api/version-check"
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private const val SIGNATURE_HEADER = "X-Signature"
        
        // 最低支援版本
        private const val MIN_SUPPORTED_VERSION_CODE = 1
        private const val MIN_SUPPORTED_API_LEVEL = 21
        
        // 強制更新版本號
        private const val FORCE_UPDATE_VERSION_CODE = 1
    }
    
    /**
     * 版本資訊數據類
     */
    data class VersionInfo(
        val versionCode: Int,
        val versionName: String,
        val minSupportedVersion: Int,
        val forceUpdateVersion: Int,
        val downloadUrl: String,
        val releaseNotes: String,
        val signature: String,
        val checksum: String,
        val releaseDate: String,
        val isSecurityUpdate: Boolean
    )
    
    /**
     * 更新策略枚舉
     */
    enum class UpdatePolicy {
        NO_UPDATE_NEEDED,    // 無需更新
        OPTIONAL_UPDATE,     // 可選更新
        RECOMMENDED_UPDATE,  // 建議更新
        FORCE_UPDATE,        // 強制更新
        SECURITY_UPDATE,     // 安全更新
        UNSUPPORTED_VERSION  // 不支援版本
    }
    
    /**
     * 檢查版本更新
     */
    suspend fun checkVersionUpdate(): Result<UpdatePolicy> = withContext(Dispatchers.IO) {
        try {
            // 1. 獲取當前應用版本
            val currentVersion = getCurrentVersionInfo()
            
            // 2. 檢查系統API等級
            if (Build.VERSION.SDK_INT < MIN_SUPPORTED_API_LEVEL) {
                return@withContext Result.failure(
                    SecurityException("System API level not supported")
                )
            }
            
            // 3. 向服務器查詢最新版本
            val serverVersionInfo = fetchServerVersionInfo(currentVersion)
            
            // 4. 驗證服務器回應簽名
            if (!verifyServerSignature(serverVersionInfo)) {
                return@withContext Result.failure(
                    SecurityException("Server response signature verification failed")
                )
            }
            
            // 5. 決定更新策略
            val policy = determineUpdatePolicy(currentVersion, serverVersionInfo)
            
            Result.success(policy)
            
        } catch (e: Exception) {
            Log.e(TAG, "Version check failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * 獲取當前應用版本資訊
     */
    private fun getCurrentVersionInfo(): VersionInfo {
        val packageManager = context.packageManager
        val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
        
        return VersionInfo(
            versionCode = packageInfo.versionCode,
            versionName = packageInfo.versionName ?: "1.0.0",
            minSupportedVersion = MIN_SUPPORTED_VERSION_CODE,
            forceUpdateVersion = FORCE_UPDATE_VERSION_CODE,
            downloadUrl = "",
            releaseNotes = "",
            signature = "",
            checksum = "",
            releaseDate = "",
            isSecurityUpdate = false
        )
    }
    
    /**
     * 從服務器獲取版本資訊
     */
    private suspend fun fetchServerVersionInfo(currentVersion: VersionInfo): VersionInfo {
        val url = URL(VERSION_CHECK_URL)
        val connection = url.openConnection() as HttpURLConnection
        
        try {
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "MTMESPackingApp/${currentVersion.versionName}")
            connection.doOutput = true
            
            // 構建請求數據
            val requestData = JSONObject().apply {
                put("currentVersion", currentVersion.versionCode)
                put("deviceInfo", getDeviceInfo())
                put("timestamp", System.currentTimeMillis())
            }
            
            // 發送請求
            connection.outputStream.use { output ->
                output.write(requestData.toString().toByteArray())
            }
            
            // 讀取回應
            val response = connection.inputStream.bufferedReader().readText()
            val jsonResponse = JSONObject(response)
            
            return VersionInfo(
                versionCode = jsonResponse.getInt("latestVersion"),
                versionName = jsonResponse.getString("versionName"),
                minSupportedVersion = jsonResponse.getInt("minSupportedVersion"),
                forceUpdateVersion = jsonResponse.getInt("forceUpdateVersion"),
                downloadUrl = jsonResponse.getString("downloadUrl"),
                releaseNotes = jsonResponse.getString("releaseNotes"),
                signature = connection.getHeaderField(SIGNATURE_HEADER) ?: "",
                checksum = jsonResponse.getString("checksum"),
                releaseDate = jsonResponse.getString("releaseDate"),
                isSecurityUpdate = jsonResponse.getBoolean("isSecurityUpdate")
            )
            
        } finally {
            connection.disconnect()
        }
    }
    
    /**
     * 驗證服務器回應簽名
     */
    private fun verifyServerSignature(versionInfo: VersionInfo): Boolean {
        return try {
            // 實際專案中應從安全位置獲取密鑰
            val secretKey = getSecretKey()
            val mac = Mac.getInstance(HMAC_ALGORITHM)
            mac.init(SecretKeySpec(secretKey.toByteArray(), HMAC_ALGORITHM))
            
            val dataToVerify = "${versionInfo.versionCode}${versionInfo.downloadUrl}${versionInfo.checksum}"
            val calculatedSignature = mac.doFinal(dataToVerify.toByteArray())
                .joinToString("") { "%02x".format(it) }
            
            calculatedSignature.equals(versionInfo.signature, ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "Signature verification failed", e)
            false
        }
    }
    
    /**
     * 決定更新策略
     */
    private fun determineUpdatePolicy(current: VersionInfo, server: VersionInfo): UpdatePolicy {
        return when {
            // 當前版本低於最低支援版本
            current.versionCode < server.minSupportedVersion -> 
                UpdatePolicy.UNSUPPORTED_VERSION
                
            // 安全更新
            server.isSecurityUpdate && current.versionCode < server.versionCode -> 
                UpdatePolicy.SECURITY_UPDATE
                
            // 強制更新
            current.versionCode <= server.forceUpdateVersion -> 
                UpdatePolicy.FORCE_UPDATE
                
            // 有新版本但非強制
            current.versionCode < server.versionCode -> {
                val versionGap = server.versionCode - current.versionCode
                if (versionGap >= 3) UpdatePolicy.RECOMMENDED_UPDATE
                else UpdatePolicy.OPTIONAL_UPDATE
            }
            
            // 無需更新
            else -> UpdatePolicy.NO_UPDATE_NEEDED
        }
    }
    
    /**
     * 獲取設備資訊（用於日誌和安全驗證）
     */
    private fun getDeviceInfo(): JSONObject {
        return JSONObject().apply {
            put("model", Build.MODEL)
            put("manufacturer", Build.MANUFACTURER)
            put("sdkVersion", Build.VERSION.SDK_INT)
            put("appVersion", getCurrentVersionInfo().versionCode)
        }
    }
    
    /**
     * 獲取密鑰（實際專案中應從安全存儲獲取）
     */
    private fun getSecretKey(): String {
        // 實際專案中建議：
        // 1. 使用 Android Keystore
        // 2. 從服務器動態獲取
        // 3. 使用證書綁定
        return BuildConfig.VERSION_CHECK_SECRET
    }
    
    /**
     * 驗證APK文件完整性
     */
    fun verifyApkIntegrity(apkPath: String, expectedChecksum: String): Boolean {
        return try {
            val fileBytes = java.io.File(apkPath).readBytes()
            val digest = MessageDigest.getInstance("SHA-256")
            val checksum = digest.digest(fileBytes).joinToString("") { "%02x".format(it) }
            
            checksum.equals(expectedChecksum, ignoreCase = true)
        } catch (e: Exception) {
            Log.e(TAG, "APK integrity verification failed", e)
            false
        }
    }
}