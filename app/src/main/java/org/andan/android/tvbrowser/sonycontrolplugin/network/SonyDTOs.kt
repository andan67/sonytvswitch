package org.andan.android.tvbrowser.sonycontrolplugin.network

import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyProgram
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


data class PlayingContentInfoResponse(
    val source: String,
    val dispNum: String,
    val programMediaType: String,
    val title: String,
    val uri: String,
    val programTitle: String,
    val startDateTime: String,
    val durationSec: Long
)

fun PlayingContentInfoResponse.asDomainModel() : PlayingContentInfo {
    return PlayingContentInfo(
        source, dispNum, programMediaType, title, uri, programTitle, startDateTime, durationSec
    )
}

data class ContentListItemResponse(val dispNum: String, val index : Int, val programMediaType: String, val title: String, val uri: String )

fun ContentListItemResponse.asDomainModel() : SonyProgram {
    return SonyProgram("", dispNum, index, programMediaType, title, uri)
}

data class SourceListItemResponse(val source: String)

data class SystemInformationResponse(val product: String, val name: String, val model: String, val macAddr: String)

data class WolModeResponse(val enabled: Boolean)

data class PowerStatusResponse(val status: Boolean)

data class PowerSavingModeResponse(val mode: Boolean)

data class RemoteControllerInfoItemResponse(val name: String, val value: String)
