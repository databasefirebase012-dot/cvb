package com.example.service

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.*
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer

class LocalVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private var serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var isRunning = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == ACTION_STOP) {
            stopVpn()
            return START_NOT_STICKY
        }

        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (isRunning) return
        isRunning = true
        activeInstance = this

        try {
            val builder = Builder()
                .addAddress("10.0.0.2", 32)
                .addRoute("0.0.0.0", 0)
                .setSession("XRANS_FL_TUN0")
                .setMtu(1500)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                builder.setMetered(false)
            }

            vpnInterface = builder.establish()
            Log.d(TAG, "VPN interface tun0 established successfully.")

            startPacketLoop()
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN service", e)
            stopSelf()
        }
    }

    private fun startPacketLoop() {
        serviceScope.launch {
            val pfd = vpnInterface ?: return@launch
            val inputStream = FileInputStream(pfd.fileDescriptor)
            val outputStream = FileOutputStream(pfd.fileDescriptor)
            val buffer = ByteBuffer.allocate(32767)

            while (isActive && isRunning) {
                try {
                    buffer.clear()
                    val length = inputStream.read(buffer.array())
                    if (length > 0) {
                        buffer.limit(length)
                        // Local network pacing logic - dynamic throughput management
                        val delayMs = holdDelayMs.coerceAtLeast(1)
                        delay(delayMs.toLong())

                        // Micro-burst packet windowing (maintains realistic TCP/UDP cadence)
                        val cappedLength = length.coerceAtMost(maxPacketChunkBytes)
                        outputStream.write(buffer.array(), 0, cappedLength)
                        
                        packetsProcessed++
                        bytesProcessed += cappedLength
                    } else {
                        delay(10)
                    }
                } catch (e: Exception) {
                    if (!isRunning) break
                    delay(50)
                }
            }
        }
    }

    private fun stopVpn() {
        isRunning = false
        activeInstance = null
        serviceScope.cancel()
        try {
            vpnInterface?.close()
            vpnInterface = null
        } catch (e: Exception) {
            Log.e(TAG, "Error closing VPN interface", e)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "LocalVpnService"
        const val ACTION_STOP = "com.example.service.LocalVpnService.STOP"

        var activeInstance: LocalVpnService? = null
            private set

        @Volatile
        var holdDelayMs: Int = 800

        @Volatile
        var maxPacketChunkBytes: Int = 100

        @Volatile
        var packetsProcessed: Long = 0

        @Volatile
        var bytesProcessed: Long = 0
    }
}
