package org.andan.android.tvbrowser.sonycontrolplugin.repository

import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.andan.android.tvbrowser.sonycontrolplugin.data.ChannelMapEntity
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlDao
import org.andan.android.tvbrowser.sonycontrolplugin.data.mapper.SonyControlDataMapper
import org.andan.android.tvbrowser.sonycontrolplugin.di.ApplicationScope
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.andan.android.tvbrowser.sonycontrolplugin.network.ContentListItemResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.InterfaceInformationResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.JsonRpcRequest
import org.andan.android.tvbrowser.sonycontrolplugin.network.PlayingContentInfoResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.PowerStatusResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_ERROR_FATAL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_ERROR_NON_FATAL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_REQUIRES_CHALLENGE_CODE
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_SUCCESSFUL
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_UNAUTHORIZED
import org.andan.android.tvbrowser.sonycontrolplugin.network.RegistrationStatus.Companion.REGISTRATION_UNKNOWN
import org.andan.android.tvbrowser.sonycontrolplugin.network.RemoteControllerInfoItemResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.Result
import org.andan.android.tvbrowser.sonycontrolplugin.network.SSDP
import org.andan.android.tvbrowser.sonycontrolplugin.network.SessionManager
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyService
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyServiceUtil
import org.andan.android.tvbrowser.sonycontrolplugin.network.SonyServiceUtil.apiCall
import org.andan.android.tvbrowser.sonycontrolplugin.network.SourceListItemResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.Status
import org.andan.android.tvbrowser.sonycontrolplugin.network.SystemInformationResponse
import org.andan.android.tvbrowser.sonycontrolplugin.network.WakeOnLan
import org.andan.android.tvbrowser.sonycontrolplugin.network.WolModeResponse
import timber.log.Timber

