package org.andan.android.tvbrowser.sonycontrolplugin.network

import android.util.Log
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.regex.Matcher
import java.util.regex.Pattern

object WakeOnLan {
    private val TAG = WakeOnLan::class.java.name
    const val PORT = 9
    private var matcher: Matcher? = null
    private const val IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
            "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$"
    private val pattern =
        Pattern.compile(IPADDRESS_PATTERN)

    fun validate(ipAddress: String?): Boolean {
        matcher =
            pattern.matcher(
                ipAddress
            )
        return matcher!!.matches()
    }

    @JvmStatic
    fun main(args: Array<String>) {

        // check ip pattern
        try {
            val address = InetAddress.getByName("192.168.178.27")
            println(address.hostAddress)
            if (validate(address.hostAddress)) {
                val broadcastAddress = address.hostAddress
                    .substring(0, address.hostAddress.lastIndexOf(".")) + ".255"
                println(broadcastAddress)
            }
        } catch (ex: UnknownHostException) {
            println(ex.message)
        }
        wakeOnLan(
            "192.168.178.27",
            "D8:D4:3C:4D:56:3D"
        )
    }

    fun wakeOnLan(host: String?, macStr: String?): Int {
        if (host == null || host.isEmpty() || macStr == null || macStr.isEmpty()) {
            return -1
        }
        try {
            val hostAddress = InetAddress.getByName(host)
            val broadcastIP: String?
            broadcastIP = if (validate(hostAddress.hostAddress)) {
                hostAddress.hostAddress
                    .substring(0, hostAddress.hostAddress.lastIndexOf(".")) + ".255"
            } else {
                throw Exception("No valid IP4 address")
            }
            val macBytes = getMacBytes(macStr)
            val bytes = ByteArray(6 + 16 * macBytes.size)
            for (i in 0..5) {
                bytes[i] = 0xff.toByte()
            }
            var i = 6
            while (i < bytes.size) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
                i += macBytes.size
            }
            val broadcastAddress = InetAddress.getByName(broadcastIP)
            val packet = DatagramPacket(
                bytes,
                bytes.size,
                broadcastAddress,
                PORT
            )
            val socket = DatagramSocket()
            socket.send(packet)
            socket.close()
            println("Wake-on-LAN packet sent.")
        } catch (e: Exception) {
            println("Failed to send Wake-on-LAN packet: $e")
            e.printStackTrace()
            return -1
        }
        return 0
    }

    @Throws(IllegalArgumentException::class)
    private fun getMacBytes(macStr: String): ByteArray {
        val bytes = ByteArray(6)
        val hex = macStr.split("[:\\-]".toRegex())
        require(hex.size == 6) { "Invalid MAC address." }
        try {
            for (i in 0..5) {
                bytes[i] = hex[i].toInt(16).toByte()
            }
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid hex digit in MAC address.")
        }
        //Log.d(TAG, "getMacBytes ${bytes[4]}")
        return bytes
    }
}