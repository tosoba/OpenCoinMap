package com.trm.opencoinmap.core.common.ext

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
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
  }
}
