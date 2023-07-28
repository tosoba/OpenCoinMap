package com.trm.opencoinmap.feature.venues

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.hideAnimated
import com.trm.opencoinmap.core.common.ext.showAnimated
import com.trm.opencoinmap.core.common.ext.takeIfInstance
import com.trm.opencoinmap.core.common.ext.toPx
import com.trm.opencoinmap.feature.venues.databinding.FragmentVenuesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class VenuesFragment : Fragment(R.layout.fragment_venues) {
  private val binding by viewBinding(FragmentVenuesBinding::bind)
  private val venuesAdapter by lazy(LazyThreadSafetyMode.NONE) { VenuesAdapter(onItemClick = {}) }
  private val expandedSheetContainerExtraTopMarginPx by
    lazy(LazyThreadSafetyMode.NONE) { 10f.toPx(requireContext()) }

  private val viewModel by viewModels<VenuesViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.initViews()
    viewModel.observeState()
    viewModel.observeEvents()
  }

  private fun FragmentVenuesBinding.initViews() {
    venuesRecyclerView.init()
    scrollUpButton.setOnClickListener { venuesRecyclerView.smoothScrollToPosition(0) }
  }

  private fun RecyclerView.init() {
    layoutManager =
      when (
        val columnCount =
          TypedValue().run {
            resources.getValue(R.dimen.venues_column_count, this, true)
            data
          }
      ) {
        1 -> LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        else -> GridLayoutManager(requireContext(), columnCount, RecyclerView.VERTICAL, false)
      }
    adapter = venuesAdapter

    addOnScrollListener(
      object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
          if (newState != RecyclerView.SCROLL_STATE_IDLE) return
          binding.scrollUpButton.apply {
            if (recyclerView.canScrollVertically(-1)) showAnimated() else hideAnimated()
          }
        }
      }
    )
  }

  private fun VenuesViewModel.observeState() {
    isLoadingForNewBounds.observe(viewLifecycleOwner) {
      binding.loadingProgressIndicator.isVisible = it
    }

    isVenuesListVisible.observe(viewLifecycleOwner) { binding.venuesRecyclerView.isVisible = it }

    pagingData.observe(viewLifecycleOwner) {
      venuesAdapter.submitData(viewLifecycleOwner.lifecycle, it)
      binding.scrollUpButton.isVisible = binding.venuesRecyclerView.canScrollVertically(-1)
    }
  }

  private fun VenuesViewModel.observeEvents() {
    sheetSlideOffset.observe(viewLifecycleOwner) { offset ->
      val searchBarHeightPx =
        requireActivity().takeIfInstance<VenuesSearchBarController>()?.searchBarHeightPx
          ?: return@observe
      val params = binding.venuesContainer.layoutParams as ViewGroup.MarginLayoutParams
      params.topMargin =
        ((searchBarHeightPx + expandedSheetContainerExtraTopMarginPx) * offset).roundToInt()
      binding.venuesContainer.layoutParams = params
    }
  }
}
