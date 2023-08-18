package com.trm.opencoinmap.feature.categories

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.CompoundButton
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.trm.opencoinmap.core.common.ext.requireAs
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.feature.categories.databinding.FragmentCategoriesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.fragment_categories) {
  private val binding by viewBinding(FragmentCategoriesBinding::bind)

  private val viewModel by viewModels<CategoriesViewModel>()

  private val allCategoryCheckedChangeListener =
    CompoundButton.OnCheckedChangeListener { view, isChecked ->
      viewModel.onCategoryCheckedChange(view.tag.toString(), isChecked)
      if (isChecked) {
        binding.categoriesChipGroup.children.filterIsInstance<Chip>().forEach {
          if (it.tag != CategoriesViewModel.ALL_CATEGORY) it.isChecked = false
        }
      }
      view.isChecked = true
    }

  private val regularCategoryCheckedChangeListener =
    CompoundButton.OnCheckedChangeListener { view, isChecked ->
      viewModel.onCategoryCheckedChange(view.tag.toString(), isChecked)

      binding.categoriesChipGroup.children
        .filterIsInstance<Chip>()
        .find { it.tag == CategoriesViewModel.ALL_CATEGORY }
        ?.also {
          it.setOnCheckedChangeListener(null)
          it.isChecked = false
          it.setOnCheckedChangeListener(allCategoryCheckedChangeListener)
        }
    }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.root.viewTreeObserver.addOnGlobalLayoutListener { viewModel.onCategoriesListLayout() }

    viewModel.categories.observe(viewLifecycleOwner) { categories ->
      categories.forEach { binding.categoriesChipGroup.addView(categoryChip(it)) }
    }
  }

  private fun categoryChip(venueCategoryCount: VenueCategoryCount): Chip {
    val (category, count) = venueCategoryCount
    return layoutInflater
      .inflate(R.layout.item_category, binding.categoriesChipGroup, false)
      .requireAs<Chip>()
      .apply {
        tag = category
        @SuppressLint("SetTextI18n")
        text = "$category - $count"
        isChecked = viewModel.isCategoryChecked(category)
        setOnCheckedChangeListener(
          if (category == CategoriesViewModel.ALL_CATEGORY) allCategoryCheckedChangeListener
          else regularCategoryCheckedChangeListener
        )
      }
  }
}
