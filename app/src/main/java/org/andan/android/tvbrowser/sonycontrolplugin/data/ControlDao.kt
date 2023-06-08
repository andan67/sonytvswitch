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

@Dao
interface ControlDao {
    @Query("SELECT * FROM $CONTROL_TABLE")
    fun getAll(): Flow<List<ControlEntity>>

    @Query("SELECT * FROM $CONTROL_TABLE WHERE uuid LIKE :uuid")
    fun getControlEntity(uuid : String) : Flow<ControlEntity?>

    @Insert
    suspend fun insertAll(vararg controls: ControlEntity)

    @Delete
    suspend fun delete(control: ControlEntity)

    @Update
    suspend fun update(control: ControlEntity)

    @Query("SELECT * FROM $CHANNEL_TABLE WHERE control_uuid LIKE :uuid")
    fun getChannelEntities(uuid : String) : Flow<List<ChannelEntity>>

    fun getChannelEntitiesForControl(controlEntity: ControlEntity) : Flow<List<ChannelEntity>> {
        return getChannelEntities(controlEntity.uuid)
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