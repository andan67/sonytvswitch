package org.andan.android.tvbrowser.sonycontrolplugin.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import okhttp3.*
import org.andan.android.tvbrowser.sonycontrolplugin.datastore.TokenStore
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.net.HttpURLConnection.HTTP_OK
import java.util.regex.Pattern
import javax.inject.Inject

interface SonyService {
    @POST
    suspend fun sonyRpcService(
        @Url url: String,
        @Body rpcRequest: JsonRpcRequest
    ): Response<JsonRpcResponse>

    @POST
    suspend fun sendIRCC(@Url url: String, @Body requestBody: RequestBody): Response<Unit>

    @POST
    fun refreshToken(@Url url: String, @Body rpcRequest: JsonRpcRequest): Call<JsonRpcResponse>
}

class AddTokenInterceptor @Inject constructor(
    private val serviceClientContext: SonyServiceClientContext?,
    private val tokenStore: TokenStore
) : Interceptor {

    /**
     * Interceptor class for setting of the headers for every request
     */
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        //request = request.newBuilder().addHeader("Cookie", tokenStore.getToken()).build()
        val builder = request.newBuilder()
        if (serviceClientContext!!.preSharedKey.isEmpty()) builder.addHeader(
            "Cookie",
            tokenStore.loadToken(serviceClientContext.uuid)
        )
        else builder.addHeader("X-Auth-PSK", serviceClientContext.preSharedKey)
        if (serviceClientContext.password.isNotEmpty()) builder.addHeader(
            "Authorization",
            Credentials.basic(serviceClientContext.username, serviceClientContext.password)
        )
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
        Log.d(TAG, "authenticate(): ${serviceClientContext.nickname}")
        val request = response.request
        //bypass authenticator for registration endpoint
        if (response.request.header("Authorization") != null) {
            return null // Give up, we've already attempted to authenticate.
        }
        return if (serviceClientContext.password.isEmpty() && request.url.toString()
                .endsWith(SonyServiceUtil.SONY_ACCESS_CONTROL_ENDPOINT)
        ) {
            null
        } else {
            val updatedToken = getNewToken()
            request.newBuilder()
                .addHeader("Cookie", updatedToken)
                .build()
        }
    }

    private fun getNewToken(): String {
        val response = serviceClientContext.sonyService!!.refreshToken(
            "http://" + serviceClientContext.ip + SonyServiceUtil.SONY_ACCESS_CONTROL_ENDPOINT,
            JsonRpcRequest.actRegister(
                serviceClientContext.nickname,
                serviceClientContext.devicename,
                serviceClientContext.uuid
            )
        ).execute()
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
    val code: Int = 0,
    val message: String? = null,
    val status: Status
) {
    class Success<T>(code: Int, data: T) : Resource<T>(data, code, status = Status.SUCCESS)
    class Loading<T>(data: T? = null) : Resource<T>(data, status = Status.LOADING)
    class Error<T>(message: String, code: Int, data: T? = null) :
        Resource<T>(data, code, message, Status.ERROR)
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
    var username: String = "",
    var password: String = "",
    var nickname: String = "",
    var devicename: String = "",
    var preSharedKey: String = ""
)

data class RegistrationStatus(val code: Int, val message: String) {
    companion object {
        const val REGISTRATION_SUCCESSFUL = 0
        const val REGISTRATION_REQUIRES_CHALLENGE_CODE = 1
        const val REGISTRATION_UNAUTHORIZED = 2
        const val REGISTRATION_ERROR_NON_FATAL = 3
        const val REGISTRATION_ERROR_FATAL = 4
    }
}

object SonyServiceUtil {
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

    val gson = GsonBuilder().create()

    inline fun <reified T> apiCall(call: () -> Response<JsonRpcResponse>): Resource<T> {
        //val response: Response<T>
        return try {
            val response = call.invoke()
            if (response.isSuccessful) {
                val jsonRpcResponse = response.body()
                when {
                    jsonRpcResponse?.error != null -> {
                        Log.d("apiCall", "evaluate error")
                        Resource.Error(
                            jsonRpcResponse.error.asJsonArray.get(1).asString, response.code()
                        )
                    }
                    else -> {
                        Log.d("apiCall", "evaluate result")
                        Resource.Success(
                            response.code(),
                            gson.fromJson(
                                when {
                                    jsonRpcResponse?.result?.asJsonArray?.size() == 0 -> {
                                        JsonObject()
                                    }
                                    jsonRpcResponse?.result?.asJsonArray?.size()!! > 1 -> {
                                        jsonRpcResponse.result.asJsonArray?.get(1)
                                    }
                                    else -> {
                                        when (jsonRpcResponse.result.asJsonArray?.get(0)) {
                                            is JsonObject -> jsonRpcResponse.result.asJsonArray?.get(
                                                0
                                            )!!.asJsonObject
                                            is JsonArray -> jsonRpcResponse.result.asJsonArray?.get(
                                                0
                                            )!!.asJsonArray
                                            else -> jsonRpcResponse.result.asJsonArray.get(0).asJsonObject
                                        }
                                    }
                                }, T::class.java
                            )
                        )
                    }
                }
            } else {
                Log.d("apiCall", "evaluate response unsuccessful")
                Resource.Error(response.message(), response.code())
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("apiCall", "evaluate exception ${e.message}")
            return Resource.Error(e.message ?: "Unknown apiCall exception", 0)
        }
    }
}
