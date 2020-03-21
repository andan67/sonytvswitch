package org.andan.android.tvbrowser.sonycontrolplugin.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.annotation.Nullable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.ControlPreferenceStore
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.ArrayList
import javax.inject.Inject
import javax.inject.Named

class SonyRepository @Inject constructor(val client: OkHttpClient, val api: SonyService, val preferenceStore: ControlPreferenceStore) {
    private val TAG = SonyRepository::class.java.name

    //var sonyControls = SonyControls()
    //var selectedSonyControl : SonyControl? = null
    //var selectedIndex = -1

    val sonyControls = preferenceStore.sonyControls
    val selectedSonyControl = preferenceStore.selectedSonyControl

    val gson = GsonBuilder().create()
    init {
        (client.authenticator as TokenAuthenticator).serviceHolder?.sonyService=api
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

    private val _playingContentInfo = MutableLiveData<Resource<PlayingContentInfoResponse>>()
    val playingContentInfo: LiveData<Resource<PlayingContentInfoResponse>> = _playingContentInfo

    suspend inline fun <reified T> avContentService(jsonRpcRequest: JsonRpcRequest): Resource<T> {
        return apiCall(call = { api.avContent("http://" + selectedSonyControl.value?.ip + SONY_AV_CONTENT_ENDPOINT, jsonRpcRequest) })
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
            _playingContentInfo.value = resource
            Log.d(TAG, resource.status.name)
            Log.d(TAG, resource.data.toString())
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
        if(isUpdated) preferenceStore.onControlsChanged()
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
                        Resource.Error(jsonRpcResponse.error.asJsonArray.get(1).asString)
                    }
                    else -> {
                        Log.d("apiCall", "evaluate result")
                        Resource.Success(
                            gson.fromJson(
                                jsonRpcResponse?.result!!.asJsonArray.get(0).asJsonObject,
                                T::class.java
                            )
                        )
                    }
                }
            } else {
                Log.d("apiCall", "evaluate response unsuccessful")
                Resource.Error(response.message())
            }

        } catch (e: Exception) {
            Log.d("apiCall", "evaluate exception ${e.message}")
            return Resource.Error(e.message!!)
        }
    }

    fun addControl(control: SonyControl) {

    }
}