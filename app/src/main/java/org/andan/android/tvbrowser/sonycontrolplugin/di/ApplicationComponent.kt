package org.andan.android.tvbrowser.sonycontrolplugin.di

import dagger.Component
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyRepository
import javax.inject.Singleton

@Component(modules = [NetworkModule::class])
@Singleton
interface ApplicationComponent {
    fun sonyRepository(): SonyRepository
    //fun inject(sonyRepository: SonyRepository)
}