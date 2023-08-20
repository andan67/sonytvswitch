package org.andan.android.tvbrowser.sonycontrolplugin.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = Constants.CHANNEL_MAP_TABLE,
    indices = arrayOf(Index("control_uuid")),
    foreignKeys = arrayOf(
        ForeignKey(entity = ControlEntity::class,
            parentColumns = arrayOf("uuid"),
            childColumns = arrayOf("control_uuid"),
            onDelete = CASCADE)
    ))
data class ChannelMapEntity(
    val control_uuid: String,
    val channelLabel: String,
    val uri: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
