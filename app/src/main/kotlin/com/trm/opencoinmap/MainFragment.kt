package com.trm.opencoinmap

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
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

  private val bottomSheetFragmentNavController: NavController
    get() = Navigation.findNavController(binding.bottomSheetContainer)

  override var searchViewsHeightPx: Int? = null
  private var searchMenuItem: MenuItem? = null
  private val searchView: SearchView?
    get() = searchMenuItem?.actionView?.safeAs<SearchView>()

  private val viewModel: MainViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    (requireActivity() as AppCompatActivity).setSupportActionBar(binding.searchBar)

    binding.showPlacesSheetFab.setOnClickListener {
      sheetController.setState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    initBottomSheet(savedInstanceState)
    initSearchViews()

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
        searchView?.isIconified == false -> {
          searchView?.isIconified = true
        }
        bottomSheetFragmentNavController.popBackStack() -> {
          return@addCallback
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

  private fun initSearchViews() {
    with(binding.searchLayout) {
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

    requireActivity()
      .addMenuProvider(
        object : MenuProvider {
          override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_main, menu)
            searchMenuItem = menu.findItem(R.id.action_search)
            searchMenuItem?.actionView?.safeAs<SearchView>()?.apply {
              maxWidth = Int.MAX_VALUE

              setOnQueryTextListener(
                object : SearchView.OnQueryTextListener {
                  override fun onQueryTextSubmit(query: String?): Boolean = true

                  override fun onQueryTextChange(newText: String?): Boolean {
                    binding.searchBar.text = newText
                    viewModel.searchQuery = newText.orEmpty()
                    return true
                  }
                }
              )

              setQuery(viewModel.searchQuery, true)
              if (viewModel.searchQuery.isNotBlank()) {
                searchView?.isIconified = false
                searchView?.clearFocus()
              }
            }
          }

          override fun onMenuItemSelected(menuItem: MenuItem): Boolean = true
        }
      )

    binding.searchBar.navigationIcon = null
    binding.searchBar.setOnClickListener { searchView?.isIconified = false }
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
      bottomSheetFragmentNavController.navigate(
        R.id.venues_fragment_to_venue_details_fragment,
        bundleOf("venueId" to it)
      )
    }
  }
}
