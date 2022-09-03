package me.reezy.cosmo.update

import android.annotation.SuppressLint
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import java.lang.ref.WeakReference

class ProgressDownloadListener(progressBar: ProgressBar?, tv: TextView?) : DownloadListener {
    private val vText = WeakReference(tv)
    private val vProgress = WeakReference(progressBar)
    override fun onStart() {
        vProgress.get()?.post {
            vText.get()?.visibility = View.VISIBLE
            vProgress.get()?.visibility = View.VISIBLE
            onProgress(0f)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onProgress(progress: Float) {
        vProgress.get()?.post {
            vText.get()?.text = "${(progress * 10).toInt() / 10f}%"
            vProgress.get()?.progress = progress.toInt()
        }
    }

    override fun onFinish() {
    }
}