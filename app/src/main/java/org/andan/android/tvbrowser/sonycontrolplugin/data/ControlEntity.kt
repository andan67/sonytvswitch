package org.andan.android.tvbrowser.sonycontrolplugin.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CONTROL_TABLE

@Entity(tableName = CONTROL_TABLE)
data class ControlEntity(
    @PrimaryKey()
    val uuid: String,
    val host: String = "",
    val nickname: String = "",
    val devicename: String = "",
    val preSharedKey: String = "",
) {
    var cookie = ""
    var systemModel = ""
    var systemName = ""
    var systemProduct = ""
    var systemMacAddr = ""
    var systemWolMode = true
    var isActive : Boolean = false
}