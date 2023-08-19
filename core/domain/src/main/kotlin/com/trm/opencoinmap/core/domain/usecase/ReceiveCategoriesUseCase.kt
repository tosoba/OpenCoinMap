package com.trm.opencoinmap.core.domain.usecase

import io.reactivex.rxjava3.core.Observable

fun interface ReceiveCategoriesUseCase {
  operator fun invoke(): Observable<List<String>>
}
