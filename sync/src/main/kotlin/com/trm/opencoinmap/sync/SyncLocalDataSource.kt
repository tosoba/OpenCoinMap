package com.trm.opencoinmap.sync

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.trm.opencoinmap.core.domain.sync.SyncDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncLocalDataSource
@Inject
constructor(
  @ApplicationContext private val context: Context,
) : SyncDataSource {
  private val isRunning = BehaviorProcessor.createDefault(true)

  init {
    Handler(Looper.getMainLooper()).post {
      WorkManager.getInstance(context)
        .getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME)
        .observeForever { isRunning.onNext(it.firstOrNull()?.state == WorkInfo.State.RUNNING) }
    }
  }

  override fun isRunningFlowable(): Flowable<Boolean> = isRunning
}
