package com.trm.opencoinmap.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import com.trm.opencoinmap.core.domain.model.MapCenter
import com.trm.opencoinmap.core.domain.repo.PreferenceDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject
import kotlinx.coroutines.rx3.asObservable
import kotlinx.coroutines.rx3.rxCompletable

class PreferenceDataSourceImpl
@Inject
constructor(@ApplicationContext private val context: Context) : PreferenceDataSource {
  private val latitudeKey = doublePreferencesKey(PreferencesDataStoreKeys.MAP_LATITUDE)
  private val longitudeKey = doublePreferencesKey(PreferencesDataStoreKeys.MAP_LONGITUDE)
  private val zoomKey = doublePreferencesKey(PreferencesDataStoreKeys.MAP_ZOOM)

  override fun getMapCenter(): Maybe<MapCenter> =
    context.preferencesDataStore.data.asObservable().firstElement().flatMap { preferences ->
      val latitude = preferences[latitudeKey]
      val longitude = preferences[longitudeKey]
      val zoom = preferences[zoomKey]
      if (latitude == null || longitude == null || zoom == null) Maybe.empty()
      else Maybe.just(MapCenter(latitude = latitude, longitude = longitude, zoom = zoom))
    }

  override fun saveMapCenter(mapCenter: MapCenter): Completable = rxCompletable {
    context.preferencesDataStore.edit { preferences ->
      preferences[latitudeKey] = mapCenter.latitude
      preferences[longitudeKey] = mapCenter.longitude
      preferences[zoomKey] = mapCenter.zoom
    }
  }
}
