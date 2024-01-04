package org.andan.android.tvbrowser.sonycontrolplugin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.andan.android.tvbrowser.sonycontrolplugin.network.InterfaceInformationResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.PowerStatusResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.Resource
import org.andan.android.tvbrowser.sonycontrolplugin.network.SSDP
import org.andan.android.tvbrowser.sonycontrolplugin.network.asDomainModel
import org.andan.android.tvbrowser.sonycontrolplugin.repository.SonyControlRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SonyControlViewModel @Inject constructor(private val sonyControlRepository: SonyControlRepository) :
    ViewModel() {
    // TODO: Implement the ViewModel

    //val requestErrorMessage = sonyControlRepository.responseMessage
    //val registrationResult = sonyControlRepository.registrationResult

    private var _selectedSonyControl = MutableLiveData<SonyControl>()
    val selectedSonyControl: LiveData<SonyControl>
        get() = _selectedSonyControl

    private var _sonyControls = MutableLiveData<SonyControls>()

    val controlList = sonyControlRepository.sonyControlsFlow
    val sonyControls: LiveData<SonyControls>
        get() = _sonyControls
    var addedControlHostAddress: String = ""

    private var uriChannelMap: MutableMap<String, SonyChannel> = HashMap()

    private var currentChannelUri: String = ""
    var lastChannelUri: String = ""

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
        /*        _playingContentInfo.value = PlayingContentInfo()
                _sonyControls = sonyControlRepository.sonyControls
                _selectedSonyControl = sonyControlRepository.selectedSonyControl
                _noControls.value = sonyControlRepository.sonyControls.value!!.controls.size;
                onSelectedIndexChange()*/
        //filterChannelNameList("")
    }

    private fun updateCurrentChannel(uri: String) {
        if (uri.isNotEmpty() && uriChannelMap.containsKey(uri) && currentChannelUri != uri) {
            lastChannelUri = currentChannelUri
            currentChannelUri = uri
        }
    }

    fun fetchPlayingContentInfo() = viewModelScope.launch(Dispatchers.IO) {
        val response = sonyControlRepository.getPlayingContentInfo()
        if (response is Resource.Success) {
            val value: PlayingContentInfo =
                if (response.data != null) response.data.asDomainModel() else PlayingContentInfo()
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
        //sonyControlRepository.fetchRemoteControllerInfo()
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
            val code = control.commandMap[name]
            if (!code.isNullOrEmpty()) {
                sonyControlRepository.sendIRCC(code)
            }
        }
    }
}
