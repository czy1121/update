package me.reezy.cosmo.update

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import java.io.File
import java.lang.ref.WeakReference

object UpdateManager {
    private var lastTime: Long = 0
    private var downloadListenerFactory: (() -> DownloadListener)? = null
    private var checker: suspend () -> UpdateInfo = { UpdateInfo() }
    private var downloader: (suspend (DownloadTask) -> Unit) = { HttpUtil.download(it) }
    private var verifier: (File, String) -> Boolean = { file, hash -> md5(file) == hash }
    private var log: (String) -> Unit = { message -> Log.i("OoO.update", message) }


    fun setLogger(value: (String) -> Unit): UpdateManager {
        log = value
        return this
    }

    fun setVerifier(value: (File, String) -> Boolean): UpdateManager {
        verifier = value
        return this
    }

    fun setChecker(value: suspend () -> UpdateInfo): UpdateManager {
        checker = value
        return this
    }

    fun setChecker(url: String, postData: ByteArray? = null, headers: Map<String, String> = mapOf()): UpdateManager {
        checker = {
            try {
                UpdateInfo.parse(HttpUtil.query(url, postData, headers))
            } catch (ex: Throwable) {
                UpdateInfo()
            }
        }
        return this
    }

    fun setDownloader(value: suspend (DownloadTask) -> Unit): UpdateManager {
        downloader = value
        return this
    }

    fun setDownloadListenerFactory(value: () -> DownloadListener): UpdateManager {
        downloadListenerFactory = value
        return this
    }

    fun check(activity: FragmentActivity, onPrompt:((FragmentActivity, UpdateAgent) -> Unit)? = null, onResult: ((UpdateResult) -> Unit)? = null) {

        val now = System.currentTimeMillis()
        if (now - lastTime < 3000) {
            return
        }
        lastTime = now

        val ref = WeakReference(activity)
        val context = activity.applicationContext
        val cacheDir = File(activity.externalCacheDir, "update_cache")

        cacheDir.mkdirs()


        UpdateExecutor(
            context, cacheDir,
            log = log,
            check = checker,
            verifier = verifier,
            download = downloader,
            createDownloadListener = downloadListenerFactory ?: {
                NotificationDownloadListener(context)
            },
            onPrompt = { agent ->
                val act = ref.get()
                if (act != null && !act.isFinishing) {
                    val func = onPrompt ?: { ac, ag -> UpdatePromptDialog(ac, ag).show() }
                    func(act, agent)
                }
            },
            onResult = onResult ?: {
                if (it.code != UpdateResult.UPDATE_NO_NEWER) {
                    Toast.makeText(activity, it.getFullMessage(context), Toast.LENGTH_LONG).show()
                }
            },
        ).execute()
    }

    fun clean(context: Context) {
        val hash = UpdateStore.updateHash ?: return
        val file = File(context.externalCacheDir, "update_cache/$hash.apk")

        log("delete apk[${file.exists()}] ==> $file")

        if (file.exists()) {
            file.delete()
        }
        UpdateStore.updateHash = ""
    }
}