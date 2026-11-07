package com.example.skin.transfer

import android.content.Context
import android.os.Environment
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.util.Collections
import java.util.concurrent.Executors

class WifiTransferServer(private val context: Context, private val port: Int = 8080) {
    private var serverSocket: ServerSocket? = null
    private val executor = Executors.newSingleThreadExecutor()
    private var isRunning = false

    fun start(): String {
        try {
            serverSocket = ServerSocket(port)
            isRunning = true
            executor.submit {
                while (isRunning) {
                    try {
                        val client = serverSocket?.accept() ?: break
                        val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                        val line = reader.readLine() ?: continue
                        if (line.startsWith("GET")) {
                            val html = """
                                <html>
                                <head><title>Skin Hi-Fi - Wi-Fi Transfer</title></head>
                                <body style="background:#1a1a1a; color:#f5f5dc; font-family:sans-serif; text-align:center; padding-top:50px;">
                                    <h1 style="color:#d4af37;">SKIN HI-FI // WI-FI TRANSFER</h1>
                                    <p>Upload MP3, FLAC, or WAV files to save on device</p>
                                    <form action="/upload" method="post" enctype="multipart/form-data" style="margin-top:30px;">
                                        <input type="file" name="file" accept=".mp3,.flac,.wav" style="padding:10px; background:#332b24; color:#fff; border:1px solid #d4af37;" required><br><br>
                                        <button type="submit" style="padding:12px 24px; background:#ffbf00; color:#000; font-weight:bold; border:none; cursor:pointer;">UPLOAD & SAVE</button>
                                    </form>
                                </body>
                                </html>
                            """.trimIndent()
                            val response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: ${html.length}\r\n\r\n$html"
                            client.getOutputStream().write(response.toByteArray())
                        } else if (line.startsWith("POST")) {
                            val musicDir = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "SkinMusic")
                            if (!musicDir.exists()) musicDir.mkdirs()
                            val outputFile = File(musicDir, "transfer_${System.currentTimeMillis()}.mp3")
                            
                            val inputStream = client.getInputStream()
                            var contentLength = 0
                            var headerLine: String?
                            while (reader.readLine().also { headerLine = it } != null) {
                                if (headerLine!!.isEmpty()) break
                                if (headerLine!!.startsWith("Content-Length:", true)) {
                                    contentLength = headerLine!!.substring(15).trim().toIntOrNull() ?: 0
                                }
                            }

                            if (contentLength > 0) {
                                FileOutputStream(outputFile).use { fos ->
                                    val buffer = ByteArray(4096)
                                    var bytesRead = 0
                                    var totalRead = 0
                                    while (totalRead < contentLength && inputStream.read(buffer).also { bytesRead = it } != -1) {
                                        fos.write(buffer, 0, bytesRead)
                                        totalRead += bytesRead
                                    }
                                }
                            }

                            val html = "<html><body style='background:#1a1a1a; color:#f5f5dc; text-align:center; padding-top:50px;'><h2 style='color:#00ff00;'>File Saved Successfully!</h2><a href='/' style='color:#ffbf00;'>Back</a></body></html>"
                            val response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: ${html.length}\r\n\r\n$html"
                            client.getOutputStream().write(response.toByteArray())
                        }
                        client.close()
                    } catch (e: Exception) {}
                }
            }
            return getIpAddress() ?: "127.0.0.1"
        } catch (e: IOException) {
            e.printStackTrace()
            return ""
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (e: Exception) {}
        serverSocket = null
    }

    private fun getIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (e: Exception) {}
        return null
    }
}
