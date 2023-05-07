package com.trm.opencoinmap.feature.venues

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.feature.venues.databinding.FragmentVenuesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VenuesFragment : Fragment(R.layout.fragment_venues) {
  private val binding by viewBinding(FragmentVenuesBinding::bind)
  private val venuesAdapter by lazy(LazyThreadSafetyMode.NONE) { VenuesAdapter(onItemClick = {}) }

  private val viewModel by viewModels<VenuesViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.venuesRecyclerView.init()
    viewModel.observeState()
  }

  private fun RecyclerView.init() {
    layoutManager =
      if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
      } else {
        GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
      }
    adapter = venuesAdapter
  }

  private fun VenuesViewModel.observeState() {
    isLoadingForNewBounds.observe(viewLifecycleOwner) {
      binding.loadingProgressIndicator.isVisible = it
    }
    isVenuesListVisible.observe(viewLifecycleOwner) { binding.venuesRecyclerView.isVisible = it }
    pagingData.observe(viewLifecycleOwner) {
      venuesAdapter.submitData(viewLifecycleOwner.lifecycle, it)
    }
  }
}