@Singleton
class SonyControlRepository
@Inject
constructor(
    private val api: SonyService,
    private val sessionManager: SessionManager,
    private val controlDao: ControlDao,
    private val sonyControlDataMapper: SonyControlDataMapper,
    @ApplicationScope private val externalScope: CoroutineScope
) {

  val activeSonyControlStateFlow =
      controlDao
          .getActiveControlWithChannelsFlow()
          .map { entity -> sonyControlDataMapper.mapControlEntity2Domain(entity) }
          .stateIn(externalScope, SharingStarted.Eagerly, null)

  val sonyControlsStateFlow =
      controlDao
          .getControls()
          .map { entityList ->
            entityList.map { entityElement ->
              sonyControlDataMapper.mapControlEntity2Domain(entityElement)
            }
          }
          .stateIn(externalScope, SharingStarted.Eagerly, emptyList<SonyControl>())

  suspend fun getActiveSonyControl(): SonyControl? {
    return activeSonyControlStateFlow.first()
  }

  companion object {
    const val SUCCESS_CODE = 0
    const val ERROR_CODE = -1
  }

  init {
    Timber.d("init")
    // reactively set session manager context with currently active control
    externalScope.launch {
      activeSonyControlStateFlow.collect { control ->
        Timber.d("Active control: $control")
        if (control != null) sessionManager.setContext(control)
      }
    }
  }

  suspend fun setActiveControl(uuid: String) {
    Timber.d("setActiveControl $uuid")
    controlDao.setActiveControl(uuid)
  }

  suspend fun setWolMode(enabled: Boolean): Result<Any> {
    return withContext(Dispatchers.IO) {
      systemService<Any>(getActiveSonyControl()!!.ip, JsonRpcRequest.setWolMode(enabled))
    }
  }

  suspend fun fetchWolMode(): Result<WolModeResponse> {
    return withContext(Dispatchers.IO) {
      val result =
          systemService<WolModeResponse>(getActiveSonyControl()!!.ip, JsonRpcRequest.getWolMode())
      if (result.status == Status.SUCCESS) {
        updateControl(getActiveSonyControl()!!.copy(systemWolMode = result.data!!.enabled))
      }
      result
    }
  }

  suspend fun setPowerSavingMode(mode: String): Result<Any> {
    return withContext(Dispatchers.IO) {
      systemService<Any>(getActiveSonyControl()!!.ip, JsonRpcRequest.setPowerSavingMode(mode))
    }
  }

  suspend fun fetchRemoteControllerInfo(): Result<Array<RemoteControllerInfoItemResponse>> {
    return withContext(Dispatchers.IO) {
      val result =
          systemService<Array<RemoteControllerInfoItemResponse>>(
              getActiveSonyControl()!!.ip, JsonRpcRequest.getRemoteControllerInfo())
      Timber.d("remoteControllerInfo(): ${getActiveSonyControl().toString()}")
      if (result.status == Status.SUCCESS) {
        val commandMap = LinkedHashMap<String, String>()
        for (remoteControllerInfoItem in result.data!!) {
          commandMap[remoteControllerInfoItem.name] = remoteControllerInfoItem.value
        }
        updateControl(getActiveSonyControl()!!.copy(commandMap = commandMap))
      }
      result
    }
  }

  suspend fun fetchSystemInformation(): Result<SystemInformationResponse> {
    return withContext(Dispatchers.IO) {
      val result =
          systemService<SystemInformationResponse>(
              getActiveSonyControl()!!.ip, JsonRpcRequest.getSystemInformation())
      Timber.d("fetchSystemInformation(): ${getActiveSonyControl().toString()}")
      if (result.status == Status.SUCCESS) {
        updateControl(
            getActiveSonyControl()!!.copy(
                systemName = result.data!!.name,
                systemProduct = result.data!!.product,
                systemModel = result.data!!.model,
                systemMacAddr = result.data!!.macAddr))
      }
      result
    }
  }

  suspend fun fetchPlayingContentInfo(): Result<PlayingContentInfoResponse> {
    return withContext(Dispatchers.IO) {
      avContentService<PlayingContentInfoResponse>(
          getActiveSonyControl()!!.ip, JsonRpcRequest.getPlayingContentInfo())
    }
  }

  suspend fun fetchInterfaceInformation(host: String): Result<InterfaceInformationResponse> {
    return withContext(Dispatchers.IO) {
      systemService<InterfaceInformationResponse>(host, JsonRpcRequest.getInterfaceInformation())
    }
  }

  suspend fun fetchPowerStatus(host: String): Result<PowerStatusResponse> {
    return withContext(Dispatchers.IO) {
      systemService<PowerStatusResponse>(host, JsonRpcRequest.getPowerStatus())
    }
  }

  suspend fun setPlayContent(uri: String): Result<Any> {
    return withContext(Dispatchers.IO) {
      avContentService<Any>(getActiveSonyControl()!!.ip, JsonRpcRequest.setPlayContent(uri))
    }
  }

  suspend fun fetchSourceList(): Result<Array<SourceListItemResponse>> {
    return withContext(Dispatchers.IO) {
      val result =
          avContentService<Array<SourceListItemResponse>>(
              getActiveSonyControl()!!.ip, JsonRpcRequest.getSourceList("tv"))
      if (result.status == Status.SUCCESS) {
        val sourceList = mutableListOf<String>()
        for (sourceItem in result.data!!) {
          if (sourceItem.source == "tv:dvbs") {
            sourceList.add(sourceItem.source + "#general")
            sourceList.add(sourceItem.source + "#preferred")
          } else {
            sourceList.add(sourceItem.source)
          }
        }
        updateControl(getActiveSonyControl()!!.copy(sourceList = sourceList))
      }
      result
    }
  }

  suspend fun fetchChannelList(): Int {
    var nFetchedChannels = -1
    withContext(Dispatchers.IO) {
      Timber.d("fetchChannelList(): ${getActiveSonyControl()!!.sourceList}")
      if (getActiveSonyControl()!!.sourceList.isEmpty()) {
        fetchSourceList()
      }
      val channelList = mutableListOf<SonyChannel>()
      if (getActiveSonyControl()!!.sourceList.isNotEmpty()) {
        for (sonySource in getActiveSonyControl()!!.sourceList) {
          // get channels in pages
          var stidx = 0
          var count = 0
          while (fetchTvContentList(
                  getActiveSonyControl()!!.ip,
                  sonySource,
                  stidx,
                  SonyControl.PAGE_SIZE,
                  channelList)
              .let {
                count = it
                it > 0
              }) {
            stidx += SonyControl.PAGE_SIZE
          }
          // Break loop over source in case of error
          if (count == -1) {
            Timber.d("fetchChannelList(): error")
            break
          }
        }
        saveChannelList(getActiveSonyControl()!!.copy(channelList = channelList))
        Timber.d("fetchChannelList(): ${channelList.size}")
        nFetchedChannels = channelList.size
      }
    }
    return nFetchedChannels
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
      val result =
          avContentService<Array<ContentListItemResponse>>(
              ip, JsonRpcRequest.getContentList(source, stIdx, cnt, type))
      if (result.status == Status.SUCCESS) {
        for (sonyChannelResponse in result.data!!) {
          if (sonyChannelResponse.programMediaType.equals("tv", true) &&
              sonyChannelResponse.title != "." &&
              !sonyChannelResponse.title.isEmpty() &&
              !sonyChannelResponse.title.contains("TEST")) {
            val sonyChannel = sonyChannelResponse.toSonyChannel(source)
            plist.add(sonyChannel)
          }
        }
        result.data.size
      } else {
        // _responseMessage.postValue(Event(resource.message))
        -1
      }
    }
  }

  suspend fun registerControl(control: SonyControl, challenge: String?): RegistrationStatus {
    return withContext(Dispatchers.IO) {
      var registrationStatus: RegistrationStatus = RegistrationStatus(REGISTRATION_UNKNOWN, "")
      Timber.d("registerControl(): ${control.nickname}")
      // set context
      sessionManager.setContext(control)
      if (challenge != null) {
        sessionManager.challenge = challenge
      }
      // sonyServiceContext.preSharedKey = it.preSharedKey?: ""
      // indicates whether pre-shared key is used
      val isPSK = control.preSharedKey.isNotEmpty()
      try {
        val response =
            if (!isPSK) {
              // register control if no pre-shared key is defined
              api.sonyRpcService(
                  "http://" + control.ip + SonyServiceUtil.SONY_ACCESS_CONTROL_ENDPOINT,
                  JsonRpcRequest.actRegister(control.nickname, control.devicename, control.uuid))
            } else {
              // make authenticated request as validity check
              api.sonyRpcService(
                  "http://" + control.ip + SonyServiceUtil.SONY_SYSTEM_ENDPOINT,
                  JsonRpcRequest.getSystemInformation())
            }
        // update token
        if (response.isSuccessful) {
          val jsonRpcResponse = response.body()
          if (jsonRpcResponse?.error != null) {
            registrationStatus =
                RegistrationStatus(
                    REGISTRATION_ERROR_NON_FATAL, jsonRpcResponse.error.asJsonArray.get(1).asString)
          } else if (!isPSK && !response.headers()["Set-Cookie"].isNullOrEmpty()) {
            // get token from set cookie and store
            val cookieString: String = response.headers()["Set-Cookie"]!!
            val pattern = Pattern.compile("auth=([A-Za-z0-9]+)")
            val matcher = pattern.matcher(cookieString)
            if (matcher.find()) {
              sessionManager.saveToken("auth=" + matcher.group(1))
              // preferenceStore.storeToken(control.uuid, "auth=" + matcher.group(1))
            }
            registrationStatus = RegistrationStatus(REGISTRATION_SUCCESSFUL, "")
          } else if (isPSK) {
            registrationStatus = RegistrationStatus(REGISTRATION_SUCCESSFUL, "")
          }
        } else {
          if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED ||
              response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
            // Navigate to enter challenge code view
            if (!isPSK && challenge.isNullOrEmpty()) {
              registrationStatus =
                  RegistrationStatus(REGISTRATION_REQUIRES_CHALLENGE_CODE, response.message())
            } else {
              registrationStatus = RegistrationStatus(REGISTRATION_UNAUTHORIZED, response.message())
            }
          } else {
            registrationStatus =
                RegistrationStatus(REGISTRATION_ERROR_NON_FATAL, response.message())
          }
        }
      } catch (se: SocketTimeoutException) {
        Timber.e("Error: ${se.message}")
        registrationStatus =
            RegistrationStatus(REGISTRATION_ERROR_FATAL, se.message ?: "Unknown failure")
      } finally {
        // reset context
        // setSonyServiceContextForControl(selectedSonyControl.value)
        sessionManager.challenge = ""
      }
      registrationStatus
    }
  }

  suspend fun sendCommand(command: String): Result<Any> {
    val code = getActiveSonyControl()!!.commandMap[command]
    Timber.d("sendCommand: $command $code")
    if (!code.isNullOrBlank()) {
      return sendIRCC(code)
    }
    return Result.Error("No valid code for command: $command", -1)
  }

  suspend fun sendIRCC(code: String): Result<Any> {

    return withContext(Dispatchers.IO) {
      if (sessionManager.hostname.isNotBlank()) {
        val requestBodyText =
            SonyServiceUtil.SONY_IRCC_REQUEST_TEMPLATE.replace("<IRCCCode>", "<IRCCCode>$code")

        val requestBody: RequestBody = requestBodyText.toRequestBody("text/xml".toMediaTypeOrNull())
        Timber.d("sendIRCC: $requestBodyText")
        try {
          val response =
              api.sendIRCC(
                  "http://" + sessionManager.hostname + SonyServiceUtil.SONY_IRCC_ENDPOINT,
                  requestBody)
          Timber.d("response: $response")
          if (!response.isSuccessful) {
            // _responseMessage.postValue(Event(response.message()))
            Timber.e("IRCC send not successful: ${response.message()}")
            return@withContext Result.Error<Any>(
                "IRCC send not successful: ${response.message()}", -1)
          } else {
            return@withContext Result.Success(0, code)
          }
        } catch (se: SocketTimeoutException) {
          Timber.e("Error: ${se.message}")
          return@withContext Result.Error<Any>("Error: ${se.message}", -1)
        }
      } else {
        return@withContext Result.Error<Any>("No host specified", -1)
      }
    }
  }

  suspend fun wakeOnLan() {
    withContext(Dispatchers.IO) {
      try {
        WakeOnLan.wakeOnLan(getActiveSonyControl()!!.ip, getActiveSonyControl()!!.systemMacAddr)
      } catch (se: SocketTimeoutException) {
        Timber.e("Error: ${se.message}")
      }
    }
  }

  suspend fun getSonyIpAndDeviceList(): List<SSDP.IpDeviceItem> {
    return withContext(Dispatchers.IO) { SSDP.getSonyIpAndDeviceList() }
  }

  suspend fun updateChannelMapsFromChannelNameList(channelNameList: List<String>) {
    Timber.d("updateChannelMapsFromChannelNameList: ${channelNameList.size}")
    sonyControlsStateFlow.value.onEach { control ->
      var newEntries = 0
      val channelMap = mutableMapOf<String, String>()
      val channelMapList = controlDao.getChannelMapEntities(control.uuid).first()
      channelMapList.onEach { channelMapEntity ->
        channelMap[channelMapEntity.channelLabel] = channelMapEntity.uri
      }
      for (channelName in channelNameList) {
        if (!channelMap.containsKey(channelName)) {
          Timber.d("updateChannelMapsFromChannelNameList: $channelName")
          channelMap[channelName] = ""
          newEntries++
        }
      }
      if (newEntries > 0) {
        saveChannelMap(control.copy(channelMap = channelMap))
        Timber.d("updated channel map for ${control.nickname} with $newEntries new entries")
      } else {
        Timber.d("no channel map updates for ${control.nickname}")
      }
    }
    Timber.d("updateChannelMapsFromChannelNameList: finished")
  }

  suspend fun insertSonyControls(sonyControls: SonyControls) {
    withContext(Dispatchers.IO) { controlDao.insertFromSonyControls(sonyControls) }
  }

  fun saveChannelMap(uuid: String, channelMap: Map<String, String>) {
    val channelMapEntityList: MutableList<ChannelMapEntity> = ArrayList()
    channelMap.forEach { channelMapEntityList.add(ChannelMapEntity(uuid, it.key, it.value)) }
    externalScope.launch { controlDao.setChannelMapForControl(channelMapEntityList, uuid) }
  }

  suspend fun deleteChannelMap(uuid: String) {
    externalScope.launch { controlDao.deleteChannelMapsForControlUuid(uuid) }
  }

  suspend fun saveChannelMap(control: SonyControl) {
    externalScope.launch {
      Timber.d("setting channel maps for control: $control}")
      controlDao.setChannelMapForControl(
          sonyControlDataMapper.mapControl2ChannelMapList(control), control.uuid)
    }
  }

  suspend fun saveChannelList(control: SonyControl) {
    externalScope.launch {
      Timber.d("setting channels for control: $control}")
      controlDao.setChannelsForControl(
          sonyControlDataMapper.mapControl2ChannelEntityList(control), control.uuid)
    }
  }

  suspend fun deleteControl(control: SonyControl) {
    externalScope.launch {
      val hasBeenActive = control.isActive
      Timber.d(" deleteControl ${control.uuid}")
      controlDao.deleteControl(control.uuid)
      sessionManager.removeToken(control.uuid)
      Timber.d("deleteControl hasBeenActive ${hasBeenActive}")
      if (hasBeenActive && sonyControlsStateFlow.value.isNotEmpty()) {
        setActiveControl(sonyControlsStateFlow.value.first().uuid)
      }
    }
  }

  suspend fun updateControl(control: SonyControl) {
    externalScope.launch {
      Timber.d("updating control: ${control.uuid}")
      controlDao.update(sonyControlDataMapper.mapControl2Entity(control))
    }
  }

  suspend fun addControl(control: SonyControl) {
    externalScope.launch {
      Timber.d("adding control: ${control.uuid}")
      controlDao.insertControlWithChannels(
          sonyControlDataMapper.mapControl2Entity(control),
          sonyControlDataMapper.mapControl2ChannelEntityList(control),
          sonyControlDataMapper.mapControl2ChannelMapList(control))
      controlDao.setActiveControl(control.uuid)
    }
  }

  private suspend inline fun <reified T> avContentService(
      host: String,
      jsonRpcRequest: JsonRpcRequest
  ): Result<T> = apiCall {
    api.sonyRpcService("http://" + host + SonyServiceUtil.SONY_AV_CONTENT_ENDPOINT, jsonRpcRequest)
  }

  private suspend inline fun <reified T> systemService(
      host: String,
      jsonRpcRequest: JsonRpcRequest
  ): Result<T> = apiCall {
    api.sonyRpcService("http://" + host + SonyServiceUtil.SONY_SYSTEM_ENDPOINT, jsonRpcRequest)
  }

  suspend fun setPlayContentFromChannelName(channelName: String?) {
    val uri = getActiveSonyControl()!!.channelMap[channelName]
    if (uri != null) {
      setPlayContent(uri)
      Timber.d("Set play content '$uri' for channel '$channelName' ")
    }
  }
}
