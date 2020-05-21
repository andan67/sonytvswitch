package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.domain.*
import org.andan.android.tvbrowser.sonycontrolplugin.network.InterfaceInformationResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.Resource
import org.andan.android.tvbrowser.sonycontrolplugin.network.SSDP
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set

class SonyControlViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val sonyControlRepository: SonyControlRepository = SonyControlApplication.get().appComponent.sonyRepository()

    val requestErrorMessage = sonyControlRepository.responseMessage
    val registrationResult = sonyControlRepository.registrationResult

    var isCreated: Boolean = false

    private var _selectedSonyControl = MutableLiveData<SonyControl>()
    val selectedSonyControl: LiveData<SonyControl>
        get() = _selectedSonyControl

    private var _sonyControls = MutableLiveData<SonyControls>()
    val sonyControls: LiveData<SonyControls>
        get() = _sonyControls

    var addedControlHostAddress: String =""

    private fun getSelectedControl(): SonyControl? {
        return selectedSonyControl.value
    }

    private var programSearchQuery: String? = null

    private var filteredProgramList = MutableLiveData<List<SonyProgram>>()

    // derived variables for selected control
    private var programChannelMap: MutableMap<String, String> = HashMap()
    var uriProgramMap: MutableMap<String, SonyProgram> = HashMap()
    var programTitleList: MutableList<String> = ArrayList()
    var selectedChannelName: String = ""

    private var currentProgramUri: String = ""
    var lastProgramUri: String = ""

    var channelNameList: MutableList<String> = ArrayList()
    private var filteredChannelNameList = MutableLiveData<List<String>>()
    var channelNameSearchQuery: String? = null

    var selectedChannelMapProgramUri = MutableLiveData<String?>()

    private val _playingContentInfo = MutableLiveData<PlayingContentInfo>()
    val playingContentInfo: LiveData<PlayingContentInfo> = _playingContentInfo

    private val _sonyIpAndDeviceList = MutableLiveData<List<SSDP.IpDeviceItem>>()
    val sonyIpAndDeviceList: LiveData<List<SSDP.IpDeviceItem>> = _sonyIpAndDeviceList

    private val _interfaceInformation = MutableLiveData<Resource<InterfaceInformationResponse>>()
    val interfaceInformation: LiveData<Resource<InterfaceInformationResponse>> = _interfaceInformation

    init {
        Timber.d("init")
        _playingContentInfo.value = PlayingContentInfo()
        _sonyControls = sonyControlRepository.sonyControls
        _selectedSonyControl = sonyControlRepository.selectedSonyControl
        onSelectedIndexChange()
        //filterChannelNameList("")
    }

    fun updateCurrentProgram(uri: String) {
        if (uri.isNotEmpty() && uriProgramMap.containsKey(uri) && currentProgramUri != uri) {
            lastProgramUri = currentProgramUri
            currentProgramUri = uri
            //Timber.d("updateCurrentProgram ${uriProgramMap[lastProgramUri]!!.title} ${uriProgramMap[currentProgramUri]!!.title}")
        }
    }

    fun addControl(control: SonyControl) {
        Timber.d("addControl(): $control")
        Timber.d("sonyControlViewModel: $this")
        sonyControlRepository.addControl(control)
    }

    fun deleteSelectedControl() {
        if (sonyControlRepository.removeControl(sonyControls.value!!.selected)) onSelectedIndexChange()
    }

    fun setSelectedControlIndex(index: Int) {
        Timber.d("setSelectedControlIndex(index: $index)")
        if (sonyControlRepository.setSelectedControlIndex(index)) onSelectedIndexChange()
    }

    fun onSelectedIndexChange() {
        Timber.d("onSelectedIndexChange(): ${_sonyControls.value!!.selected}")
        lastProgramUri = ""
        currentProgramUri = ""
        //activeContentInfo.value = noActiveProgram
        refreshDerivedVariablesForSelectedControl()
        filterProgramList(null)
        filterChannelNameList(null)
    }

    fun refreshDerivedVariablesForSelectedControl() {
        Timber.d("refreshDerivedVariablesForSelectedControl()")
        programTitleList.clear()
        channelNameList.clear()
        uriProgramMap.clear()
        if (getSelectedControl()?.programList != null) {
            if (getSelectedControl()?.channelProgramMap != null) {
                for (mappedChannelName in getSelectedControl()?.channelProgramMap!!.keys) {
                    channelNameList.add(mappedChannelName)
                    val programUri = getSelectedControl()!!.channelProgramMap[mappedChannelName]
                    if (programUri != null && getSelectedControl()!!.programUriMap!!.containsKey(
                            programUri
                        )
                    ) {
                        programChannelMap[programUri] = mappedChannelName
                    }
                }
            }

            for (program in getSelectedControl()!!.programList) {
                programTitleList.add(program.title)
                uriProgramMap[program.uri] = program
            }
        }
    }

    fun getFilteredProgramList(): LiveData<List<SonyProgram>> {
        return filteredProgramList
    }


    fun filterProgramList(query: String?) {
        Timber.d("filter program list query='$query' ")
        if (getSelectedControl()?.programList != null) {
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
        return programSearchQuery
    }

    fun getChannelForProgramUri(uri: String): String {
        return programChannelMap[uri] ?: ""
    }

    fun getFilteredChannelNameList(): LiveData<List<String>> {
        // get list of channel names from preference
        if (filteredChannelNameList.value == null) {
            filterChannelNameList("")
        }
        return filteredChannelNameList
    }

    fun filterChannelNameList(query: String?) {
        Timber.d("filter channel name list query='$query' ")
        channelNameSearchQuery = query
        filteredChannelNameList.value = channelNameList.filter { c ->
            channelNameSearchQuery.isNullOrEmpty() || c.contains(
                channelNameSearchQuery!!,
                true
            )
        }
    }

    internal fun createProgramUriMatchList(
        channelName: String?,
        query: String?
    ): ArrayList<String> {
        Timber.d("createProgramUriMatchList()")
        val programUriMatchList: ArrayList<String> = ArrayList()
        if (programTitleList.isNotEmpty()) {
            var matchTopSet: MutableSet<Int> = LinkedHashSet()
            if (query == null || query.isEmpty()) {
                matchTopSet.addAll(
                    ProgramFuzzyMatch.matchTop(
                        channelName!!,
                        programTitleList,
                        30,
                        true
                    )
                )
            } else {
                for (i in programTitleList.indices) {
                    val programTitle = programTitleList[i]
                    if (programTitle.toLowerCase().contains(query.toLowerCase())) {
                        matchTopSet.add(i)
                        if (matchTopSet.size == 30) break
                    }
                }
            }
            if (!getSelectedControl()?.programList.isNullOrEmpty()) {
                matchTopSet.forEach { programUriMatchList.add(getSelectedControl()!!.programList[it].uri) }
            }
            sonyControlRepository.saveControls()
        }
        return programUriMatchList
    }

    fun fetchPlayingContentInfo() = viewModelScope.launch(Dispatchers.IO) {
        val result = sonyControlRepository.getPlayingContentInfo()
        _playingContentInfo.postValue(result)
        updateCurrentProgram(result.uri)
    }

    fun fetchProgramList() = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.fetchProgramList()
    }

    fun setPlayContent(uri: String) = viewModelScope.launch(Dispatchers.IO) {
        if (sonyControlRepository.setPlayContent(uri) == SonyControlRepository.SUCCESS_CODE) {
            updateCurrentProgram(uri)
        }
    }

    fun setAndFetchPlayContent(uri: String) = viewModelScope.launch(Dispatchers.IO) {
        if (sonyControlRepository.setPlayContent(uri) == SonyControlRepository.SUCCESS_CODE) {
            fetchPlayingContentInfo()
        }
    }

    fun wakeOnLan() = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.wakeOnLan()
    }

    /*fun fetchInterfaceInformation(host: String) = viewModelScope.launch(Dispatchers.IO) {
        _interfaceInformation.postValue(sonyControlRepository.getInterfaceInformation(host))
    }*/

    fun fetchInterfaceInformation(host: String) = viewModelScope.launch(Dispatchers.IO) {
        _interfaceInformation.postValue(sonyControlRepository.getInterfaceInformation(host))
    }

    fun fetchSonyIpAndDeviceList() = viewModelScope.launch(Dispatchers.IO) {
        val list = sonyControlRepository.getSonyIpAndDeviceList()
       /* val list = listOf<SSDP.IpDeviceItem>(
            SSDP.IpDeviceItem("192.168.178.27", "BRAVIA KDL-50W656A"),
            SSDP.IpDeviceItem("192.168.178.37", "BRAVIA KDL-40W250"))*/
        Timber.d("fetchSonyIpAndDeviceList(): $list")
        _sonyIpAndDeviceList.postValue(list)
    }

    fun registerControl() {
        registerControl(null)
    }

    fun registerControl(challenge: String?) = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.registerControl(challenge)
    }

    fun postRegistrationFetches() = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.fetchRemoteControllerInfo()
        sonyControlRepository.fetchSourceList()
        sonyControlRepository.setWolMode(true)
        sonyControlRepository.fetchWolMode()
        sonyControlRepository.fetchSystemInformation()
        sonyControlRepository.fetchProgramList()
    }

    fun setPowerSavingMode(mode: String) = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.setPowerSavingMode(mode)
    }

    fun setSelectedChannelMapProgramUri(channelName: String?, programUri: String?) {
        Timber.d("setSelectedChannelMapProgramUri()")
        selectedChannelMapProgramUri.value = programUri
        getSelectedControl()!!.channelProgramMap[channelName!!] = programUri!!
        refreshDerivedVariablesForSelectedControl()
        sonyControlRepository.saveControls()
    }

    fun sendIRRCCByName(name: String) = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.sendIRCC(getSelectedControl()!!.commandList[name]!!)
    }

    internal fun performFuzzyMatchForChannelList() {
        Timber.d("performFuzzyMatchForChannelList()")
        if (channelNameList.isNotEmpty() && programTitleList.isNotEmpty() && getSelectedControl()?.programList != null) {
            for (channelName in channelNameList) {
                val index1 = ProgramFuzzyMatch.matchOne(channelName, programTitleList, true)
                if (index1 >= 0) {
                    val programUri = getSelectedControl()!!.programList[index1].uri
                    getSelectedControl()!!.channelProgramMap[channelName] = programUri
                }
            }
            sonyControlRepository.saveControls()
        }
    }

    internal fun clearMapping() {
        Timber.d("clearMapping()")
        if (getSelectedControl()?.channelProgramMap != null) {
            for (channelName in channelNameList) {
                getSelectedControl()!!.channelProgramMap[channelName] = ""
            }
            sonyControlRepository.saveControls()
        }
    }
}
