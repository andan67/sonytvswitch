package org.andan.android.tvbrowser.sonycontrolplugin.domain

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.net.URLDecoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashMap

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
    var ip: String = "",
    var nickname: String = "",
    var devicename: String= "",
    var preSharedKey: String = "",
    val uuid: String = java.util.UUID.randomUUID().toString()
) {

    companion object {
        private val gson = Gson()
        fun fromJson(json: String) = gson.fromJson(json, SonyControl::class.java)
        const val PAGE_SIZE = 50
    }


    @SerializedName(value = "channelList", alternate = ["programList"])
    var channelList: List<SonyChannel> = listOf()
        set(value) {
            _uriSonyChannelMap = LinkedHashMap()
            field = value
        }

    var cookie = ""
    var sourceList: List<String> = listOf()
    var systemModel = ""
    var systemName = ""
    var systemProduct = ""
    var systemMacAddr = ""
    var systemWolMode = true
    var commandList: Map<String, String> = mapOf()

    @Transient
    var isActive = false

    
    @Transient
    private var _uriSonyChannelMap: LinkedHashMap<String, SonyChannel> = LinkedHashMap()

    @Transient
    val uriSonyChannelMap: LinkedHashMap<String, SonyChannel> = LinkedHashMap()
        get() {
            // lazy construction
            if (_uriSonyChannelMap .isEmpty()) {
                for (channel in channelList) {
                    _uriSonyChannelMap[channel.uri] = channel
                }
            }
            // This is a workaround to allow transient annotation (why?)
            // Note that field as implicit backing field is null, so explicit backing value is returned
            return field
        }

    @SerializedName(value = "channelMap", alternate = ["channelProgramMap"])
    var channelMap: Map<String, String> = mapOf()

    override fun toString(): String {
        return "$nickname ($devicename)"
    }

}

data class SonyChannel(
    var source: String,
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
    val title: String = "Not available\"",
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