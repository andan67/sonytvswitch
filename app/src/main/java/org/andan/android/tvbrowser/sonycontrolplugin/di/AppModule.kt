package org.andan.android.tvbrowser.sonycontrolplugin.di

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.Nullable
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.ControlsStore
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.TokenStore
import javax.inject.Named
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindTokenStore(myTokenStore: ControlPreferenceStore) : TokenStore
    @Binds
    abstract fun bindControlsStore(myControlsStore: ControlPreferenceStore) : ControlsStore

}

