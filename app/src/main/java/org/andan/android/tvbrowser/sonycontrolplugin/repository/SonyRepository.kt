package org.andan.android.tvbrowser.sonycontrolplugin.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.andan.android.tvbrowser.sonycontrolplugin.network.*
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject

class SonyRepository @Inject constructor(val client: OkHttpClient, val api: SonyService) {
    private val TAG = SonyRepository::class.java.name

    /*
    var tokenRepository =
        TokenRepository("auth=16f2695f210e5c7ce96f9b023d15812caf3920fe7e89be2e726b8339a564c83f")

    val httpLoggingInterceptor =
        HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    val builder = OkHttpClient.Builder()

    val serviceHolder = SonyServiceHolder()

    val client = builder
        .addInterceptor(AddTokenInterceptor(tokenRepository))
        .addInterceptor(httpLoggingInterceptor)
        .authenticator(TokenAuthenticator(serviceHolder, tokenRepository))
        .build()
    */

    val gson = GsonBuilder().create()

    /*
    val gsonConverter = GsonConverterFactory.create(gson)

    val api: SonyService = Retrofit.Builder() // Create retrofit builder.
        .baseUrl("http://192.168.178.27/") // Base url for the api has to end with a slash.
        //.baseUrl(server.url("/"))
        //.addConverterFactory(JsonRPCConverterFactory.create())
        .addConverterFactory(gsonConverter) // Use GSON converter for JSON to POJO object mapping.
        .client(client) // Here we set the custom OkHttp client we just created.
        .build().create(SonyService::class.java)
    */

    init {
        (client.authenticator as TokenAuthenticator).serviceHolder?.sonyService=api
    }

    //simplified version of the retrofit call that comes from support with coroutines
    //Note that this does NOT handle errors, to be added
    /* suspend fun getPowerStatus(): LiveData<String> = liveData {
         val response: JsonRpcResponse =
             api.system(JsonRpcRequest(50, "getPowerStatus", emptyList()))
         //return response.result.asJsonArray.get(0).asJsonObject.get("status").asString
         emit(response.result.asJsonArray.get(0).asJsonObject.get("status").asString)
     }
 */

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

    private val _playingContentInfo = MutableLiveData<Resource<PlayingContentInfo>>()
    val playingContentInfo: LiveData<Resource<PlayingContentInfo>> = _playingContentInfo

    suspend fun getPlayingContentInfo2() {
        withContext(Dispatchers.Main) {

            try {
                val response =
                    api.avContent(BASE_URL+ AV_CONTENT_ENDPOINT,JsonRpcRequest(103, "getPlayingContentInfo", emptyList()))
                val jsonRpcResponse = response.body()
                if (response.isSuccessful) {
                    when {
                        jsonRpcResponse?.error != null -> {
                            Log.d(TAG, "evaluate error")
                            _playingContentInfo.value =
                                Resource.Error(jsonRpcResponse.error.asJsonArray.get(1).asString)
                        }
                        jsonRpcResponse?.result != null -> {
                            Log.d(TAG, "evaluate result")
                            _playingContentInfo.value = Resource.Success(
                                gson.fromJson(
                                    jsonRpcResponse.result.asJsonArray.get(0).getAsJsonObject(),
                                    PlayingContentInfo::class.java
                                )
                            )
                        }
                    }
                } else {
                    Log.d(TAG, "evaluate response unsuccessful")
                    _playingContentInfo.value = Resource.Error(response.message())
                }

            } catch (e: Exception) {
                Log.d(TAG, "evaluate exception ${e.message}")
                _playingContentInfo.value = Resource.Error(e.message!!)
            }
            // _playingContentInfo.value = gson.fromJson(response.result.asJsonArray.get(0).getAsJsonObject(),PlayingContentInfo::class.java)

        }
    }

    suspend inline fun <reified T> avContentService(jsonRpcRequest: JsonRpcRequest): Resource<T> {
        return apiCall(call = { api.avContent(BASE_URL+ AV_CONTENT_ENDPOINT, jsonRpcRequest) })
    }

    suspend fun getPlayingContentInfo() {
        withContext(Dispatchers.Main) {
            val resource = avContentService<PlayingContentInfo>(
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
                        //return Resource.Success(gson.fromJson(jsonRpcResponse?.result!!.asJsonArray.get(0).asJsonObject,object : TypeToken<T>() {}.type))
                        Resource.Success(
                            gson.fromJson(
                                jsonRpcResponse?.result!!.asJsonArray.get(0).asJsonObject,
                                T::class.java
                            )
                        )
                        //return Resource.Success(gson.fromJson(jsonRpcResponse?.result!!.asJsonArray.get(0).asJsonObject, playingContentInfo::class.java))
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
}