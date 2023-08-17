package com.trm.opencoinmap.feature.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
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

  private val _categories = MutableLiveData<List<VenueCategoryCount>>(emptyList())
  val categories: LiveData<List<VenueCategoryCount>> = _categories

  private val checkedCategories = mutableSetOf<String>()

  private val _categoryAtIndexUpdated = LiveEvent<Int>()
  val categoryAtIndexUpdated: LiveData<Int> = _categoryAtIndexUpdated

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

  fun isCategoryAtIndexChecked(index: Int): Boolean =
    _categories.value?.get(index)?.category?.let(checkedCategories::contains) ?: false

  fun onCategoryAtIndexCheckedChange(index: Int, isChecked: Boolean) {
    val (category) = _categories.value?.get(index) ?: return
    checkedCategories.apply { if (isChecked) add(category) else remove(category) }
    _categoryAtIndexUpdated.postValue(index)
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }
}
