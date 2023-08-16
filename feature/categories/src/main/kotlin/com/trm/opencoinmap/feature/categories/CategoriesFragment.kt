package com.trm.opencoinmap.feature.categories

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.feature.categories.databinding.FragmentCategoriesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoriesFragment : Fragment(R.layout.fragment_categories) {
  private val binding by viewBinding(FragmentCategoriesBinding::bind)
  private val adapter by
    lazy(LazyThreadSafetyMode.NONE) {
      CategoriesAdapter(
        isChecked = viewModel::isCategoryAtIndexChecked,
        onCheckedChange = viewModel::onCategoryAtIndexCheckedChange
      )
    }

  private val viewModel by viewModels<CategoriesViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.categoriesRecyclerView.adapter = adapter
    binding.categoriesRecyclerView.viewTreeObserver.addOnGlobalLayoutListener {
      viewModel.onCategoriesListLayout()
    }
    viewModel.categories.observe(viewLifecycleOwner) { if (it.isNotEmpty()) adapter.submitList(it) }
  }
}
