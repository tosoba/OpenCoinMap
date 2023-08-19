package com.trm.opencoinmap.core.domain.usecase

fun interface SendCategoriesUseCase {
  operator fun invoke(categories: List<String>)
}
