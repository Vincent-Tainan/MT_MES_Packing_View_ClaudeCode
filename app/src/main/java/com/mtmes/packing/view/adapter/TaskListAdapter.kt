package com.mtmes.packing.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mtmes.packing.view.R
import com.mtmes.packing.view.databinding.ItemTaskBinding
import com.mtmes.packing.view.model.Task

/**
 * 任務列表適配器
 */
class TaskListAdapter(
    private var tasks: List<Task> = emptyList(),
    private val onItemClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskListAdapter.TaskViewHolder>() {

    /**
     * 更新任務列表數據
     */
    fun updateTasks(newTasks: List<Task>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    /**
     * 任務項目視圖持有者
     */
    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(tasks[position])
                }
            }
        }

        fun bind(task: Task) {
            with(binding) {
                // 設定基本信息
                tvTaskId.text = task.taskId
                tvPrepareDate.text = task.prepareDate
                tvDescription.text = task.description
                
                // 設定進度信息
                tvProgress.text = root.context.getString(
                    R.string.progress_format,
                    task.itemsScanned,
                    task.totalItems
                )
                
                // 計算並顯示進度百分比
                val progressPercentage = task.getProgressPercentage()
                tvProgressPercentage.text = "${progressPercentage.toInt()}%"
                progressBar.progress = progressPercentage.toInt()
                
                // 設定狀態標籤及其背景顏色
                tvStatus.text = task.status
                val statusBackground = when (task.status) {
                    Task.STATUS_PENDING -> R.drawable.rounded_status_bg_pending
                    Task.STATUS_IN_PROGRESS -> R.drawable.rounded_status_bg_in_progress
                    Task.STATUS_COMPLETED -> R.drawable.rounded_status_bg_completed
                    else -> R.drawable.rounded_status_bg_default
                }
                tvStatus.background = ContextCompat.getDrawable(root.context, statusBackground)
                
                // 根據狀態設定文字顏色
                val textColor = when (task.status) {
                    Task.STATUS_PENDING -> ContextCompat.getColor(root.context, R.color.white)
                    Task.STATUS_IN_PROGRESS -> ContextCompat.getColor(root.context, R.color.white)
                    Task.STATUS_COMPLETED -> ContextCompat.getColor(root.context, R.color.white)
                    else -> ContextCompat.getColor(root.context, R.color.white)
                }
                tvStatus.setTextColor(textColor)
            }
        }
    }
}