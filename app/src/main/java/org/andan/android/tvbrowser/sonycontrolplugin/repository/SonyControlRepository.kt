package org.andan.android.tvbrowser.sonycontrolplugin.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.toImmutableList
import org.andan.android.tvbrowser.sonycontrolplugin.data.ChannelMapEntity
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlDao
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.data.mapper.SonyControlDataMapper
import org.andan.android.tvbrowser.sonycontrolplugin.data.mapper.SonyControlDomainMapper
import org.andan.android.tvbrowser.sonycontrolplugin.data.mapper.SonyControlWithChannelsDomainMapper
import org.andan.android.tvbrowser.sonycontrolplugin.di.ApplicationScope
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_ERROR_FATAL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_ERROR_NON_FATAL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_REQUIRES_CHALLENGE_CODE
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_SUCCESSFUL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_UNAUTHORIZED
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_UNKNOWN
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyServiceUtil.apiCall
import timber.log.Timber
import java.net.HttpURLConnection
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.SocketTimeoutException
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class
SonyControlRepository @Inject constructor(
    private val api: SonyService,
    private val preferenceStore: ControlPreferenceStore,
    private val sonyServiceContext: SonyServiceClientContext,
    private val controlDao: ControlDao,
    private val sonyControlDataMapper: SonyControlDataMapper,
    @ApplicationScope private val externalScope: CoroutineScope
) {
    //var sonyControls = MutableLiveData<SonyControls>()
    //var selectedSonyControl = MutableLiveData<SonyControl>()

    val activeSonyControl: Flow<SonyControl> = controlDao.getActiveControl().map { entity  ->  sonyControlDataMapper.mapControlEntity2Domain(entity)}
    val activeSonyControlWithChannels: Flow<SonyControl>
        = controlDao.getActiveControlWithChannels().map { entity  ->
        val control = sonyControlDataMapper.mapControlEntity2Domain(entity)
        // lazy creation of uriSonyChannelMap
        control.uriSonyChannelMap
        Timber.d("activeSonyControlWithChannels  ${control.channelMap["3sat"]} ${control.uriSonyChannelMap}")
        control
        }
    val sonyControls: Flow<List<SonyControl>> =
        controlDao.getControls().map { entityList -> entityList.map { entityElement -> sonyControlDataMapper.mapControlEntity2Domain(entityElement)  } }
    //else entityList.map { entityElement -> sonyControlDomainMapper.map(entityElement)  } }

    companion object {
        const val SUCCESS_CODE = 0
        const val ERROR_CODE = -1
    }

    init {
        Timber.d("init")
        // for backward compatibility
        //val sonyControlsFromPreferenceStore = preferenceStore.loadControls()
        val job = externalScope.launch {
            val numberOfControls = controlDao.getNumberOfControls()
            Timber.d("numberOfControls: ${numberOfControls}");
/*            controlDao.getControls().collectLatest { entityList ->
                Timber.d("entitList.size: ${entityList.size}");
                val sonyControlsFromPreferenceStore = preferenceStore.loadMockControls()
                if (!sonyControlsFromPreferenceStore.controls.isEmpty()) {
                    Timber.d("Insert from preference store")
                    controlDao.insertFromSonyControls(sonyControlsFromPreferenceStore)
                }
            }*/
                /*            if(sonyControls.lastOrNull() == null) {
                val sonyControlsFromPreferenceStore = preferenceStore.loadMockControls()
                if (!sonyControlsFromPreferenceStore.controls.isEmpty()) {
                    Timber.d("Insert from preference store")
                    controlDao.insertFromSonyControls(sonyControlsFromPreferenceStore)
                }
            }*/
            /*sonyControls.collectLatest { controls ->
                if (controls.isEmpty()) {
                    val sonyControlsFromPreferenceStore = preferenceStore.loadMockControls()
                    if (!sonyControlsFromPreferenceStore.controls.isEmpty()) {
                        Timber.d("Insert from preference store")
                        controlDao.insertFromSonyControls(sonyControlsFromPreferenceStore)
                    }
                }
            }*/
        }
        //job.cancel()
        Timber.d("end init")
        sonyServiceContext.sonyService = api
    }

    suspend fun setActiveControl(uuid: String) {
        controlDao.setActiveControl(uuid)
    }

/*    private val _responseMessage = MutableLiveData<Event<String>>()
    val responseMessage: LiveData<Event<String>>
        get() = _responseMessage*/

    private fun onSonyControlsChange() {
        Timber.d("onSonyControlsChange()")
        //setSonyServiceContextForControl(selectedSonyControl.value)
        //sonyControls.notifyObserverBackground()
    }

    fun setSonyServiceContextForControl(control: SonyControl?) {
        sonyServiceContext.sonyService = api
        if (control != null) {
            sonyServiceContext.ip = control.ip
            sonyServiceContext.uuid = control.uuid
            sonyServiceContext.nickname = control.nickname
            sonyServiceContext.devicename = control.devicename
            sonyServiceContext.preSharedKey = control.preSharedKey ?: ""
        }
    }

    fun getSelectedControl(): SonyControl? {
/*        return if (sonyControls.value!!.selected >= 0 && sonyControls.value!!.selected <= sonyControls.value!!.controls.size - 1) {
            sonyControls.value!!.controls[sonyControls.value!!.selected]
        } else null*/
        return null
    }

    suspend fun setWolMode(enabled: Boolean) {
        getSelectedControl()?.let { control ->
            val resource =
                systemService<Any>(control.ip, JsonRpcRequest.setWolMode(enabled))
            if (resource.status == Status.ERROR) {
                //_responseMessage.postValue(Event(resource.message))
            }
        }
    }

    suspend fun fetchWolMode() {
        getSelectedControl()?.let { control ->
            val resource =
                systemService<WolModeResponse>(
                    control.ip,
                    JsonRpcRequest.getWolMode()
                )
            if (resource.status == Status.SUCCESS) {
                control.systemWolMode = resource.data!!.enabled
                saveControls(true)
            } else {
                //_responseMessage.postValue(Event(resource.message))
            }
        }
    }

    suspend fun setPowerSavingMode(mode: String) {
        getSelectedControl()?.let { control ->
            val resource =
                systemService<Any>(
                    control.ip,
                    JsonRpcRequest.setPowerSavingMode(mode)
                )
            if (resource.status == Status.ERROR) {
                //_responseMessage.postValue(Event(resource.message))
            }
        }
    }


    suspend fun fetchRemoteControllerInfo() {
        getSelectedControl()?.let { control ->
            val resource = systemService<Array<RemoteControllerInfoItemResponse>>(
                control.ip,
                JsonRpcRequest.getRemoteControllerInfo()
            )
            Timber.d("remoteControllerInfo(): ${control.toString()}")
            if (resource.status == Status.SUCCESS) {
                control.commandList = LinkedHashMap()
                for (remoteControllerInfoItem in resource.data!!) {
/*                    control.commandList[remoteControllerInfoItem.name] =
                        remoteControllerInfoItem.value*/
                }
                saveControls(true)
            } else {
                //_responseMessage.postValue(Event(resource.message))
            }
        }
    }

    suspend fun fetchSystemInformation() {
        getSelectedControl()?.let { control ->
            val resource = systemService<SystemInformationResponse>(
                control.ip,
                JsonRpcRequest.getSystemInformation()
            )
            Timber.d("remoteControllerInfo(): ${control.toString()}")
            if (resource.status == Status.SUCCESS) {
                control.systemName = resource.data!!.name
                control.systemProduct = resource.data!!.product
                control.systemModel = resource.data!!.model
                control.systemMacAddr = resource.data!!.macAddr
                saveControls(true)
            } else {
                //_responseMessage.postValue(Event(resource.message))
            }
        }
    }


    suspend fun getPlayingContentInfo(): Resource<PlayingContentInfoResponse> {
        getSelectedControl()?.let { control ->
            return withContext(Dispatchers.IO) {
                avContentService<PlayingContentInfoResponse>(
                    control.ip,
                    JsonRpcRequest.getPlayingContentInfo()
                )
            }
        }
        return Resource.Error("No control defined", code = HTTP_INTERNAL_ERROR)
    }

    suspend fun getInterfaceInformation(host: String): Resource<InterfaceInformationResponse> {
        return withContext(Dispatchers.IO) {
            systemService<InterfaceInformationResponse>(
                host,
                JsonRpcRequest.getInterfaceInformation()
            )
        }
    }

    suspend fun getPowerStatus(host: String): Resource<PowerStatusResponse> {
        return withContext(Dispatchers.IO) {
            systemService<PowerStatusResponse>(
                host,
                JsonRpcRequest.getPowerStatus()
            )
        }
    }

    suspend fun setPlayContent(uri: String): Int {
        getSelectedControl()?.let { control ->
            val resource =
                avContentService<Unit>(
                    control.ip,
                    JsonRpcRequest.setPlayContent(uri)
                )
            if (resource.status == Status.ERROR) {
                //_responseMessage.postValue(Event(resource.message))
                return ERROR_CODE
            }
            return SUCCESS_CODE
        }
        return ERROR_CODE
    }

    suspend fun fetchSourceList() {
        getSelectedControl()?.let { control ->
            val resource = avContentService<Array<SourceListItemResponse>>(
                control.ip, JsonRpcRequest.getSourceList("tv")
            )
            if (resource.status == Status.SUCCESS) {
               /* control.sourceList = mutableListOf()
                for (sourceItem in resource.data!!) {
                    if (sourceItem.source == "tv:dvbs") {
                        control.sourceList.add(sourceItem.source + "#general")
                        control.sourceList.add(sourceItem.source + "#preferred")
                    } else {
                        control.sourceList.add(sourceItem.source)
                    }
                }*/
                saveControls(true)
            } else {
                //_responseMessage.postValue(Event(resource.message))
            }
        }
    }

    suspend fun fetchChannelList(): Boolean {
        getSelectedControl()?.let { control ->
            Timber.d("fetchChannelList(): ${control.sourceList}")
            if (control.sourceList.isNullOrEmpty()) {
                fetchSourceList()
            }
            val channelList = mutableListOf<SonyChannel>()
            if (!control.sourceList.isNullOrEmpty()) {
                for (sonySource in control.sourceList) {
                    // get channels in pages
                    var stidx = 0
                    var count = 0
                    while (fetchTvContentList(
                            control.ip,
                            sonySource,
                            stidx,
                            SonyControl.PAGE_SIZE,
                            channelList
                        ).let {
                            count = it; it > 0
                        }
                    ) {
                        stidx += SonyControl.PAGE_SIZE
                    }
                    // Break loop over source in case of error
                    if (count == -1) {
                        Timber.d("fetchChannelList(): error")
                        return false
                    }
                }
                control.channelList = channelList.toImmutableList()
                saveControls(true)
                //_responseMessage.postValue(Event("Fetched ${control.channelList.size} channels from TV"))
                Timber.d("fetchChannelList(): ${control.channelList.size}")
                return true
            }
        }
        return false
    }

    private suspend fun fetchTvContentList(
        ip: String,
        sourceType: String,
        stIdx: Int,
        cnt: Int,
        plist: MutableList<SonyChannel>
    ): Int {
        return withContext(Dispatchers.IO) {
            val sourceSplit = sourceType.split("#").toTypedArray()
            val source = sourceSplit[0]
            var type = ""
            if (sourceSplit.size > 1) type = sourceSplit[1]
            val resource = avContentService<Array<ContentListItemResponse>>(
                ip, JsonRpcRequest.getContentList(
                    source,
                    stIdx,
                    cnt,
                    type
                )
            )
            if (resource.status == Status.SUCCESS) {
                for (sonyChannelResponse in resource.data!!) {
                    if (sonyChannelResponse.programMediaType.equals("tv", true)
                        && sonyChannelResponse.title != "." && !sonyChannelResponse.title.isEmpty()
                        && !sonyChannelResponse.title.contains("TEST")
                    ) {
                        val sonyChannel = sonyChannelResponse.asDomainModel()
                        sonyChannel.source = source
                        plist.add(sonyChannel)
                    }
                }
                resource.data!!.size
            } else {
                //_responseMessage.postValue(Event(resource.message))
                -1
            }
        }
    }

    suspend fun registerControl(control: SonyControl, challenge: String?): RegistrationStatus {
        return withContext(Dispatchers.IO) {
            var registrationStatus: RegistrationStatus =
                RegistrationStatus(REGISTRATION_UNKNOWN, "")
            Timber.d("registerControl(): ${control.nickname}")
            // set context
            setSonyServiceContextForControl(control)
            if (challenge != null) {
                sonyServiceContext.password = challenge
            }
            //sonyServiceContext.preSharedKey = it.preSharedKey?: ""
            // indicates whether pre-shared key is used
            val isPSK = control.preSharedKey.isNotEmpty()
            try {
                val response = if (!isPSK) {
                    // register control if no pre-shared key is defined
                    api.sonyRpcService(
                        "http://" + control.ip + SonyServiceUtil.SONY_ACCESS_CONTROL_ENDPOINT,
                        JsonRpcRequest.actRegister(
                            control.nickname,
                            control.devicename,
                            control.uuid
                        )
                    )
                } else {
                    // make authenticated request as validity check
                    api.sonyRpcService(
                        "http://" + control.ip + SonyServiceUtil.SONY_SYSTEM_ENDPOINT,
                        JsonRpcRequest.getSystemInformation()
                    )
                }
                // update token
                if (response.isSuccessful) {
                    val jsonRpcResponse = response.body()
                    if (jsonRpcResponse?.error != null) {
                        registrationStatus = RegistrationStatus(
                            REGISTRATION_ERROR_NON_FATAL,
                            jsonRpcResponse.error.asJsonArray.get(1).asString
                        )
                    } else if (!isPSK && !response.headers()["Set-Cookie"].isNullOrEmpty()) {
                        // get token from set cookie and store
                        val cookieString: String? = response.headers()["Set-Cookie"]
                        val pattern = Pattern.compile("auth=([A-Za-z0-9]+)")
                        val matcher = pattern.matcher(cookieString)
                        if (matcher.find()) {
                            preferenceStore.storeToken(control.uuid, "auth=" + matcher.group(1))
                        }
                        registrationStatus = RegistrationStatus(REGISTRATION_SUCCESSFUL, "")
                    } else if (isPSK) {
                        registrationStatus = RegistrationStatus(REGISTRATION_SUCCESSFUL, "")
                    }
                } else {
                    if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED ||
                        response.code() == HttpURLConnection.HTTP_FORBIDDEN
                    ) {
                        // Navigate to enter challenge code view
                        if (!isPSK && challenge.isNullOrEmpty()) {
                            registrationStatus = RegistrationStatus(
                                REGISTRATION_REQUIRES_CHALLENGE_CODE,
                                response.message()
                            )
                        } else {
                            registrationStatus =
                                RegistrationStatus(REGISTRATION_UNAUTHORIZED, response.message())
                        }
                    } else {
                        registrationStatus = RegistrationStatus(
                            REGISTRATION_ERROR_NON_FATAL,
                            response.message()
                        )
                    }
                }
            } catch (se: SocketTimeoutException) {
                Timber.e("Error: ${se.message}")
                registrationStatus = RegistrationStatus(
                    REGISTRATION_ERROR_FATAL,
                    se.message ?: "Unknown failure"
                )
            } finally {
                // reset context
                //setSonyServiceContextForControl(selectedSonyControl.value)
                sonyServiceContext.password = ""
            }
            registrationStatus
        }
    }

    suspend fun sendIRCC(code: String) {
        withContext(Dispatchers.IO) {
            /*selectedSonyControl.value?.let { control ->
                val requestBodyText =
                    SonyServiceUtil.SONY_IRCC_REQUEST_TEMPLATE.replace(
                        "<IRCCCode>",
                        "<IRCCCode>$code"
                    )

                val requestBody: RequestBody =
                    requestBodyText.toRequestBody("text/xml".toMediaTypeOrNull())
                Timber.d("sendIRCC: $requestBodyText")
                try {
                    val response = api.sendIRCC(
                        "http://" + control.ip + SonyServiceUtil.SONY_IRCC_ENDPOINT,
                        requestBody
                    )
                    if (!response.isSuccessful) {
                        _responseMessage.postValue(Event(response.message()))
                    }
                } catch (se: SocketTimeoutException) {
                    Timber.e("Error: ${se.message}")
                }
            }*/
        }
    }

    suspend fun wakeOnLan() {
        withContext(Dispatchers.IO) {
            try {
                /*selectedSonyControl.value?.let { control ->
                    WakeOnLan.wakeOnLan(control.ip, control.systemMacAddr) }*/
            } catch (se: SocketTimeoutException) {
                Timber.e("Error: ${se.message}")
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
        /*for (control in sonyControls.value!!.controls) {
            Timber.d("updateChannelMapsFromChannelNameList: ${channelNameList.size} ${control.channelMap.size}")
            for (channelName in channelNameList) {
                // create mapping entry for unmapped channels
                if (!control.channelMap.containsKey(channelName)) {
                    Timber.d("updateChannelMapsFromChannelNameList: $channelName")
                    control.channelMap[channelName] = ""
                    isUpdated = true
                }
                //ToDo: Handle deletion of channels
            }
        }*/
        if (isUpdated) saveControls()
        Timber.d("updateChannelMapsFromChannelNameList: finished")
    }

    suspend fun insertSonyControls(sonyControls: SonyControls) {
        withContext(Dispatchers.IO) {
            controlDao.insertFromSonyControls(sonyControls)
        }
    }

    fun saveControls() {
        saveControls(false)
    }

    suspend fun saveChannelMap(uuid: String, channelMap: Map<String, String>) {
        var channelMapEntityList: MutableList<ChannelMapEntity> = ArrayList()
        channelMap.forEach {
            channelMapEntityList.add(ChannelMapEntity(uuid,it.key, it.value))
        }
        externalScope.launch {
            controlDao.setChannelMapForControl(channelMapEntityList, uuid)
        }
    }

    private fun saveControls(fromBackground: Boolean) {
        /*preferenceStore.storeControls(sonyControls.value!!)
        if (fromBackground) {
            //sonyControls.notifyObserverBackground()
            //selectedSonyControl.postValue(getSelectedControl())
        } else {
            //sonyControls.notifyObserver()
            //selectedSonyControl.value = getSelectedControl()
        }
        onSonyControlsChange()*/
    }


    suspend fun deleteControl(control: SonyControl) {
        externalScope.launch {
            val hasBeenActive = control.isActive
            Timber.d(" deleteControl ${control.uuid}")
            controlDao.deleteControl(control.uuid)
            Timber.d(" deleteControl hasBeenActive ${hasBeenActive}")
            if (hasBeenActive) {
                sonyControls.cancellable().collect {
                    val uuid = it.first().uuid
                    Timber.d(" deleteControl setActive ${uuid}")
                    setActiveControl(uuid)
                    currentCoroutineContext().cancel()
                }
            }
        }.join()
    }

    suspend fun addControl(control: SonyControl)
    {
        externalScope.launch {
            Timber.d("adding control: ${control.uuid}")
            controlDao.insertControlWithChannels(
                sonyControlDataMapper.mapControl2Entity(control),
                sonyControlDataMapper.mapControl2ChannelEntityList(control),
                sonyControlDataMapper.mapControl2ChannelMapList(control)
            )
            controlDao.setActiveControl(control.uuid)
        }.join()
    }

    fun removeControl(index: Int): Boolean {
       /* if (index >= 0 && index < sonyControls.value!!.controls.size) {

            var newSelected = sonyControls.value!!.selected - 1
            if (sonyControls.value!!.selected == 0 && sonyControls.value!!.controls.size > 1) {
                newSelected = sonyControls.value!!.controls.size - 2
            }
            sonyControls.value!!.controls.removeAt(index)
            sonyControls.value!!.selected = newSelected
            saveControls()
            return true
        }*/
        return false
    }

    fun setSelectedControlIndex(index: Int): Boolean {
        /*if (index < sonyControls.value!!.controls.size) {
            sonyControls.value!!.selected = index
            saveControls()
            return true
        }*/
        return false
    }

    private suspend inline fun <reified T> avContentService(
        host: String,
        jsonRpcRequest: JsonRpcRequest
    ): Resource<T> =
        apiCall {
            api.sonyRpcService(
                "http://" + host + SonyServiceUtil.SONY_AV_CONTENT_ENDPOINT,
                jsonRpcRequest
            )
        }

    private suspend inline fun <reified T> systemService(
        host: String,
        jsonRpcRequest: JsonRpcRequest
    ): Resource<T> =
        apiCall {
            api.sonyRpcService(
                "http://" + host + SonyServiceUtil.SONY_SYSTEM_ENDPOINT,
                jsonRpcRequest
            )
        }
}
