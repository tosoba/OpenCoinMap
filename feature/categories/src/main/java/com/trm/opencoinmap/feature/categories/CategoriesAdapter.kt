package com.trm.opencoinmap.feature.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.feature.categories.databinding.ItemCategoryBinding

class CategoriesAdapter :
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

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.bind(getItem(position))
  }

  inner class ViewHolder(
    private val binding: ItemCategoryBinding,
  ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: VenueCategoryCount) {
      binding.categoryLetterIcon.letter = item.category.first().uppercase()
      binding.categoryNameTextView.text = item.category
    }
  }
}
