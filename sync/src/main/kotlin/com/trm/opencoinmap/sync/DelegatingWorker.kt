package com.trm.opencoinmap.sync

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Data
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import io.reactivex.rxjava3.core.Single
import kotlin.reflect.KClass

@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltWorkerFactoryEntryPoint {
  fun hiltWorkerFactory(): HiltWorkerFactory
}

private const val WORKER_CLASS_NAME = "RouterWorkerDelegateClassName"

internal fun KClass<out RxWorker>.delegatedData() =
  Data.Builder().putString(WORKER_CLASS_NAME, qualifiedName).build()

class DelegatingWorker(
  appContext: Context,
  workerParams: WorkerParameters,
) : RxWorker(appContext, workerParams) {
  private val workerClassName = workerParams.inputData.getString(WORKER_CLASS_NAME) ?: ""

  private val delegateWorker =
    EntryPointAccessors.fromApplication<HiltWorkerFactoryEntryPoint>(appContext)
      .hiltWorkerFactory()
      .createWorker(appContext, workerClassName, workerParams) as? RxWorker
      ?: throw IllegalArgumentException("Unable to find appropriate worker")

  override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> =
    delegateWorker.foregroundInfoAsync

  override fun createWork(): Single<Result> = delegateWorker.createWork()
}
