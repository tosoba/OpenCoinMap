package com.trm.opencoinmap.core.domain.model

sealed interface MarkersLoadingStatus {
  object InProgress : MarkersLoadingStatus
  object Success : MarkersLoadingStatus
  data class Error(val throwable: Throwable?) : MarkersLoadingStatus
}
