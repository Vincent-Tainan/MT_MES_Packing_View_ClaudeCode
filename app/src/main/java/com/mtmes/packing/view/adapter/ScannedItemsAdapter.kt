package com.mtmes.packing.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mtmes.packing.view.R
import com.mtmes.packing.view.databinding.ItemScannedItemBinding
import com.mtmes.packing.view.model.ScannedItem

/**
 * 已掃描項目列表適配器
 */
class ScannedItemsAdapter(
    private val scannedItems: MutableList<ScannedItem> = mutableListOf()
) : RecyclerView.Adapter<ScannedItemsAdapter.ScannedItemViewHolder>() {

    /**
     * 添加新項目
     */
    fun addItem(item: ScannedItem) {
        scannedItems.add(0, item) // 添加到列表頂部
        notifyItemInserted(0)
    }

    /**
     * 移除項目
     */
    fun removeItem(position: Int) {
        if (position >= 0 && position < scannedItems.size) {
            scannedItems.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * 檢查是否包含指定料號
     */
    fun containsPartNumber(partNumber: String): Boolean {
        return scannedItems.any { it.partNumber.equals(partNumber, ignoreCase = true) }
    }

    /**
     * 獲取所有已掃描項目
     */
    fun getAllItems(): List<ScannedItem> = scannedItems.toList()

    /**
     * 清空所有項目
     */
    fun clear() {
        val size = scannedItems.size
        scannedItems.clear()
        notifyItemRangeRemoved(0, size)
    }

    /**
     * 設定初始數據
     */
    fun setItems(items: List<ScannedItem>) {
        scannedItems.clear()
        scannedItems.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedItemViewHolder {
        val binding = ItemScannedItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ScannedItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScannedItemViewHolder, position: Int) {
        holder.bind(scannedItems[position])
    }

    override fun getItemCount(): Int = scannedItems.size

    /**
     * 已掃描項目視圖持有者
     */
    inner class ScannedItemViewHolder(
        private val binding: ItemScannedItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScannedItem) {
            with(binding) {
                tvPartNumber.text = item.partNumber
                tvScannedTime.text = root.context.getString(
                    R.string.scanned_time_format,
                    item.scannedTime
                )
            }
        }
    }
}