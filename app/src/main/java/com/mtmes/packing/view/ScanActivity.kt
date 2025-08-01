package com.mtmes.packing.view

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.mtmes.packing.view.adapter.ScannedItemsAdapter
import com.mtmes.packing.view.data.MockData
import com.mtmes.packing.view.databinding.ActivityScanBinding
import com.mtmes.packing.view.model.ScannedItem
import com.mtmes.packing.view.utils.LanguageManager

/**
 * 掃描活動 - 管理掃描作業功能
 */
class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var scannedItemsAdapter: ScannedItemsAdapter
    
    private var currentTaskId: String = ""
    private var taskDescription: String = ""
    private var isDuplicateCheckEnabled: Boolean = true
    
    // ZXing 掃描器
    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, getString(R.string.scan_cancelled), Toast.LENGTH_SHORT).show()
            updateScanStatus(getString(R.string.scan_area_ready))
        } else {
            val scannedText = result.contents
            Toast.makeText(this, "掃描結果: $scannedText", Toast.LENGTH_LONG).show()
            addScannedItem(scannedText)
            updateScanStatus(getString(R.string.scan_success))
            
            // 延遲重新啟動掃描
            binding.root.postDelayed({
                startBarcodeScanning()
            }, 1500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        getIntentData()
        setupToolbar()
        setupRecyclerView()
        setupTaskInfo()
        setupInputAndButtons()
        setupDuplicateCheckSwitch()
        setupScanArea()
        loadExistingScannedItems()
        
        // 自動啟動掃描
        startBarcodeScanning()
    }

    override fun attachBaseContext(newBase: Context?) {
        val language = newBase?.let { LanguageManager.getSavedLanguage(it) } ?: LanguageManager.LANGUAGE_CHINESE
        val context = newBase?.let { LanguageManager.setAppLanguage(it, language) }
        super.attachBaseContext(context)
    }

    /**
     * 獲取 Intent 傳遞的數據
     */
    private fun getIntentData() {
        currentTaskId = intent.getStringExtra("taskId") ?: ""
        taskDescription = intent.getStringExtra("taskDescription") ?: ""
    }

    /**
     * 設定工具欄
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        // 設定資料檢查按鈕
        binding.btnDataCheck.setOnClickListener {
            showDataCheckDialog()
        }
        
        // 設定結束作業按鈕
        binding.btnEndOperation.setOnClickListener {
            showEndOperationDialog()
        }
        
        // 處理返回按鈕
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    /**
     * 設定任務信息顯示
     */
    private fun setupTaskInfo() {
        binding.tvCurrentTaskId.text = getString(R.string.task_id_format, currentTaskId)
        updateScanCount()
    }
    
    /**
     * 更新掃描筆數顯示
     */
    private fun updateScanCount() {
        val count = scannedItemsAdapter.itemCount
        binding.tvScanCount.text = getString(R.string.scan_count_format, count)
    }

    /**
     * 設定 RecyclerView
     */
    private fun setupRecyclerView() {
        scannedItemsAdapter = ScannedItemsAdapter()
        
        binding.rvScannedItems.apply {
            layoutManager = LinearLayoutManager(this@ScanActivity)
            adapter = scannedItemsAdapter
        }
    }

    /**
     * 設定輸入框和按鈕
     */
    private fun setupInputAndButtons() {
        // 監聽手動輸入框
        binding.etManualInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // 當輸入框有內容時啟用新增按鈕
                binding.btnAdd.isEnabled = !s.isNullOrBlank()
            }
        })
        
        // 新增按鈕點擊事件
        binding.btnAdd.setOnClickListener {
            val partNumber = binding.etManualInput.text.toString().trim()
            if (partNumber.isNotEmpty()) {
                addItem(partNumber)
                binding.etManualInput.text.clear()
            }
        }
        
        // 初始狀態：新增按鈕不可用
        binding.btnAdd.isEnabled = false
    }

    /**
     * 設定重複檢查開關
     */
    private fun setupDuplicateCheckSwitch() {
        binding.switchDuplicateCheck.setOnCheckedChangeListener { _, isChecked ->
            isDuplicateCheckEnabled = isChecked
        }
    }

    /**
     * 設定掃描區域
     */
    private fun setupScanArea() {
        // 設定掃描區域點擊事件
        binding.layoutScanArea.setOnClickListener {
            startBarcodeScanning()
        }
        
        // 設定初始狀態
        updateScanStatus(getString(R.string.scan_area_ready))
    }

    /**
     * 啟動條碼掃描
     */
    private fun startBarcodeScanning() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt(getString(R.string.scan_prompt))
            setCameraId(0)  // 使用後置鏡頭
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
            setTimeout(30000) // 30秒超時
        }
        
        updateScanStatus(getString(R.string.scan_starting))
        scanLauncher.launch(options)
    }

    /**
     * 更新掃描狀態顯示
     */
    private fun updateScanStatus(status: String) {
        binding.tvScanStatus.text = status
    }

    /**
     * 載入現有的掃描項目
     */
    private fun loadExistingScannedItems() {
        val existingItems = MockData.getScannedItemsForTask(currentTaskId)
        scannedItemsAdapter.setItems(existingItems)
        updateScanCount()
    }

    /**
     * 添加項目到掃描清單
     */
    private fun addItem(partNumber: String) {
        // 重複檢查
        if (isDuplicateCheckEnabled && scannedItemsAdapter.containsPartNumber(partNumber)) {
            Toast.makeText(this, "料號 $partNumber 已存在，請檢查重複", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 創建新的掃描項目
        val scannedItem = ScannedItem.createWithCurrentTime(partNumber)
        
        // 添加到適配器
        scannedItemsAdapter.addItem(scannedItem)
        
        // 更新掃描筆數顯示
        updateScanCount()
        
        // 顯示成功提示
        Toast.makeText(this, "已添加料號：$partNumber", Toast.LENGTH_SHORT).show()
        
        // 滾動到列表頂部顯示新添加的項目
        binding.rvScannedItems.scrollToPosition(0)
    }

    /**
     * 顯示資料檢查對話框
     */
    private fun showDataCheckDialog() {
        val scannedCount = scannedItemsAdapter.itemCount
        val taskInfo = getString(R.string.task_id_format, currentTaskId)
        
        val message = """
            $taskInfo
            ${getString(R.string.progress_format, scannedCount, scannedCount)}
            
            已掃描項目清單：
            ${scannedItemsAdapter.getAllItems().joinToString("\n") { "• ${it.partNumber}" }}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.btn_data_check))
            .setMessage(message)
            .setPositiveButton(getString(R.string.btn_confirm), null)
            .show()
    }

    /**
     * 顯示結束作業確認對話框
     */
    private fun showEndOperationDialog() {
        val scannedCount = scannedItemsAdapter.itemCount
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.end_operation))
            .setMessage(getString(R.string.msg_confirm_end_operation))
            .setPositiveButton(getString(R.string.btn_confirm)) { _, _ ->
                // TODO: 階段四將在此處實現數據同步到後端
                Toast.makeText(this, "作業已結束", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    /**
     * 顯示刪除項目確認對話框
     */
    private fun showDeleteItemDialog(item: ScannedItem, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("刪除項目")
            .setMessage("確定要刪除料號「${item.partNumber}」嗎？")
            .setPositiveButton("刪除") { _, _ ->
                scannedItemsAdapter.removeItem(position)
                Toast.makeText(this, "已刪除料號：${item.partNumber}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }

    override fun onBackPressed() {
        // 如果有掃描項目，提示用戶確認是否離開
        if (scannedItemsAdapter.itemCount > 0) {
            AlertDialog.Builder(this)
                .setTitle("離開掃描")
                .setMessage("您有未完成的掃描作業，確定要離開嗎？")
                .setPositiveButton("確定") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("取消", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }
}