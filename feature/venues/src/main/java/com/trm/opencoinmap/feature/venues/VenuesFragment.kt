package com.trm.opencoinmap.feature.venues

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.feature.venues.databinding.FragmentVenuesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VenuesFragment : Fragment(R.layout.fragment_venues) {
  private val binding by viewBinding(FragmentVenuesBinding::bind)
  private val adapter by lazy(LazyThreadSafetyMode.NONE) { VenuesAdapter(onItemClick = {}) }

  private val viewModel by viewModels<VenuesViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.venuesRecyclerView.adapter = adapter
    viewModel.isLoadingForNewBounds.observe(viewLifecycleOwner) {
      binding.loadingProgressIndicator.isVisible = it
    }
    viewModel.isVenuesListVisible.observe(viewLifecycleOwner) {
      binding.venuesRecyclerView.isVisible = it
    }
    viewModel.pagingData.observe(viewLifecycleOwner) {
      adapter.submitData(viewLifecycleOwner.lifecycle, it)
    }
  }
}
