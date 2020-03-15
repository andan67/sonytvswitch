package org.andan.android.tvbrowser.sonycontrolplugin.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Url
import java.net.HttpURLConnection.HTTP_OK
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

const val BASE_URL = "http://192.168.178.27"
const val AV_CONTENT_ENDPOINT = "/sony/avContent"

interface SonyService {
    @POST("/sony/system")
    suspend fun system(@Body rpcRequest: JsonRpcRequest): JsonRpcResponse

    /*@POST("/sony/avContent")
    suspend fun avContent(@Body rpcRequest: JsonRpcRequest): Response<JsonRpcResponse>*/

    @POST
    suspend fun avContent(@Url url: String, @Body rpcRequest: JsonRpcRequest): Response<JsonRpcResponse>

    @POST("/sony/accessControl")
    suspend fun accessControl(@Body rpcRequest: JsonRpcRequest): JsonRpcResponse

    @POST("/sony/accessControl")
    fun refreshToken(@Body rpcRequest: JsonRpcRequest): Call<JsonRpcResponse>
}

data class JsonRpcRequest(
    val id: Long,
    val method: String,
    val params: List<Any>,
    val version: String = "1.0"
)

data class JsonRpcResponse(
    val id: Long,
    val result: JsonElement,
    val error: JsonElement
)

data class JsonRpcError(
    val code: Int,
    val message: String
)

data class PlayingContentInfo(
    val source: String = "",
    val dispNumber: String = "",
    val programMediaType: String = "",
    val title: String = "N/A",
    val uri: String = "",
    val programTitle: String = "N/A",
    val startDateTime: String = "",
    val durationSec: Long = 0
)

data class TokenRepository(
    var tokenValue: String = "",
    var expiryTime: Long = 0
)

class AddTokenInterceptor @Inject constructor(val tokenRepository: TokenRepository) : Interceptor {

    /**
     * Interceptor class for setting of the headers for every request
     */
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        request = request.newBuilder().addHeader("Cookie", tokenRepository.tokenValue).build()
        return chain.proceed(request)
    }
}

class TokenAuthenticator @Inject constructor(
    private val _serviceHolder: SonyServiceHolder?,
    private val _tokenRepository: TokenRepository
) : Authenticator {

    val tokenRepository = _tokenRepository

    val serviceHolder = _serviceHolder

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        // This is a synchronous call
        val updatedToken = getNewToken()
        val request = response.request
        return request.newBuilder()
            .addHeader("Cookie", updatedToken)
            .build()
    }

    private fun getNewToken(): String {
        val params = ArrayList<Any>()
        params.add(
            hashMapOf(
                "clientid" to "value1",
                "nickname" to "Nexus 5 (TV SideView)",
                "level" to "private"
            )
        )
        params.add(
            listOf(
                hashMapOf(
                    "value" to "yes",
                    "function" to "WOL"
                )
            )
        )

        if (serviceHolder != null) {
            val response =
                serviceHolder.sonyService!!.refreshToken(JsonRpcRequest(8, "actRegister", params)).execute()
            if (response.code() == HTTP_OK) {
                if (!response.headers()["Set-Cookie"].isNullOrEmpty()) {
                    val cookieString: String? = response.headers()["Set-Cookie"]
                    var pattern =
                        Pattern.compile("auth=([A-Za-z0-9]+)")
                    var matcher = pattern.matcher(cookieString)
                    if (matcher.find()) {
                        tokenRepository.tokenValue = "auth=" + matcher.group(1)
                        pattern = Pattern.compile("max-age=([0-9]+)")
                        matcher = pattern.matcher(cookieString)
                        if (matcher.find()) {
                            tokenRepository.expiryTime =
                                System.currentTimeMillis() + 1000 * matcher.group(1).toLong()
                        }
                    }

                }
            }
        }
        return tokenRepository.tokenValue
    }
}

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val status: Status
) {
    class Success<T>(data: T) : Resource<T>(data, status = Status.SUCCESS)
    class Loading<T>(data: T? = null) : Resource<T>(data, status = Status.LOADING)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message, Status.ERROR)
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

class SonyServiceHolder(
    var sonyService: SonyService? = null
)