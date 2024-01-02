package org.andan.android.tvbrowser.sonycontrolplugin.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ControlPreferenceStore @Inject constructor(@ApplicationContext private val context: Context) :
    ControlsStore, TokenStore {
    private var controlsPreferences: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.pref_control_file_key), Context.MODE_PRIVATE
    )
    private var tokenPreferences: SharedPreferences =
        context.getSharedPreferences("token_store", Context.MODE_PRIVATE)

    init {
        Timber.d("init ControlPreferenceStore: $this ")
    }

    override fun loadControls(): SonyControls {
        val controlConfig = controlsPreferences.getString("controlConfig", "")
        Timber.d("loadControls()")
        val sonyControls = if (controlConfig!!.isNotEmpty()) {
            SonyControls.fromJson(controlConfig)
        } else {
            SonyControls()
        }

        reconcileTokenStore(sonyControls)
        Timber.i("#loaded controls: ${sonyControls.controls.size}, selected=${sonyControls.selected}")
        if (sonyControls.selected >= 0) {
            val uuid = sonyControls.controls[sonyControls.selected].uuid
            Timber.i("Selected control token: ${loadToken(uuid)}")
        }
        return sonyControls
    }

    fun loadMockControls(): SonyControls {
        return SonyControls.fromJson(
            context.assets.open("controls.json").bufferedReader()
                .use { it.readText() }) ?: SonyControls()
    }

    override fun storeControls(sonyControls: SonyControls) {
        Timber.d("storeControls()")
        reconcileTokenStore(sonyControls)
        controlsPreferences.edit().putString("controlConfig", sonyControls.toJson()).commit()
    }

    override fun loadToken(uuid: String): String {
        return tokenPreferences.getString(uuid, "")!!
    }

    override fun storeToken(uuid: String, token: String) {
        tokenPreferences.edit().putString(uuid, token).commit()
    }

    private fun reconcileTokenStore(sonyControls: SonyControls) {
        // build set of existing uuids in sonyControls
        val controlUuids = HashMap<String, SonyControl>()
        val tokenStoreEditor = tokenPreferences.edit()
        val tokenEntries = tokenPreferences.all
        for (i in 0 until sonyControls.controls.size) {
            val control = sonyControls.controls[i]
            val uuid = control.uuid
            controlUuids[uuid] = control
            // add token entry if it not exists
            if (uuid !in tokenEntries.keys) {
                tokenStoreEditor.putString(uuid, control.cookie)
            }
        }
        for (tokenEntry in tokenPreferences.all) {
            val uuid = tokenEntry.key
            // remove token entry if not exists in list of controls (e.g. control has been deleted)
            if (uuid !in controlUuids.keys) {
                tokenStoreEditor.remove(uuid)
            } else {
                //controlUuids[uuid]!!.cookie = tokenEntry.value as String
            }
        }
        tokenStoreEditor.commit()
    }
}