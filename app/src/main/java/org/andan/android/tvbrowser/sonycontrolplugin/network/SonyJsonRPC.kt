package org.andan.android.tvbrowser.sonycontrolplugin.network

import com.google.gson.JsonElement
import java.util.*

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

        fun getInterfaceInformation(): JsonRpcRequest {
            val params = ArrayList<Any>()
            return JsonRpcRequest(33, "getInterfaceInformation", params)
        }

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

        fun setPowerSavingMode(mode: String): JsonRpcRequest {
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