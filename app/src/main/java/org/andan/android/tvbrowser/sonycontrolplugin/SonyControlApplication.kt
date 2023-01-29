package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.Application
import androidx.activity.viewModels
import dagger.hilt.android.HiltAndroidApp
import org.andan.android.tvbrowser.sonycontrolplugin.di.AppModule
import org.andan.android.tvbrowser.sonycontrolplugin.di.ApplicationComponent
import org.andan.android.tvbrowser.sonycontrolplugin.di.DaggerApplicationComponent
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.SonyControlViewModel
import timber.log.Timber

@HiltAndroidApp
class SonyControlApplication : Application() {
    lateinit var appComponent: ApplicationComponent

    // Reference to the application graph that is used across the whole app
    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        appComponent = DaggerApplicationComponent
            .builder().appModule(AppModule(this))
            .build()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        Timber.d("onCreate() $appComponent")
    }

    companion object {
        private var INSTANCE: SonyControlApplication? = null

        @JvmStatic
        fun get(): SonyControlApplication = INSTANCE!!
    }
}
