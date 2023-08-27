package com.trm.opencoinmap.core.domain.usecase

import io.reactivex.rxjava3.core.Flowable

fun interface ReceiveWebViewScrollableUpwardsUseCase {
  operator fun invoke(): Flowable<Boolean>
}
