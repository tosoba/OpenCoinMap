package com.trm.opencoinmap.feature.venue.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.chip.Chip
import com.trm.opencoinmap.core.common.ext.findParentFragmentOfType
import com.trm.opencoinmap.core.common.ext.getSystemWindowTopInsetPx
import com.trm.opencoinmap.core.common.ext.requireAs
import com.trm.opencoinmap.core.common.view.BottomSheetController
import com.trm.opencoinmap.core.common.view.OnBackPressedController
import com.trm.opencoinmap.core.common.view.ScrollDirection
import com.trm.opencoinmap.feature.venue.details.databinding.FragmentVenueDetailsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt
import com.trm.opencoinmap.core.common.R as commonR

@AndroidEntryPoint
class VenueDetailsFragment : Fragment(R.layout.fragment_venue_details) {
  private val binding by viewBinding(FragmentVenueDetailsBinding::bind)

  private val viewModel by viewModels<VenueDetailsViewModel>()

  private var systemWindowTopInsetPx: Int? = null

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.initViews()

    viewModel.viewState.observe(viewLifecycleOwner) { binding.onViewState(it) }
    viewModel.viewEvent.observe(viewLifecycleOwner, ::onViewEvent)

    viewModel.sheetSlideOffset.observe(viewLifecycleOwner) { offset ->
      binding.venueDetailsWebView.interactionDisabled = offset < 1f
      binding.updateErrorGroupsAlpha(offset)

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
          updateErrorGroupsAlpha(bottomSheetSlideOffset)

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
    venueDetailsWebView.setOnScrollChangeListener { _, _, _, _, _ ->
      viewModel.onWebViewScrolled(
        venueDetailsWebView.canScrollVertically(ScrollDirection.UPWARDS.direction)
      )
    }
    venueDetailsWebView.webViewClient =
      object : WebViewClient() {
        var currentPageUrl: String? = null

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
          currentPageUrl = url
          venueDetailsWebView.isVisible = true
          venueDetailsWebsiteLoadingProgressIndicator.isVisible = true
          venueDetailsWebsiteExpandedErrorGroup.isVisible = false
          venueDetailsWebsiteCollapsedErrorGroup?.isVisible = false
        }

        override fun onPageFinished(view: WebView, url: String) {
          venueDetailsWebsiteLoadingProgressIndicator.isVisible = false
          expandedGoBackButton.isEnabled = venueDetailsWebView.canGoBack()
          collapsedGoBackButton?.isEnabled = venueDetailsWebView.canGoBack()
        }

        override fun onReceivedError(
          view: WebView,
          request: WebResourceRequest,
          error: WebResourceError,
        ) {
          super.onReceivedError(view, request, error)
          onWebViewError(request)
        }

        override fun onReceivedHttpError(
          view: WebView,
          request: WebResourceRequest,
          errorResponse: WebResourceResponse,
        ) {
          super.onReceivedHttpError(view, request, errorResponse)
          onWebViewError(request)
        }

        private fun onWebViewError(request: WebResourceRequest) {
          if (currentPageUrl != request.url.toString()) return
          updateViewsOnWebViewError(
            R.string.error_occurred_try_to_open_in_browser,
            commonR.drawable.error,
          )
        }
      }

    expandedGoBackButton.setOnClickListener { venueDetailsWebView.goBack() }
    expandedRefreshButton.setOnClickListener { venueDetailsWebView.reload() }

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

