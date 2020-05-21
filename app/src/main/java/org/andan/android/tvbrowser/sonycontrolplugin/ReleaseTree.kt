package org.andan.android.tvbrowser.sonycontrolplugin

import android.util.Log
import timber.log.Timber

class ReleaseTree : Timber.DebugTree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Don't log VERBOSE, DEBUG
        return !(priority == Log.VERBOSE || priority == Log.DEBUG)
    }
}