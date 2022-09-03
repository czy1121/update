package me.reezy.cosmo.update

interface DownloadListener {
    fun onStart()
    fun onProgress(progress: Float)
    fun onFinish()
}