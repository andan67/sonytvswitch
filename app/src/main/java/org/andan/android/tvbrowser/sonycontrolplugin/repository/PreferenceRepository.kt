package org.andan.android.tvbrowser.sonycontrolplugin.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.di.ApplicationScope
import org.andan.android.tvbrowser.sonycontrolplugin.di.DataModule
import org.andan.android.tvbrowser.sonycontrolplugin.ui.components.navigation.NavDestinations

@Singleton
class PreferenceRepository
@Inject
constructor(
    @DataModule.UserSettings private val prefDataStore: DataStore<Preferences>,
    @ApplicationScope private val externalScope: CoroutineScope
) {
  companion object {
    val START_SCREEN = stringPreferencesKey("START_SCREEN")
  }

  init {
    externalScope.launch { prefDataStore.data.first() }
  }

  val startScreenFlow: Flow<String> =
      prefDataStore.data.map { preferences ->
        // No type safety.
        preferences[START_SCREEN] ?: NavDestinations.RemoteControl.route
      }

  suspend fun setStartScreen(startDestintation: String) {
    prefDataStore.edit { preferences -> preferences[START_SCREEN] = startDestintation }
  }
}
