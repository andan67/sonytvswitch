package org.andan.android.tvbrowser.sonycontrolplugin.datastore

import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls

interface ControlsStore {
    fun loadControls(): SonyControls
    fun storeControls(sonyControls: SonyControls)
}

interface TokenStore {
    fun loadToken(uuid: String): String
    fun storeToken(uuid: String, token: String)
}