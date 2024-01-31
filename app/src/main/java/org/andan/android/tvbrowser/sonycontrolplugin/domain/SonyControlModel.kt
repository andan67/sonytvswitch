package org.andan.android.tvbrowser.sonycontrolplugin.domain

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.net.URLDecoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar

data class SonyControls(
    val controls: MutableList<SonyControl> = ArrayList(),
    var selected: Int = -1
) {

    fun toJson(): String = gson.toJson(this)

    companion object {
        private val gson = Gson()
        fun fromJson(json: String) = gson.fromJson(json, SonyControls::class.java)
    }
}

data class SonyControl(
    val ip: String = "",
    val nickname: String = "",
    val devicename: String = "",
    val preSharedKey: String = "",
    val uuid: String = java.util.UUID.randomUUID().toString(),
    val cookie: String = "",
    @Transient
    val isActive: Boolean = false,
    @SerializedName(value = "channelList", alternate = ["programList"])
    val channelList: List<SonyChannel> = listOf(),
    @SerializedName(value = "channelMap", alternate = ["channelProgramMap"])
    val channelMap: Map<String, String> = mapOf(),
    val sourceList: List<String> = listOf(),
    val commandMap: Map<String, String> = mapOf(),
    val systemModel: String = "",
    val systemName: String = "",
    val systemProduct: String = "",
    val systemMacAddr: String = "",
    val systemWolMode: Boolean = true
) {

    companion object {
        private val gson = Gson()
        fun fromJson(json: String) = gson.fromJson(json, SonyControl::class.java)
        const val PAGE_SIZE = 50
    }

    @Transient
    private var _uriSonyChannelMap: LinkedHashMap<String, SonyChannel> = LinkedHashMap()

    //@Transient
    val uriSonyChannelMap: LinkedHashMap<String, SonyChannel> by lazy {
        val uriMap: LinkedHashMap<String, SonyChannel> = LinkedHashMap()
        if (uriMap.isEmpty()) {
            for (channel in channelList) {
                uriMap[channel.uri] = channel
            }
        }
        uriMap
    }

    val channelReverseMap: Map<String, String> by lazy {
        channelMap.map { (k, v) -> v to k }.toMap()
    }

    val sonyChannelTitleList: List<String> by lazy {
        val titleList: MutableList<String> = ArrayList()
        if (titleList.isEmpty()) {
            for (channel in channelList) {
                titleList.add(channel.title)
            }
        }
        titleList
    }


    override fun toString(): String {
        return "$nickname ($devicename)"
    }
}

data class SonyChannel(
    val source: String,
    val dispNumber: String,
    val index: Int,
    val mediaType: String,
    val title: String,
    val uri: String
) {

    companion object {
        fun <T> fromUri(uri: String): SonyChannel {
            // example uri"tv:dvbs?trip=d1.1107.17500&srvName=dSAT.1"
            return SonyChannel(
                uri.substringBefore("?"),
                "",
                -1,
                uri.substringBefore(":"),
                URLDecoder.decode(uri.substringAfter("srvName="), "UTF-8"),
                uri
            )
        }
    }

    constructor(playingContentInfo: PlayingContentInfo):
            this(playingContentInfo.source,
                playingContentInfo.dispNum,
                0,
                playingContentInfo.programMediaType,
                playingContentInfo.title,
                playingContentInfo.uri)

    val shortSource: String
        get() {
            var i2 = source.indexOf("#")
            if (i2 < 0) i2 = source.length
            val i1 = source.indexOf(":") + 1
            return source.substring(i1, i2)
        }

    val type: String
        get() {
            val i2 = source.indexOf("#")
            return if (i2 < 0) "" else "(" + source.substring(i2 + 1) + ")"
        }

    val sourceWithType: String
        get() {
            return "$shortSource $type"
        }
}

data class PlayingContentInfo(
    val source: String = "",
    val dispNum: String = "----",
    val programMediaType: String = "",
    val title: String = "Not available",
    val uri: String = "",
    val programTitle: String = "",
    val startDateTime: String = "",
    val durationSec: Long = 0
) {

    companion object {
        private val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ")
        private val DateTimeFormat =
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT)
        private val TimeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT)
        private val cal = Calendar.getInstance()
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