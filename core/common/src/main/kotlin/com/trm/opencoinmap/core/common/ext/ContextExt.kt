package com.trm.opencoinmap.core.common.ext

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import io.reactivex.rxjava3.core.Maybe

fun Context.lazyBitmapResource(@DrawableRes drawableId: Int): Lazy<Bitmap> =
  lazy(LazyThreadSafetyMode.NONE) {
    requireNotNull(ContextCompat.getDrawable(this, drawableId)).toBitmap()
  }

@SuppressLint("MissingPermission")
fun Context.getCurrentUserLocation(): Maybe<Location> {
  val cancellationTokenSource = CancellationTokenSource()
  return Maybe.create { emitter ->
    LocationServices.getFusedLocationProviderClient(this)
      .getCurrentLocation(
        CurrentLocationRequest.Builder()
          .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
          .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
          .setDurationMillis(5_000L)
          .setMaxUpdateAgeMillis(60_000L)
          .build(),
        cancellationTokenSource.token
      )
      .addOnSuccessListener(emitter::onSuccess)
      .addOnCanceledListener(emitter::onComplete)
      .addOnFailureListener(emitter::onError)
      .addOnCompleteListener { emitter.onComplete() }
  }
}

@SuppressLint("MissingPermission")
fun Context.isNetworkConnected(): Boolean {
  val connectivityManager = getSystemService<ConnectivityManager>()!!
  return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    connectivityManager
      .getNetworkCapabilities(connectivityManager.activeNetwork)
      .isNetworkCapabilitiesValid()
  } else {
    connectivityManager.activeNetworkInfo?.isConnected == true
  }
}

private fun NetworkCapabilities?.isNetworkCapabilitiesValid(): Boolean =
  when {
    this == null -> false
    hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
      hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
      (hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
        hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) -> true
    else -> false
  }
