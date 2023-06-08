package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import android.content.Context
import androidx.lifecycle.*
import dagger.hilt.EntryPoint
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.domain.*
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import timber.log.Timber
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.collections.set
@HiltViewModel
class SonyControlViewModel  @Inject constructor(private val sonyControlRepository: SonyControlRepository) :ViewModel() {
    // TODO: Implement the ViewModel

    //val requestErrorMessage = sonyControlRepository.responseMessage
    //val registrationResult = sonyControlRepository.registrationResult

    var isCreated: Boolean = false

    private var _selectedSonyControl = MutableLiveData<SonyControl>()
    val selectedSonyControl: LiveData<SonyControl>
        get() = _selectedSonyControl

    private var _sonyControls = MutableLiveData<SonyControls>()
    val sonyControls: LiveData<SonyControls>
        get() = _sonyControls
    var addedControlHostAddress: String = ""

    private var channelSearchQuery: String = ""

    private var _noControls = MutableLiveData<Int>(0)
    val noControls: LiveData<Int>
        get() = _noControls

    //val filteredChannelList: MutableState<List<SonyChannel>> = mutableStateOf(emptyList<SonyChannel>())
    val filteredChannelList = MutableLiveData<List<SonyChannel>>()
    //val filteredChannelList: LiveData<List<SonyChannel>>
    //    get() = _filteredChannelList

    // derived variables for selected control
    private var channelMap: MutableMap<String, String> = HashMap()
    private var uriChannelMap: MutableMap<String, SonyChannel> = HashMap()
    private var channelNameList: MutableList<String> = ArrayList()
    var selectedChannelName: String = ""

    private var currentChannelUri: String = ""
    var lastChannelUri: String = ""

    var tvbChannelNameList: MutableList<String> = ArrayList()
    private var filteredTvbChannelNameList = MutableLiveData<List<String>>()
    var tvbChannelNameSearchQuery: String? = null

    var selectedChannelMapChannelUri = MutableLiveData<String?>()

    private val _playingContentInfo = MutableLiveData<PlayingContentInfo>()
    val playingContentInfo: LiveData<PlayingContentInfo> = _playingContentInfo

    private val _sonyIpAndDeviceList = MutableLiveData<List<SSDP.IpDeviceItem>>()
    val sonyIpAndDeviceList: LiveData<List<SSDP.IpDeviceItem>> = _sonyIpAndDeviceList

    private val _interfaceInformation = MutableLiveData<Resource<InterfaceInformationResponse>>()
    val interfaceInformation: LiveData<Resource<InterfaceInformationResponse>> =
        _interfaceInformation

