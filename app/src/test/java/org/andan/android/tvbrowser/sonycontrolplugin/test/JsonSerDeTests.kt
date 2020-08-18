package org.andan.android.tvbrowser.sonycontrolplugin.test

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import junit.framework.Assert.assertEquals
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControl
import org.andan.android.tvbrowser.sonycontrolplugin.domain.SonyControls
import org.junit.Before
import org.junit.Test
import java.io.File
import java.net.URL
import java.util.regex.Matcher
import java.util.regex.Pattern

class JsonSerDeTests {

    lateinit var gson: Gson
    lateinit var sonyControl: SonyControl
    lateinit var sonyControl2: SonyControl
    lateinit var sonyControl3: SonyControl
    lateinit var sonyControls: SonyControls

    lateinit var sonyControlAsJsonString: String
    val sonyControlAsJsonStringExpected =
        "{\"programList\":[" +
                "{\"source\":\"dvbs\",\"dispNumber\":\"0001\",\"index\":0,\"mediaType\":\"TV\",\"title\":\"Das Erste\",\"uri\":\"uri1\"}," +
                "{\"source\":\"dvbs\",\"dispNumber\":\"0002\",\"index\":0,\"mediaType\":\"TV\",\"title\":\"ZDF\",\"uri\":\"uri2\"}]," +
                "\"cookie\":\"\",\"sourceList\":[],\"systemModel\":\"\",\"systemName\":\"\",\"systemProduct\":\"\",\"systemMacAddr\":\"\",\"systemWolMode\":true," +
                "\"channelProgramMap\":{\"Das Erste (ARD)\":\"tv:dvbs?trip\\u003d1.1019.10301\\u0026srvName\\u003dDas%20Erste%20HD\"," +
                "\"ZDF\":\"tv:dvbs?trip\\u003d1.1011.11110\\u0026srvName\\u003dZDF%20HD\"}," +
                "\"ip\":\"192.168.178.27\",\"nickname\":\"android\",\"devicename\":\"Sony TV\",\"uuid\":\"6c034f06-fa84-4032-9b31-b714f2c20b9c\"}"


    @Before
    fun setUp() {
        gson = GsonBuilder().create()
        /*sonyControl = SonyControl("192.168.178.27","android","Sony TV", "6c034f06-fa84-4032-9b31-b714f2c20b9c")
        sonyControl.channelProgramMap["Das Erste (ARD)"] = "tv:dvbs?trip=1.1019.10301&srvName=Das%20Erste%20HD"
        sonyControl.channelProgramMap["ZDF"] = "tv:dvbs?trip=1.1011.11110&srvName=ZDF%20HD"
        val sonyProgram1 = SonyProgram("dvbs","0001",0,"TV","Das Erste", "uri1")
        val sonyProgram2 = SonyProgram("dvbs","0002",0,"TV","ZDF", "uri2")
        val plist = mutableListOf<SonyProgram>()
        plist.add(sonyProgram1)
        plist.add(sonyProgram2)
        sonyControl.programList = plist
        println(gson.toJson(sonyControl))
        //sonyControl.programList.add(sonyProgram2)
        sonyControl2 = gson.fromJson(sonyControlAsJsonStringExpected, SonyControl::class.java)
        var bufferedReader: BufferedReader = File("data/control.json").bufferedReader()
        // Read the text from bufferedReader and store in String variable
        //println(inputString)
        sonyControl3 = gson.fromJson(bufferedReader.use { it.readText() }, SonyControl::class.java)
        println("get 1 ${sonyControl3.programUriMap}")
        println("get 2 ${sonyControl3.programUriMap}")
        println("get 3 ${sonyControl3.programUriMap?.get("tv:dvbs?trip=1.1022.6912&srvName=CGTN%20Documentary")!!.title}")
        bufferedReader = File("data/controls.json").bufferedReader()
        // Read the text from bufferedReader and store in String variable
        //val sonyControls = SonyControls(listOf(sonyControl, sonyControl2),0)
        sonyControls = SonyControls.fromJson(bufferedReader.use { it.readText() })
        println(sonyControls.toJson())*/
        //val sonyControlSample = SonyControl.fromJson(File("/home/andan/Development/Android/TV-Browser_Projects/sonytvswitch_refactored/app/src/main/assets/controls_2.1.5.json").bufferedReader().use {it.readText()})
        //println("sonyControlSample: $sonyControlSample")
        /*sonyControl3.programList = plist
        println("get 4 ${sonyControl3.programUriMap}")
        println("get 5 ${sonyControl3.programUriMap}")
        println("get 6 ${sonyControl3.programUriMap?.get("uri1")!!.title}")*/
    }

