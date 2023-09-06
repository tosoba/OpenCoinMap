package com.trm.opencoinmap.feature.venues

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.GridLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.addOnScrollIdleListener
import com.trm.opencoinmap.core.common.ext.findParentFragmentOfType
import com.trm.opencoinmap.core.common.ext.getSystemWindowTopInsetPx
import com.trm.opencoinmap.core.common.ext.hideAnimated
import com.trm.opencoinmap.core.common.ext.requireAs
import com.trm.opencoinmap.core.common.ext.showAnimated
import com.trm.opencoinmap.core.common.ext.toDp
import com.trm.opencoinmap.core.common.view.BottomSheetController
import com.trm.opencoinmap.feature.venues.databinding.FragmentVenuesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.max
import kotlin.math.roundToInt

@AndroidEntryPoint
class VenuesFragment : Fragment(R.layout.fragment_venues) {
  private val binding by viewBinding(FragmentVenuesBinding::bind)
  private val venuesAdapter by
    lazy(LazyThreadSafetyMode.NONE) { VenuesAdapter(onItemClick = viewModel::onVenueClicked) }

  private val viewModel by viewModels<VenuesViewModel>()

  private var systemWindowTopInsetPx: Int? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    binding.initViews()
    viewModel.observeState()
    viewModel.observeEvents()
  }

  override fun onDestroyView() {
    systemWindowTopInsetPx = null
    super.onDestroyView()
  }

  private fun FragmentVenuesBinding.initViews() {
    requireView().setOnApplyWindowInsetsListener { _, insets ->
      if (systemWindowTopInsetPx != null) return@setOnApplyWindowInsetsListener insets

      systemWindowTopInsetPx = insets.getSystemWindowTopInsetPx()

      findParentFragmentOfType<BottomSheetController>()
        ?.run { bottomSheetContainerTopMarginPx to bottomSheetSlideOffset }
        ?.let { (searchViewsHeightPx, bottomSheetSlideOffset) ->
          binding.updateErrorGroupsAlpha(bottomSheetSlideOffset)

          if (searchViewsHeightPx != null) {
            updateContainerLayoutParams(searchViewsHeightPx, bottomSheetSlideOffset)
          }
        }

      insets
    }

    with(venuesDragHandleView) { setPadding(paddingLeft, 0, paddingRight, 0) }

    root.viewTreeObserver.addOnGlobalLayoutListener(
      object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
          root.viewTreeObserver.removeOnGlobalLayoutListener(this)
          val columnsCount = max((root.width.toFloat().toDp(requireContext()) / 220).toInt(), 1)
          loadingProgressItemsLayout.root.init(columnsCount)
          venuesRecyclerView.init(columnsCount)
        }
      }
    )

    scrollUpButton.setOnClickListener { venuesRecyclerView.smoothScrollToPosition(0) }
  }

  private fun GridLayout.init(columnsCount: Int) {
    columnCount = columnsCount
    repeat(columnsCount * 3) {
      addView(loadingProgressItem(row = it / columnsCount, column = it % columnsCount))
    }
  }

  private fun GridLayout.loadingProgressItem(row: Int, column: Int): View =
    layoutInflater.inflate(R.layout.item_venue, this, false).apply {
      layoutParams =
        GridLayout.LayoutParams().apply {
          width = 0
          height = GridLayout.LayoutParams.WRAP_CONTENT
          setGravity(Gravity.FILL)
          rowSpec = GridLayout.spec(row)
          columnSpec = GridLayout.spec(column, 1, 1f)
          setMargins(
            resources.getDimensionPixelSize(R.dimen.item_venue_margin_horizontal),
            0,
            resources.getDimensionPixelSize(R.dimen.item_venue_margin_horizontal),
            resources.getDimensionPixelSize(R.dimen.item_venue_margin_bottom)
          )
        }
    }

  private fun RecyclerView.init(columnsCount: Int) {
    layoutManager = GridLayoutManager(requireContext(), columnsCount, RecyclerView.VERTICAL, false)
    adapter = venuesAdapter
    addOnScrollIdleListener { binding.toggleScrollUpButtonVisibility() }
    venuesAdapter.addOnPagesUpdatedListener { binding.toggleScrollUpButtonVisibility() }
    venuesAdapter.addLoadStateListener { viewModel.onLoadStatesChange(it, venuesAdapter.itemCount) }
  }

  private fun FragmentVenuesBinding.toggleScrollUpButtonVisibility() {
    scrollUpButton.apply {
      if (venuesRecyclerView.canScrollVertically(-1)) showAnimated() else hideAnimated()
    }
  }

  private fun VenuesViewModel.observeState() {
    isLoadingVisible.observe(viewLifecycleOwner, binding.loadingProgressLayout::isVisible::set)
    isVenuesListVisible.observe(viewLifecycleOwner, binding.venuesRecyclerView::isVisible::set)
    isErrorVisible.observe(viewLifecycleOwner) {
      binding.venuesCollapsedErrorGroup.isVisible = it
      binding.venuesExpandedErrorGroup.isVisible = it
    }

    pagingData.observe(viewLifecycleOwner) {
      venuesAdapter.submitData(viewLifecycleOwner.lifecycle, it)
    }
  }

  private fun VenuesViewModel.observeEvents() {
    sheetSlideOffset.observe(viewLifecycleOwner) { offset ->
      binding.updateErrorGroupsAlpha(offset)

      val searchBarHeightPx =
        findParentFragmentOfType<BottomSheetController>()?.bottomSheetContainerTopMarginPx
          ?: return@observe
      binding.updateContainerLayoutParams(searchBarHeightPx, offset)
    }
  }

  private fun FragmentVenuesBinding.updateContainerLayoutParams(
    searchBarHeightPx: Int,
    offset: Float
  ) {
    venuesContainer.layoutParams =
      venuesContainer.layoutParams.requireAs<ViewGroup.MarginLayoutParams>().apply {
        topMargin = (((systemWindowTopInsetPx ?: 0) + searchBarHeightPx) * offset).roundToInt()
      }
  }

  private fun FragmentVenuesBinding.updateErrorGroupsAlpha(bottomSheetSlideOffset: Float) {
    venuesCollapsedErrorGroup.referencedIds.forEach {
      binding.root.findViewById<View>(it).alpha = 1f - bottomSheetSlideOffset
    }
    venuesExpandedErrorGroup.referencedIds.forEach {
      binding.root.findViewById<View>(it).alpha = bottomSheetSlideOffset
    }
  }
}