    private val _powerStatus = MutableLiveData<Resource<PowerStatusResponse>>()
    val powerStatus: LiveData<Resource<PowerStatusResponse>> = _powerStatus
    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.setValue(this.value)
    }
    init {
        Timber.d("init $sonyControlRepository")
        _playingContentInfo.value = PlayingContentInfo()
        _sonyControls = sonyControlRepository.sonyControls
        _selectedSonyControl = sonyControlRepository.selectedSonyControl
        _noControls.value = sonyControlRepository.sonyControls.value!!.controls.size;
        onSelectedIndexChange()
        //filterChannelNameList("")
    }

    private fun updateCurrentChannel(uri: String) {
        if (uri.isNotEmpty() && uriChannelMap.containsKey(uri) && currentChannelUri != uri) {
            lastChannelUri = currentChannelUri
            currentChannelUri = uri
        }
    }

    fun addSampleControl(context: Context, host : String) {
        addedControlHostAddress = host

        val sampleSonyControl = SonyControl.fromJson(
            context.assets.open("SonyControl_sample.json").bufferedReader()
                .use { it.readText() }.replace("android sample",host))
        addControl(sampleSonyControl)
    }
    fun addControl(control: SonyControl) {
        Timber.d("addControl(): $control")
        Timber.d("sonyControlViewModel: $this")
        sonyControlRepository.addControl(control)
        _noControls.postValue(sonyControls.value!!.controls.size)
        Timber.d("_noControls: $_noControls")
        //_sonyControls.notifyObserver()
    }

    fun deleteSelectedControl() {
        //if (sonyControlRepository.removeControl(sonyControls.value!!.selected)) onSelectedIndexChange()
        if (sonyControlRepository.removeControl(sonyControls.value!!.selected)) {
            onSelectedIndexChange()
            _noControls.postValue(sonyControls.value!!.controls.size)
        }
    }

    fun setSelectedControlIndex(index: Int) {
        Timber.d("setSelectedControlIndex(index: $index)")
        if (sonyControlRepository.setSelectedControlIndex(index)) onSelectedIndexChange()
    }

    fun onSelectedIndexChange() {
        //Timber.d("onSelectedIndexChange(): ${_sonyControls.value!!.selected}")
        lastChannelUri = ""
        currentChannelUri = ""
        refreshDerivedVariablesForSelectedControl()
        filterChannelList("")
        filterTvbChannelNameList(null)
    }

    private fun refreshDerivedVariablesForSelectedControl() {
        Timber.d("refreshDerivedVariablesForSelectedControl()")
        selectedSonyControl.value?.let { control ->
            // helper collections for channel
            channelNameList.clear()
            uriChannelMap.clear()
            for (channel in control.channelList) {
                channelNameList.add(channel.title)
                uriChannelMap[channel.uri] = channel
            }
            tvbChannelNameList.clear()
            for (mappedChannelName in control.channelMap.keys) {
                tvbChannelNameList.add(mappedChannelName)
                val channelUri = control.channelMap[mappedChannelName]
                if (channelUri != null && control.channelUriMap!!.containsKey(
                        channelUri
                    )
                ) {
                    channelMap[channelUri] = mappedChannelName
                }
            }
        }
    }

    fun filterChannelList(query: String) {
        Timber.d("filter channel list query='$query' ")
        selectedSonyControl.value?.let { control ->
            channelSearchQuery = query
            val filteredList = ArrayList<SonyChannel>()
            control.channelList.forEach { channel ->
                if (channelSearchQuery.isNullOrEmpty() || channel.title.contains(
                        channelSearchQuery!!,
                        true
                    )
                ) {
                    filteredList.add(channel)
                }
                filteredChannelList.value = filteredList
            }
        }
    }

    fun getChannelSearchQuery(): String {
        return channelSearchQuery
    }

    fun getChannelForUri(uri: String): String {
        return channelMap[uri] ?: ""
    }

    fun getSonyChannelForTvbChannelName(tvbChannelName: String): SonyChannel? {
        val uri = channelMap[tvbChannelName]
        val sonyChannel = uriChannelMap[uri]
        return sonyChannel
    }

    fun getFilteredTvbChannelNameList(): LiveData<List<String>> {
        // get list of channel names from preference
        if (filteredTvbChannelNameList.value == null) {
            filterTvbChannelNameList("")
        }
        return filteredTvbChannelNameList
    }

    fun filterTvbChannelNameList(query: String?) {
        Timber.d("filter channel name list query='$query' ")
        tvbChannelNameSearchQuery = query
        filteredTvbChannelNameList.value = tvbChannelNameList.filter { c ->
            tvbChannelNameSearchQuery.isNullOrEmpty() || c.contains(
                tvbChannelNameSearchQuery!!,
                true
            )
        }
    }

    internal fun createChannelUriMatchList(
        channelName: String?,
        query: String?
    ): ArrayList<String> {
        Timber.d("createChannelUriMatchList()")
        val channelUriMatchList: ArrayList<String> = ArrayList()
        selectedSonyControl.value?.let { control ->
            if (channelNameList.isNotEmpty()) {
                var matchTopSet: MutableSet<Int> = LinkedHashSet()
                if (query == null || query.isEmpty()) {
                    matchTopSet.addAll(
                        ChannelNameFuzzyMatch.matchTop(
                            channelName!!,
                            channelNameList,
                            30,
                            true
                        )
                    )
                } else {
                    for (i in channelNameList.indices) {
                        val channel = channelNameList[i]
                        if (channel.toLowerCase().contains(query.toLowerCase())) {
                            matchTopSet.add(i)
                            if (matchTopSet.size == 30) break
                        }
                    }
                }
                if (!control.channelList.isNullOrEmpty()) {
                    matchTopSet.forEach { channelUriMatchList.add(control.channelList[it].uri) }
                }
                sonyControlRepository.saveControls()
            }
        }
        return channelUriMatchList
    }

    fun fetchPlayingContentInfo() = viewModelScope.launch(Dispatchers.IO) {
        val response = sonyControlRepository.getPlayingContentInfo()
        if (response is Resource.Success) {
            val value: PlayingContentInfo = if(response.data != null) response.data.asDomainModel() else PlayingContentInfo()
            _playingContentInfo.postValue(value)
            updateCurrentChannel(value.uri)
        }
    }

    fun fetchChannelList() = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.fetchChannelList()
    }

    fun setPlayContent(uri: String) = viewModelScope.launch(Dispatchers.IO) {
        if (sonyControlRepository.setPlayContent(uri) == SonyControlRepository.SUCCESS_CODE) {
            updateCurrentChannel(uri)
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

    fun fetchPowerStatus(host: String) {
        _powerStatus.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.IO) {
            Timber.d("fetchPowerStatus: postValue")
            _powerStatus.postValue(sonyControlRepository.getPowerStatus(host))
        }
    }

    val powerStatus2: LiveData<Resource<PowerStatusResponse>> =
        liveData<Resource<PowerStatusResponse>> {
            emit(Resource.Loading())
            //sonyControlRepository.getPowerStatus(host)
            viewModelScope.launch(Dispatchers.IO) {
                emit(
                    sonyControlRepository.getPowerStatus(
                        addedControlHostAddress
                    )
                )
            }
        }

    fun fetchSystemInformation() = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.fetchSystemInformation()
    }

    fun fetchSonyIpAndDeviceList() = viewModelScope.launch(Dispatchers.IO) {
        val list = sonyControlRepository.getSonyIpAndDeviceList()
        /* val list = listOf<SSDP.IpDeviceItem>(
             SSDP.IpDeviceItem("192.168.178.27", "BRAVIA KDL-50W656A"),
             SSDP.IpDeviceItem("192.168.178.37", "BRAVIA KDL-40W250"))*/
        Timber.d("fetchSonyIpAndDeviceList(): $list")
        _sonyIpAndDeviceList.postValue(list)
    }

    fun registerControl(control: SonyControl) {
        registerControl(control, null)
    }

    fun registerControl(control: SonyControl, challenge: String?) =
        viewModelScope.launch(Dispatchers.IO) {
            sonyControlRepository.registerControl(control, challenge)
        }

    fun postRegistrationFetches() = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.fetchRemoteControllerInfo()
        sonyControlRepository.fetchSourceList()
        sonyControlRepository.setWolMode(true)
        sonyControlRepository.fetchWolMode()
        sonyControlRepository.fetchSystemInformation()
        sonyControlRepository.fetchChannelList()
    }

    fun setPowerSavingMode(mode: String) = viewModelScope.launch(Dispatchers.IO) {
        sonyControlRepository.setPowerSavingMode(mode)
    }

    fun sendIRRCCByName(name: String) = viewModelScope.launch(Dispatchers.IO) {
        selectedSonyControl.value?.let { control ->
            val code = control.commandList[name]
            if (!code.isNullOrEmpty()) {
                sonyControlRepository.sendIRCC(code)
            }
        }
    }

    fun setSelectedChannelMapChannelUri(channelName: String?, channelUri: String?) {
        Timber.d("setSelectedChannelMapChannelUri()")
        selectedSonyControl.value?.let { control ->
            selectedChannelMapChannelUri.value = channelUri
            control.channelMap[channelName!!] = channelUri!!
            refreshDerivedVariablesForSelectedControl()
            sonyControlRepository.saveControls()
        }
    }

    internal fun performFuzzyMatchForChannelList() {
        Timber.d("performFuzzyMatchForChannelList()")
        selectedSonyControl.value?.let { control ->
            if (tvbChannelNameList.isNotEmpty() && channelNameList.isNotEmpty()) {
                for (channelName in tvbChannelNameList) {
                    val index1 = ChannelNameFuzzyMatch.matchOne(channelName, channelNameList, true)
                    if (index1 >= 0) {
                        control.channelMap[channelName] = control.channelList[index1].uri
                    }
                }
                sonyControlRepository.saveControls()
            }
        }
    }

    internal fun clearMapping() {
        Timber.d("clearMapping()")
        selectedSonyControl.value?.let { control ->
            for (channelName in tvbChannelNameList) {
                control.channelMap[channelName] = ""
            }
            sonyControlRepository.saveControls()
        }
    }

    fun removeUTFCharacters(data: String): String {
        val p: Pattern = Pattern.compile("\\\\u(\\p{XDigit}{4})")
        val m: Matcher = p.matcher(data)
        val buf = StringBuffer(data.length)
        while (m.find()) {
            val ch: String = m.group(1).toInt(16).toChar().toString()
            m.appendReplacement(buf, Matcher.quoteReplacement(ch))
        }
        m.appendTail(buf)
        return buf.toString()
    }
}
