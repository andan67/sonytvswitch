package org.andan.android.tvbrowser.sonycontrolplugin.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlsStore
import org.andan.android.tvbrowser.sonycontrolplugin.data.TokenStore


@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindTokenStore(myTokenStore: ControlPreferenceStore): TokenStore

    @Binds
    abstract fun bindControlsStore(myControlsStore: ControlPreferenceStore): ControlsStore
}

