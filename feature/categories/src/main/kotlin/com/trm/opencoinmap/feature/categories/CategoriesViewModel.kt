package com.trm.opencoinmap.feature.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.domain.usecase.GetCategoriesUseCase
import com.trm.opencoinmap.core.domain.usecase.SendCategoriesListLayoutEventUseCase
import com.trm.opencoinmap.core.domain.util.RxSchedulers
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

  private val _categories = mutableListOf<CheckedVenueCategoryCount>()

  private val _categoriesLive = MutableLiveData<List<CheckedVenueCategoryCount>>(_categories)
  val categories: LiveData<List<CheckedVenueCategoryCount>> = _categoriesLive

  init {
    getCategoriesUseCase()
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy(
        onNext = {
          _categories.clear()
          _categories.addAll(
            it.map { count -> CheckedVenueCategoryCount(categoryCount = count, isChecked = false) }
          )
          _categoriesLive.value = _categories
        }
      )
      .addTo(compositeDisposable)
  }

  fun onCategoriesListLayout() {
    sendCategoriesListLayoutEventUseCase()
  }

  fun isCategoryAtIndexChecked(index: Int): Boolean = _categories[index].isChecked

  fun onCategoryAtIndexCheckedChange(index: Int, isChecked: Boolean) {
    _categories[index] = _categories[index].copy(isChecked = isChecked)
    _categoriesLive.value = _categories
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
