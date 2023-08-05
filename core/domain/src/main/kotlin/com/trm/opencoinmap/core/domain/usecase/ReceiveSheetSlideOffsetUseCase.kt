package com.trm.opencoinmap.core.domain.usecase

import io.reactivex.rxjava3.core.Flowable

fun interface ReceiveSheetSlideOffsetUseCase {
  operator fun invoke(): Flowable<Float>
}
