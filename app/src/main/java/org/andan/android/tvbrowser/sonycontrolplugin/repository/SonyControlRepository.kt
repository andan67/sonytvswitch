package org.andan.android.tvbrowser.sonycontrolplugin.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyProgram
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_ERROR_FATAL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_ERROR_NON_FATAL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_REQUIRES_CHALLENGE_CODE
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_SUCCESSFUL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_UNAUTHORIZED
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyServiceUtil.apiCall
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.util.regex.Pattern
import javax.inject.Inject

class SonyControlRepository @Inject constructor(
    val client: OkHttpClient,
    val api: SonyService,
    val preferenceStore: ControlPreferenceStore
) {
    var sonyControls = MutableLiveData<SonyControls>()
    var selectedSonyControl = MutableLiveData<SonyControl>()
    private val sonyServiceContext = SonyControlApplication.get().appComponent.sonyServiceContext()

    companion object {
        const val SUCCESS_CODE = 0
        const val ERROR_CODE = -1
    }

    init {
        Timber.d("init")
        sonyControls.value = preferenceStore.loadControls()
        sonyServiceContext.sonyService = api
        selectedSonyControl.value = getSelectedControl()
        onSonyControlsChange()
    }

    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>>
        get() = _responseMessage

    private val _registrationResult = MutableLiveData<Event<RegistrationStatus>>()
    val registrationResult: LiveData<Event<RegistrationStatus>>
        get() = _registrationResult

    private fun onSonyControlsChange() {
        sonyServiceContext.sonyService = api
        if (selectedSonyControl.value != null) {
            Timber.d("onSonyControlsChange(): ${selectedSonyControl.value}")
            sonyServiceContext.ip = selectedSonyControl.value!!.ip
            sonyServiceContext.uuid = selectedSonyControl.value!!.uuid
            sonyServiceContext.nickname = selectedSonyControl.value!!.nickname
            sonyServiceContext.devicename = selectedSonyControl.value!!.devicename
        }
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    private fun <T> MutableLiveData<T>.notifyObserverBackground() {
        this.postValue(this.value)
    }

    fun getSelectedControl(): SonyControl? {
        return if (sonyControls.value!!.selected >= 0 && sonyControls.value!!.selected <= sonyControls.value!!.controls.size - 1) {
            sonyControls.value!!.controls[sonyControls.value!!.selected]
        } else null
    }

    suspend fun setWolMode(enabled: Boolean) {
        val resource = systemService<Any>(getSelectedControl()!!.ip, JsonRpcRequest.setWolMode(enabled))
        if (resource.status == Status.ERROR) {
            _responseMessage.postValue(Event(resource.message))
        }
    }

    suspend fun fetchWolMode() {
        val resource = systemService<WolModeResponse>(getSelectedControl()!!.ip, JsonRpcRequest.getWolMode())
        if (resource.status == Status.SUCCESS) {
            getSelectedControl()!!.systemWolMode = resource.data!!.enabled
            saveControls(true)
        } else {
            _responseMessage.postValue(Event(resource.message))
        }
    }

    suspend fun setPowerSavingMode(mode: String) {
        val resource = systemService<Any>(getSelectedControl()!!.ip, JsonRpcRequest.setPowerSavingMode(mode))
        if (resource.status == Status.ERROR) {
            _responseMessage.postValue(Event(resource.message))
        }
    }


    suspend fun fetchRemoteControllerInfo() {
        val resource = systemService<Array<RemoteControllerInfoItemResponse>>(getSelectedControl()!!.ip, JsonRpcRequest.getRemoteControllerInfo())
        Timber.d("remoteControllerInfo(): ${getSelectedControl()!!.toString()}")
        if (resource.status == Status.SUCCESS) {
            getSelectedControl()!!.commandList = LinkedHashMap()
            for (remoteControllerInfoItem in resource.data!!) {
                getSelectedControl()!!.commandList[remoteControllerInfoItem.name] = remoteControllerInfoItem.value
            }
            saveControls(true)
        } else {
            _responseMessage.postValue(Event(resource.message))
        }
    }

    suspend fun fetchSystemInformation() {
        val resource = systemService<SystemInformationResponse>(getSelectedControl()!!.ip, JsonRpcRequest.getSystemInformation())
        Timber.d("remoteControllerInfo(): ${getSelectedControl()!!.toString()}")
        if (resource.status == Status.SUCCESS) {
            getSelectedControl()!!.systemName = resource.data!!.name
            getSelectedControl()!!.systemProduct = resource.data!!.product
            getSelectedControl()!!.systemModel = resource.data!!.model
            getSelectedControl()!!.systemMacAddr = resource.data!!.macAddr
            saveControls(true)
        } else {
            _responseMessage.postValue(Event(resource.message))
        }
    }

    suspend fun getPlayingContentInfo(): PlayingContentInfo {
        return withContext(Dispatchers.IO) {
            val resource = avContentService<PlayingContentInfoResponse>(getSelectedControl()!!.ip, JsonRpcRequest.getPlayingContentInfo())
            if (resource.status == Status.ERROR) {
                _responseMessage.postValue(Event(resource.message))
                PlayingContentInfo()
            } else {
                resource.data?.asDomainModel() ?: PlayingContentInfo()
            }
        }
    }

    suspend fun getInterfaceInformation(host: String): Resource<InterfaceInformationResponse> {
        return withContext(Dispatchers.IO) { systemService<InterfaceInformationResponse>(
                host,
                JsonRpcRequest.getInterfaceInformation())
        }
    }

    suspend fun setPlayContent(uri: String): Int {
        val resource = avContentService<Unit>(getSelectedControl()!!.ip, JsonRpcRequest.setPlayContent(uri))
        if (resource.status == Status.ERROR) {
            _responseMessage.postValue(Event(resource.message))
            return ERROR_CODE
        }
        return SUCCESS_CODE
    }

    suspend fun fetchSourceList() {
        val resource = avContentService<Array<SourceListItemResponse>>(
            getSelectedControl()!!.ip, JsonRpcRequest.getSourceList("tv")
        )
        if (resource.status == Status.SUCCESS) {
            getSelectedControl()!!.sourceList = mutableListOf()
            for (sourceItem in resource.data!!) {
                if (sourceItem.source == "tv:dvbs") {
                    getSelectedControl()!!.sourceList.add(sourceItem.source + "#general")
                    getSelectedControl()!!.sourceList.add(sourceItem.source + "#preferred")
                } else {
                    getSelectedControl()!!.sourceList.add(sourceItem.source)
                }
            }
            saveControls(true)
        } else {
            _responseMessage.postValue(Event(resource.message))
        }
    }

    suspend fun fetchProgramList() {
        Timber.d("fetchProgramList(): ${getSelectedControl()!!.sourceList}")
        if (getSelectedControl()!!.sourceList.isNullOrEmpty()) {
            fetchSourceList()
        }
        getSelectedControl()!!.programList.clear()
        val programList = mutableListOf<SonyProgram>()
        if (!getSelectedControl()!!.sourceList.isNullOrEmpty()) {
            for (sonySource in getSelectedControl()!!.sourceList) {
                // get programs in pages
                var stidx = 0
                var count = 0
                while (fetchTvContentList(
                        sonySource,
                        stidx,
                        SonyControl.PAGE_SIZE,
                        programList
                    ).let {
                        count = it; it > 0
                    }
                ) {
                    stidx += SonyControl.PAGE_SIZE
                }
                // Break loop over source in case of error
                if (count == -1) {
                    Timber.d("fetchProgramList(): error")
                }
            }
            getSelectedControl()!!.programList.clear()
            getSelectedControl()!!.programList.addAll(programList)
            saveControls(true)
            _responseMessage.postValue(Event("Fetched ${getSelectedControl()!!.programList.size} programs from TV"))
            Timber.d("fetchProgramList(): ${getSelectedControl()!!.programList.size}")
        }
    }

    private suspend fun fetchTvContentList(
        sourceType: String,
        stIdx: Int,
        cnt: Int,
        plist: MutableList<SonyProgram>
    ): Int {
        return withContext(Dispatchers.IO) {
            val sourceSplit = sourceType.split("#").toTypedArray()
            val source = sourceSplit[0]
            var type = ""
            if (sourceSplit.size > 1) type = sourceSplit[1]
            val resource = avContentService<Array<ContentListItemResponse>>(
                getSelectedControl()!!.ip, JsonRpcRequest.getContentList(
                    source,
                    stIdx,
                    cnt,
                    type
                )
            )
            if (resource.status == Status.SUCCESS) {
                for (sonyProgramResponse in resource.data!!) {
                    if (sonyProgramResponse.programMediaType.equals("tv", true)
                        && sonyProgramResponse.title != "." && !sonyProgramResponse.title.isEmpty()
                        && !sonyProgramResponse.title.contains("TEST")
                    ) {
                        val sonyProgram = sonyProgramResponse.asDomainModel()
                        sonyProgram.source = source
                        plist.add(sonyProgram)
                    }
                }
                resource.data!!.size
            } else {
                _responseMessage.postValue(Event(resource.message))
                -1
            }
        }
    }

    suspend fun registerControl(challenge: String?) {
        withContext(Dispatchers.IO) {
            selectedSonyControl.value?.let {
                Timber.d("registerControl(): ${sonyServiceContext.nickname}")
                if (challenge != null) {
                    sonyServiceContext.password = challenge
                }
                sonyServiceContext.preSharedKey = it.preSharedKey
                try {
                    val response = api.sonyRpcService(
                        "http://" + it.ip + SonyServiceUtil.SONY_ACCESS_CONTROL_ENDPOINT,
                        JsonRpcRequest.actRegister(it.nickname, it.devicename, it.uuid)
                    )
                    // update token
                    if (response.isSuccessful) {
                        val jsonRpcResponse = response.body()
                        if (jsonRpcResponse?.error != null) {
                            _registrationResult.postValue(Event(RegistrationStatus(
                                REGISTRATION_ERROR_NON_FATAL, jsonRpcResponse.error.asJsonArray.get(1).asString)))
                        } else if (!response.headers()["Set-Cookie"].isNullOrEmpty()) {
                            // get token from set cookie and store
                            val cookieString: String? = response.headers()["Set-Cookie"]
                            val pattern = Pattern.compile("auth=([A-Za-z0-9]+)")
                            val matcher = pattern.matcher(cookieString)
                            if (matcher.find()) {
                                preferenceStore.storeToken(it.uuid, "auth=" + matcher.group(1))
                            }
                            _registrationResult.postValue(Event(RegistrationStatus(REGISTRATION_SUCCESSFUL, "" )))
                        }
                    } else {
                        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            // Navigate to enter challenge code view
                            if(getSelectedControl()!!.preSharedKey.isNullOrEmpty() && challenge.isNullOrEmpty()) {
                                _registrationResult.postValue(Event(RegistrationStatus(REGISTRATION_REQUIRES_CHALLENGE_CODE, response.message())))
                                }
                            else _registrationResult.postValue(Event(RegistrationStatus(REGISTRATION_UNAUTHORIZED, response.message())))
                        } else {
                            _registrationResult.postValue(Event(RegistrationStatus(
                                REGISTRATION_ERROR_NON_FATAL, response.message())))
                        }
                    }
                } catch (se: SocketTimeoutException) {
                    Timber.e("Error: ${se.message}")
                    _registrationResult.postValue(Event(RegistrationStatus(REGISTRATION_ERROR_FATAL, se.message?: "Unknown failure")))
                }
                sonyServiceContext.password = ""
            }
        }
    }

    suspend fun sendIRCC(code: String) {
        withContext(Dispatchers.IO) {
            selectedSonyControl.value?.let {
                val requestBodyText =
                    SonyServiceUtil.SONY_IRCC_REQUEST_TEMPLATE.replace(
                        "<IRCCCode>",
                        "<IRCCCode>$code"
                    )

                val requestBody: RequestBody =
                    requestBodyText.toRequestBody("text/xml".toMediaTypeOrNull())
                Timber.d("sendIRCC: $requestBodyText")
                val response = api.sendIRCC(
                    "http://" + it.ip + SonyServiceUtil.SONY_IRCC_ENDPOINT,
                    requestBody
                )
                if (!response.isSuccessful) {
                    _responseMessage.postValue(Event(response.message()))
                }
            }
        }
    }

    suspend fun wakeOnLan() {
        withContext(Dispatchers.IO) {
            selectedSonyControl.value?.let {
                WakeOnLan.wakeOnLan(getSelectedControl()!!.ip, getSelectedControl()!!.systemMacAddr)
            }
        }
    }

    suspend fun getSonyIpAndDeviceList(): List<SSDP.IpDeviceItem> {
        return withContext(Dispatchers.IO) {
            SSDP.getSonyIpAndDeviceList()
        }
    }

    fun updateChannelMapsFromChannelNameList(channelNameList: List<String>) {
        var isUpdated = false
        for (control in sonyControls.value!!.controls) {
            Timber.d("updateChannelMapsFromChannelNameList: ${channelNameList.size} ${control.channelProgramMap.size}")
            for (channelName in channelNameList) {
                // create mapping entry for unmapped channels
                if (!control.channelProgramMap.containsKey(channelName)) {
                    Timber.d("updateChannelMapsFromChannelNameList: $channelName")
                    control.channelProgramMap[channelName] = ""
                    isUpdated = true
                }
                //ToDo: Handle deletion of channels
            }
        }
        if (isUpdated) saveControls()
        Timber.d("updateChannelMapsFromChannelNameList: finished")
    }

    fun saveControls() {
        saveControls(false)
    }

    private fun saveControls(fromBackground: Boolean) {
        preferenceStore.storeControls(sonyControls.value!!)
        if (fromBackground) {
            sonyControls.notifyObserverBackground()
            selectedSonyControl.postValue(getSelectedControl())
        } else {
            sonyControls.notifyObserver()
            selectedSonyControl.value = getSelectedControl()
        }
        onSonyControlsChange()
    }

    fun addControl(control: SonyControl) {
        sonyControls.value!!.controls.add(control)
        sonyControls.value!!.selected = sonyControls.value!!.controls.size - 1
        Timber.d("addControl: #${sonyControls.value!!.selected} $control")
        saveControls()
    }

    fun removeControl(index: Int): Boolean {
        if (index >= 0 && index < sonyControls.value!!.controls.size) {

            var newSelected = sonyControls.value!!.selected - 1
            if (sonyControls.value!!.selected == 0 && sonyControls.value!!.controls.size > 1) {
                newSelected = sonyControls.value!!.controls.size - 2
            }
            sonyControls.value!!.controls.removeAt(index)
            sonyControls.value!!.selected = newSelected
            saveControls()
            return true
        }
        return false
    }

    fun setSelectedControlIndex(index: Int): Boolean {
        if (index < sonyControls.value!!.controls.size) {
            sonyControls.value!!.selected = index
            saveControls()
            return true
        }
        return false
    }

    private suspend inline fun <reified T> avContentService(host: String, jsonRpcRequest: JsonRpcRequest): Resource<T> =
        apiCall {
            api.sonyRpcService(
                "http://" + host+ SonyServiceUtil.SONY_AV_CONTENT_ENDPOINT,
                jsonRpcRequest
            )
        }

    private suspend inline fun <reified T> systemService(host: String, jsonRpcRequest: JsonRpcRequest): Resource<T> =
        apiCall {
            api.sonyRpcService(
                "http://" + host + SonyServiceUtil.SONY_SYSTEM_ENDPOINT,
                jsonRpcRequest
            )
        }
}

open class Event<out T>(private val content: T? = null) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        content
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T? = content
}

class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(event: Event<T>?) {
        event?.getContentIfNotHandled()?.let { value ->
            onEventUnhandledContent(value)
        }
    }
}