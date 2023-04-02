package com.trm.opencoinmap

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
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

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)
    setSupportActionBar(binding.toolbar)

    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    rightSheetBehavior.state = RightSheetBehavior.STATE_EXPANDED

    initNavigation()

    viewModel.observeSnackbarMessage()
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
