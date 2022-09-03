package me.reezy.cosmo.update

class UpdateAgent(
    val info: UpdateInfo,
    private val task: DownloadTask,
    private val update: (UpdateInfo, DownloadTask) -> Unit
) {
    fun update() {
        update(info, task)
    }
    fun cancel() {
        task.isCancelled = true
    }
    fun ignore() {
        task.isCancelled = true
        UpdateStore.ignoreHash = info.hash
    }

    fun addDownloadListener(listener: DownloadListener) {
        task.addListener(listener)
    }
}