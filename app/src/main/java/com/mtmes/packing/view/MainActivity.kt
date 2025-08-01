package com.mtmes.packing.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.mtmes.packing.view.adapter.TaskListAdapter
import com.mtmes.packing.view.data.MockData
import com.mtmes.packing.view.databinding.ActivityMainBinding
import com.mtmes.packing.view.model.Task
import com.mtmes.packing.view.utils.LanguageManager
import com.mtmes.packing.view.security.UpdateExecutor

/**
 * 主活動 - 管理備料任務清單
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskListAdapter: TaskListAdapter
    private lateinit var updateExecutor: UpdateExecutor
    
    private var originalTasks: List<Task> = emptyList()
    private var currentSearchText: String = ""
    private var currentSortOption: String = ""
    private var currentStatusFilter: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化更新執行器
        updateExecutor = UpdateExecutor(this)
        
        setupToolbar()
        setupLanguageSwitch()
        loadTasks()
        setupRecyclerView()
        setupSpinners()
        setupSearch()
        
        // 執行版本檢查（延遲執行避免影響啟動速度）
        if (BuildConfig.ENABLE_VERSION_CHECK) {
            binding.root.postDelayed({
                updateExecutor.executeUpdateCheck()
            }, 2000)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val language = newBase?.let { LanguageManager.getSavedLanguage(it) } ?: LanguageManager.LANGUAGE_CHINESE
        val context = newBase?.let { LanguageManager.setAppLanguage(it, language) }
        super.attachBaseContext(context)
    }

    /**
     * 設定工具欄
     */
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    /**
     * 設定語言切換按鈕
     */
    private fun setupLanguageSwitch() {
        binding.btnLanguageSwitch.setOnClickListener {
            val newLanguage = LanguageManager.switchLanguage(this)
            // 重新啟動 Activity 以套用新語言
            recreate()
        }
    }

    /**
     * 載入任務數據
     */
    private fun loadTasks() {
        originalTasks = MockData.getTaskList()
    }

    /**
     * 設定 RecyclerView
     */
    private fun setupRecyclerView() {
        taskListAdapter = TaskListAdapter { task ->
            // 點擊任務項目，跳轉到掃描頁面
            val intent = Intent(this, ScanActivity::class.java).apply {
                putExtra("taskId", task.taskId)
                putExtra("taskDescription", task.description)
            }
            startActivity(intent)
        }
        
        binding.rvTasks.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = taskListAdapter
        }
        
        // 初始載入所有任務
        applyFiltersAndSort()
    }

    /**
     * 設定下拉選單
     */
    private fun setupSpinners() {
        // 設定排序選項 - 使用字串資源
        val sortOptions = resources.getStringArray(R.array.sort_options)
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSort.adapter = sortAdapter
        
        // 設定狀態篩選選項 - 使用字串資源
        val statusOptions = resources.getStringArray(R.array.status_filter_options)
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, statusOptions)
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerStatus.adapter = statusAdapter
        
        // 設定選擇監聽器
        binding.spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentSortOption = sortOptions[position]
                applyFiltersAndSort()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        binding.spinnerStatus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                currentStatusFilter = statusOptions[position]
                applyFiltersAndSort()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    /**
     * 設定搜尋功能
     */
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearchText = s?.toString() ?: ""
                applyFiltersAndSort()
            }
        })
    }

    /**
     * 應用篩選和排序
     */
    private fun applyFiltersAndSort() {
        var filteredTasks = originalTasks.toList()
        
        // 搜尋篩選
        if (currentSearchText.isNotEmpty()) {
            filteredTasks = filteredTasks.filter { task ->
                task.taskId.contains(currentSearchText, ignoreCase = true) ||
                task.description.contains(currentSearchText, ignoreCase = true)
            }
        }
        
        // 狀態篩選 - 使用資源字串比較
        val allStatusText = getString(R.string.all_status)
        if (currentStatusFilter.isNotEmpty() && currentStatusFilter != allStatusText) {
            val statusPending = getString(R.string.status_pending)
            val statusInProgress = getString(R.string.status_in_progress) 
            val statusCompleted = getString(R.string.status_completed)
            
            val targetStatus = when (currentStatusFilter) {
                statusPending -> Task.STATUS_PENDING
                statusInProgress -> Task.STATUS_IN_PROGRESS
                statusCompleted -> Task.STATUS_COMPLETED
                else -> currentStatusFilter
            }
            
            filteredTasks = filteredTasks.filter { task ->
                task.status == targetStatus
            }
        }
        
        // 排序 - 使用資源字串比較
        val sortByDate = getString(R.string.sort_by_date)
        val sortByTaskId = getString(R.string.sort_by_task_id)
        val sortByStatus = getString(R.string.sort_by_status)
        val sortByProgress = getString(R.string.sort_by_progress)
        
        filteredTasks = when (currentSortOption) {
            sortByDate -> filteredTasks.sortedBy { it.prepareDate }
            "$sortByDate (新到舊)", "Preparation Date (New to Old)" -> filteredTasks.sortedByDescending { it.prepareDate }
            sortByTaskId -> filteredTasks.sortedBy { it.taskId }
            "$sortByTaskId (Z-A)", "Task Number (Z-A)" -> filteredTasks.sortedByDescending { it.taskId }
            sortByStatus -> filteredTasks.sortedBy { 
                when (it.status) {
                    Task.STATUS_PENDING -> 0
                    Task.STATUS_IN_PROGRESS -> 1
                    Task.STATUS_COMPLETED -> 2
                    else -> 3
                }
            }
            sortByProgress -> filteredTasks.sortedBy { it.getProgressPercentage() }
            "掃描進度 (高到低)", "Scan Progress (High to Low)" -> filteredTasks.sortedByDescending { it.getProgressPercentage() }
            else -> filteredTasks.sortedBy { it.prepareDate }
        }
        
        // 更新適配器
        taskListAdapter.updateTasks(filteredTasks)
    }

    override fun onResume() {
        super.onResume()
        // 從掃描頁面返回時重新載入數據
        loadTasks()
        applyFiltersAndSort()
    }
}