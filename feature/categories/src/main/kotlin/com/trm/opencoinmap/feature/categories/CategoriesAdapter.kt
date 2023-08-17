package com.trm.opencoinmap.feature.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.feature.categories.databinding.ItemCategoryBinding

class CategoriesAdapter(
  private val isChecked: (Int) -> Boolean,
  private val onCheckedChange: (Int, Boolean) -> Unit
) :
  ListAdapter<VenueCategoryCount, CategoriesAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<VenueCategoryCount>() {
      override fun areItemsTheSame(
        oldItem: VenueCategoryCount,
        newItem: VenueCategoryCount
      ): Boolean = oldItem == newItem

      override fun areContentsTheSame(
        oldItem: VenueCategoryCount,
        newItem: VenueCategoryCount
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
    fun bind(item: VenueCategoryCount, index: Int) {
      with(binding.categoryChip) {
        text = item.category
        isChecked = isChecked(index)
      }
    }
  }
}
