package com.trm.opencoinmap.core.domain.usecase

fun interface SendWebViewScrollableUpwardsUseCase {
  operator fun invoke(isScrollable: Boolean)
}
