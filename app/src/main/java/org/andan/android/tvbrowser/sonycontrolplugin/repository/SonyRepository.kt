package org.andan.android.tvbrowser.sonycontrolplugin.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.andan.android.tvbrowser.sonycontrolplugin.SonyControlApplication
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import retrofit2.Response
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.util.ArrayList
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
        if(selectedSonyControl.value != null) {
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
                JsonRpcRequest(
                    103,
                    "getPlayingContentInfo",
                    emptyList()
                )
            )
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
            params.add(
                hashMapOf(
                    "uri" to uri
                )
            )
            val resource = avContentService<Unit>(
                JsonRpcRequest(
                    101,
                    "setPlayContent",
                    params
                )
            )
            if(resource.status == Status.ERROR) {
                _requestErrorMessage.postValue(resource.message)
            }
        }
    }

    suspend fun registerControl() {
        withContext(Dispatchers.Main) {
            selectedSonyControl.value?.let {
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
                        Resource.Success(
                            response.code(),
                            gson.fromJson(
                                jsonRpcResponse?.result!!.asJsonArray.get(0).asJsonObject,
                                T::class.java
                            )
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