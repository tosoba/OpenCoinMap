package com.trm.opencoinmap.feature.venue.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.trm.opencoinmap.core.common.ext.findParentFragmentOfType
import com.trm.opencoinmap.core.common.ext.getSystemWindowTopInsetPx
import com.trm.opencoinmap.core.common.ext.requireAs
import com.trm.opencoinmap.core.common.view.BottomSheetController
import com.trm.opencoinmap.feature.venue.details.databinding.FragmentVenueDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class VenueDetailsFragment : Fragment(R.layout.fragment_venue_details) {
  private val binding by viewBinding(FragmentVenueDetailsBinding::bind)

  private val viewModel by viewModels<VenueDetailsViewModel>()

  private var systemWindowTopInsetPx: Int? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.initViews()

    viewModel.viewState.observe(viewLifecycleOwner) { binding.onViewState(it) }

    viewModel.sheetSlideOffset.observe(viewLifecycleOwner) { offset ->
      val searchBarHeightPx =
        findParentFragmentOfType<BottomSheetController>()?.bottomSheetContainerTopMarginPx
          ?: return@observe
      binding.updateContainerLayoutParams(searchBarHeightPx, offset)
    }
  }

  override fun onDestroyView() {
    systemWindowTopInsetPx = null
    super.onDestroyView()
  }

  private fun FragmentVenueDetailsBinding.initViews() {
    requireView().setOnApplyWindowInsetsListener { _, insets ->
      if (systemWindowTopInsetPx != null) return@setOnApplyWindowInsetsListener insets

      systemWindowTopInsetPx = insets.getSystemWindowTopInsetPx()

      findParentFragmentOfType<BottomSheetController>()
        ?.run { bottomSheetContainerTopMarginPx to bottomSheetSlideOffset }
        ?.let { (searchViewsHeightPx, bottomSheetSlideOffset) ->
          if (searchViewsHeightPx != null) {
            updateContainerLayoutParams(searchViewsHeightPx, bottomSheetSlideOffset)
          }
        }

      insets
    }

    with(binding.venueDetailsDragHandleView) { setPadding(paddingLeft, 0, paddingRight, 0) }

    venueDetailsWebView.webViewClient =
      object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
          view.setOnClickListener(null)
        }

        override fun onPageFinished(view: WebView, url: String) {
          view.setOnClickListener {
            if (findParentFragmentOfType<BottomSheetController>()?.bottomSheetExpanded == true) {
              goToUrlInBrowser(url)
            }
          }
        }
      }

    venueDetailsRetryButton.setOnClickListener { viewModel.onRetryClick() }
  }

  private fun FragmentVenueDetailsBinding.updateContainerLayoutParams(
    searchBarHeightPx: Int,
    offset: Float
  ) {
    venueDetailsContainer.layoutParams =
      venueDetailsContainer.layoutParams.requireAs<ViewGroup.MarginLayoutParams>().apply {
        topMargin = (((systemWindowTopInsetPx ?: 0) + searchBarHeightPx) * offset).roundToInt()
      }
  }

  private fun FragmentVenueDetailsBinding.onViewState(viewState: VenueDetailsViewModel.ViewState) {
    venueDetailsProgressIndicator.isVisible = viewState is VenueDetailsViewModel.ViewState.Loading
    venueDetailsWebView.isVisible = viewState is VenueDetailsViewModel.ViewState.Loaded
    venueDetailsRetryButton.isVisible = viewState is VenueDetailsViewModel.ViewState.Error

    if (viewState is VenueDetailsViewModel.ViewState.Loaded) {
      viewState.venueDetails.website?.replace("http:", "https:")?.let(venueDetailsWebView::loadUrl)

      venueDetailsActionsScrollView.isVisible = viewState.actionsScrollViewVisible
      phoneImageView.isVisible = viewState.phoneVisible
      emailImageView.isVisible = viewState.emailVisible
      facebookImageView.isVisible = viewState.facebookVisible
      twitterImageView.isVisible = viewState.twitterVisible
      instagramImageView.isVisible = viewState.instagramVisible
    }
  }

  private fun goToUrlInBrowser(url: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (ex: ActivityNotFoundException) {
      Toast.makeText(
          requireContext(),
          getString(R.string.browser_app_was_not_found),
          Toast.LENGTH_SHORT
        )
        .show()
    }
  }
}
