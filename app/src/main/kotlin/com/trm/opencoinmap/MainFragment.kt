package com.trm.opencoinmap

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
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

  private val viewModel: MainViewModel by viewModels()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    binding.showPlacesSheetFab.setOnClickListener {
      sheetController.setState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    initBottomSheet(savedInstanceState)
    binding.initSearchViews()

    initNavigation()

    lifecycle.addObserver(snackbarMessageObserver)
    viewModel.observe()
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

    searchBar.setContent { SearchBar() }
  }

  @Composable
  private fun SearchBar() {
    val query = viewModel.searchBarQuery.observeAsState(initial = "")
    val enabled = viewModel.searchBarEnabled.observeAsState(initial = true)
    val leadingIconMode =
      viewModel.searchBarLeadingIconMode.observeAsState(initial = SearchBarLeadingIconMode.SEARCH)

    val focusRequester = remember(::FocusRequester)
    val focusManager = LocalFocusManager.current
    val focused = viewModel.searchFocused.observeAsState(initial = false)
    LaunchedEffect(focused.value) {
      if (focused.value) focusRequester.requestFocus() else focusManager.clearFocus()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    SearchBar(
      query = query.value,
      onQueryChange = viewModel.searchQuery::setValue,
      onSearch = viewModel.searchQuery::setValue,
      active = false,
      onActiveChange = {},
      leadingIcon = {
        when (leadingIconMode.value) {
          SearchBarLeadingIconMode.SEARCH -> {
            Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = stringResource(id = R.string.search)
            )
          }
          SearchBarLeadingIconMode.BACK -> {
            Icon(
              imageVector = Icons.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.back),
              modifier =
                Modifier.clickable { requireActivity().onBackPressedDispatcher.onBackPressed() }
            )
          }
        }
      },
      colors =
        SearchBarDefaults.colors(
          inputFieldColors =
            SearchBarDefaults.inputFieldColors(
              disabledTextColor = MaterialTheme.colorScheme.onSurface,
              disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface
            )
        ),
      windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
      enabled = enabled.value,
      modifier =
        Modifier.padding(horizontal = 10.dp).focusRequester(focusRequester).onFocusChanged {
          viewModel.searchFocused.value = it.isFocused
        }
    ) {}
  }

  private fun initNavigation() {
    requireActivity().onBackPressedDispatcher.addCallback {
      when {
        viewModel.searchFocused.value == true -> {
          viewModel.searchFocused.value = false
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

    bottomSheetFragmentNavController.addOnDestinationChangedListener { _, destination, arguments ->
      viewModel.onBottomSheetFragmentChanged(
        destinationId = destination.id,
        venueName = arguments?.getString(VENUE_NAME_KEY, getString(R.string.unknown_place))
      )
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
          bundleOf(VENUE_ID_KEY to it.id, VENUE_NAME_KEY to it.name)
        )
      }
    }
  }

  companion object {
    private const val VENUE_ID_KEY = "venueId"
    private const val VENUE_NAME_KEY = "venueName"
  }
}
