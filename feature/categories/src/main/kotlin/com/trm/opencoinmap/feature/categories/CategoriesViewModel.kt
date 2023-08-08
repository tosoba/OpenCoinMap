package com.trm.opencoinmap.feature.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.domain.util.RxSchedulers
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.core.domain.usecase.GetCategoriesUseCase
import com.trm.opencoinmap.core.domain.usecase.SendCategoriesListLayoutEventUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import javax.inject.Inject

@HiltViewModel
internal class CategoriesViewModel
@Inject
constructor(
  getCategoriesUseCase: GetCategoriesUseCase,
  private val sendCategoriesListLayoutEventUseCase: SendCategoriesListLayoutEventUseCase,
  schedulers: RxSchedulers
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _categories = MutableLiveData(emptyList<VenueCategoryCount>())
  val categories: LiveData<List<VenueCategoryCount>> = _categories

  init {
    getCategoriesUseCase()
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy(onNext = _categories::setValue)
      .addTo(compositeDisposable)
  }

  fun onCategoriesListLayout() {
    sendCategoriesListLayoutEventUseCase()
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
