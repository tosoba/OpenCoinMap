package com.trm.opencoinmap.feature.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.trm.opencoinmap.feature.categories.databinding.ItemCategoryBinding

class CategoriesAdapter :
  ListAdapter<String, CategoriesAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<String>() {
      override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem

      override fun areContentsTheSame(oldItem: String, newItem: String): Boolean =
        oldItem == newItem
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
    fun bind(item: String) {
      binding.categoryLetterIcon.letter = item.first().uppercase()
      binding.categoryNameTextView.text = item
    }
  }
}
