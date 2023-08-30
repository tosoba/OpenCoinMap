package com.trm.opencoinmap.feature.venue.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.trm.opencoinmap.core.common.ext.findParentFragmentOfType
import com.trm.opencoinmap.core.common.ext.getSystemWindowTopInsetPx
import com.trm.opencoinmap.core.common.ext.requireAs
import com.trm.opencoinmap.core.common.view.BottomSheetController
import com.trm.opencoinmap.core.common.view.OnBackPressedController
import com.trm.opencoinmap.core.common.view.ScrollDirection
import com.trm.opencoinmap.feature.venue.details.databinding.FragmentVenueDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import timber.log.Timber

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
      binding.venueDetailsWebView.interactionDisabled = offset < 1f

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
          binding.venueDetailsWebView.interactionDisabled = bottomSheetSlideOffset < 1f

          if (searchViewsHeightPx != null) {
            updateContainerLayoutParams(searchViewsHeightPx, bottomSheetSlideOffset)
          }
        }

      insets
    }

    with(binding.venueDetailsDragHandleView) { setPadding(paddingLeft, 0, paddingRight, 0) }

    viewModel.onWebViewScrolled(
      venueDetailsWebView.canScrollVertically(ScrollDirection.UPWARDS.direction)
    )
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      venueDetailsWebView.setOnScrollChangeListener { _, _, _, _, _ ->
        viewModel.onWebViewScrolled(
          venueDetailsWebView.canScrollVertically(ScrollDirection.UPWARDS.direction)
        )
      }
    }
    venueDetailsWebView.webViewClient =
      object : WebViewClient() {
        var currentPageUrl: String? = null

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
          Timber.tag("WEBVIEW").e("Started $url")
          currentPageUrl = url

          venueDetailsWebView.isVisible = true
          venueDetailsWebsiteLoadingProgressIndicator.isVisible = true
          venueDetailsWebsiteErrorGroup.isVisible = false
        }

        override fun onPageFinished(view: WebView, url: String) {
          Timber.tag("WEBVIEW").e("Finished $url")

          venueDetailsWebsiteLoadingProgressIndicator.isVisible = false
          goBackButton.isEnabled = venueDetailsWebView.canGoBack()
        }

        override fun onReceivedError(
          view: WebView,
          request: WebResourceRequest,
          error: WebResourceError
        ) {
          super.onReceivedError(view, request, error)
          onWebViewError(request)
        }

        override fun onReceivedHttpError(
          view: WebView,
          request: WebResourceRequest,
          errorResponse: WebResourceResponse
        ) {
          super.onReceivedHttpError(view, request, errorResponse)
          onWebViewError(request)
        }

        private fun onWebViewError(request: WebResourceRequest) {
          Timber.tag("WEBVIEW").e("Error occurred for ${request.url}")
          if (currentPageUrl != request.url.toString()) return

          venueDetailsWebView.isVisible = false
          venueDetailsWebsiteLoadingProgressIndicator.isVisible = false
          venueDetailsWebsiteErrorGroup.isVisible = true
        }
      }

    goBackButton.setOnClickListener { venueDetailsWebView.goBack() }
    refreshButton.setOnClickListener { venueDetailsWebView.reload() }

    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
      when {
        venueDetailsWebView.canGoBack() -> {
          venueDetailsWebView.goBack()
        }
        venueDetailsWebView.canScrollVertically(ScrollDirection.UPWARDS.direction) -> {
          venueDetailsWebView.scrollTo(0, 0)
        }
        else -> {
          findParentFragmentOfType<OnBackPressedController>()?.onBackPressed()
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
      viewState.websiteUrl?.let(venueDetailsWebView::loadUrl)
      venueDetailsActionsChipGroup.updateActionChips(viewState)
      venueDetailsActionsScrollView.isVisible = viewState.actionsScrollViewVisible
    }
  }

  private fun ChipGroup.updateActionChips(viewState: VenueDetailsViewModel.ViewState.Loaded) {
    removeAllViews()
    viewState.websiteUrl?.let { url ->
      addView(actionChip(R.string.open_in_browser, R.drawable.browser) { goToUrlInBrowser(url) })
    }
    if (viewState.phoneVisible) {
      addView(actionChip(R.string.call, R.drawable.phone) {})
    }
    if (viewState.emailVisible) {
      addView(actionChip(R.string.email, R.drawable.email) {})
    }
    if (viewState.facebookVisible) {
      addView(actionChip(R.string.facebook, R.drawable.facebook) {})
    }
    if (viewState.twitterVisible) {
      addView(actionChip(R.string.twitter, R.drawable.twitter) {})
    }
    if (viewState.instagramVisible) {
      addView(actionChip(R.string.instagram, R.drawable.instagram) {})
    }
  }

  private fun actionChip(
    @StringRes textRes: Int,
    @DrawableRes drawableRes: Int,
    onClick: View.OnClickListener
  ): Chip =
    layoutInflater
      .inflate(R.layout.item_venue_details_action, binding.venueDetailsActionsChipGroup, false)
      .requireAs<Chip>()
      .also {
        it.setText(textRes)
        it.setChipIconResource(drawableRes)
        it.setOnClickListener(onClick)
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
