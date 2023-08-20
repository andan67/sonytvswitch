package org.andan.android.tvbrowser.sonycontrolplugin.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlsStore
import org.andan.android.tvbrowser.sonycontrolplugin.data.TokenStore
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindTokenStore(myTokenStore: ControlPreferenceStore): TokenStore

    @Binds
    abstract fun bindControlsStore(myControlsStore: ControlPreferenceStore): ControlsStore
}

