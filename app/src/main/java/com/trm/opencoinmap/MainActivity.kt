package com.trm.opencoinmap

import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.trm.opencoinmap.core.common.ext.toSnackbarLength
import com.trm.opencoinmap.core.domain.model.Message
import com.trm.opencoinmap.databinding.ActivityMainBinding
import com.trm.opencoinmap.feature.venues.VenuesFragment
import dagger.hilt.android.AndroidEntryPoint
import eu.okatrych.rightsheet.RightSheetBehavior

@AndroidEntryPoint
class MainActivity : AppCompatActivity(R.layout.activity_main) {
  private val binding by viewBinding(ActivityMainBinding::bind)
  private var snackbar: Snackbar? = null

  private val navController: NavController
    get() {
      val navHost =
        supportFragmentManager.findFragmentById(R.id.nav_host_container) as NavHostFragment
      return navHost.navController
    }
  private val appBarConfiguration: AppBarConfiguration by
    lazy(LazyThreadSafetyMode.NONE) { AppBarConfiguration(navController.graph) }

  private val bottomSheetBehavior: BottomSheetBehavior<FrameLayout> by
    lazy(LazyThreadSafetyMode.NONE) { BottomSheetBehavior.from(binding.bottomSheetContainer) }
  private val rightSheetBehavior: RightSheetBehavior<FrameLayout> by
    lazy(LazyThreadSafetyMode.NONE) { RightSheetBehavior.from(binding.rightSheetContainer) }

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
    setSupportActionBar(binding.toolbar)

    binding.showPlacesSheetFab.setOnClickListener {
      bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
      rightSheetBehavior.state = RightSheetBehavior.STATE_COLLAPSED
    }

    initSheets()

    initNavigation()

    viewModel.observeSnackbarMessage()
  }

  private fun initSheets() {
    initSheetBehaviors()
    initSheetsVisibility()
    initSheetFragment()
  }

  private fun initSheetsVisibility() {
    val orientation = resources.configuration.orientation
    binding.rightSheetContainer.isVisible = orientation == Configuration.ORIENTATION_LANDSCAPE
    binding.bottomSheetContainer.isVisible = orientation == Configuration.ORIENTATION_PORTRAIT
  }

  private fun initSheetBehaviors() {
    bottomSheetBehavior.addBottomSheetCallback(
      object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
          if (
            newState == BottomSheetBehavior.STATE_SETTLING ||
              newState == BottomSheetBehavior.STATE_DRAGGING
          ) {
            return
          }
          rightSheetBehavior.state = newState
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) = Unit
      }
    )

    rightSheetBehavior.addRightSheetCallback(
      object : RightSheetBehavior.RightSheetCallback() {
        override fun onStateChanged(rightSheet: View, newState: Int) {
          if (
            newState == RightSheetBehavior.STATE_SETTLING ||
              newState == RightSheetBehavior.STATE_DRAGGING
          ) {
            return
          }
          bottomSheetBehavior.state = newState
        }

        override fun onSlide(rightSheet: View, slideOffset: Float) = Unit
      }
    )
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

  override fun onDestroy() {
    super.onDestroy()
    snackbar = null
  }

  private fun initNavigation() {
    setupActionBarWithNavController(navController, appBarConfiguration)
  }

  private fun MainViewModel.observeSnackbarMessage() {
    snackbarMessage.observe(this@MainActivity) { message ->
      snackbar =
        when (message) {
          is Message.Hidden -> {
            snackbar?.dismiss()
            null
          }
          is Message.Shown -> {
            Snackbar.make(
                binding.coordinatorLayout,
                message.textResId,
                message.length.toSnackbarLength()
              )
              .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE)
              .addCallback(
                object : Snackbar.Callback() {
                  override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    snackbar = null
                  }
                }
              )
              .run {
                val action = message.action
                if (action == null) this else setAction(action.labelResId) { action() }
              }
              .apply(Snackbar::show)
          }
        }
    }
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
}
