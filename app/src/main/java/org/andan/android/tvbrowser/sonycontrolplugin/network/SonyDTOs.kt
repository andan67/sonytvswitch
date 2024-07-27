package org.andan.android.tvbrowser.sonycontrolplugin.network

import org.andan.android.tvbrowser.sonycontrolplugin.domain.PlayingContentInfo
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel

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
  fun toPlayingContentInfo(): PlayingContentInfo {
    return PlayingContentInfo(
        source ?: "",
        dispNum ?: "----",
        programMediaType ?: "",
        title ?: "Not available",
        uri ?: "",
        programTitle ?: "",
        startDateTime ?: "",
        durationSec ?: 0)
  }
}

data class ContentListItemResponse(
    val dispNum: String,
    val index: Int,
    val programMediaType: String,
    val title: String,
    val uri: String
) {
  fun toSonyChannel(source: String = ""): SonyChannel {
    return SonyChannel(source, dispNum, index, programMediaType, title, uri)
  }
}

data class SourceListItemResponse(val source: String)

data class SystemInformationResponse(
    val product: String,
    val name: String,
    val model: String,
    val macAddr: String
)

data class InterfaceInformationResponse(
    val interfaceVersion: String = "",
    val modelName: String = "",
    val productCategory: String = "",
    val productName: String = "",
    val serverName: String = ""
)

data class WolModeResponse(val enabled: Boolean)

data class PowerStatusResponse(val status: Boolean)

data class PowerSavingModeResponse(val mode: Boolean)

data class RemoteControllerInfoItemResponse(val name: String, val value: String)
