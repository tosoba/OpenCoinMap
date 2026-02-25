package com.trm.opencoinmap.feature.about

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.toDp
import com.trm.opencoinmap.feature.about.databinding.FragmentAboutBinding
import com.trm.opencoinmap.core.common.R as commonR

class AboutFragment : DialogFragment(R.layout.fragment_about) {
  private val binding by viewBinding(FragmentAboutBinding::bind)

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
    super.onCreateDialog(savedInstanceState).apply {
      window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.initViews()
  }

  override fun onResume() {
    super.onResume()
    setDialogWindowWidth()
  }

  private fun FragmentAboutBinding.initViews() {
    TextViewCompat.setAutoSizeTextTypeWithDefaults(
      aboutAppName,
      TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM,
    )

    aboutClose.setOnClickListener { dismiss() }

    aboutOsmCreditLayout.setOnClickListener { goToUrlInBrowser("https://www.openstreetmap.org/") }
    aboutCoinMapCreditLayout.setOnClickListener { goToUrlInBrowser("https://coinmap.org/") }
  }

  private fun setDialogWindowWidth() {
    val screenWidthPixels = requireContext().resources.displayMetrics.widthPixels
    dialog?.window?.apply {
      attributes =
        attributes.also {
          it.width =
            (screenWidthPixels *
                when {
                  screenWidthPixels.toFloat().toDp(requireContext()) <= 600 -> .8
                  screenWidthPixels.toFloat().toDp(requireContext()) <= 840 -> .5
                  else -> .3
                })
              .toInt()
        }
    }
  }

  private fun goToUrlInBrowser(url: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (ex: ActivityNotFoundException) {
      Toast.makeText(
          requireContext(),
          getString(commonR.string.browser_app_was_not_found),
          Toast.LENGTH_SHORT,
        )
        .show()
    }
  }
}
