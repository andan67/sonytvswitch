package org.andan.android.tvbrowser.sonycontrolplugin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CHANNEL_TABLE
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CONTROL_TABLE
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls

@Dao
interface ControlDao {
    @Query("SELECT * FROM $CONTROL_TABLE")
    fun getControls(): Flow<List<ControlEntity>>

    @Query("SELECT * FROM $CONTROL_TABLE WHERE uuid LIKE :uuid")
    fun getControl(uuid : String) : Flow<ControlEntity?>

    @Insert
    suspend fun insertControls(vararg controls: ControlEntity)

    @Transaction
    suspend fun insertFromSonyControls(sonyControls: SonyControls) {
        for ((index ,sonyControl) in sonyControls.controls.withIndex()) {
            val controlEntity = ControlEntity(
                uuid = sonyControl.uuid,
                host = sonyControl.ip,
                nickname = sonyControl.nickname,
                devicename = sonyControl.devicename,
                preSharedKey = sonyControl.preSharedKey
            )
            controlEntity.systemModel = sonyControl.systemModel
            controlEntity.systemName = sonyControl.systemName
            controlEntity.systemProduct = sonyControl.systemProduct
            controlEntity.systemMacAddr = sonyControl.systemMacAddr
            controlEntity.isActive = index == sonyControls.selected
            var channelList : MutableList<ChannelEntity> = mutableListOf()
            for (sonyChannel in sonyControl.channelList) {
                val channelEntity = ChannelEntity(
                    displayNumber = sonyChannel.dispNumber,
                    control_uuid = sonyControl.uuid,
                    source = sonyChannel.source,
                    index = sonyChannel.index,
                    mediaType = sonyChannel.mediaType,
                    title = sonyChannel.title,
                    uri = sonyChannel.uri
                )
                channelList.add(channelEntity)
            }
            insertControls(controlEntity)
            insertChannels(channelList)
        }
    }

    @Delete
    suspend fun deleteControl(control: ControlEntity)

    @Update
    suspend fun update(control: ControlEntity)

    @Query("SELECT * FROM $CHANNEL_TABLE WHERE control_uuid LIKE :uuid")
    fun getChannels(uuid : String) : Flow<List<ChannelEntity>>

    fun getChannelEntitiesForControl(controlEntity: ControlEntity) : Flow<List<ChannelEntity>> {
        return getChannels(controlEntity.uuid)
    }

    @Query("UPDATE $CONTROL_TABLE SET isActive = CASE WHEN uuid = :uuid THEN 1 ELSE 0 END")
    suspend fun setActiveControl(uuid : String)

    @Query("SELECT * FROM $CONTROL_TABLE WHERE isActive = 1")
    fun getActiveControl() : Flow<ControlEntity>

    suspend fun setActiveControl(controlEntity: ControlEntity) {
        return setActiveControl(controlEntity.uuid)
    }

    @Insert
    suspend fun insertChannels( channelList :List<ChannelEntity>)

    @Delete
    suspend fun deleteChannels( channelList :List<ChannelEntity>)


    @Query("DELETE FROM $CHANNEL_TABLE WHERE control_uuid LIKE :uuid")
    suspend fun deleteChannelsForControlUuid(uuid: String)

    suspend fun deleteChannelsForControl(controlEntity: ControlEntity) {
        deleteChannelsForControlUuid(controlEntity.uuid)
    }

    @Transaction
    suspend fun setChannelsForControl( channelList :List<ChannelEntity>, controlEntity: ControlEntity) {
        deleteChannelsForControl(controlEntity)
        insertChannels(channelList.filter { channelEntity -> channelEntity.control_uuid == controlEntity.uuid })
    }

}