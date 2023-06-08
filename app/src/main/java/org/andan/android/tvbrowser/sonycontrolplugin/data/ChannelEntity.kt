package org.andan.android.tvbrowser.sonycontrolplugin.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(tableName = Constants.CHANNEL_TABLE,
    foreignKeys = arrayOf(
        ForeignKey(entity = ControlEntity::class,
            parentColumns = arrayOf("uuid"),
            childColumns = arrayOf("control_uuid"),
            onDelete = CASCADE)
    ))
data class ChannelEntity(
    @PrimaryKey()
    val displayNumber: String,
    val control_uuid: String,
    val source: String,
    val index: Int,
    val mediaType: String,
    var title: String,
    val uri: String
)
