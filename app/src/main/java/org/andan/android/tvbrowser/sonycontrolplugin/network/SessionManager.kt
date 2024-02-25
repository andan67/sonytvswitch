package org.andan.android.tvbrowser.sonycontrolplugin.network

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Provider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import org.andan.android.tvbrowser.sonycontrolplugin.di.NetworkModule
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl

class SessionManager
@Inject
constructor(
    @NetworkModule.SessionTokens private val tokenStore: DataStore<Preferences>,
    val sonyServiceProvider: Provider<SonyService>
) {
  var activeControlUuid: String = ""
  var hostname: String = ""
  var nickname: String = ""
  var devicename: String = ""
  var preSharedKey: String = ""
  var challenge: String = ""

  private var tokens: Preferences? = null

  suspend fun saveToken(token: String) {

    tokenStore.edit { it[stringPreferencesKey(activeControlUuid)] = token }
  }

  suspend fun getToken(): String {
    // return empty token in case no one is stored
    return tokenStore.data.map { it[stringPreferencesKey(activeControlUuid)] }.firstOrNull() ?: ""
  }

  suspend fun removeToken(uuid: String) {
    tokenStore.edit { it.remove(stringPreferencesKey(uuid)) }
  }

  suspend fun removeToken() {
    tokenStore.edit { it.remove(stringPreferencesKey(activeControlUuid)) }
  }

  fun setContext(control: SonyControl) {
    activeControlUuid = control.uuid
    hostname = control.ip
    nickname = control.nickname
    devicename = control.devicename
    preSharedKey = control.preSharedKey
  }
}
