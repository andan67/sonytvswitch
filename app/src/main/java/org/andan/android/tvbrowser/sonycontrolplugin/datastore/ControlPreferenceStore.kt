package org.andan.android.tvbrowser.sonycontrolplugin.datastore

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import java.text.SimpleDateFormat
import javax.inject.Inject

class ControlPreferenceStore @Inject constructor(context: Context) {
    private val TAG = ControlPreferenceStore::class.java.name

    private var controlsPreferences: SharedPreferences = context.getSharedPreferences(
        context.getString(R.string.pref_control_file_key), Context.MODE_PRIVATE)
    private var tokenPreferences: SharedPreferences = context.getSharedPreferences("token_store", Context.MODE_PRIVATE)


    private fun loadControls(): SonyControls {
        val controlConfig = controlsPreferences.getString("controlConfig", "")
        Log.d(TAG, "loadControls()")
        return if (controlConfig!!.isNotEmpty()) {
            SonyControls.fromJson(controlConfig)}
        else SonyControls()
    }

}