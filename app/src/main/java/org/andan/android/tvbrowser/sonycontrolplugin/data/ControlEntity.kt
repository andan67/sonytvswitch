package org.andan.android.tvbrowser.sonycontrolplugin.data

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import okhttp3.internal.immutableListOf
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CONTROL_TABLE
import java.util.LinkedHashMap

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
    var sourceList = immutableListOf<String>()
    var commandMap = mapOf <String, String>()
    var isActive : Boolean = false
}

data class ControlEntityWithChannels(
    @Embedded val control: ControlEntity,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "control_uuid"
    )
    val channels: List<ChannelEntity>,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "control_uuid"
    )
    val channelMaps: List<ChannelMapEntity>
)