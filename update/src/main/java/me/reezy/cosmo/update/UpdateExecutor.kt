package me.reezy.cosmo.update

import android.content.Context
import kotlinx.coroutines.*
import java.io.File

internal class UpdateExecutor(
    private val context: Context,
    private val cacheDir: File?,
    private val log: (String) -> Unit,
    private val verifier: (File, String) -> Boolean,
    private val downloadListenerFactory: () -> DownloadListener,
    private val check: suspend () -> UpdateInfo,
    private val download: suspend (DownloadTask) -> Unit,
    private val onPrompt: (UpdateAgent) -> Unit,
    private val onResult: (UpdateResult) -> Unit,
) {
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun execute() = scope.launch {

        try {
            // 查询更新信息
            val info = withContext(Dispatchers.IO) { check() }
            log("update => $info")

            val task = DownloadTask(info.url, cacheDir, info.hash)

            if (info.isShowNotification) {
                task.addListener(downloadListenerFactory())
            }

            when {
                // 没有新版本 -> 返回结果
                !info.hasUpdate -> returnResult(UpdateResult(UpdateResult.UPDATE_NO_NEWER))
                // 强制更新 -> 提示用户更新，不显示[取消][忽略]按钮
                info.isForce -> prompt(info, task)
                // 静默更新 -> 不提示用户直接更新
                info.isSilent -> update(info, task)
                // 不可忽略的版本 -> 提示用户更新，不显示[忽略]按钮
                !info.isIgnorable -> prompt(info, task)
                // 该版本已经被忽略 -> 返回结果
                info.isIgnored -> returnResult(UpdateResult(UpdateResult.UPDATE_IGNORED))
                // 提示用户更新
                else -> prompt(info, task)
            }
        } catch (ex: UpdateResult) {
            returnResult(ex)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            returnResult(UpdateResult(UpdateResult.UNKNOWN_EXCEPTION))
        }
    }

    private suspend fun returnResult(result: UpdateResult) {
        withContext(Dispatchers.Main) {
            onResult(result)
        }
    }

    private suspend fun prompt(info: UpdateInfo, task: DownloadTask) {
        withContext(Dispatchers.Main) {
            onPrompt(UpdateAgent(info, task, this@UpdateExecutor::onUserUpdate))
        }
    }

    // 更新：如果已经下载，就直接安装，否则下载完成后再安装
    private suspend fun update(info: UpdateInfo, task: DownloadTask) {
        val destFile = File(cacheDir, "${info.hash}.apk")
        if (verify(destFile, info.hash)) {
            install(context, destFile, true)
        } else {
            withContext(Dispatchers.IO) {
                download(task)
            }
            if (verify(task.file, info.hash)) {
                task.file.renameTo(destFile)
                install(context, destFile, true)
            } else {
                throw UpdateResult(UpdateResult.INSTALL_VERIFY)
            }
        }
    }

    // 用户确认更新
    private fun onUserUpdate(info: UpdateInfo, task: DownloadTask) = scope.launch {
        try {
            update(info, task)
        } catch (ex: UpdateResult) {
            returnResult(ex)
        }
    }


    private fun verify(file: File, hash: String) = when {
        !file.exists() -> false
        verifier.invoke(file, hash) -> true
        else -> {
            file.delete()
            false
        }
    }
}