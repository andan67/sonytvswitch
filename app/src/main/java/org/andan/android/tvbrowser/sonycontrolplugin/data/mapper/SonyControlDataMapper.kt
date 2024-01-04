package org.andan.android.tvbrowser.sonycontrolplugin.data.mapper

import org.andan.android.tvbrowser.sonycontrolplugin.data.ChannelEntity
import org.andan.android.tvbrowser.sonycontrolplugin.data.ChannelMapEntity
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlEntity
import org.andan.android.tvbrowser.sonycontrolplugin.data.ControlEntityWithChannels
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl

class SonyControlWithChannelsDomainMapper(
    private val channelListDomainMapper: ListMapper<ChannelEntity, SonyChannel>
) : Mapper<ControlEntityWithChannels?, SonyControl> {
    override fun map(controlEntityWithChannels: ControlEntityWithChannels?): SonyControl {
        if (controlEntityWithChannels != null) {
            val controlEntity = controlEntityWithChannels.control
            val sonyControl: SonyControl = SonyControl(
                ip = controlEntity.host,
                devicename = controlEntity.devicename,
                nickname = controlEntity.nickname,
                uuid = controlEntity.uuid,
                preSharedKey = controlEntity.preSharedKey,
                isActive = controlEntity.isActive,
                cookie = controlEntity.cookie,
                systemModel = controlEntity.systemModel,
                systemName = controlEntity.systemName,
                systemMacAddr = controlEntity.systemMacAddr,
                systemProduct = controlEntity.systemProduct,
                systemWolMode = controlEntity.systemWolMode,
                sourceList = controlEntity.sourceList,
                commandMap = controlEntity.commandMap,
                channelList = channelListDomainMapper.map(controlEntityWithChannels.channels),
                channelMap = controlEntityWithChannels.channelMaps.associate { it.channelLabel to it.uri }
            )
            return sonyControl
        }
        return SonyControl()

    }
}

class SonyControlDomainMapper() : Mapper<ControlEntity?, SonyControl> {
    override fun map(controlEntity: ControlEntity?): SonyControl {
        if (controlEntity != null) {
            val sonyControl: SonyControl = SonyControl(
                ip = controlEntity.host,
                devicename = controlEntity.devicename,
                nickname = controlEntity.nickname,
                uuid = controlEntity.uuid,
                preSharedKey = controlEntity.preSharedKey,
                isActive = controlEntity.isActive,
                cookie = controlEntity.cookie,
                systemModel = controlEntity.systemModel,
                systemName = controlEntity.systemName,
                systemMacAddr = controlEntity.systemMacAddr,
                systemProduct = controlEntity.systemProduct,
                systemWolMode = controlEntity.systemWolMode,
                sourceList = controlEntity.sourceList,
                commandMap = controlEntity.commandMap
            )
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

class SonyControlDTOMapper() : Mapper<SonyControl, ControlEntity> {
    override fun map(sonyControl: SonyControl): ControlEntity {
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
        controlEntity.commandMap = sonyControl.commandMap
        controlEntity.isActive = sonyControl.isActive
        return controlEntity
    }
}

class SonyChannelListDTOMapper() : Mapper<SonyControl, List<ChannelEntity>> {
    override fun map(sonyControl: SonyControl): List<ChannelEntity> {
        var channelList: MutableList<ChannelEntity> = mutableListOf()
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
        return channelList
    }
}

class SonyChanneMapDTOMapper() : Mapper<SonyControl, List<ChannelMapEntity>> {
    override fun map(sonyControl: SonyControl): List<ChannelMapEntity> {
        var channelMapList: MutableList<ChannelMapEntity> = mutableListOf()
        for (entry in sonyControl.channelMap) {
            val channelMapEntity = ChannelMapEntity(
                control_uuid = sonyControl.uuid,
                channelLabel = entry.key, uri = entry.value
            )
            channelMapList.add(channelMapEntity)
        }
        return channelMapList
    }
}

class SonyControlDataMapper() {
    private val sonyControlWithChannelsDomainMapper =
        SonyControlWithChannelsDomainMapper(ListMapperImpl(SonyChannelDomainMapper()))
    private val sonyControlMapper = SonyControlDomainMapper()
    private val sonyChannelDomainMapper = SonyChannelDomainMapper()
    private val sonyControlDTOMapper = SonyControlDTOMapper()
    private val sonyChannelListDTOMapper = SonyChannelListDTOMapper()
    private val sonyChanneMapDTOMapper = SonyChanneMapDTOMapper()

    fun mapControlEntity2Domain(controlEntityWithChannels: ControlEntityWithChannels?): SonyControl {
        return sonyControlWithChannelsDomainMapper.map(controlEntityWithChannels)
    }

    fun mapControlEntity2Domain(controlEntity: ControlEntity?): SonyControl {
        return sonyControlMapper.map(controlEntity)
    }

    fun mapChannelEntity2Domain(channelEntity: ChannelEntity): SonyChannel {
        return sonyChannelDomainMapper.map(channelEntity)
    }

    fun mapControl2Entity(sonyControl: SonyControl): ControlEntity {
        return sonyControlDTOMapper.map(sonyControl)
    }

    fun mapControl2ChannelEntityList(sonyControl: SonyControl): List<ChannelEntity> {
        return sonyChannelListDTOMapper.map(sonyControl)
    }

    fun mapControl2ChannelMapList(sonyControl: SonyControl): List<ChannelMapEntity> {
        return sonyChanneMapDTOMapper.map(sonyControl)
    }
}


