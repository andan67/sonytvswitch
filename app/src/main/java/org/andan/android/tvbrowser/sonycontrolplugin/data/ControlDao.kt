package org.andan.android.tvbrowser.sonycontrolplugin.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CHANNEL_MAP_TABLE
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CHANNEL_TABLE
import org.andan.android.tvbrowser.sonycontrolplugin.data.Constants.CONTROL_TABLE
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls

@Dao
interface ControlDao {
    @Query("SELECT * FROM $CONTROL_TABLE")
    fun getControls(): Flow<List<ControlEntity>>

    @Query("SELECT * FROM $CONTROL_TABLE WHERE uuid LIKE :uuid")
    fun getControl(uuid : String) : Flow<ControlEntity?>

    @Query("SELECT * FROM $CONTROL_TABLE")
    fun getControlsWithChannels(): Flow<List<ControlEntityWithChannels>>

    @Query("SELECT * FROM $CONTROL_TABLE WHERE uuid LIKE :uuid")
    fun getControlWithChannels(uuid : String) : Flow<ControlEntityWithChannels?>

    @Query("SELECT * FROM $CONTROL_TABLE WHERE isActive = 1")
    fun getActiveControl() : Flow<ControlEntity>

    @Insert
    suspend fun insertControlWithChannels(controlEntity: ControlEntity,
                                          channels: List<ChannelEntity>,
                                          channelMaps: List<ChannelMapEntity>)

    @Transaction
    @Query("SELECT * FROM $CONTROL_TABLE WHERE isActive = 1")
    fun getActiveControlWithChannels() : Flow<ControlEntityWithChannels>

    suspend fun setActiveControl(controlEntity: ControlEntity) {
        return setActiveControl(controlEntity.uuid)
    }

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
            controlEntity.sourceList = sonyControl.sourceList
            controlEntity.commandMap = sonyControl.commandList
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
            var channelMapList : MutableList<ChannelMapEntity> = mutableListOf()
            for(entry in sonyControl.channelMap) {
                val channelMapEntity = ChannelMapEntity(control_uuid = sonyControl.uuid,
                channelLabel = entry.key, uri = entry.value)
                channelMapList.add(channelMapEntity)
            }
            /*
            insertControls(controlEntity)
            insertChannels(channelList)
            insertChannelMaps(channelMapList)
             */
            insertControlWithChannels(controlEntity, channelList, channelMapList)
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

    @Query("SELECT * FROM $CHANNEL_MAP_TABLE WHERE control_uuid LIKE :uuid")
    fun getChannelMapEntities(uuid : String) : Flow<List<ChannelMapEntity>>

    @Insert
    suspend fun insertChannelMaps( channelMapList :List<ChannelMapEntity>)

    @Delete
    suspend fun deleteChannelMaps( channelMapList :List<ChannelMapEntity>)

    @Query("DELETE FROM $CHANNEL_TABLE WHERE control_uuid LIKE :uuid")
    suspend fun deleteChannelMapsForControlUuid(uuid: String)

    suspend fun deleteChannelMapsForControl(controlEntity: ControlEntity) {
        deleteChannelMapsForControlUuid(controlEntity.uuid)
    }
}