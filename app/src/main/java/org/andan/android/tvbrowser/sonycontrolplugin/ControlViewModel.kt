package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import org.andan.av.sony.ProgramFuzzyMatch
import org.andan.av.sony.SonyIPControl
import org.andan.av.sony.model.SonyPlayingContentInfo
import org.andan.av.sony.model.SonyProgram
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


class ControlViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = ControlViewModel::class.java.name

    // repository for control data
    private var controlRepository: ControlRepository = ControlRepository(application)

    //private var controlList: MutableLiveData<ArrayList<SonyIPControl>> = controlRepository.getControls()

    private var programSearchQuery: String? = null

    private var filteredProgramList = MutableLiveData<List<SonyProgram>>()

    // derived variables for selected control
    private var programChannelMap: MutableMap<String, String> = HashMap()
    var uriProgramMap: MutableMap<String, SonyProgram> = HashMap()
    var programTitleList: MutableList<String> = ArrayList()
    var selectedChannelName: String = ""

    val noActiveProgram = SonyPlayingContentInfo("",
        "----", "", "No current program",
        "NULL","",0, "")
    var activeContentInfo = MutableLiveData<SonyPlayingContentInfo>()
    private var currentProgram: SonyProgram? = null
    var lastProgram: SonyProgram? = null

    var isCreated: Boolean = false

    var selectedChannelMapProgramUri = MutableLiveData<String?>()

    private var channelNameList: MutableList<String> = ArrayList()
    private var filteredChannelNameList = MutableLiveData<List<String>>()
    private var channelNameSearchQuery: String? = null

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
        Log.d(TAG, "notifyObserver")
    }

    init {
        Log.i(TAG, "init")
        getSelectedControlIndex()
        // init
        onSelectedIndexChange()
    }

    fun getControls(): MutableLiveData<ArrayList<SonyIPControl>> {
        //Log.d(TAG,"getControls()")
        return controlRepository.getControls()
    }

    fun getSelectedControlIndex(): Int {
        //Log.d(TAG,"getSelectedControlIndex()")
        return controlRepository.getSelectedControlIndex()
    }


    fun getSelectedControl(): SonyIPControl? {
        //Log.d(TAG,"getSelectedControl()")
        if(getSelectedControlIndex()<0) return null
        return controlRepository.getControls().value!![getSelectedControlIndex()]
    }

    fun setSelectedControl(control: SonyIPControl) {
        Log.d(TAG,"setSelectedControl(control: SonyIPControl()")
        if(controlRepository.setControl(getSelectedControlIndex(), control)) onSelectedIndexChange()
    }

    fun deleteSelectedControl() {
        Log.d(TAG,"deleteSelectedControl()")
        if(controlRepository.removeControl(getSelectedControlIndex())) onSelectedIndexChange()
    }

    fun addControl(control: SonyIPControl) {
        Log.d(TAG,"addControl(control: SonyIPControl()")
        if(controlRepository.addControl(control)) onSelectedIndexChange()
    }

    fun getSelectedControlAsJson(): String {
        Log.d(TAG,"getSelectedControlAsJson()")
        return SonyIPControl.getGson()
            .toJson(controlRepository.getControls().value!![getSelectedControlIndex()].toJSON())
    }

    fun setSelectedControlIndex(index: Int) {
        Log.d(TAG,"setSelectedControlIndex(index: Int)")
        if(controlRepository.setSelectedControlIndex(index)) onSelectedIndexChange()
    }

    fun setSelectedChannelMapProgramUri(channelName: String?, programUri: String?) {
        Log.d(TAG,"setSelectedChannelMapProgramUri()")
        selectedChannelMapProgramUri.value = programUri
        getSelectedControl()!!.channelProgramUriMap[channelName] = programUri
        refreshDerivedVariablesForSelectedControl()
        saveControls(true)
    }

    private fun onSelectedIndexChange() {
        lastProgram = null
        currentProgram = null
        //activeContentInfo.value = noActiveProgram
        refreshDerivedVariablesForSelectedControl()
        filterProgramList(null)
        filterChannelNameList(null)
    }

    private fun refreshDerivedVariablesForSelectedControl() {
        Log.d(TAG,"refreshDerivedVariablesForSelectedControl()")
        programTitleList.clear()
        channelNameList.clear()
        uriProgramMap.clear()
        if (getSelectedControl()?.programList != null) {
            if (getSelectedControl()?.channelProgramUriMap != null) {
                for (mappedChannelName in getSelectedControl()?.channelProgramUriMap!!.keys) {
                    channelNameList.add(mappedChannelName)
                    val programUri = getSelectedControl()!!.channelProgramUriMap[mappedChannelName]
                    if (programUri != null && getSelectedControl()!!.programUriMap.containsKey(programUri)) {
                        programChannelMap[programUri] = mappedChannelName
                    }
                }
            }

            for (program in getSelectedControl()!!.programList) {
                programTitleList.add(program.title)
                uriProgramMap[program.uri]=program
            }
        }
    }

    fun getFilteredProgramList(): MutableLiveData<List<SonyProgram>> {
        return filteredProgramList
    }


    fun filterProgramList(query: String?) {
        Log.d(TAG,"filterProgramList(query: String?)")
        Log.d(TAG, "filter program list $query ")
        if(getSelectedControl()?.programList!=null) {
            programSearchQuery = query
            filteredProgramList.value = getSelectedControl()!!.programList.filter { p ->
                programSearchQuery.isNullOrEmpty() || p.title.contains(
                    programSearchQuery!!,
                    true
                )
            }
        } else {
            filteredProgramList.value = ArrayList()
        }
    }

    fun getProgramSearchQuery(): String? {
        Log.d(TAG,"getProgramSearchQuery()")
        return programSearchQuery
    }

    fun getChannelForProgramUri(uri: String): String {
        //Log.d(TAG,"getChannelForProgramUri(uri: String)")
        return programChannelMap[uri] ?: ""
    }


    fun updateCurrentProgram(program: SonyProgram) {
        lastProgram=currentProgram
        currentProgram=program
        Log.d(TAG, "updateCurrentProgram ${lastProgram?.title} ${currentProgram?.title} ${program.title}")
    }

    fun onProgramLongClicked(program: SonyProgram): Boolean {
        Log.d(TAG, "onProgramLongClicked: ${program.index}")
        return true
    }

    fun setChannelNameListFromPreference() {
        Log.d(TAG,"setChannelNameListFromPreference()")
        channelNameList = controlRepository.getChannelNameList()
        updateChannelMapsFromChannelNameList()
    }

    fun getFilteredChannelNameList(): MutableLiveData<List<String>> {
        //Log.d(TAG,"getFilteredChannelNameList()")
        // get list of channel names from preference
        if(filteredChannelNameList.value==null) {
            filterChannelNameList("")
        }
        return filteredChannelNameList
    }

    fun filterChannelNameList(query: String?) {
        Log.d(TAG, "filter channel name list $query ")
        if(channelNameList != null) {
            channelNameSearchQuery = query
            filteredChannelNameList.value = channelNameList.filter { c ->
                channelNameSearchQuery.isNullOrEmpty() || c.contains(
                    channelNameSearchQuery!!,
                    true
                )
            }
            Log.d(TAG, "channelNameList!=null ${filteredChannelNameList.value!!.size} ")
            Log.d(TAG, "channelNameList!=null ${channelNameList.size} ")
        }
        else {
            filteredChannelNameList.value = ArrayList()
            Log.d(TAG, "channelNameList==null ${filteredChannelNameList.value!!.size} ")
        }
    }

    fun getChannelNameSearchQuery(): String? {
        Log.d(TAG, "getChannelNameSearchQuery()")
        return channelNameSearchQuery
    }

    fun getChannelNameList(): List<String>? {
        Log.d(TAG, "getChannelNameList()")
        // get list of channel names from preference
        return channelNameList
    }


    private fun updateChannelMapsFromChannelNameList() {
        Log.d(TAG, "updateChannelMapsFromChannelNameList()")
        var isUpdated = false

        for (control in getControls().value!!) {
            if (control.channelProgramUriMap == null) {
                control.channelProgramUriMap = LinkedHashMap<String, String>()
                isUpdated = true
            }
            for (channelName in channelNameList) {
                // create mapping entry for unmapped channels
                if (!control.channelProgramUriMap.containsKey(channelName)) {
                    control.channelProgramUriMap[channelName] = ""
                    isUpdated = true
                }
                //ToDo: Handle deletion of channels
            }
        }
        filterChannelNameList("")
        saveControls(isUpdated)
    }

    internal fun performFuzzyMatchForChannelList() {
        Log.d(TAG, "performFuzzyMatchForChannelList()")
        if (channelNameList.isNotEmpty() && programTitleList.isNotEmpty() && getSelectedControl()?.programList != null ) {
            for (channelName in channelNameList) {
                val index1 = ProgramFuzzyMatch.matchOne(channelName, programTitleList, true)
                if (index1 >= 0) {
                    if (getSelectedControl()?.channelProgramUriMap == null) {
                        getSelectedControl()?.channelProgramUriMap = LinkedHashMap<String, String>()
                    }
                    val programUri = getSelectedControl()!!.programList[index1].uri

                    getSelectedControl()!!.channelProgramUriMap[channelName]= programUri
                }
                controlRepository.getControls().notifyObserver()
            }
            saveControls(true)
        }
    }

    internal fun clearMapping(clearMatch: Boolean) {
        Log.d(TAG, "clearMapping(clearMatch: Boolean)")
        if (getSelectedControl()?.channelProgramUriMap != null) {
            for (channelName in channelNameList) {
                getSelectedControl()!!.channelProgramUriMap[channelName]=""
                controlRepository.getControls().notifyObserver()
            }
        }
    }

    private fun saveControls(hasChanged: Boolean) {
        Log.d(TAG, "caveControls(hasChanged: Boolean) $hasChanged")
        controlRepository.saveControls(hasChanged)
    }

    internal fun createProgramUriMatchList(channelName: String?, query: String?) : ArrayList<String> {
        Log.d(TAG, "createProgramUriMatchList()")
        val programUriMatchList: ArrayList<String> = ArrayList()
        if (programTitleList.isNotEmpty()) {
            var matchTopSet: MutableSet<Int>? = null
            if (query == null || query.isEmpty()) {
                matchTopSet = ProgramFuzzyMatch.matchTop(channelName, programTitleList, 30, true)
            } else {
                matchTopSet = LinkedHashSet()
                for (i in programTitleList.indices) {
                    val programTitle = programTitleList[i]
                    if (programTitle.toLowerCase().contains(query.toLowerCase())) {
                        matchTopSet.add(i)
                        if (matchTopSet.size == 30) break
                    }
                }
            }
            if (matchTopSet != null && !getSelectedControl()?.programList.isNullOrEmpty()) {
                matchTopSet.forEach {programUriMatchList.add(getSelectedControl()!!.programList[it].uri)}
            }
            saveControls(true)
        }
        return programUriMatchList
    }
}