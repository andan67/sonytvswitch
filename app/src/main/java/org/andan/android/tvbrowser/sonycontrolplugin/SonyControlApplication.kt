package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.Application
import android.util.Log
import org.andan.android.tvbrowser.sonycontrolplugin.di.DaggerApplicationComponent
import org.andan.android.tvbrowser.sonycontrolplugin.di.AppModule
import org.andan.android.tvbrowser.sonycontrolplugin.di.ApplicationComponent
import org.andan.android.tvbrowser.sonycontrolplugin.viewmodels.TestViewModel

class SonyControlApplication : Application() {
    private val TAG = SonyControlApplication::class.java.name
    lateinit var appComponent: ApplicationComponent

    // Reference to the application graph that is used across the whole app
    override fun onCreate() {
        super.onCreate()
        INSTANCE=this
        appComponent = DaggerApplicationComponent
            .builder().appModule(AppModule(this))
            .build()
        Log.d(TAG,"onCreate() $appComponent")

    }

    companion object {
        private var INSTANCE: SonyControlApplication? = null
        @JvmStatic
        fun get(): SonyControlApplication = INSTANCE!!
    }
}