    @Test
    fun testSonyControlJson() {
        /*assertEquals(sonyControlAsJsonStringExpected, gson.toJson(sonyControl))
        assertEquals(sonyControlAsJsonStringExpected, gson.toJson(sonyControl2))
        assertEquals("CGTN Documentary", sonyControl3.programUriMap?.get("tv:dvbs?trip=1.1022.6912&srvName=CGTN%20Documentary")!!.title)
        assertEquals("ZDF HD", sonyControls.controls[sonyControls.selected].programList[1].title)
        assertEquals("tv:dvbs?trip=1.1039.10377&srvName=ARD-alpha%20HD", sonyControls.controls[sonyControls.selected].channelProgramMap["ARD-alpha"])*/
    }

    @Test
    fun testLoadControls() {
        //val bufferedReader = File("src/test/data/controls.json").bufferedReader()
        val bufferedReader = File("src/test/data/controls_2.1.5.json").bufferedReader()
        sonyControls = SonyControls.fromJson(bufferedReader.use { it.readText() })
        println(sonyControls.toJson())
        /*assertEquals(sonyControlAsJsonStringExpected, gson.toJson(sonyControl))
        assertEquals(sonyControlAsJsonStringExpected, gson.toJson(sonyControl2))
        assertEquals("CGTN Documentary", sonyControl3.programUriMap?.get("tv:dvbs?trip=1.1022.6912&srvName=CGTN%20Documentary")!!.title)
        assertEquals("ZDF HD", sonyControls.controls[sonyControls.selected].programList[1].title)
        assertEquals("tv:dvbs?trip=1.1039.10377&srvName=ARD-alpha%20HD", sonyControls.controls[sonyControls.selected].channelProgramMap["ARD-alpha"])*/
    }

    @Test
    fun checkUrl() {
        //val url = Url("http://192.168.178.27/sony/accessControl")
        val url = URL("http://192.168.178.27/sony/accessControl")
        assertEquals(true, url.toString().endsWith("accessControl"))
    }

    @Test
    fun testUnicodeConversion() {
        var text ="{\n" +
                "  \"controls\": [\n" +
                "    {\n" +
                "      \"channelProgramMap\": {\n" +
                "        \"ANIXE HD Serie\": \"tv:dvbs?trip\\u003d1.1053.21100\\u0026srvName\\u003dANIXE%20HD\",\n" +
                "        \"ARD-alpha\": \"tv:dvbs?trip\\u003d1.1039.10377\\u0026srvName\\u003dARD-alpha%20HD\",\n" +
                "        \"arte\": \"tv:dvbs?trip\\u003d1.1019.10302\\u0026srvName\\u003darte%20HD\","
        println(removeUTFCharacters(text))

    }
    fun removeUTFCharacters(data: String): String {
        val p: Pattern = Pattern.compile("\\\\u(\\p{XDigit}{4})")
        //val p: Pattern = Pattern.compile("\\\\u([0-9A-Fa-f]{4})")
        val m: Matcher = p.matcher(data)
        val buf = StringBuffer(data.length)
        while (m.find()) {
            println(m.group(1))
            println(m.group(1).toInt(16).toChar())
            val ch: String = m.group(1).toInt(16).toChar().toString()
            println(ch)
            m.appendReplacement(buf, Matcher.quoteReplacement(ch))
        }
        m.appendTail(buf)
        return buf.toString()
    }
}