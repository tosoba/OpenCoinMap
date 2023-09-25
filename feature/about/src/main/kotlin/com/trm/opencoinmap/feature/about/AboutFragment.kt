package com.trm.opencoinmap.feature.about

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.R as commonR
import com.trm.opencoinmap.feature.about.databinding.FragmentAboutBinding

class AboutFragment : DialogFragment(R.layout.fragment_about) {
  private val binding by viewBinding(FragmentAboutBinding::bind)

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
    super.onCreateDialog(savedInstanceState).apply {
      requestWindowFeature(Window.FEATURE_NO_TITLE)
      window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.initViews()
  }

  private fun FragmentAboutBinding.initViews() {
    aboutOsmCreditLayer.setOnClickListener { goToUrlInBrowser("https://www.openstreetmap.org/") }
    aboutCoinMapCreditLayer.setOnClickListener { goToUrlInBrowser("https://coinmap.org/") }
  }

  private fun goToUrlInBrowser(url: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (ex: ActivityNotFoundException) {
      Toast.makeText(
          requireContext(),
          getString(commonR.string.browser_app_was_not_found),
          Toast.LENGTH_SHORT
        )
        .show()
    }
  }
}
