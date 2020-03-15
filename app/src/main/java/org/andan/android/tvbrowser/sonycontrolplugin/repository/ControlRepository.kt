package org.andan.android.tvbrowser.sonycontrolplugin.repository

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.andan.android.tvbrowser.sonycontrolplugin.R
import org.andan.android.tvbrowser.sonycontrolplugin.plugin.TVBrowserSonyIPControlPlugin.Companion.CHANNELS_LIST_CONFIG
import org.andan.av.sony.SonyIPControl

class ControlRepository(application: Application) {
    private val TAG = ControlRepository::class.java.name

    private var controlPreferences : SharedPreferences
    private var channelPreferences : SharedPreferences


    private var controlList = ArrayList<SonyIPControl>()
    private var controlListLiveData = MutableLiveData<ArrayList<SonyIPControl>>()
    private var selectedControlIndexLiveData = MutableLiveData<Int>()

    init {
        Log.d(TAG, "init")
        controlPreferences = application.getSharedPreferences(application.getString(
            R.string.pref_control_file_key
        ),
            Context.MODE_PRIVATE)
        channelPreferences = application.getSharedPreferences(application.getString(
            R.string.pref_channel_switch_file_key
        ),
            Context.MODE_PRIVATE)
        controlListLiveData.value = controlList
        loadControls()
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }


    fun getControls(): MutableLiveData<ArrayList<SonyIPControl>> {
        return controlListLiveData
    }

    fun getSelectedControlIndexLiveData(): MutableLiveData<Int> {
        return selectedControlIndexLiveData
    }

    fun setControl(index: Int, control: SonyIPControl): Boolean {
        if(index >= 0 && index < controlList.size) {
            controlList[index] = control
            saveControls(true)
            return true
        }
        return false
    }

    fun setSelectedControlIndex(index : Int): Boolean {
        if(index < controlList.size) {
            selectedControlIndexLiveData.value = index
            saveControls(true)
            return true
        }
        return false
    }

    fun addControl(control: SonyIPControl): Boolean {
        controlList.add(control)
        selectedControlIndexLiveData.value=controlList.size-1
        controlListLiveData.value= controlList
        saveControls(true)
        return true
    }

    fun removeControl(index: Int): Boolean {
        if(index >= 0 && index < controlList.size) {

            var newselectedControlIndexLiveData = selectedControlIndexLiveData.value!! -1
            if  (selectedControlIndexLiveData.value == 0 && controlList.size > 1)
            {
                newselectedControlIndexLiveData = controlList.size-2
            }
            controlList.removeAt(index)
            //controlListLiveData.value=controlList
            selectedControlIndexLiveData.value=newselectedControlIndexLiveData
            saveControls(true)
            return true
        }
        return false
    }

    private fun loadControls() {
        val controlConfig = controlPreferences.getString("controlConfig", "")
        if (controlConfig!!.isNotEmpty()) {
            val controlsJSON =
                SonyIPControl.getGson().fromJson(controlConfig, JsonObject::class.java)
            val controls = controlsJSON.get("controls") as JsonArray
            for (i in 0 until controls.size()) {
                controlList.add(SonyIPControl(controls.get(i) as JsonObject))
            }
            controlListLiveData.value = controlList
            if (controlsJSON.has("selected")) {
                selectedControlIndexLiveData.value = controlsJSON.get("selected").asInt
                if (selectedControlIndexLiveData.value!! >= controlConfig.length) selectedControlIndexLiveData.value = 0
            }
            else {
                selectedControlIndexLiveData.value = 0
            }
        } else {
            selectedControlIndexLiveData.value = -1
        }
    }

    fun saveControls(hasChanged: Boolean) {
        if(hasChanged) controlListLiveData.notifyObserver()
        saveControls()
    }

    private fun saveControls() {
        try {
            val controlsJSON = JsonObject()
            val controls = JsonArray()

            for (ipControl in controlList.orEmpty()) {
                try {
                    val e = ipControl.toJSON()
                    controls.add(e)
                } catch (ex: Exception) {
                }

            }
            controlsJSON.add("controls", controls)
            controlsJSON.addProperty("selected", selectedControlIndexLiveData.value)
            val editor = controlPreferences.edit()
            editor.putString("controlConfig", SonyIPControl.getGson().toJson(controlsJSON))
            editor.commit()
            Log.d(TAG, "saved")
        } catch (ex: Exception) {
            Log.d(TAG, ex.message)
        }
    }

    fun getChannelNameList(): MutableList<String> {
        val channelNameConfig = channelPreferences.getString(CHANNELS_LIST_CONFIG, "")
        if (channelNameConfig!!.isNotEmpty()) {
            val channelsJSON =
                SonyIPControl.getGson().fromJson(channelNameConfig, JsonObject::class.java)
            val channelNames= channelsJSON.get("subscribed channels") as JsonArray
            var itr: MutableIterator<JsonElement> = channelNames.iterator()
            return  SonyIPControl.getGson().fromJson(channelNames,Array<String>::class.java).toMutableList()
        }
        return ArrayList()
    }

}