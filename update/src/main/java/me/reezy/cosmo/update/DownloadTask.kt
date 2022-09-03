package me.reezy.cosmo.update

import android.annotation.SuppressLint
import java.io.File
import java.io.IOException

class DownloadTask(val url: String, cacheDir: File?, hash: String) {

    val file: File by lazy { createTempFile(cacheDir, hash) }

    // 此次下载的起始位置
    val bytesStart: Long by lazy { if (file.exists()) file.length() else 0 }
    // 文件总大小
    var bytesTotal: Long = 0
        private set
    // 此次下载的字节数
    var bytesCopied: Long = 0
        private set

    var timeStart: Long = 0
        private set
    var timeUsed: Long = 1
        private set
    var timeLast: Long = 0
        private set

    var speed: Long = 0
        private set

    private val listeners: MutableSet<DownloadListener> = mutableSetOf()

    @Volatile
    var isCancelled: Boolean = false


    @SuppressLint("UsableSpace")
    fun isDiskNoSpace(): Boolean {
        val storage = file.parentFile?.usableSpace ?: 0L
        return bytesTotal - bytesStart > storage
    }

    fun addListener(listener: DownloadListener) {
        listeners.add(listener)
    }


    fun start(total: Long) {
        bytesTotal = total
        timeStart = System.currentTimeMillis()
        listeners.forEach { it.onStart() }
    }

    fun progress(bytes: Long, now: Long) {
        bytesCopied += bytes
        if (now - timeLast > 900) {
            timeLast = now
            timeUsed = now - timeStart
            speed = bytesCopied * 1000 / timeUsed

            val progress = 100f * (bytesCopied + bytesStart) / bytesTotal

            listeners.forEach { it.onProgress(progress) }
        }
    }

    fun finish() {
        listeners.forEach { it.onFinish() }
    }


    private fun createTempFile(cacheDir: File?, hash: String): File {
        val file = File(cacheDir, hash)
        val oldHash = UpdateStore.updateHash
        if (hash != oldHash) {
            UpdateStore.updateHash = hash

            // delete old temp file
            if (!oldHash.isNullOrEmpty()) {
                val oldFile = File(cacheDir, oldHash)
                if (oldFile.exists()) {
                    oldFile.delete()
                }
            }
        }
        // create new temp file
        if (!file.exists()) {
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }
}