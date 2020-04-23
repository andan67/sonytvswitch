package org.andan.android.tvbrowser.sonycontrolplugin.datastore

import android.content.Context
import android.content.SharedPreferences
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import timber.log.Timber
import javax.inject.Inject

class ControlPreferenceStore @Inject constructor(context: Context): ControlsStore, TokenStore {
    private var controlsPreferences: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.pref_control_file_key), Context.MODE_PRIVATE)
    private var tokenPreferences: SharedPreferences = context.getSharedPreferences("token_store", Context.MODE_PRIVATE)

    override fun loadControls(): SonyControls {
        val controlConfig = controlsPreferences.getString("controlConfig", "")
        Timber.d("loadControls()")
        val sonyControls = if (controlConfig!!.isNotEmpty()) {
            SonyControls.fromJson(controlConfig)
        } else {
            SonyControls()
        }
        Timber.i("#loaded controls: ${sonyControls.controls.size}, selected=${sonyControls.selected} ")

        reconcileTokenStore(sonyControls)
        return sonyControls
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
        val controlUuids = HashSet<String>()
        val tokenStoreEditor = tokenPreferences.edit()
        val tokenEntries = tokenPreferences.all
        for(control in sonyControls.controls) {
            val uuid = control.uuid
            controlUuids.add(uuid)
            // add token entry if it not exists
            if(uuid !in tokenEntries.keys) {
                tokenStoreEditor.putString(uuid, control.cookie)
            }
        }
        for(tokenEntry in tokenPreferences.all) {
            val uuid = tokenEntry.key
            // remove token entry if not exists in list of controls (e.g. control has been deleted)
            if(uuid !in controlUuids) {
                tokenStoreEditor.remove(uuid)
            }
        }
        tokenStoreEditor.commit()
    }
}