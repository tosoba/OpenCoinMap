package com.trm.opencoinmap.feature.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trm.opencoinmap.feature.categories.databinding.ItemCategoryBinding

class CategoriesAdapter(
  private val isChecked: (Int) -> Boolean,
  private val onCheckedChange: (Int, Boolean) -> Unit
) :
  ListAdapter<CheckedVenueCategoryCount, CategoriesAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<CheckedVenueCategoryCount>() {
      override fun areItemsTheSame(
        oldItem: CheckedVenueCategoryCount,
        newItem: CheckedVenueCategoryCount
      ): Boolean = oldItem == newItem

      override fun areContentsTheSame(
        oldItem: CheckedVenueCategoryCount,
        newItem: CheckedVenueCategoryCount
      ): Boolean = oldItem == newItem
    }
  ) {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
    ViewHolder(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
      .apply {
        binding.categoryChip.setOnCheckedChangeListener { _, isChecked ->
          onCheckedChange(bindingAdapterPosition, isChecked)
        }
      }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position), position)
  }

  inner class ViewHolder(
    val binding: ItemCategoryBinding,
  ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: CheckedVenueCategoryCount, index: Int) {
      with(binding.categoryChip) {
        text = item.categoryCount.category
        isChecked = isChecked(index)
      }
    }
  }
}
