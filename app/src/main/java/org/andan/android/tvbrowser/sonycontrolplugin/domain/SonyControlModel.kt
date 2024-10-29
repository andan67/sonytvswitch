package org.andan.android.tvbrowser.sonycontrolplugin.domain

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    @Transient val isActive: Boolean = false,
    @SerializedName(value = "channelList", alternate = ["programList"])
    val channelList: List<SonyChannel> = listOf(),
    @SerializedName(value = "channelMap", alternate = ["channelProgramMap"])
    val channelMap: Map<String, String> = mapOf(),
    val sourceList: List<String> = listOf(),
    @SerializedName(value = "commandMap", alternate = ["commandList"])
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

  // @Transient
  val uriSonyChannelMap: LinkedHashMap<String, SonyChannel> by lazy {
    val uriMap: LinkedHashMap<String, SonyChannel> = LinkedHashMap()
    if (uriMap.isEmpty()) {
      for (channel in channelList) {
        uriMap[channel.uri] = channel
      }
    }
    uriMap
  }

  val channelReverseMap: Map<String, String> by lazy { channelMap.map { (k, v) -> v to k }.toMap() }

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

  companion object {}

  constructor(
      playingContentInfo: PlayingContentInfo
  ) : this(
      playingContentInfo.source,
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
    @SuppressLint("SimpleDateFormat")
    private val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZZZZ")
    private val cal = Calendar.getInstance()
  }

  fun getStartDateTimeFormatted(): String? {
    return try {
      sdfInput.parse(startDateTime)?.let {
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT, Locale.getDefault())
            .format(it)
      }
    } catch (e: Exception) {
      ""
    }
  }

  fun getEndDateTimeFormatted(): String? {
    return try {
      sdfInput.parse(startDateTime)?.let {
        cal.time = it
        cal.add(Calendar.SECOND, durationSec.toInt())
        DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.DEFAULT, Locale.getDefault())
            .format(cal.time)
      }
    } catch (e: Exception) {
      ""
    }
  }

  fun getStartEndTimeFormatted(): String? {
    return try {
      sdfInput.parse(startDateTime)?.let {
        val startTime = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(it)
        cal.time = it
        cal.add(Calendar.SECOND, durationSec.toInt())
        val endTime =
            DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault()).format(cal.time)
        "$startTime - $endTime"
      }
    } catch (e: Exception) {
      ""
    }
  }
}
