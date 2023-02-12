package com.trm.opencoinmap.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import androidx.work.rxjava3.RxWorker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.core.Single
import java.time.Duration

@HiltWorker
class SyncWorker
@AssistedInject
constructor(
  @Assisted private val context: Context,
  @Assisted workerParams: WorkerParameters,
) : RxWorker(context, workerParams) {
  override fun createWork(): Single<Result> {
    return Single.just(Result.success())
  }

  companion object {
    fun workRequest(): PeriodicWorkRequest =
      PeriodicWorkRequestBuilder<DelegatingWorker>(Duration.ofDays(7L))
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .setInputData(SyncWorker::class.delegatedData())
        .build()
  }
}
