package com.trm.opencoinmap

import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.appcompat.widget.SearchView
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trm.opencoinmap.core.common.ext.requireAs
import com.trm.opencoinmap.core.common.ext.safeAs
import com.trm.opencoinmap.core.common.ext.toDp
import com.trm.opencoinmap.core.common.ext.toPx
import com.trm.opencoinmap.core.common.view.SheetController
import com.trm.opencoinmap.core.common.view.SnackbarMessageObserver
import com.trm.opencoinmap.databinding.FragmentMainBinding
import com.trm.opencoinmap.feature.venues.VenuesSearchController
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainFragment : Fragment(R.layout.fragment_main), VenuesSearchController {
  private val binding by viewBinding(FragmentMainBinding::bind)

  private val snackbarMessageObserver: SnackbarMessageObserver by
    lazy(LazyThreadSafetyMode.NONE) {
      SnackbarMessageObserver(
        view = binding.coordinatorLayout,
        onShown = { binding.fabsLayout.isVisible = false },
        onDismissed = { binding.fabsLayout.isVisible = true }
      )
    }

  private val collapsedSheetAlpha: Float by
    lazy(LazyThreadSafetyMode.NONE) {
      TypedValue().run {
        resources.getValue(R.dimen.collapsed_sheet_alpha, this, true)
        float
      }
    }

  private val fabMarginPx: Float by
    lazy(LazyThreadSafetyMode.NONE) {
      resources.getDimension(com.trm.opencoinmap.core.common.R.dimen.fab_margin)
    }

  private val sheetPeekHeightPx: Float by
    lazy(LazyThreadSafetyMode.NONE) { resources.getDimension(R.dimen.sheet_peek_height) }

  private val usingHalfScreenSheetWidth: Boolean by
    lazy(LazyThreadSafetyMode.NONE) {
      resources.configuration.screenWidthDp >=
        resources.getDimension(R.dimen.half_bottom_sheet_min_screen_width).toDp(requireContext())
    }

  private val sheetController: SheetController by
    lazy(LazyThreadSafetyMode.NONE) {
      SheetController(
        bottomSheetView = binding.bottomSheetContainer,
        collapsedAlpha = collapsedSheetAlpha,
        onStateChanged = { state ->
          binding.showPlacesSheetFab.isVisible = state == BottomSheetBehavior.STATE_HIDDEN

          if (usingHalfScreenSheetWidth) return@SheetController

          binding.fabsLayout.layoutParams =
            binding.fabsLayout.layoutParams.requireAs<ViewGroup.MarginLayoutParams>().apply {
              bottomMargin =
                if (state == BottomSheetBehavior.STATE_HIDDEN) {
                    fabMarginPx
                  } else {
                    fabMarginPx + sheetPeekHeightPx
                  }
                  .roundToInt()
            }
        },
        onSlide = { slideOffset ->
          binding.searchBar.alpha = collapsedSheetAlpha + slideOffset * (1f - collapsedSheetAlpha)
          viewModel.onSheetSlide(slideOffset)
        }
      )
    }

  private val bottomSheetFragmentNavController: NavController by
    lazy(LazyThreadSafetyMode.NONE) {
      childFragmentManager
        .findFragmentById(R.id.bottom_sheet_container)!!
        .requireAs<NavHostFragment>()
        .navController
    }

  override var searchViewsHeightPx: Int? = null
  private var searchMenuItem: MenuItem? = null
  private val searchView: SearchView?
    get() = searchMenuItem?.actionView?.safeAs<SearchView>()

  private val viewModel: MainViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.showPlacesSheetFab.setOnClickListener {
      sheetController.setState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    initBottomSheet(savedInstanceState)
    binding.initSearchViews()

    lifecycle.addObserver(snackbarMessageObserver)
    viewModel.observe()
  }

  override fun onDestroyView() {
    searchMenuItem = null
    super.onDestroyView()
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    sheetController.saveState(outState)
  }

  private fun initBottomSheet(savedInstanceState: Bundle?) {
    if (usingHalfScreenSheetWidth) {
      binding.bottomSheetContainer.layoutParams =
        binding.bottomSheetContainer.layoutParams
          .requireAs<CoordinatorLayout.LayoutParams>()
          .apply {
            width =
              (resources.configuration.screenWidthDp / 2)
                .toFloat()
                .toPx(requireContext())
                .roundToInt()
          }
    }

    sheetController.initFrom(savedInstanceState)

    requireActivity().onBackPressedDispatcher.addCallback {
      when {
        viewModel.searchFocused.value == true -> {
          viewModel.setSearchFocused(false)
        }
        bottomSheetFragmentNavController.popBackStack() -> {
          return@addCallback
        }
        searchView?.isIconified == false -> {
          searchView?.isIconified = true
        }
        sheetController.state == BottomSheetBehavior.STATE_EXPANDED ||
          sheetController.state == BottomSheetBehavior.STATE_HALF_EXPANDED -> {
          sheetController.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        else -> {
          requireActivity().finish()
        }
      }
    }
  }

  private fun FragmentMainBinding.initSearchViews() {
    with(searchLayout) {
      alpha =
        if (sheetController.state == BottomSheetBehavior.STATE_EXPANDED) 1f else collapsedSheetAlpha

      viewTreeObserver.addOnGlobalLayoutListener(
        object : ViewTreeObserver.OnGlobalLayoutListener {
          override fun onGlobalLayout() {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            searchViewsHeightPx = measuredHeight + marginTop + marginBottom
            viewModel.onSearchViewsSizeMeasure(sheetState = sheetController.state)
          }
        }
      )
    }

    searchBar.setContent {
      val query = viewModel.searchQuery.observeAsState(initial = "")
      val focusRequester = remember(::FocusRequester)
      val focusManager = LocalFocusManager.current

      @OptIn(ExperimentalMaterial3Api::class)
      SearchBar(
        query = query.value,
        onQueryChange = viewModel::setSearchQuery,
        onSearch = viewModel::setSearchQuery,
        active = false,
        onActiveChange = {},
        modifier =
          Modifier.padding(horizontal = 10.dp).focusRequester(focusRequester).onFocusChanged {
            viewModel.setSearchFocused(it.isFocused)
          }
      ) {}

      val focused = viewModel.searchFocused.observeAsState(initial = false)
      LaunchedEffect(focused.value) {
        if (focused.value) {
          focusRequester.requestFocus()
        } else {
          focusManager.clearFocus()
        }
      }
    }
  }

  private fun MainViewModel.observe() {
    snackbarMessage.observe(viewLifecycleOwner, snackbarMessageObserver)

    categoriesUpdatedEvent.observe(viewLifecycleOwner) {
      with(binding.searchLayout) { measuredHeight + marginTop + marginBottom }
        .also {
          if (it == searchViewsHeightPx) return@also
          searchViewsHeightPx = it
          viewModel.onSearchViewsSizeMeasure(sheetState = sheetController.state)
        }
    }

    venueClicked.observe(viewLifecycleOwner) {
      if (
        bottomSheetFragmentNavController.currentDestination?.id !=
          R.id.venues_fragment_to_venue_details_fragment
      ) {
        bottomSheetFragmentNavController.navigate(
          R.id.venues_fragment_to_venue_details_fragment,
          bundleOf("venueId" to it.id, "venueName" to it.name)
        )
      }
    }
  }
}
