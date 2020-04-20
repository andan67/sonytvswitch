package org.andan.android.tvbrowser.sonycontrolplugin.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import javax.inject.Inject

class ControlPreferenceStore @Inject constructor(context: Context): ControlsStore, TokenStore {
    private val TAG = ControlPreferenceStore::class.java.name

    private var controlsPreferences: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.pref_control_file_key), Context.MODE_PRIVATE)
    private var tokenPreferences: SharedPreferences = context.getSharedPreferences("token_store", Context.MODE_PRIVATE)

    override fun loadControls(): SonyControls {
        val controlConfig = controlsPreferences.getString("controlConfig", "")
        Log.d(TAG, "loadControls()")
        val sonyControls = if (controlConfig!!.isNotEmpty()) {
            SonyControls.fromJson(controlConfig)
        } else {
            SonyControls()
        }
        reconcileTokenStore(sonyControls)
        return sonyControls
    }

    override fun storeControls(sonyControls: SonyControls) {
        Log.d(TAG, "storeControls()")
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