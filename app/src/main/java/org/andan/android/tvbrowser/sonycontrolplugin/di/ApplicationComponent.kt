package org.andan.android.tvbrowser.sonycontrolplugin.di

import dagger.Component
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyServiceHolder
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyRepository
import javax.inject.Singleton

@Component(modules = [NetworkModule::class, AppModule::class])
@Singleton
interface ApplicationComponent {
    fun sonyRepository(): SonyRepository
    fun serviceHolder(): SonyServiceHolder
    //fun inject(sonyRepository: SonyRepository)

  /*  @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(context: Context): Builder
        fun build(): ApplicationComponent
    }*/
}