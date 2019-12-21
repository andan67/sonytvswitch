package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.andan.android.tvbrowser.sonycontrolplugin.TVBrowserSonyIPControlPlugin.CHANNELS_LIST_CONFIG
import org.andan.av.sony.SonyIPControl
import java.util.*
import kotlin.collections.ArrayList

class ControlRepository(application: Application) {
    private val TAG = ControlRepository::class.java.name

    private var controlPreferences : SharedPreferences
    private var channelPreferences : SharedPreferences


    private var controlList = ArrayList<SonyIPControl>()
    private var controlListLiveData = MutableLiveData<ArrayList<SonyIPControl>>()
    private var selectedControlIndex: Int = -1

    init {
        Log.d(TAG, "init")
        controlPreferences = application.getSharedPreferences(application.getString(R.string.pref_control_file_key),
            Context.MODE_PRIVATE)
        channelPreferences = application.getSharedPreferences(application.getString(R.string.pref_channel_switch_file_key),
            Context.MODE_PRIVATE)
        controlListLiveData.value = controlList
        loadControls()
    }

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }


    fun getControls(): MutableLiveData<ArrayList<SonyIPControl>> {
        return controlListLiveData
    }

    fun getSelectedControlIndex(): Int {
        return selectedControlIndex
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
            selectedControlIndex = index
            saveControls(true)
            return true
        }
        return false
    }

    fun addControl(control: SonyIPControl): Boolean {
        controlList.add(control)
        selectedControlIndex=controlList.size-1
        controlListLiveData.value= controlList
        saveControls(true)
        return true
    }

    fun removeControl(index: Int): Boolean {
        if(index >= 0 && index < controlList.size) {

            var newSelectedControlIndex = selectedControlIndex -1
            if  (selectedControlIndex == 0 && controlList.size > 1)
            {
                newSelectedControlIndex = controlList.size-2
            }
            controlList.removeAt(index)
            //controlListLiveData.value=controlList
            selectedControlIndex=newSelectedControlIndex
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
                selectedControlIndex = controlsJSON.get("selected").asInt
                if (selectedControlIndex >= controlConfig.length) selectedControlIndex = 0
            }
            else {
                selectedControlIndex = 0
            }
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
            controlsJSON.addProperty("selected", selectedControlIndex)
            val editor = controlPreferences.edit()
            editor.putString("controlConfig", SonyIPControl.getGson().toJson(controlsJSON))
            editor.commit()
            Log.d(TAG, "saved")
        } catch (ex: Exception) {
            Log.d(TAG, ex.message)
        }
    }

    fun getChannelNameList(): MutableList<String> {
        //var channelNameList: ArrayList<String> = ArrayList()
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