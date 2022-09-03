package me.reezy.cosmo.update

import androidx.annotation.WorkerThread
import java.io.BufferedInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL

object HttpUtil {

    @WorkerThread
    fun query(url: String, postData: ByteArray? = null, headers: Map<String, String> = mapOf()): String {
        var connection: HttpURLConnection? = null
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/json")
            if (postData == null) {
                connection.requestMethod = "GET"
                headers.forEach {
                    connection.setRequestProperty(it.key, it.value)
                }
                connection.connect()
            } else {
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.instanceFollowRedirects = false
                connection.useCaches = false
                headers.forEach {
                    connection.setRequestProperty(it.key, it.value)
                }
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.setRequestProperty("Content-Length", postData.size.toString())
                connection.outputStream.write(postData)
            }
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw UpdateResult(UpdateResult.CHECK_HTTP_STATUS, connection.responseCode.toString())
            }
            return String(connection.inputStream.readBytes())
        } catch (ex: IOException) {
            ex.printStackTrace()
            throw UpdateResult(UpdateResult.CHECK_NETWORK_IO)
        } finally {
            connection?.disconnect()
        }
    }


    @WorkerThread
    fun download(task: DownloadTask) {
        var connection: HttpURLConnection? = null
        try {
            connection = connect(task.url)

            task.start(connection.contentLength.toLong())

            if (task.bytesTotal == task.bytesStart) {
                throw UpdateResult()
            }
            if (task.isDiskNoSpace()) {
                throw UpdateResult(UpdateResult.DOWNLOAD_DISK_NO_SPACE)
            }
            if (task.bytesStart > 0) {
                connection.disconnect()
                connection = connect(task.url, mapOf("Range" to "bytes=${task.bytesStart}-"))
            }

            connection.inputStream.buffered(1024 * 100).copyTo(task)

            if (task.isCancelled) {
                throw UpdateResult(UpdateResult.UPDATE_CANCELLED)
            }
            if (task.bytesStart + task.bytesCopied != task.bytesTotal && task.bytesTotal != -1L) {
                throw UpdateResult(UpdateResult.DOWNLOAD_INCOMPLETE)
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            throw UpdateResult(UpdateResult.DOWNLOAD_DISK_IO)
        } catch (e: IOException) {
            e.printStackTrace()
            throw UpdateResult(UpdateResult.DOWNLOAD_NETWORK_IO)
        } finally {
            connection?.disconnect()
            task.finish()
        }
    }

    private fun connect(url: String, headers: Map<String, String> = mapOf()): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("Accept", "application/*")
        headers.forEach {
            connection.setRequestProperty(it.key, it.value)
        }
        connection.connectTimeout = 10000
        connection.connect()

        if (connection.responseCode != 200 && connection.responseCode != 206) {
            throw UpdateResult(UpdateResult.DOWNLOAD_HTTP_STATUS, "${connection.responseCode}")
        }
        return connection
    }

    private fun BufferedInputStream.copyTo(task: DownloadTask) = use {
        val buffer = ByteArray(1024 * 100)
        RandomAccessFile(task.file, "rw").use { out ->
            out.seek(out.length())
            var previousBlockTime: Long = -1
            while (!task.isCancelled) {
                val bytes = it.read(buffer)
                if (bytes == -1) {
                    break
                }
                out.write(buffer, 0, bytes)
                val now = System.currentTimeMillis()
                task.progress(bytes.toLong(), now)
                when {
                    task.speed != 0L -> previousBlockTime = -1L
                    previousBlockTime == -1L -> previousBlockTime = now
                    System.currentTimeMillis() - previousBlockTime > 30000 -> throw UpdateResult(UpdateResult.DOWNLOAD_NETWORK_TIMEOUT)
                }
            }
        }
    }
}