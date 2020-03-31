package org.andan.android.tvbrowser.sonycontrolplugin.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyProgram2
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import org.andan.av.sony.network.SonyJsonRpcResponse
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

class SonyRepository @Inject constructor(val client: OkHttpClient, val api: SonyService, val preferenceStore: ControlPreferenceStore) {
    private val TAG = SonyRepository::class.java.name

    //var sonyControls = SonyControls()
    //var selectedSonyControl : SonyControl? = null
    //var selectedIndex = -1

    var sonyControls = MutableLiveData<SonyControls>()
    var selectedSonyControl = MutableLiveData<SonyControl>()
    val sonyServiceContext = SonyControlApplication.get().appComponent.sonyServiceContext()

    private val _requestErrorMessage = MutableLiveData("")
    val requestErrorMessage: LiveData<String> = _requestErrorMessage

    val gson = GsonBuilder().create()
    init {
        Log.d(TAG, "init")
        sonyControls.value = preferenceStore.loadControls()
        sonyServiceContext.sonyService = api
        onSonyControlsChange()
    }

    private fun onSonyControlsChange() {
        selectedSonyControl.value = getSelectedControl()
        sonyServiceContext.sonyService = api
        Log.d(TAG, "onSonyControlsChange()")
        if(selectedSonyControl.value != null) {
            Log.d(TAG, "onSonyControlsChange(): ${selectedSonyControl.value}")
            sonyServiceContext.ip = selectedSonyControl.value!!.ip
            sonyServiceContext.uuid = selectedSonyControl.value!!.uuid
            sonyServiceContext.nickname = selectedSonyControl.value!!.nickname
            sonyServiceContext.devicename = selectedSonyControl.value!!.devicename
        }
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    private fun getSelectedControl() : SonyControl? {
        return if(sonyControls.value!!.selected >= 0 && sonyControls.value!!.selected <= sonyControls.value!!.controls.size-1) {
            sonyControls.value!!.controls[sonyControls.value!!.selected]
        } else null
    }

    private val _currentTime = MutableLiveData("N/A")
    val currentTime: LiveData<String> = _currentTime

    suspend fun getCurrentTime() {
        withContext(Dispatchers.Main) {
            _currentTime.value = "Fetching new data..."
            val response: JsonRpcResponse =
                api.system(JsonRpcRequest(51, "getCurrentTime", emptyList()))
            _currentTime.value = response.result.asJsonArray.get(0).asString
        }
    }

    private val _playingContentInfo = MutableLiveData<PlayingContentInfoResponse>()
    val playingContentInfo: LiveData<PlayingContentInfoResponse> = _playingContentInfo

    suspend inline fun <reified T> avContentService(jsonRpcRequest: JsonRpcRequest): Resource<T> {
        return apiCall(call = { api.avContent("http://" + selectedSonyControl.value?.ip + SONY_AV_CONTENT_ENDPOINT, jsonRpcRequest) })
    }

    suspend inline fun <reified T> accessControlService(jsonRpcRequest: JsonRpcRequest): Resource<T> {
        return apiCall(call = { api.accessControl("http://" + selectedSonyControl.value?.ip + SONY_ACCESS_CONTROL_ENDPOINT, jsonRpcRequest) })
    }

    suspend fun getPlayingContentInfo() {
        withContext(Dispatchers.Main) {
            val resource = avContentService<PlayingContentInfoResponse>(
                JsonRpcRequest(103, "getPlayingContentInfo", emptyList()))
            if(resource.status == Status.ERROR) {
                _requestErrorMessage.postValue(resource.message)
                _playingContentInfo.value = PlayingContentInfoResponse.notAvailableValue
            } else {
                _playingContentInfo.value = resource.data
            }
            //Log.d(TAG, resource.data!!.title)
        }
    }

    suspend fun setPlayContent(uri: String) {
        withContext(Dispatchers.Main) {
            val params = ArrayList<Any>()
            params.add(hashMapOf("uri" to uri))
            val resource = avContentService<Unit>(JsonRpcRequest(101, "setPlayContent", params))
            if(resource.status == Status.ERROR) {
                _requestErrorMessage.postValue(resource.message)
            }
        }
    }

    suspend fun fetchProgramList() {
        Log.d(TAG, "fetchProgramList(): ${getSelectedControl()!!.sourceList}")
        if(getSelectedControl()!!.sourceList.isNullOrEmpty()) {
        }
        getSelectedControl()!!.programList.clear()
        if (!getSelectedControl()!!.sourceList.isNullOrEmpty()) {
            for (sonySource in getSelectedControl()!!.sourceList) {
                // get programs in pages
                var response: SonyJsonRpcResponse
                var stidx = 0
                do {
                    val sizeOld = getSelectedControl()!!.programList.size
                    getSelectedControl()!!.programList.addAll(getTvContentList(sonySource!!, stidx, SonyControl.PAGE_SIZE))
                    stidx += SonyControl.PAGE_SIZE
                } while (getSelectedControl()!!.programList.size - sizeOld > 0)
            }
            Log.d(TAG, "fetchProgramList(): ${getSelectedControl()!!.programList.size}")
        }
    }

    private suspend fun getTvContentList(sourceType: String, stIdx: Int, cnt: Int): List<SonyProgram2> {
        val sourceSplit = sourceType.split("#").toTypedArray()
        val source = sourceSplit[0]
        var type = ""
        if (sourceSplit.size > 1) type = sourceSplit[1]
        val params = ArrayList<Any>()
        params.add(hashMapOf("source" to source, "stIdx" to stIdx, "cnt" to cnt, "type" to type))
        var fetchedPrograms = 0
        //Log.d(TAG, "getTvContentList(): avContentService")
        val resource = avContentService<Array<SonyProgram2>>(JsonRpcRequest(103, "getContentList", params))
        //Log.d(TAG, "getTvContentList(): $resource")
        return if(resource.status==Status.SUCCESS) {
            val programList = mutableListOf<SonyProgram2>()
            val programArrayFromResponse = resource.data!!
            for(sonyProgram in programArrayFromResponse) {
                //Log.d(TAG, "$sonyProgram")
                sonyProgram.source = sourceType
                if (sonyProgram.programMediaType.equals("tv", true)
                    && sonyProgram.title != "." && !sonyProgram.title.isEmpty() && !sonyProgram.title.contains("TEST")) programList.add(sonyProgram)
            }
            //Log.d(TAG, "getTvContentList(): ${programList.size}")
            programList
        } else {
            emptyList()
        }
    }

    suspend fun registerControl(challenge: String?) {
        withContext(Dispatchers.Main) {
            selectedSonyControl.value?.let {
                Log.d(TAG, "registerControl(): ${sonyServiceContext.nickname}")
                if(challenge != null) {
                    sonyServiceContext.password=challenge
                }
                try {
                    val response = api.accessControl(
                        "http://" + it.ip + SONY_ACCESS_CONTROL_ENDPOINT, JsonRpcRequest.actRegisterRequest(it.nickname, it.devicename, it.uuid)
                    )
                    // update token
                    if (response.isSuccessful) {
                        val jsonRpcResponse = response.body()
                        if (jsonRpcResponse?.error != null) {
                            _requestErrorMessage.postValue(jsonRpcResponse.error.asJsonArray.get(1).asString)
                        } else if (!response.headers()["Set-Cookie"].isNullOrEmpty()) {
                            // get token from set cookie and store
                            val cookieString: String? = response.headers()["Set-Cookie"]
                            val pattern = Pattern.compile("auth=([A-Za-z0-9]+)")
                            val matcher = pattern.matcher(cookieString)
                            if (matcher.find()) {
                                preferenceStore.storeToken(it.uuid, "auth=" + matcher.group(1))
                            }
                        }
                    } else {
                        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                            // Navigate to enter challenge code view
                            _requestErrorMessage.postValue(response.message())
                        } else {
                            _requestErrorMessage.postValue(response.message())
                        }
                    }
                } catch (se: SocketTimeoutException) {
                    Log.e(TAG, "Error: ${se.message}")
                    _requestErrorMessage.postValue(se.message)
                }
                sonyServiceContext.password=""
            }
        }
    }


