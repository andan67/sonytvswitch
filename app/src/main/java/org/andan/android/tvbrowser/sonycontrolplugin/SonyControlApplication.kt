package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.Application
import org.andan.android.tvbrowser.sonycontrolplugin.di.DaggerApplicationComponent

class SonyControlApplication: Application() {
    // Reference to the application graph that is used across the whole app
    val appComponent = DaggerApplicationComponent.create()
}