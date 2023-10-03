package org.andan.android.tvbrowser.sonycontrolplugin.data.mapper

import org.andan.android.tvbrowser.sonycontrolplugin.data.ChannelEntity
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlEntity
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlEntityWithChannels
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl

class SonyControlWithChannelsDomainMapper(
    private val channelListDomainMapper: ListMapper<ChannelEntity, SonyChannel>
) : Mapper<ControlEntityWithChannels, SonyControl>
{
    override fun map(controlEntityWithChannels: ControlEntityWithChannels): SonyControl {
        val controlEntity = controlEntityWithChannels.control
        val sonyControl: SonyControl = SonyControl(
            ip = controlEntity.host,
            devicename = controlEntity.devicename,
            nickname = controlEntity.nickname,
            uuid = controlEntity.uuid,
            preSharedKey = controlEntity.preSharedKey
        )
        sonyControl.isActive = controlEntity.isActive
        sonyControl.cookie = controlEntity.cookie
        sonyControl.systemModel = controlEntity.systemModel
        sonyControl.systemName = controlEntity.systemName
        sonyControl.systemMacAddr = controlEntity.systemMacAddr
        sonyControl.systemProduct = controlEntity.systemProduct
        sonyControl.systemWolMode = controlEntity.systemWolMode
        sonyControl.sourceList = controlEntity.sourceList
        sonyControl.commandList = controlEntity.commandMap
        sonyControl.channelList = channelListDomainMapper.map(controlEntityWithChannels.channels)
        sonyControl.channelMap = controlEntityWithChannels.channelMaps.associate { it.channelLabel to it.uri }
        return sonyControl
    }
}

class SonyControlDomainMapper() : Mapper<ControlEntity?, SonyControl>
{
    override fun map(controlEntity: ControlEntity?): SonyControl {
        if (controlEntity != null) {
            val sonyControl: SonyControl = SonyControl(
                ip = controlEntity.host,
                devicename = controlEntity.devicename,
                nickname = controlEntity.nickname,
                uuid = controlEntity.uuid,
                preSharedKey = controlEntity.preSharedKey
            )
            sonyControl.isActive = controlEntity.isActive
            sonyControl.cookie = controlEntity.cookie
            sonyControl.systemModel = controlEntity.systemModel
            sonyControl.systemName = controlEntity.systemName
            sonyControl.systemMacAddr = controlEntity.systemMacAddr
            sonyControl.systemProduct = controlEntity.systemProduct
            sonyControl.systemWolMode = controlEntity.systemWolMode
            sonyControl.sourceList = controlEntity.sourceList
            sonyControl.commandList = controlEntity.commandMap
            return sonyControl
        }
        return SonyControl()
    }
}

class SonyChannelDomainMapper() : Mapper<ChannelEntity, SonyChannel> {
    override fun map(channelEntity: ChannelEntity): SonyChannel {
        return SonyChannel(
            source = channelEntity.source,
            dispNumber = channelEntity.displayNumber,
            index = channelEntity.index,
            mediaType = channelEntity.mediaType,
            title = channelEntity.title,
            uri = channelEntity.uri
        )
    }
}



