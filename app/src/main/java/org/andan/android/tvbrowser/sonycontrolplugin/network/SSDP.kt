package org.andan.android.tvbrowser.sonycontrolplugin.network

import java.io.IOException
import java.net.*
import java.util.regex.Pattern


object SSDP {
    private const val DISCOVER_TIMEOUT = 1000
    private const val LINE_END = "\r\n"
    private const val DEFAULT_QUERY = "M-SEARCH * HTTP/1.1" + LINE_END +
            "HOST:239.255.255.250:1900" + LINE_END +
            "MAN: \"ssdp:discover\"" + LINE_END +
            "MX:1" + LINE_END +
            "ST:upnp:rootdevice" + LINE_END +
            LINE_END
    private const val DEFAULT_PORT = 1900
    private const val DEFAULT_ADDRESS = "239.255.255.250"

    @JvmStatic
    fun main(args: Array<String>) {
        val responseList = getSsdpResponses()
        //responseList.forEach { println(it) }
        getSonyIpAndModelNames().forEach {
            println(it.key)
            println(it.value)
        }
    }

    fun getSonyIpAndModelNames(): HashMap<String,String> {
        val sonyIpAndModelNameMap = linkedMapOf<String, String>()
        val responseList = getSsdpResponses()
        // seach for Sony Model
        val sonyPattern = Pattern.compile("X-AV-Server-Info:.*mn=\"(.*)\"; ")
        val ipPattern = Pattern.compile("Location: http://(.*):")
        responseList.forEach {
            println(it)
            var matcher = sonyPattern.matcher(it)
            var model :String? = null
            var ip: String? = null
            println(matcher)
            if (matcher.find()) {
                model = matcher.group(1)
            }
            matcher = ipPattern.matcher(it)
            if (matcher.find()) {
                ip = matcher.group(1)
            }
            if(!model.isNullOrEmpty() && !ip.isNullOrEmpty()) {
                sonyIpAndModelNameMap[ip]=model
            }
        }
        return sonyIpAndModelNameMap
    }

    fun getSsdpResponses(): List<String> {
        var socket: DatagramSocket? = null
        val responseList = mutableListOf<String>()
        try {
            val group: InetAddress = InetAddress.getByName(DEFAULT_ADDRESS)
            socket = DatagramSocket(null)
            socket.broadcast = true
            socket.soTimeout = DISCOVER_TIMEOUT

            val datagramPacketRequest =
                DatagramPacket(
                    DEFAULT_QUERY.toByteArray(),
                    DEFAULT_QUERY.length,
                    group,
                    DEFAULT_PORT
                )
            socket.send(datagramPacketRequest)
            while (true) {
                val datagramPacket =
                    DatagramPacket(ByteArray(1024), 1024)
                //DatagramPacket(ByteArray(2048), 2048)
                try {
                    socket.receive(datagramPacket)
                } catch (e: SocketTimeoutException) {
                    break
                }
                val response =
                    String(datagramPacket.data, 0, datagramPacket.length)
                if (!response.startsWith("HTTP/1.1 200 OK")) {
                    break;
                } else {
                    //println(response)
                    responseList.add(response)
                }

            }

        } catch (e: IOException) {
            e.printStackTrace();
        } finally {
            socket?.close()
        }
        return responseList
    }
}