    fun updateChannelMapsFromChannelNameList(channelNameList: List<String>) {
        var isUpdated = false
        for (control in sonyControls.value!!.controls) {
            Log.d(TAG, "updateChannelMapsFromChannelNameList: ${channelNameList.size} ${control.channelProgramMap.size}")
            for (channelName in channelNameList) {
                // create mapping entry for unmapped channels
                if (!control.channelProgramMap.containsKey(channelName)) {
                    Log.d(TAG, "updateChannelMapsFromChannelNameList: $channelName")
                    control.channelProgramMap[channelName] = ""
                    isUpdated = true
                }
                //ToDo: Handle deletion of channels
            }
        }
        if(isUpdated) saveControls()
        Log.d(TAG, "updateChannelMapsFromChannelNameList: finished")
    }

    suspend inline fun <reified T> apiCall(call: suspend () -> Response<JsonRpcResponse>): Resource<T> {
        //val response: Response<T>
        return try {
            val response = call.invoke()
            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                when {
                    jsonRpcResponse?.error != null -> {
                        Log.d("apiCall", "evaluate error")
                        Resource.Error(jsonRpcResponse.error.asJsonArray.get(1).asString, response.code())
                    }
                    else -> {
                        Log.d("apiCall", "evaluate result")
                        Resource.Success(response.code(),
                            gson.fromJson(
                                when (jsonRpcResponse?.result?.asJsonArray?.get(0)) {
                                    is JsonObject -> jsonRpcResponse.result.asJsonArray?.get(0)!!.asJsonObject
                                    is JsonArray -> jsonRpcResponse.result.asJsonArray?.get(0)!!.asJsonArray
                                    else -> jsonRpcResponse?.result!!.asJsonArray.get(0).asJsonObject
                                }, T::class.java)
                        )
                    }
                }
            } else {
                Log.d("apiCall", "evaluate response unsuccessful")
                Resource.Error(response.message(), response.code())
            }

        } catch (e: Exception) {
            Log.d("apiCall", "evaluate exception ${e.message}")
            return Resource.Error(e.message!!, 0)
        }
    }

    fun saveControls() {
        preferenceStore.storeControls(sonyControls.value!!)
        sonyControls.notifyObserver()
        selectedSonyControl.value =getSelectedControl()
        onSonyControlsChange()
    }

    fun addControl(control: SonyControl) {
        sonyControls.value!!.controls.add(control)
        sonyControls.value!!.selected = sonyControls.value!!.controls.size-1
        Log.d(TAG, "addControl: #${sonyControls.value!!.selected} $control")
        saveControls()
    }

    fun removeControl(index: Int): Boolean {
        if(index >=0 && index < sonyControls.value!!.controls.size) {

            var newSelected = sonyControls.value!!.selected -1
            if  (sonyControls.value!!.selected == 0 && sonyControls.value!!.controls.size > 1)
            {
                newSelected = sonyControls.value!!.controls.size - 2
            }
            sonyControls.value!!.controls.removeAt(index)
            sonyControls.value!!.selected = newSelected
            saveControls()
            return true
        }
        return false
    }

    fun setSelectedControlIndex(index : Int): Boolean {
        if(index < sonyControls.value!!.controls.size) {
            sonyControls.value!!.selected = index
            saveControls()
            return true
        }
        return false
    }
}