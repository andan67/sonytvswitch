package org.andan.android.tvbrowser.sonycontrolplugin.test

import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyChannel
import org.junit.Assert
import org.junit.Test

class DataClassTests {

    //partially mutable
    data class SampleData1(
        val nickname: String = "")
    {
        var flag: Boolean = true
        var channelList: List<String> = listOf()
    }

    //immutable
    data class SampleData2(
        val nickname: String = "",
        val flag: Boolean,
        var channelList: List<String> = listOf()
        )

    @Test
    fun testEqualities() {
        val sampleData1 = SampleData1("nick1")
        sampleData1.flag = false
        sampleData1.channelList = listOf("one", "two")

        val sampleData1_1 = SampleData1("nick1")
        sampleData1_1.flag = true
        sampleData1_1.channelList = listOf("one")

        val sampleData1_2 = SampleData1("nick2")
        sampleData1_2.flag = false
        sampleData1_2.channelList = listOf("one", "two")

        Assert.assertEquals(sampleData1,sampleData1_1 )
        Assert.assertNotEquals(sampleData1,sampleData1_2 )

        val sampleData2 = SampleData2(nickname = "nick1", flag = false, channelList = listOf("one", "two"))
        val sampleData2_1 = SampleData2(nickname = "nick1", flag = true, channelList = listOf("one"))
        val sampleData2_1c = sampleData2.copy(flag = true, channelList = listOf("one"))
        val sampleData2_2 = sampleData2.copy(channelList = sampleData2.channelList.toList())
        val sampleData2_3 = sampleData2.copy(channelList = sampleData2.channelList.toMutableList())
        val sampleData2_4 = sampleData2.copy(channelList = listOf("one", "three"))


        Assert.assertFalse(sampleData2 === sampleData2_1 )
        Assert.assertFalse(sampleData2 === sampleData2_1 )
        Assert.assertNotEquals(sampleData2, sampleData2_1 )
        Assert.assertNotEquals(sampleData2, sampleData2_1c )
        Assert.assertEquals(sampleData2_1, sampleData2_1c )
        Assert.assertEquals(sampleData2, sampleData2_2 )
        Assert.assertNotEquals(sampleData2, sampleData2_4 )

    }

}