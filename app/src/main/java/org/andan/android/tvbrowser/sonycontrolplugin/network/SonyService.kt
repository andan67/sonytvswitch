package org.andan.android.tvbrowser.sonycontrolplugin.network

import com.google.gson.JsonElement
import okhttp3.*
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.TokenStore
import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.net.HttpURLConnection.HTTP_OK
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject

const val SONY_AV_CONTENT_ENDPOINT = "/sony/avContent"
const val SONY_ACCESS_CONTROL_ENDPOINT = "/sony/accessControl"

interface SonyService {
    @POST("/sony/system")
    suspend fun system(@Body rpcRequest: JsonRpcRequest): JsonRpcResponse

    /*@POST("/sony/avContent")
    suspend fun avContent(@Body rpcRequest: JsonRpcRequest): Response<JsonRpcResponse>*/


    //POST("/sony/avContent")
    @POST
    suspend fun avContent(@Url url: String, @Body rpcRequest: JsonRpcRequest): Response<JsonRpcResponse>

    //@POST("/sony/accessControl")
    @POST
    suspend fun accessControl(@Url url: String, @Body rpcRequest: JsonRpcRequest): Response<JsonRpcResponse>

    //@POST("/sony/accessControl")
    @POST
    fun refreshToken(@Url url: String, @Body rpcRequest: JsonRpcRequest): Call<JsonRpcResponse>
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

data class PlayingContentInfoResponse(
    val source: String,
    val dispNumber: String,
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
    }

    fun getStartDateTimeFormatted(): String? {
        return try {
            val date = sdfInput.parse(startDateTime)
            DateTimeFormat.format(date)
        } catch (e: ParseException) {
            startDateTime
        }
    }

    fun getEndDateTimeFormatted(): String? {
        return try {
            val date = sdfInput.parse(startDateTime)
            cal.time = date
            cal.add(Calendar.SECOND, durationSec.toInt())
            DateTimeFormat.format(cal.time)
        } catch (e: ParseException) {
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
        } catch (e: ParseException) {
            ""
        }
    }

}

fun PlayingContentInfoResponse.asDomainModel() : PlayingContentInfo{
    return PlayingContentInfo(
        source, dispNumber, programMediaType, title, uri, programTitle, startDateTime, durationSec
    )
}

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

        if (serviceClientContext != null) {
            val response =
                serviceClientContext.sonyService!!.refreshToken("http://" + serviceClientContext.ip + SONY_ACCESS_CONTROL_ENDPOINT, JsonRpcRequest(8, "actRegister", params)).execute()
            if (response.code() == HTTP_OK) {
                if (!response.headers()["Set-Cookie"].isNullOrEmpty()) {
                    val cookieString: String? = response.headers()["Set-Cookie"]
                    var pattern =
                        Pattern.compile("auth=([A-Za-z0-9]+)")
                    var matcher = pattern.matcher(cookieString)
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
    var preSharedKey : String = ""
)