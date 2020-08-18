package org.andan.android.tvbrowser.sonycontrolplugin.di

import dagger.Component
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyServiceClientContext
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import javax.inject.Singleton

@Component(modules = [NetworkModule::class, AppModule::class])
@Singleton
interface ApplicationComponent {
    fun sonyRepository(): SonyControlRepository
    fun sonyServiceContext(): SonyServiceClientContext
}