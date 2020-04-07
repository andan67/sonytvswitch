package org.andan.android.tvbrowser.sonycontrolplugin.domain

import com.google.gson.Gson
import org.andan.android.tvbrowser.sonycontrolplugin.network.RemoteControllerInfoItemResponse
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

data class SonyControls(val controls: MutableList<SonyControl> = ArrayList(), var selected: Int = -1) {

    fun toJson() : String = gson.toJson(this)

    companion object {
        private val gson = Gson()
        fun fromJson(json: String) = gson.fromJson(json, SonyControls::class.java)
    }
}

data class SonyControl(val ip: String, val nickname: String, val devicename: String, val uuid: String = java.util.UUID.randomUUID().toString()) {

    companion object {
        private val gson = Gson()
        fun fromJson(json: String) = gson.fromJson(json, SonyControl::class.java)
        const val PAGE_SIZE = 25
    }

    private var _programUriMap : LinkedHashMap<String, SonyProgram2>? = null

    var programList= mutableListOf<SonyProgram2>()
    set(value) {
        _programUriMap=null
        field = value
    }

    var cookie = ""
    var sourceList= mutableListOf<String>()
    var systemModel = ""
    var systemName = ""
    var systemProduct = ""
    var systemMacAddr = ""
    var systemWolMode = true
    var commandList = mutableListOf<HashMap<String,String>>()
    //var commandList = mutableListOf<RemoteControllerInfoItemResponse>()

    @Transient
    val programUriMap : LinkedHashMap<String, SonyProgram2>? = null
    get() {
        // lazy construction
        if(_programUriMap == null) {
            _programUriMap = LinkedHashMap<String, SonyProgram2>()
            for(program in programList) {
                _programUriMap!![program.uri] = program
            }
        }
        // This is a workaround to allow transient annotation (why?)
        // Note that field as implicit backing field is null, so explicit backing value is returned
        return field?: _programUriMap
    }

    var channelProgramMap = LinkedHashMap<String, String>()

    override fun toString(): String {
        return "$nickname ($devicename)"
    }

}

data class SonyProgram2(var source: String, val dispNumber: String, val index : Int, val mediaType: String, val title: String, val uri: String ) {

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
    val dispNum: String = "",
    val programMediaType: String = "",
    val title: String = "N/A",
    val uri: String = "",
    val programTitle: String = "N/A",
    val startDateTime: String = "",
    val durationSec: Long = 0
)