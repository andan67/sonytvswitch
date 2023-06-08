package org.andan.android.tvbrowser.sonycontrolplugin.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var controlDao: ControlDao
    private lateinit var controlDatabase: ControlDatabase

    @Before
    fun initialize() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        controlDatabase = Room.inMemoryDatabaseBuilder(
            context, ControlDatabase::class.java
        ).build()
        controlDao = controlDatabase.controlDao()
    }

    @After
    fun cleanup() {
        controlDatabase.close()
    }

    /*
    companion object {
        @JvmStatic
        private lateinit var controlDao: ControlDao
        @JvmStatic
        private lateinit var controlDatabase: ControlDatabase

        @BeforeClass
        @JvmStatic
        fun initialize() {
            val context = ApplicationProvider.getApplicationContext<Context>()
            controlDatabase = Room.inMemoryDatabaseBuilder(
                context, ControlDatabase::class.java
            ).build()
            controlDao = controlDatabase.controlDao()
        }

        @AfterClass
        @JvmStatic
        fun cleanup() {
            controlDatabase.close()
        }
    }
*/

    @Test
    fun addControl() = runTest {

        val controlEntity = ControlEntity(
            "1234", "localhost",
            "test", "tv device", "KEY"
        )
        controlEntity.systemModel = "Sony Bravia"
        controlDao.insertAll(controlEntity)

        controlDao.getAll().test {
            val controlEntityList = awaitItem()
            assert(controlEntityList.contains(controlEntity))
            val controlEntityFromList = controlEntityList[0]
            assert(controlEntityFromList.systemModel.equals(controlEntity.systemModel))
            cancel()
        }
    }

    @Test
    fun addAndDeleteControlsAndChannels() = runTest {

        val controlEntity = ControlEntity(
            "1234", "localhost",
            "test", "tv device", "KEY"
        )
        controlEntity.systemModel = "Sony Bravia"
        controlDao.insertAll(controlEntity)
        lateinit var control: ControlEntity

        controlDao.getAll().test {
            val controlEntityList = awaitItem()
            assert(controlEntityList.contains(controlEntity))
            control = controlEntityList[0]
            assert(control.systemModel.equals(controlEntity.systemModel))
            cancel()
        }

        val uuid = control.uuid

        val channelList = ArrayList<ChannelEntity>()
        channelList.add(
            ChannelEntity(
                "0001", uuid, "dvbs",
                0, "tv", "RTL", "tv:0001"
            )
        )
        channelList.add(
            ChannelEntity(
                "0002", uuid, "dvbs",
                0, "tv", "VOX", "tv:0002"
            )
        )

        controlDao.setChannelsForControl(channelList, control);

        controlDao.getChannelEntitiesForControl(control).test {
            val channelEntityList = awaitItem()
            assert(channelEntityList.size == 2)
            assert(channelEntityList[0].displayNumber.equals("0001"))
            cancel()
        }

        channelList.add(
            ChannelEntity(
                "0003", uuid, "dvbs",
                0, "tv", "PRO7", "tv:0003"
            )
        )
        channelList.get(0).title = "RTL2"
        controlDao.setChannelsForControl(channelList, control);
        controlDao.getChannelEntitiesForControl(control).test {
            val channelEntityList = awaitItem()
            assert(channelEntityList.size == 3)
            assert(channelEntityList[0].title.equals("RTL2"))
            cancel()
        }

        controlDao.delete(control)
        controlDao.getAll().test {
            val controlEntityList = awaitItem()
            assert(controlEntityList.size == 0)
        }

        // check cascading delete of channels
        controlDao.getChannelEntities(uuid).test {
            val channelEntityList = awaitItem()
            assert(channelEntityList.size == 0)
        }
    }
}