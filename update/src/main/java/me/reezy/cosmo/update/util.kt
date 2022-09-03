package me.reezy.cosmo.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.system.exitProcess


internal fun install(context: Context, file: File, exit: Boolean) {
    val intent = Intent(Intent.ACTION_VIEW)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
    } else {
        val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        intent.setDataAndType(uri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val resInfoList: List<ResolveInfo> = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (info in resInfoList) {
            context.grantUriPermission(info.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    context.startActivity(intent)

    if (exit) {
        exitProcess(0)
    }
}

internal fun md5(file: File): String {
    if (!file.isFile) {
        return ""
    }
    val buffer = ByteArray(1024)
    return try {
        val digest = MessageDigest.getInstance("MD5")
        val fis = FileInputStream(file)
        file.inputStream().use {
            do {
                val len = fis.read(buffer, 0, 1024)
                if (len == -1) break
                digest.update(buffer, 0, len)
            } while (true)
        }
        String.format("%032x", BigInteger(1, digest.digest()))
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}