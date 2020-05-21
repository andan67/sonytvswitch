package org.andan.android.tvbrowser.sonycontrolplugin.network

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.regex.Pattern


object SSDP {
    private const val DISCOVER_TIMEOUT = 1000
    private const val DEFAULT_PORT = 1900
    private const val DEFAULT_ADDRESS_IP4 = "239.255.255.250"
    private const val DEFAULT_ADDRESS_IP6 = "[FF05::C]"
    private const val LINE_END = "\r\n"
    private const val DEFAULT_QUERY_IP4 = "M-SEARCH * HTTP/1.1" + LINE_END +
            "HOST:" + DEFAULT_ADDRESS_IP4 + ":" + DEFAULT_PORT + LINE_END +
            "MAN: \"ssdp:discover\"" + LINE_END +
            "MX:1" + LINE_END +
            "ST:upnp:rootdevice" + LINE_END +
            LINE_END
    private const val DEFAULT_QUERY_IP6 = "M-SEARCH * HTTP/1.1" + LINE_END +
            "HOST:" + DEFAULT_ADDRESS_IP6 + ":" + DEFAULT_PORT + LINE_END +
            "MAN: \"ssdp:discover\"" + LINE_END +
            "MX:1" + LINE_END +
            "ST:upnp:rootdevice" + LINE_END +
            LINE_END

    @JvmStatic
    fun main(args: Array<String>) {
        val responseList = getSsdpResponses()
        responseList.forEach { println(it) }
        //getSonyIpAndDeviceList().forEach { println(it) }
    }

    fun getSonyIpAndDeviceList(): List<IpDeviceItem> {
        val sonyIpDeviceList = mutableListOf<IpDeviceItem>()
        val responseList = getSsdpResponses()
        // seach for Sony Model
        val sonyPattern = Pattern.compile("X-AV-Server-Info:.*mn=\"(.*)\"; ")
        val ipPattern = Pattern.compile("LOCATION: http://(.*?)[:|/]")
        responseList.forEach {
            //println(it)
            var matcher = sonyPattern.matcher(it)
            var model :String? = null
            var ip: String? = null
            //println(matcher)
            if (matcher.find()) {
                model = matcher.group(1)
            }
            matcher = ipPattern.matcher(it)
            if (matcher.find()) {
                ip = matcher.group(1)
                //println("ip: $ip")
            }
            if(!model.isNullOrEmpty() && !ip.isNullOrEmpty()) {
                sonyIpDeviceList.add(IpDeviceItem(ip, model))
            }
        }
        return sonyIpDeviceList
    }

    fun getSsdpResponses(): List<String> {
        var socket: DatagramSocket? = null
        val responseList = mutableListOf<String>()
        try {
            val group: InetAddress = InetAddress.getByName(DEFAULT_ADDRESS_IP4)
            socket = DatagramSocket(null)
            socket.broadcast = true
            socket.soTimeout = DISCOVER_TIMEOUT

            val datagramPacketRequest =
                DatagramPacket(
                    DEFAULT_QUERY_IP4.toByteArray(),
                    DEFAULT_QUERY_IP4.length,
                    group,
                    DEFAULT_PORT
                )
            socket.send(datagramPacketRequest)
            while (true) {
                val datagramPacket =
                    DatagramPacket(ByteArray(1024), 1024)
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

    data class IpDeviceItem(val ip: String = "", val device: String = "") {
        override fun toString(): String {
            return if(ip.isEmpty()) {
                ""
            } else {
                "$device (ip=$ip)"
            }
        }
    }
}