package org.andan.android.tvbrowser.sonycontrolplugin.di

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CONTROL_DATABASE
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlDatabase
import org.andan.android.tvbrowser.sonycontrolplugin.data.mapper.SonyControlDataMapper

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

  @Qualifier @Retention(AnnotationRetention.BINARY) annotation class UserSettings

  @UserSettings
  @Provides
  @Singleton
  fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler(produceNewData = { emptyPreferences() }),
        produceFile = { context.preferencesDataStoreFile("user_settings") })
  }

  @Provides
  @Singleton
  fun provide(@ApplicationContext context: Context) =
      Room.databaseBuilder(context, ControlDatabase::class.java, CONTROL_DATABASE)
          .allowMainThreadQueries()
          .fallbackToDestructiveMigration()
          .build()

  @Provides @Singleton fun provideDao(db: ControlDatabase) = db.controlDao()

  @Provides @Singleton fun provideSonyControlDataMapper() = SonyControlDataMapper()

  @Provides
  @Singleton
  fun provideControlsPreferences(@ApplicationContext context: Context): SharedPreferences {
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(
            context.getString(R.string.pref_control_file_key), Context.MODE_PRIVATE)
    val controlConfig = context.assets.open("controls.json").bufferedReader().use { it.readText() }
    if (sharedPreferences.getString("controlConfig", "").isNullOrEmpty()) {
      sharedPreferences.edit().putString("controlConfig", controlConfig).apply()
    }
    return sharedPreferences
  }
}
