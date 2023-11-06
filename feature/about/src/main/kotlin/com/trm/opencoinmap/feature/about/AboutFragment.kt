package com.trm.opencoinmap.feature.about

import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.widget.TextViewCompat
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.R as commonR
import com.trm.opencoinmap.core.common.ext.toDp
import com.trm.opencoinmap.feature.about.databinding.FragmentAboutBinding

class AboutFragment : DialogFragment(R.layout.fragment_about) {
  private val binding by viewBinding(FragmentAboutBinding::bind)

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
    super.onCreateDialog(savedInstanceState).apply {
      window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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
      TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
    )

    aboutClose.setOnClickListener { dismiss() }

    aboutOsmCreditLayout.setOnClickListener { goToUrlInBrowser("https://www.openstreetmap.org/") }
    aboutCoinMapCreditLayout.setOnClickListener { goToUrlInBrowser("https://coinmap.org/") }

    aboutBitcoinSupportMeLabel.isSelected = true
    aboutEthereumSupportMeLabel.isSelected = true
    aboutXrpSupportMeLabel.isSelected = true

    aboutBitcoinSupportMeLayout.setOnClickListener {
      copyToClipboard(addressRes = R.string.bitcoin_address)
    }
    aboutEthereumSupportMeLayout.setOnClickListener {
      copyToClipboard(addressRes = R.string.ethereum_address)
    }
    aboutXrpSupportMeLayout.setOnClickListener {
      copyToClipboard(addressRes = R.string.xrp_address)
    }
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

  private fun copyToClipboard(@StringRes addressRes: Int) {
    getSystemService(requireContext(), ClipboardManager::class.java)
      ?.setPrimaryClip(ClipData.newPlainText(null, requireContext().getString(addressRes)))
      ?.also {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
          Toast.makeText(requireContext(), R.string.address_copied, Toast.LENGTH_SHORT).show()
        }
      }
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
