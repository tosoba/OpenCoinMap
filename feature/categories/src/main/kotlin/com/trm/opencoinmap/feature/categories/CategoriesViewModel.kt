package com.trm.opencoinmap.feature.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.trm.opencoinmap.core.domain.model.VenueCategoryCount
import com.trm.opencoinmap.core.domain.usecase.GetCategoriesUseCase
import com.trm.opencoinmap.core.domain.usecase.SendCategoriesListLayoutEventUseCase
import com.trm.opencoinmap.core.domain.usecase.SendCategoriesUseCase
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
  private val sendCategoriesUseCase: SendCategoriesUseCase,
  schedulers: RxSchedulers
) : ViewModel() {
  private val compositeDisposable = CompositeDisposable()

  private val _categories = MutableLiveData<List<VenueCategoryCount>>(emptyList())
  val categories: LiveData<List<VenueCategoryCount>> = _categories

  private val checkedCategories = mutableSetOf<String>()

  init {
    getCategoriesUseCase()
      .filter(List<VenueCategoryCount>::isNotEmpty)
      .map {
        buildList {
          add(
            VenueCategoryCount(category = ALL_CATEGORY, count = it.sumOf(VenueCategoryCount::count))
          )
          addAll(it)
        }
      }
      .subscribeOn(schedulers.io)
      .observeOn(schedulers.main)
      .subscribeBy {
        checkedCategories.add(ALL_CATEGORY)
        _categories.setValue(it)
      }
      .addTo(compositeDisposable)
  }

  fun onCategoriesListLayout() {
    sendCategoriesListLayoutEventUseCase()
  }

  fun isCategoryChecked(category: String): Boolean = checkedCategories.contains(category)

  fun onCategoryCheckedChange(category: String, isChecked: Boolean) {
    checkedCategories.apply {
      if (isChecked) {
        add(category)
        if (category != ALL_CATEGORY) remove(ALL_CATEGORY)
      } else {
        remove(category)
      }
    }

    sendCategoriesUseCase(checkedCategories.filter { it != ALL_CATEGORY })
  }

  override fun onCleared() {
    super.onCleared()
    compositeDisposable.clear()
  }

  companion object {
    const val ALL_CATEGORY = "All"
  }
}
