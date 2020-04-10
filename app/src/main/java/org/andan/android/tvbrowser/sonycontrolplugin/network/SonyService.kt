package org.andan.android.tvbrowser.sonycontrolplugin.network

import android.util.Log
import com.google.gson.JsonElement
import okhttp3.*
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.TokenStore
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyProgram2
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.net.HttpURLConnection.HTTP_OK
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

const val SONY_AV_CONTENT_ENDPOINT = "/sony/avContent"
const val SONY_ACCESS_CONTROL_ENDPOINT = "/sony/accessControl"
const val SONY_SYSTEM_ENDPOINT = "/sony/system"
const val SONY_IRCC_ENDPOINT = "/sony/IRCC"
const val SONY_IRCC_REQUEST_TEMPLATE = "<?xml version=\"1.0\"?>\n" +
        "<s:Envelope\n" +
        "    xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
        "    s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
        "    <s:Body>\n" +
        "        <u:X_SendIRCC xmlns:u=\"urn:schemas-sony-com:service:IRCC:1\">\n" +
        "            <IRCCCode></IRCCCode>\n" +
        "        </u:X_SendIRCC>\n" +
        "    </s:Body>\n" +
        "</s:Envelope>"

interface SonyService {
    @POST
    suspend fun sonyRpcService(@Url url: String, @Body rpcRequest: JsonRpcRequest): Response<JsonRpcResponse>

    @POST
    suspend fun sendIRCC(@Url url: String, @Body requestBody: RequestBody): Response<Unit>

    @POST
    fun refreshToken(@Url url: String, @Body rpcRequest: JsonRpcRequest): Call<JsonRpcResponse>
}

data class JsonRpcRequest(
    val id: Long,
    val method: String,
    val params: List<Any>,
    val version: String = "1.0"
) {
    companion object {

        // accessControl service

        fun actRegister(nickname: String, devicename: String, uuid: String): JsonRpcRequest {
            val params = ArrayList<Any>()
            params.add(hashMapOf("nickname" to "$nickname ($devicename)",
                    "clientid" to "$nickname:$uuid",
                    "level" to "private"))
            params.add(listOf(hashMapOf("value" to "yes", "function" to "WOL")))
            return JsonRpcRequest(8, "actRegister", params)
        }

        //system service

        fun getRemoteControllerInfo(): JsonRpcRequest {
            val params = ArrayList<Any>()
            return JsonRpcRequest(10, "getRemoteControllerInfo", params)
        }

        fun getSystemInformation(): JsonRpcRequest {
            val params = ArrayList<Any>()
            return JsonRpcRequest(33, "getSystemInformation", params)
        }

        fun setWolMode(enabled: Boolean): JsonRpcRequest {
            val params = ArrayList<Any>()
            params.add(hashMapOf("enabled" to enabled))
            return JsonRpcRequest(55, "setWolMode", params)
        }

        fun getWolMode(): JsonRpcRequest {
            val params = ArrayList<Any>()
            return JsonRpcRequest(50, "getWolMode", params)
        }

        fun setPowerStatus(status: Boolean): JsonRpcRequest {
            val params = ArrayList<Any>()
            params.add(hashMapOf("status" to status))
            return JsonRpcRequest(55, "setPowerStatus", params)
        }

        fun getPowerStatus(): JsonRpcRequest {
            val params = ArrayList<Any>()
            return JsonRpcRequest(50, "getPowerStatus", params)
        }

        fun setPowerSavingMode(mode: Boolean): JsonRpcRequest {
            val params = ArrayList<Any>()
            params.add(hashMapOf("mode" to mode))
            return JsonRpcRequest(52, "setPowerSavingMode", params)
        }

        fun getPowerStatusMode(): JsonRpcRequest {
            val params = ArrayList<Any>()
            return JsonRpcRequest(51, "getPowerStatusMode", params)
        }

        // avContent service

        fun getSourceList(scheme: String): JsonRpcRequest {
            val params = ArrayList<Any>()
            params.add(hashMapOf("scheme" to scheme))
            return JsonRpcRequest(2, "getSourceList", params)
        }

        fun setPlayContent(uri: String): JsonRpcRequest {
            val params = ArrayList<Any>()
            params.add(hashMapOf("uri" to uri))
            return JsonRpcRequest(101, "setPlayContent", params)
        }

        fun getPlayingContentInfo(): JsonRpcRequest {
            val params = ArrayList<Any>()
            return JsonRpcRequest(103, "getPlayingContentInfo", params)
        }

        fun getContentList(source: String, stIdx: Int, cnt: Int, type: String): JsonRpcRequest {
            val params = ArrayList<Any>()
            params.add(hashMapOf("source" to source, "stIdx" to stIdx, "cnt" to cnt, "type" to type))
            return JsonRpcRequest(103, "getContentList", params)
        }
    }
}

data class JsonRpcResponse(
    val id: Long,
    val result: JsonElement,
    val error: JsonElement
)

data class JsonRpcError(
    val code: Int,
    val message: String
)