    collapsedVenueDetailsRetryButton.setOnClickListener { viewModel.onRetryClick() }
  }

  private fun FragmentVenueDetailsBinding.updateViewsOnWebViewError(
    @StringRes errorMessageRes: Int,
    @DrawableRes errorDrawableRes: Int,
  ) {
    venueDetailsWebsiteLoadingProgressIndicator.isVisible = false
    venueDetailsWebView.isVisible = false

    val drawable = ContextCompat.getDrawable(requireContext(), errorDrawableRes)
    collapsedErrorImageView?.setImageDrawable(drawable)
    expandedErrorImageView.setImageDrawable(drawable)

    collapsedErrorTextView?.setText(errorMessageRes)
    expandedErrorTextView.setText(errorMessageRes)

    venueDetailsWebsiteExpandedErrorGroup.isVisible = true
    venueDetailsWebsiteCollapsedErrorGroup?.isVisible = true
  }

  private fun FragmentVenueDetailsBinding.updateErrorGroupsAlpha(bottomSheetSlideOffset: Float) {
    venueDetailsWebsiteCollapsedErrorGroup?.referencedIds?.forEach {
      binding.root.findViewById<View>(it).alpha = 1f - bottomSheetSlideOffset
    }
    venueDetailsWebsiteExpandedErrorGroup.referencedIds.forEach {
      binding.root.findViewById<View>(it).alpha = bottomSheetSlideOffset
    }
    collapsedVenueDetailsErrorGroup.referencedIds.forEach {
      binding.root.findViewById<View>(it).alpha = 1f - bottomSheetSlideOffset
    }
  }

  private fun FragmentVenueDetailsBinding.updateContainerLayoutParams(
    searchBarHeightPx: Int,
    offset: Float,
  ) {
    venueDetailsContainer.layoutParams =
      venueDetailsContainer.layoutParams.requireAs<ViewGroup.MarginLayoutParams>().apply {
        topMargin = (((systemWindowTopInsetPx ?: 0) + searchBarHeightPx) * offset).roundToInt()
      }
  }

  private fun FragmentVenueDetailsBinding.onViewState(viewState: VenueDetailsViewModel.ViewState) {
    venueDetailsWebsiteLoadingProgressIndicator.isVisible =
      viewState is VenueDetailsViewModel.ViewState.Loading
    venueDetailsWebView.isVisible = viewState is VenueDetailsViewModel.ViewState.Loaded
    collapsedVenueDetailsErrorGroup.isVisible = viewState is VenueDetailsViewModel.ViewState.Error

    if (viewState is VenueDetailsViewModel.ViewState.Loaded) {
      viewState.websiteUrl?.let(venueDetailsWebView::loadUrl)
        ?: run {
          updateViewsOnWebViewError(R.string.no_website, commonR.drawable.error)
          expandedGoBackButton.isEnabled = false
          expandedRefreshButton.isEnabled = false
          collapsedGoBackButton?.isEnabled = false
          collapsedRefreshButton?.isEnabled = false
        }
      updateActionsScrollView(viewState)
    }
  }

  private fun FragmentVenueDetailsBinding.updateActionsScrollView(
    viewState: VenueDetailsViewModel.ViewState.Loaded
  ) {
    venueDetailsActionsChipGroup.removeAllViews()
    val actionChips = viewModel.actionChips(viewState)
    actionChips.map(::actionChip).forEach(venueDetailsActionsChipGroup::addView)
    venueDetailsActionsScrollView.isVisible = actionChips.isNotEmpty()
  }

  private fun actionChip(action: VenueDetailsChipAction): Chip =
    layoutInflater
      .inflate(R.layout.item_venue_details_action, binding.venueDetailsActionsChipGroup, false)
      .requireAs<Chip>()
      .also {
        it.setText(action.textRes)
        it.setChipIconResource(action.drawableRes)
        it.setOnClickListener(action.onClick)
      }

  private fun onViewEvent(viewEvent: VenueDetailsViewModel.ViewEvent) {
    when (viewEvent) {
      is VenueDetailsViewModel.ViewEvent.OpenInBrowser -> {
        goToUrlInBrowser(viewEvent.url)
      }
      is VenueDetailsViewModel.ViewEvent.Navigate -> {
        goToGoogleMapsNavigate(viewEvent.lat, viewEvent.lon)
      }
      is VenueDetailsViewModel.ViewEvent.Dial -> {
        goToDial(viewEvent.number)
      }
      is VenueDetailsViewModel.ViewEvent.Mail -> {
        goToMail(viewEvent.address)
      }
      is VenueDetailsViewModel.ViewEvent.Facebook -> {
        goToFacebook(viewEvent.name)
      }
      is VenueDetailsViewModel.ViewEvent.Twitter -> {
        goToTwitter(viewEvent.name)
      }
      is VenueDetailsViewModel.ViewEvent.Instagram -> {
        goToInstagram(viewEvent.name)
      }
    }
  }

  private fun goToUrlInBrowser(url: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (ex: ActivityNotFoundException) {
      showAppNotFoundToast(commonR.string.browser_app_was_not_found)
    }
  }

  private fun goToGoogleMapsNavigate(lat: Double, lon: Double) {
    try {
      startActivity(
        Intent(Intent.ACTION_VIEW, "google.navigation:q=$lat,$lon".toUri())
          .setPackage("com.google.android.apps.maps")
      )
    } catch (ex: ActivityNotFoundException) {
      showAppNotFoundToast(R.string.google_maps_app_was_not_found)
    }
  }

  private fun goToDial(number: String) {
    try {
      startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number, null)))
    } catch (ex: ActivityNotFoundException) {
      showAppNotFoundToast(R.string.phone_app_was_not_found)
    }
  }

  private fun goToMail(address: String) {
    try {
      startActivity(
        Intent.createChooser(
          Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", address, null)),
          null,
        )
      )
    } catch (ex: ActivityNotFoundException) {
      showAppNotFoundToast(R.string.mail_app_was_not_found)
    }
  }

  private fun goToFacebook(name: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, "fb://page/$name".toUri()))
    } catch (ex: ActivityNotFoundException) {
      try {
        startActivity(Intent(Intent.ACTION_VIEW, "https://www.facebook.com/$name".toUri()))
      } catch (ex: ActivityNotFoundException) {
        showAppNotFoundToast(R.string.facebook_app_was_not_found)
      }
    }
  }

  private fun goToInstagram(name: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, "https://instagram.com/_u/$name".toUri()))
    } catch (ex: ActivityNotFoundException) {
      try {
        startActivity(Intent(Intent.ACTION_VIEW, "https://instagram.com/$name".toUri()))
      } catch (ex: ActivityNotFoundException) {
        showAppNotFoundToast(R.string.instagram_app_was_not_found)
      }
    }
  }

  private fun goToTwitter(name: String) {
    try {
      startActivity(Intent(Intent.ACTION_VIEW, "twitter://user?screen_name=$name".toUri()))
    } catch (ex: ActivityNotFoundException) {
      try {
        startActivity(Intent(Intent.ACTION_VIEW, "https://twitter.com/#!/$name".toUri()))
      } catch (ex: ActivityNotFoundException) {
        showAppNotFoundToast(R.string.twitter_app_was_not_found)
      }
    }
  }

  private fun showAppNotFoundToast(@StringRes messageRes: Int) {
    Toast.makeText(requireContext(), getString(messageRes), Toast.LENGTH_SHORT).show()
  }
}
