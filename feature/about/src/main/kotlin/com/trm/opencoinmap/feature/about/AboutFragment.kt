package com.trm.opencoinmap.feature.about

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.fragment.app.DialogFragment
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.feature.about.databinding.FragmentAboutBinding

class AboutFragment : DialogFragment(R.layout.fragment_about) {
  private val binding by viewBinding(FragmentAboutBinding::bind)

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val dialog = super.onCreateDialog(savedInstanceState)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    return dialog
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
  }
}
