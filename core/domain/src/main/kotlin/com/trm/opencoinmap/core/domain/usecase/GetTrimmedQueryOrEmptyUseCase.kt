package com.trm.opencoinmap.core.domain.usecase

import javax.inject.Inject

class GetTrimmedQueryOrEmptyUseCase @Inject constructor() {
  operator fun invoke(query: String, minLength: Int): String {
    require(minLength >= 0) { "Min query length cannot be negative." }
    return query.trim().takeIf { it.length >= minLength }.orEmpty()
  }
}
