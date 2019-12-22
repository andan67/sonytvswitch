package org.andan.android.tvbrowser.sonycontrolplugin

import android.app.IntentService
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.andan.av.sony.SonyIPControl
import java.net.HttpURLConnection

class SonyIPControlIntentService : IntentService("SonyIPControlIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        Log.i(TAG, "onHandleIntent")
        if (intent != null) {
            val action = intent.getIntExtra(ACTION, NO_ACTION)
            var ipControl: SonyIPControl? = null
            try {
                val control = intent.getStringExtra(CONTROL)
                ipControl = SonyIPControl(control)
            } catch (ex: Exception) {
                Log.e(TAG, ex.localizedMessage)
            }

            if (ipControl != null) {
                when (action) {
                    SEND_IRCC_BY_NAME_ACTION, SEND_IRCC_BY_CODE_ACTION, SEND_CHANNEL_SWITCH_ACTION -> {
                        val code = intent.getStringExtra(CODE)
                        sendIRRC(ipControl, action, code)
                    }
                    REGISTER_CONTROL_ACTION -> {
                        val code = intent.getStringExtra(CODE)
                        registerControl(ipControl, action, code)
                    }
                    SET_PROGRAM_LIST_ACTION -> setProgramListAction(ipControl, action)
                    SET_PLAY_CONTENT_ACTION -> {
                        val uri = intent.getStringExtra(URI)
                        setPlayContent(ipControl, action, uri, false)
                    }
                    SET_AND_GET_PLAY_CONTENT_ACTION -> {
                        val uri = intent.getStringExtra(URI)
                        setPlayContent(ipControl, action, uri, true)

                    }
                    ENABLE_WOL_ACTION -> {
                        enableWakeOnLan(ipControl, action)
                    }
                    GET_PLAYING_CONTENT_INFO_ACTION -> getPlayingContentInfo(ipControl, action)
                    WOL_ACTION -> wakeOnLan(ipControl, action)
                    SCREEN_OFF_ACTION -> setPowerSavingMode(ipControl, action, "off")
                    SCREEN_ON_ACTION -> setPowerSavingMode(ipControl, action, "pictureOff")
                }
            }
            Log.i(TAG, "onHandleIntent:end")
        }
    }

    private fun sendIRRC(ipControl: SonyIPControl, action: Int, code: String) {
        Log.i(TAG, "sendIRCC:$code")

        if (action == SEND_CHANNEL_SWITCH_ACTION) {
            for (c in code.toCharArray()) {
                if (Character.isDigit(c)) {
                    ipControl.sendIRCCByName("Num$c")
                    try {
                        synchronized(ipControl) {
                            Thread.sleep(100)
                        }
                    } catch (ex: Exception) {
                    }

                }
            }
        } else if (action == SEND_IRCC_BY_CODE_ACTION)
            ipControl.sendIRCC(code)
        else if (action == SEND_IRCC_BY_NAME_ACTION) ipControl.sendIRCCByName(code)
    }

    private fun registerControl(ipControl: SonyIPControl, action: Int, code: String?) {
        Log.i(TAG, "registerControl:$code")
        val response = ipControl.registerRemoteControl(code)
        var result = RESULT_OK
        if (response.hasError()) {
            if (response.responseErrorOrStatusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                result = RESULT_AUTH_REQUIRED
            } else
                result = RESULT_IOERROR
        }
        ipControl.setCodeMapFromRemoteControllerInfo()
        Log.i(TAG, "registerControl:" + SonyIPControl.getGson().toJson(ipControl.toJSON()))
        broadcastEvent(
            action,
            result,
            response.responseErrorOrStatusMessage,
            SonyIPControl.getGson().toJson(ipControl.toJSON())
        )
    }

    private fun setProgramListAction(ipControl: SonyIPControl, action: Int) {
        Log.i(TAG, "setProgramListAction:")
        ipControl.setProgramListFromTV()
        var result = RESULT_UNKNOWN
        if (ipControl.programList != null) result = RESULT_OK
        Log.i(TAG, "setProgramListAction:" + SonyIPControl.getGson().toJson(ipControl.toJSON()))
        broadcastEvent(action, result, "", SonyIPControl.getGson().toJson(ipControl.toJSON()))
    }

    private fun setPlayContent(
        ipControl: SonyIPControl,
        action: Int,
        uri: String,
        callPlayingContent: Boolean
    ) {
        Log.i(TAG, "setPlayContent:$uri")
        val response = ipControl.setPlayContent(uri)
        var result = RESULT_OK
        if (response.hasError()) result = response.responseErrorOrStatusCode
        broadcastEvent(
            action, result, response.responseErrorOrStatusMessage,
            SonyIPControl.getGson().toJson(ipControl.toJSON())
        )
        if (callPlayingContent) {
            getPlayingContentInfo(ipControl, GET_PLAYING_CONTENT_INFO_ACTION)
        }
    }

    private fun getPlayingContentInfo(ipControl: SonyIPControl, action: Int) {
        Log.i(TAG, "getPlayingContentInfo")
        val sonyPlayingContentInfo = ipControl.playingContentInfo
        val intent = Intent(ACTION)
        intent.putExtra(ACTION, action)
        intent.putExtra(
            PLAYING_CONTENT_INFO,
            SonyIPControl.getGson().toJson(sonyPlayingContentInfo).toString()
        )
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun wakeOnLan(ipControl: SonyIPControl, action: Int) {
        Log.i(TAG, "wakeOnLan")
        val result = ipControl.wakeOnLan()
        val message = if (result == 0) "" else "Wake on LAN failed"
        broadcastEvent(action, result, message, SonyIPControl.getGson().toJson(ipControl.toJSON()))
    }

    private fun enableWakeOnLan(ipControl: SonyIPControl, action: Int) {
        Log.i(TAG, "enableWakeOnLan")
        val response = ipControl.setWolMode(true)
        var result = RESULT_OK
        if (response.hasError()) result = response.responseErrorOrStatusCode
        broadcastEvent(
            action, result, response.responseErrorOrStatusMessage,
            SonyIPControl.getGson().toJson(ipControl.toJSON())
        )
    }

    private fun setPowerSavingMode(ipControl: SonyIPControl, action: Int, mode: String) {
        Log.i(TAG, "setPowerSavingMode")
        val response = ipControl.setPowerSavingMode(mode)
        var result = RESULT_OK
        if (response.hasError()) result = response.responseErrorOrStatusCode
        broadcastEvent(
            action, result, response.responseErrorOrStatusMessage,
            SonyIPControl.getGson().toJson(ipControl.toJSON())
        )
    }

    private fun broadcastEvent(
        action: Int,
        resultCode: Int,
        resultMessage: String,
        control: String
    ) {
        val intent = Intent(ACTION)
        intent.putExtra(ACTION, action)
        intent.putExtra(RESULT_CODE, resultCode)
        intent.putExtra(RESULT_MESSAGE, resultMessage)
        intent.putExtra(CONTROL, control)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        const val CONTROL = "CONTROL"
        const val PLAYING_CONTENT_INFO = "PLAYING_CONTENT_INFO"
        const val CODE = "CODE"
        const val URI = "URI"
        const val ACTION = "ACTION"
        const val RESULT_CODE = "RESULT_CODE"
        const val RESULT_MESSAGE = "RESULT_MESSAGE"
        const val NO_ACTION = -1
        const val SEND_IRCC_BY_NAME_ACTION = 0
        const val SEND_IRCC_BY_CODE_ACTION = 1
        const val SEND_CHANNEL_SWITCH_ACTION = 2
        const val REGISTER_CONTROL_ACTION = 3
        const val SET_PROGRAM_LIST_ACTION = 4
        const val SET_PLAY_CONTENT_ACTION = 5
        const val GET_PLAYING_CONTENT_INFO_ACTION = 6
        const val SET_AND_GET_PLAY_CONTENT_ACTION = 7
        const val WOL_ACTION = 8
        const val ENABLE_WOL_ACTION = 9
        const val SCREEN_ON_ACTION = 10
        const val SCREEN_OFF_ACTION = 11
        const val RESULT_OK = 0
        const val RESULT_AUTH_REQUIRED = 1
        const val RESULT_IOERROR = 3
        const val RESULT_UNKNOWN = -1
        // TODO: Rename parameters
        private val TAG = SonyIPControlIntentService::class.java.simpleName
    }


}