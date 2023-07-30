package com.trm.opencoinmap

import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.trm.opencoinmap.core.common.R as coreR
import com.trm.opencoinmap.core.common.view.SheetController
import com.trm.opencoinmap.core.common.view.SnackbarMessageObserver
import com.trm.opencoinmap.databinding.ActivityMainBinding
import com.trm.opencoinmap.feature.venues.VenuesSearchController
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main), VenuesSearchController {
  private val binding by viewBinding(ActivityMainBinding::bind)

  private val navController: NavController
    get() =
      (supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment)
        .navController

  private val appBarConfiguration: AppBarConfiguration by
    lazy(LazyThreadSafetyMode.NONE) { AppBarConfiguration(navController.graph) }

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
    lazy(LazyThreadSafetyMode.NONE) { resources.getDimension(coreR.dimen.fab_margin) }

  private val sheetPeekHeightPx: Float by
    lazy(LazyThreadSafetyMode.NONE) { resources.getDimension(R.dimen.sheet_peek_height) }

  private val sheetController: SheetController by
    lazy(LazyThreadSafetyMode.NONE) {
      SheetController(
        bottomSheetView = binding.bottomSheetContainer,
        collapsedAlpha = collapsedSheetAlpha,
        onStateChanged = { state ->
          updateShowPlacesFabVisibility(sheetState = state)
          updateFabsLayoutParams(sheetState = state)
        },
        onSlide = { slideOffset ->
          binding.searchBar.alpha = collapsedSheetAlpha + slideOffset * (1f - collapsedSheetAlpha)
          viewModel.onSheetSlide(slideOffset)
        }
      )
    }

  override var searchViewsHeightPx: Int? = null

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.searchBar)

    binding.showPlacesSheetFab.setOnClickListener {
      sheetController.setState(BottomSheetBehavior.STATE_COLLAPSED)
    }

    initBottomSheet(savedInstanceState)
    initNavigation()
    initSearchViews()

    lifecycle.addObserver(snackbarMessageObserver)
    viewModel.observe()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean =
    when (item.itemId) {
      R.id.action_settings -> true
      else -> super.onOptionsItemSelected(item)
    }

  override fun onSupportNavigateUp(): Boolean =
    navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    sheetController.saveState(outState)
  }

  private fun initBottomSheet(savedInstanceState: Bundle?) {
    sheetController.initFrom(savedInstanceState)
    onBackPressedDispatcher.addCallback {
      if (sheetController.state == BottomSheetBehavior.STATE_EXPANDED) {
        sheetController.setState(BottomSheetBehavior.STATE_COLLAPSED)
      } else {
        finish()
      }
    }
  }

  private fun initNavigation() {
    setupActionBarWithNavController(navController, appBarConfiguration)
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
  }

  private fun MainViewModel.observe() {
    bottomSheetVisible.observe(this@MainActivity) {
      binding.bottomSheetContainer.isVisible = it
      if (it) {
        updateShowPlacesFabVisibility(sheetState = sheetController.state)
        updateFabsLayoutParams(sheetState = sheetController.state)
      }
    }

    snackbarMessage.observe(this@MainActivity, snackbarMessageObserver)

    categoriesUpdatedEvent.observe(this@MainActivity) {
      with(binding.searchLayout) { measuredHeight + marginTop + marginBottom }
        .also {
          if (it == searchViewsHeightPx) return@also
          searchViewsHeightPx = it
          viewModel.onSearchViewsSizeMeasure(sheetState = sheetController.state)
        }
    }
  }

  private fun updateShowPlacesFabVisibility(sheetState: Int) {
    binding.showPlacesSheetFab.isVisible =
      binding.bottomSheetContainer.isVisible && sheetState == BottomSheetBehavior.STATE_HIDDEN
  }

  private fun updateFabsLayoutParams(@BottomSheetBehavior.State sheetState: Int) {
    val params = binding.fabsLayout.layoutParams as ViewGroup.MarginLayoutParams
    params.bottomMargin =
      if (binding.bottomSheetContainer.isGone || sheetState == BottomSheetBehavior.STATE_HIDDEN) {
          fabMarginPx
        } else {
          fabMarginPx + sheetPeekHeightPx
        }
        .roundToInt()
    binding.fabsLayout.layoutParams = params
  }
}
