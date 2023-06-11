package org.andan.android.tvbrowser.sonycontrolplugin.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import app.cash.turbine.testIn
import kotlinx.coroutines.test.runTest
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

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
        controlDao.insertControls(controlEntity)

        controlDao.getControls().test {
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
        controlDao.insertControls(controlEntity)
        lateinit var control: ControlEntity

        controlDao.getControls().test {
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

        controlDao.deleteControl(control)
        controlDao.getControls().test {
            val controlEntityList = awaitItem()
            assert(controlEntityList.size == 0)
        }

        // check cascading delete of channels
        controlDao.getChannels(uuid).test {
            val channelEntityList = awaitItem()
            assert(channelEntityList.size == 0)
        }
    }

    @Test
    fun changeSelectedControl() = runTest {

        val controlEntity1 = ControlEntity(
            "1234", "localhost",
            "test1", "tv device 1", "KEY1"
        )

        val controlEntity2 = ControlEntity(
            "5678", "localhost",
            "test1", "tv device 2", "KEY2"
        )

        controlDao.insertControls(controlEntity1, controlEntity2)
        var control: ControlEntity? = null

        var uuid1 = controlEntity1.uuid
        var uuid2 = controlEntity2.uuid

        controlDao.getControls().test {
            val controlEntityList = awaitItem()
            assert(controlEntityList.contains(controlEntity1))
            assert(controlEntityList.contains(controlEntity2))
            control = controlEntityList[0]
            cancel()
        }

        controlDao.setActiveControl(controlEntity1)
        val controlEntityList = controlDao.getControls().testIn(backgroundScope).awaitItem()
        val activeControl =  controlDao.getActiveControl().testIn(backgroundScope).awaitItem()
        assert(controlEntityList.get(0).isActive == true)
        assert(controlEntityList.get(1).isActive == false)
        assert(activeControl.uuid.equals(uuid1))
        controlDao.getControls().test {
            val controlEntityList = awaitItem()

            assert(controlEntityList.contains(controlEntity1))
            assert(controlEntityList.contains(controlEntity2))
            control = controlEntityList[0]
            cancel()
        }
    }

    @Test
    fun readFromFile() = runTest {
        val appContext: Context = InstrumentationRegistry.getInstrumentation().getContext();
        val sonyControls = SonyControls.fromJson(
            appContext.assets.open("controls.json").bufferedReader()
                .use { it.readText() })
        Assert.assertEquals(2, sonyControls.controls.size)
        controlDao.insertFromSonyControls(sonyControls)
        val controlEntityList = controlDao.getControls().testIn(backgroundScope).awaitItem()
        Assert.assertEquals(2, controlEntityList.size)
        val channelList1 = controlDao.getChannelEntitiesForControl(controlEntityList.get(0))
            .testIn(backgroundScope).awaitItem()
        Assert.assertEquals("ZDF HD", channelList1.get(1).title)
    }

    fun readFileFromResourceAsString(fileName: String): String {
        try {
            val inputStream =
                javaClass.classLoader?.getResourceAsStream(fileName)
            return inputStream?.bufferedReader().use {
                it!!.readText()
            }
        } catch (ex: Exception) {
            return ""
        }
    }
}