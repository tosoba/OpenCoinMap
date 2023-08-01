package com.trm.opencoinmap.feature.venues

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.addOnScrollIdleListener
import com.trm.opencoinmap.core.common.ext.hideAnimated
import com.trm.opencoinmap.core.common.ext.maxHorizontalSpanCount
import com.trm.opencoinmap.core.common.ext.requireAs
import com.trm.opencoinmap.core.common.ext.safeAs
import com.trm.opencoinmap.core.common.ext.showAnimated
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
      GridLayoutManager(context, context.maxHorizontalSpanCount(220), RecyclerView.VERTICAL, false)
    adapter = venuesAdapter

    addOnScrollIdleListener { binding.toggleScrollUpButtonVisibility() }
    venuesAdapter.addOnPagesUpdatedListener { binding.toggleScrollUpButtonVisibility() }
  }

  private fun FragmentVenuesBinding.toggleScrollUpButtonVisibility() {
    scrollUpButton.apply {
      if (venuesRecyclerView.canScrollVertically(-1)) showAnimated() else hideAnimated()
    }
  }

  private fun VenuesViewModel.observeState() {
    isLoadingProgressLayoutVisible.observe(
      viewLifecycleOwner,
      binding.loadingProgressLayout::isVisible::set
    )

    isVenuesListVisible.observe(viewLifecycleOwner, binding.venuesRecyclerView::isVisible::set)

    pagingData.observe(viewLifecycleOwner) {
      venuesAdapter.submitData(viewLifecycleOwner.lifecycle, it)
    }
  }

  private fun VenuesViewModel.observeEvents() {
    sheetSlideOffset.observe(viewLifecycleOwner) { offset ->
      val searchBarHeightPx =
        requireActivity().safeAs<VenuesSearchController>()?.searchViewsHeightPx ?: return@observe
      binding.venuesContainer.layoutParams =
        binding.venuesContainer.layoutParams.requireAs<ViewGroup.MarginLayoutParams>().apply {
          topMargin =
            ((searchBarHeightPx + expandedSheetContainerExtraTopMarginPx) * offset).roundToInt()
        }
    }
  }
}
