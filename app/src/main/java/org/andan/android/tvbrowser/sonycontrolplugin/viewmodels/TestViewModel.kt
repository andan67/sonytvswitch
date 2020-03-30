package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.di.DaggerApplicationComponent
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyProgram2
import org.andan.android.tvbrowser.sonycontrolplugin.network.PlayingContentInfoResponse
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyRepository
import org.andan.av.sony.ProgramFuzzyMatch
import java.net.SocketTimeoutException
import java.util.LinkedHashSet

class TestViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val TAG = TestViewModel::class.java.name
    // repository for control data
    private val repository: SonyRepository = SonyControlApplication.get().appComponent.sonyRepository()
    // val sampleText = "This is a sample text"

    /*val powerStatus : LiveData<String> = liveData(Dispatchers.IO) {
        val result = repository.getPowerStatus()
        emit(result)
    }*/

   /* fun getCurrentTime() = liveData(Dispatchers.IO) {
        //val result = repository.getCurrentTime()
        // Log.d(TAG,"currentTime: " + result)
        emit(repository.getCurrentTime())
    }*/

    val requestErrorMessage = repository.requestErrorMessage

    var isCreated: Boolean = false

    private var _selectedSonyControl = MutableLiveData<SonyControl>()
    val selectedSonyControl: LiveData<SonyControl>
        get() = _selectedSonyControl

    private var _sonyControls = MutableLiveData<SonyControls>()
    val sonyControls: LiveData<SonyControls>
        get() = _sonyControls



    private fun getSelectedControl(): SonyControl? {
        return selectedSonyControl.value
    }

    private var programSearchQuery: String? = null

    private var filteredProgramList = MutableLiveData<List<SonyProgram2>>()

    // derived variables for selected control
    private var programChannelMap: MutableMap<String, String> = HashMap()
    var uriProgramMap: MutableMap<String, SonyProgram2> = HashMap()
    var programTitleList: MutableList<String> = ArrayList()
    var selectedChannelName: String = ""

    //var activeContentInfo = MutableLiveData<PlayingContentInfoResponse>()
    private var currentProgram: SonyProgram2? = null
    var lastProgram: SonyProgram2? = null

    var channelNameList: MutableList<String> = ArrayList()
    private var filteredChannelNameList = MutableLiveData<List<String>>()
    var channelNameSearchQuery: String? = null

    var selectedChannelMapProgramUri = MutableLiveData<String?>()

    init {

        Log.d(TAG, "init")
        _sonyControls = repository.sonyControls
        _selectedSonyControl = repository.selectedSonyControl
        onSelectedIndexChange()
        //filterChannelNameList("")
    }

    fun updateCurrentProgram(program: SonyProgram2) {
        if(currentProgram?.uri!=program.uri) {
            lastProgram = currentProgram
            currentProgram = program
            Log.d(TAG, "updateCurrentProgram ${lastProgram?.title} ${currentProgram?.title} ${program.title}"
            )
        }
    }

    fun addControl(control: SonyControl) {
        Log.d(TAG, "addControl(): $control")
        Log.d(TAG, "testViewModel: $this")
        repository.addControl(control)
    }

    fun deleteSelectedControl() {
        if(repository.removeControl(sonyControls.value!!.selected)) onSelectedIndexChange()
    }

    fun setSelectedControlIndex(index: Int) {
        Log.d(TAG,"setSelectedControlIndex(index: $index)")
        if(repository.setSelectedControlIndex(index)) onSelectedIndexChange()
    }

    fun onSelectedIndexChange() {
        Log.d(TAG,"onSelectedIndexChange(): ${_sonyControls.value!!.selected}")
        lastProgram = null
        currentProgram = null
        //activeContentInfo.value = noActiveProgram
        refreshDerivedVariablesForSelectedControl()
        filterProgramList(null)
        filterChannelNameList(null)
    }

    fun refreshDerivedVariablesForSelectedControl() {
        Log.d(TAG,"refreshDerivedVariablesForSelectedControl()")
        programTitleList.clear()
        channelNameList.clear()
        uriProgramMap.clear()
        if (getSelectedControl()?.programList != null) {
            if (getSelectedControl()?.channelProgramMap != null) {
                for (mappedChannelName in getSelectedControl()?.channelProgramMap!!.keys) {
                    channelNameList.add(mappedChannelName)
                    val programUri = getSelectedControl()!!.channelProgramMap[mappedChannelName]
                    if (programUri != null && getSelectedControl()!!.programUriMap!!.containsKey(programUri)) {
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

    fun getFilteredProgramList(): MutableLiveData<List<SonyProgram2>> {
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
            repository.saveControls()
        }
        return programUriMatchList
    }

    val playingContentInfo = repository.playingContentInfo
    fun fetchPlayingContentInfo() = viewModelScope.launch(Dispatchers.IO) {
        repository.getPlayingContentInfo()
    }

    fun fetchProgramList() = viewModelScope.launch(Dispatchers.IO) {
        repository.fetchProgramList()
    }

    fun setPlayContent(uri: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.setPlayContent(uri)
    }

    fun registerControl() {
        registerControl(null)
    }

    fun registerControl(challenge: String?) = viewModelScope.launch(Dispatchers.IO) {
        repository.registerControl(challenge)
    }

    fun setSelectedChannelMapProgramUri(channelName: String?, programUri: String?) {
        Log.d(TAG,"setSelectedChannelMapProgramUri()")
        selectedChannelMapProgramUri.value = programUri
        getSelectedControl()!!.channelProgramMap[channelName!!] = programUri!!
        refreshDerivedVariablesForSelectedControl()
        repository.saveControls()
    }

    internal fun performFuzzyMatchForChannelList() {
        Log.d(TAG, "performFuzzyMatchForChannelList()")
        if (channelNameList.isNotEmpty() && programTitleList.isNotEmpty() && getSelectedControl()?.programList != null ) {
            for (channelName in channelNameList) {
                val index1 = ProgramFuzzyMatch.matchOne(channelName, programTitleList, true)
                if (index1 >= 0) {
                    val programUri = getSelectedControl()!!.programList[index1].uri
                    getSelectedControl()!!.channelProgramMap[channelName]= programUri
                }
            }
            repository.saveControls()
        }
    }
}
