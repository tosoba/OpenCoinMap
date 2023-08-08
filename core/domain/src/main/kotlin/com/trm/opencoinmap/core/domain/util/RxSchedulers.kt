package com.trm.opencoinmap.core.domain.util

import io.reactivex.rxjava3.core.Scheduler

interface RxSchedulers {
  val io: Scheduler
  val main: Scheduler
}
