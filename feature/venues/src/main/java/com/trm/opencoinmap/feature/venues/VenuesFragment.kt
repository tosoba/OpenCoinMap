package com.trm.opencoinmap.feature.venues

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VenuesFragment : Fragment(R.layout.fragment_venues) {
  private val viewModel by viewModels<VenuesViewModel>()
}
