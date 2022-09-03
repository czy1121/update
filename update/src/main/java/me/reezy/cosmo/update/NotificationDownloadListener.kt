package me.reezy.cosmo.update

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class NotificationDownloadListener(
    private val context: Context,
    private val channelId: String = "update",
    private val notifyId: Int = 998
) : DownloadListener {
    private var builder: NotificationCompat.Builder? = null

    override fun onStart() {
        if (builder == null) {
            val title = "Downloading - " + context.getString(context.applicationInfo.labelRes)
            builder = NotificationCompat.Builder(context, channelId)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSmallIcon(context.applicationInfo.icon)
                .setTicker(title)
                .setContentTitle(title)
        }
        onProgress(0f)
    }

    override fun onProgress(progress: Float) {
        builder?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "Download progress", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }
            updateChannel(progress == 0f)
            it.setDefaults(if (progress > 0f) 0 else Notification.DEFAULT_VIBRATE)
            it.setProgress(100, progress.toInt(), false)
            notificationManager.notify(notifyId, it.build())
        }
    }

    override fun onFinish() {
        notificationManager.cancel(notifyId)
    }

    private fun updateChannel(first: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = if (first) NotificationManager.IMPORTANCE_DEFAULT else NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(channelId, "this is channel title", importance)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private val notificationManager: NotificationManager get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}