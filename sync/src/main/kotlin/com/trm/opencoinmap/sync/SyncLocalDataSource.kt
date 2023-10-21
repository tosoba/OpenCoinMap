package com.trm.opencoinmap.sync

import android.content.Context
import android.os.Looper
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.trm.opencoinmap.core.domain.sync.SyncDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class SyncLocalDataSource
@Inject
constructor(
  @ApplicationContext private val context: Context,
) : SyncDataSource {
  override fun isRunningFlowable(): Flowable<Boolean> {
    val workDataLiveData =
      WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
    return Flowable.create(
        { emitter ->
          val observer = Observer<List<WorkInfo>> { emitter.onNext(it) }
          emitter.setDisposable(disposeInUiThread { workDataLiveData.removeObserver(observer) })
          workDataLiveData.observeForever(observer)
        },
        BackpressureStrategy.LATEST
      )
      .doOnNext {
        val isRunning = it.firstOrNull()?.state == WorkInfo.State.RUNNING
        Timber.tag("SYNC_RUN").e(isRunning.toString())
        Timber.tag("SYNC_STATE").e(it.firstOrNull()?.state?.toString().orEmpty())
      }
      .map { it.firstOrNull()?.state == WorkInfo.State.RUNNING }
  }

  private fun disposeInUiThread(action: Action): Disposable =
    Disposable.fromAction {
      if (Looper.getMainLooper() == Looper.myLooper()) {
        action.run()
      } else {
        val inner = AndroidSchedulers.mainThread().createWorker()
        inner.schedule {
          try {
            action.run()
          } catch (e: Exception) {
            Timber.e(e, "Could not unregister receiver in UI Thread")
          }
          inner.dispose()
        }
      }
    }
}
