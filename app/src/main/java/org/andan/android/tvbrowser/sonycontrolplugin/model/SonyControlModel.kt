package org.andan.android.tvbrowser.sonycontrolplugin.model

import org.andan.av.sony.model.SonyProgram
import java.util.*
import kotlin.collections.ArrayList

data class SonyControl(val ip: String, val nickname: String, val devicename: String, val uuid: String = java.util.UUID.randomUUID().toString()) {

    private var _programUriMap : LinkedHashMap<String, SonyProgram2>? = null

    var programList : List<SonyProgram2> = emptyList()
    set(value) {
        _programUriMap=null
        field = value
    }

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

    val channelProgramMap = LinkedHashMap<String, String>()

}

data class SonyProgram2(val source: String, val dispNumber: String, val index : Int, val mediaType: String, val title: String, val uri: String ) {

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