data class PlayingContentInfoResponse(
    val source: String,
    val dispNum: String,
    val programMediaType: String,
    val title: String,
    val uri: String,
    val programTitle: String,
    val startDateTime: String,
    val durationSec: Long
) {
    companion object {
        private val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ")
        private val DateTimeFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT)
        private val TimeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT)
        private val cal = Calendar.getInstance()
        val notAvailableValue = PlayingContentInfoResponse("","----","","Not available","","","",0)
    }

    fun getStartDateTimeFormatted(): String? {
        return try {
            val date = sdfInput.parse(startDateTime)
            DateTimeFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

    fun getEndDateTimeFormatted(): String? {
        return try {
            val date = sdfInput.parse(startDateTime)
            cal.time = date
            cal.add(Calendar.SECOND, durationSec.toInt())
            DateTimeFormat.format(cal.time)
        } catch (e: Exception) {
            ""
        }
    }

    fun getStartEndTimeFormatted(): String? {
        return try {
            val date = sdfInput.parse(startDateTime)
            val startTime = TimeFormat.format(date)
            cal.time = date
            cal.add(Calendar.SECOND, durationSec.toInt())
            val endTime =
                TimeFormat.format(cal.time)
            "$startTime - $endTime"
        } catch (e: Exception) {
            ""
        }
    }

}

fun PlayingContentInfoResponse.asDomainModel() : PlayingContentInfo{
    return PlayingContentInfo(
        source, dispNum, programMediaType, title, uri, programTitle, startDateTime, durationSec
    )
}

data class ContentListItemResponse(val dispNum: String, val index : Int, val programMediaType: String, val title: String, val uri: String )

fun ContentListItemResponse.asDomainModel() : SonyProgram2{
    return SonyProgram2("", dispNum, index, programMediaType, title, uri)
}

data class SourceListItemResponse(val source: String)

data class SystemInformationResponse(val product: String, val name: String, val model: String, val macAddr: String)

data class WolModeResponse(val enabled: Boolean)

data class PowerStatusResponse(val status: Boolean)

data class PowerSavingModeResponse(val mode: Boolean)

data class RemoteControllerInfoItemResponse(val name: String, val value: String)

class AddTokenInterceptor @Inject constructor(private val serviceClientContext: SonyServiceClientContext?,
                                              private val tokenStore: TokenStore) : Interceptor {

    /**
     * Interceptor class for setting of the headers for every request
     */
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        //request = request.newBuilder().addHeader("Cookie", tokenStore.getToken()).build()
        val builder = request.newBuilder()
        if(serviceClientContext!!.preSharedKey.isEmpty()) builder.addHeader("Cookie", tokenStore.loadToken(serviceClientContext.uuid))
        else builder.addHeader("X-Auth-PSK", serviceClientContext.preSharedKey)
        if(serviceClientContext.password.isNotEmpty())  builder.addHeader("Authorization", Credentials.basic(serviceClientContext.username, serviceClientContext.password))
        request = builder.build()
        return chain.proceed(request)
    }
}

class TokenAuthenticator @Inject constructor(
    private val serviceClientContext: SonyServiceClientContext,
    private val tokenStore: TokenStore
) : Authenticator {
    val TAG = TokenAuthenticator::class.java.name

    override fun authenticate(route: Route?, response: okhttp3.Response): Request? {
        // This is a synchronous call
        Log.d(TAG,"authenticate(): ${serviceClientContext.nickname}")
        val request = response.request
        //bypass authenticator for registration endpoint
        return if(request.url.toString().endsWith(SONY_ACCESS_CONTROL_ENDPOINT)) {
            //request.newBuilder().build()
            null
        } else {
            val updatedToken = getNewToken()
            request.newBuilder()
                .addHeader("Cookie", updatedToken)
                .build()
        }
    }

    private fun getNewToken(): String {
        val response = serviceClientContext.sonyService!!.refreshToken("http://" + serviceClientContext.ip + SONY_ACCESS_CONTROL_ENDPOINT,
                JsonRpcRequest.actRegister(serviceClientContext.nickname, serviceClientContext.devicename, serviceClientContext.uuid)).execute()
        if (response.code() == HTTP_OK) {
            if (!response.headers()["Set-Cookie"].isNullOrEmpty()) {
                val cookieString: String? = response.headers()["Set-Cookie"]
                val pattern =
                    Pattern.compile("auth=([A-Za-z0-9]+)")
                val matcher = pattern.matcher(cookieString)
                if (matcher.find()) {
                    tokenStore.storeToken(serviceClientContext.uuid, "auth=" + matcher.group(1))
                    /*
                    pattern = Pattern.compile("max-age=([0-9]+)")
                    matcher = pattern.matcher(cookieString)
                    if (matcher.find()) {
                        tokenRepository.expiryTime =
                            System.currentTimeMillis() + 1000 * matcher.group(1).toLong()
                    }
                    */

                }

            }
        }
        return tokenStore.loadToken(serviceClientContext.uuid)
    }
}

sealed class Resource<T>(
    val data: T? = null,
    val code: Int=0,
    val message: String? = null,
    val status: Status
) {
    class Success<T>(code: Int, data: T) : Resource<T>(data, code, status = Status.SUCCESS)
    class Loading<T>(data: T? = null) : Resource<T>(data, status = Status.LOADING)
    class Error<T>(message: String, code: Int, data: T? = null) : Resource<T>(data, code, message, Status.ERROR)
}

enum class Status {
    SUCCESS,
    ERROR,
    LOADING
}

class SonyServiceClientContext(
    var sonyService: SonyService? = null,
    var ip: String = "",
    var uuid: String = "",
    var username : String = "",
    var password : String = "",
    var nickname: String = "",
    var devicename: String = "",
    var preSharedKey : String = ""
)