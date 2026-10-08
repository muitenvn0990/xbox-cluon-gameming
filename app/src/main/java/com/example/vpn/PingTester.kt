package com.example.vpn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object PingTester {

    /**
     * Measures TCP handshake latency to a given host and port.
     * Returns round-trip time in milliseconds, or -1 if unreachable.
     */
    suspend fun measureLatency(host: String, port: Int = 443, timeoutMs: Int = 1500): Int = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                (System.currentTimeMillis() - startTime).toInt()
            }
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Measures latency specifically to a ServerLocation's IP and port.
     */
    suspend fun testServerPing(server: ServerLocation, timeoutMs: Int = 1500): Int = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(server.ip, server.port), timeoutMs)
                (System.currentTimeMillis() - start).toInt()
            }
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Quick test if an IP and port responds
     */
    suspend fun isHostReachable(host: String, port: Int, timeoutMs: Int = 1200): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), timeoutMs)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
