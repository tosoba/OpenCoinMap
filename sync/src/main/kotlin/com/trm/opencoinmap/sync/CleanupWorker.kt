package com.trm.opencoinmap.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import androidx.work.rxjava3.RxWorker
import com.trm.opencoinmap.core.domain.repo.VenueRepo
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.reactivex.rxjava3.core.Single
import java.time.Duration
import timber.log.Timber

@HiltWorker
class CleanupWorker
@AssistedInject
constructor(
  @Assisted private val context: Context,
  @Assisted workerParams: WorkerParameters,
  private val repo: VenueRepo,
) : RxWorker(context, workerParams) {
  override fun createWork(): Single<Result> =
    repo
      .deleteVenueDetailsOlderThan(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1_000)
      .toSingle(Result::success)
      .doOnError(Timber.Forest::e)
      .onErrorReturnItem(Result.failure())

  internal companion object {
    const val WORK_NAME = "CleanupWork"

    fun workRequest(): PeriodicWorkRequest =
      PeriodicWorkRequestBuilder<DelegatingWorker>(Duration.ofDays(7L))
        .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
        .setInputData(CleanupWorker::class.delegatedData())
        .build()
  }
}
