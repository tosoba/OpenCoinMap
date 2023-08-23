package com.trm.opencoinmap.feature.venue.details

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.feature.venue.details.databinding.FragmentVenueDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VenueDetailsFragment : Fragment(R.layout.fragment_venue_details) {
  private val binding by viewBinding(FragmentVenueDetailsBinding::bind)

  private val viewModel by viewModels<VenueDetailsViewModel>()

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.initViews()
    viewModel.viewState.observe(viewLifecycleOwner) { binding.onViewState(it) }
  }

  private fun FragmentVenueDetailsBinding.initViews() {
    venueDetailsWebView.webViewClient = WebViewClient()
    venueDetailsRetryButton.setOnClickListener { viewModel.onRetryClick() }
  }

  private fun FragmentVenueDetailsBinding.onViewState(viewState: VenueDetailsViewModel.ViewState) {
    venueDetailsProgressIndicator.isVisible = viewState is VenueDetailsViewModel.ViewState.Loading
    venueDetailsWebView.isVisible = viewState is VenueDetailsViewModel.ViewState.Loaded
    venueDetailsRetryButton.isVisible = viewState is VenueDetailsViewModel.ViewState.Error
    venueDetailsWebsiteMissing.isVisible =
      viewState is VenueDetailsViewModel.ViewState.WebsiteMissing
    if (viewState is VenueDetailsViewModel.ViewState.Loaded) {
      venueDetailsWebView.loadUrl(
        requireNotNull(viewState.venueDetails.website).replace("http:", "https:")
      )
    }
  }

  companion object {
    fun argsBundle(): Bundle = bundleOf()
  }
}
