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

class ControlPreferenceStore2 @Inject constructor(context: Context) {
    private val TAG = ControlPreferenceStore2::class.java.name
    var sonyControls = MutableLiveData<SonyControls>()
    val selectedSonyControl = MutableLiveData<SonyControl>()
    var selectedIndex = MutableLiveData<Int>()
    private var _selectedIndex = -1
    private lateinit var controlsPreferences: SharedPreferences

    init {
        controlsPreferences= context.getSharedPreferences(
            context.getString(R.string.pref_control_file_key), Context.MODE_PRIVATE)
        loadControls()
    }

    private fun <T> MutableLiveData<T>.notifyObserverAsync() {
        this.postValue(this.value)
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    private fun loadControls() {
        val controlConfig = controlsPreferences.getString("controlConfig", "")
        if (controlConfig!!.isNotEmpty()) {
            sonyControls.value = SonyControls.fromJson(controlsPreferences.getString("controlConfig", "")!!)
            _selectedIndex = sonyControls.value!!.selected
            selectedIndex.value = _selectedIndex
            selectedSonyControl.value = sonyControls.value!!.controls[_selectedIndex]
            Log.d(TAG, "loadControls: ${selectedSonyControl.value!!.programList[0].title}")
        }
    }

    fun onControlsChanged()  {
        Log.d(TAG, "onControlsChanged")
        onControlsChanged(false)
    }

    private fun onControlsChanged(fromBackgroundThread: Boolean)  {
        if(fromBackgroundThread) {
            sonyControls.notifyObserverAsync()
        }
        else {
            //sonyControls.notifyObserver()
        }
        Log.d(TAG, "onControlsChanged")
        controlsPreferences.edit().putString("controlConfig", sonyControls.value!!.toJson()).commit()
    }

    fun getToken(): String {
        //storeToken("")
        return selectedSonyControl.value!!.cookie?: ""
    }

    fun storeToken(token: String) {
        val sdf = SimpleDateFormat("HH:mm:ss")
        selectedSonyControl.value!!.systemProduct = sdf.format(java.util.Calendar.getInstance().time)
        selectedSonyControl.notifyObserverAsync()
        onControlsChanged(true)
        //selectedSonyControl.value!!.cookie = token
    }

    fun addControl(control: SonyControl) {
        sonyControls.value!!.controls.add(control)

    }

}