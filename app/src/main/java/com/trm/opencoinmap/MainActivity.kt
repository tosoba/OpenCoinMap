package com.trm.opencoinmap

import android.content.res.Configuration
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.commitNow
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.view.SheetController
import com.trm.opencoinmap.core.common.view.SheetState
import com.trm.opencoinmap.core.common.view.SnackbarMessageObserver
import com.trm.opencoinmap.databinding.ActivityMainBinding
import com.trm.opencoinmap.feature.venues.VenuesFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
  private val binding by viewBinding(ActivityMainBinding::bind)

  private val navController: NavController
    get() {
      val navHost =
        supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
      return navHost.navController
    }
  private val appBarConfiguration by
    lazy(LazyThreadSafetyMode.NONE) { AppBarConfiguration(navController.graph) }

  private val snackbarMessageObserver by
    lazy(LazyThreadSafetyMode.NONE) {
      SnackbarMessageObserver(
        view = binding.coordinatorLayout,
        onShown = { binding.fabsLayout.isVisible = false },
        onDismissed = { binding.fabsLayout.isVisible = true }
      )
    }

  private val sheetController by
    lazy(LazyThreadSafetyMode.NONE) {
      SheetController(
        bottomSheetView = binding.bottomSheetContainer,
        rightSheetView = binding.rightSheetContainer,
        collapsedAlpha =
          TypedValue().run {
            resources.getValue(R.dimen.collapsed_sheet_alpha, this, true)
            float
          },
        onStateChanged = { state ->
          binding.showPlacesSheetFab.isVisible = state == SheetState.STATE_HIDDEN
        }
      )
    }

  private val existingSheetFragment: Fragment?
    get() =
      supportFragmentManager.run {
        findFragmentById(R.id.bottom_sheet_container)
          ?: findFragmentById(R.id.right_sheet_container)
      }

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.searchBar)

    binding.showPlacesSheetFab.setOnClickListener {
      sheetController.setState(SheetState.STATE_COLLAPSED)
    }

    initSheets(savedInstanceState)

    initNavigation()

    lifecycle.addObserver(snackbarMessageObserver)
    viewModel.observeSnackbarMessage()
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

  private fun initSheets(savedInstanceState: Bundle?) {
    sheetController.initFrom(savedInstanceState)
    initSheetsVisibility()
    initSheetFragment()
  }

  private fun initSheetsVisibility() {
    val orientation = resources.configuration.orientation
    binding.rightSheetContainer.isVisible = orientation == Configuration.ORIENTATION_LANDSCAPE
    binding.bottomSheetContainer.isVisible = orientation == Configuration.ORIENTATION_PORTRAIT
  }

  private fun initSheetFragment() {
    val orientation = resources.configuration.orientation
    val sheetFragment =
      existingSheetFragment?.also { supportFragmentManager.commitNow { remove(it) } }
        ?: VenuesFragment()
    supportFragmentManager.commit {
      replace(
        if (orientation == Configuration.ORIENTATION_PORTRAIT) R.id.bottom_sheet_container
        else R.id.right_sheet_container,
        sheetFragment
      )
    }
  }

  private fun initNavigation() {
    setupActionBarWithNavController(navController, appBarConfiguration)
  }

  private fun MainViewModel.observeSnackbarMessage() {
    snackbarMessage.observe(this@MainActivity, snackbarMessageObserver)
  }
}
