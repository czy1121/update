package me.reezy.cosmo.update

import androidx.annotation.Keep
import org.json.JSONObject

@Keep
data class UpdateInfo(
    // 是否有新版本
    val hasUpdate: Boolean = false,

    // 是否强制安装：为true时不安装无法使用app
    val isForce: Boolean = false,

    // 是否静默更新：不提示用户直接下载安装
    val isSilent: Boolean = false,

    // 是否可忽略该版本：忽略后不再提示用户更新该版本
    val isIgnorable: Boolean = false,

    // 是否在通知栏显示下载进度：为true时显示
    val isShowNotification: Boolean = true,

    // 更新内容文案
    val updateContent: String = "",

    // 新包下载地址
    val url: String = "",

    // 新包哈希值
    val hash: String = "",
) {

    val isIgnored: Boolean get() = UpdateStore.ignoreHash == hash

    companion object {
        fun parse(s: String): UpdateInfo = parse(JSONObject(s))

        fun parse(o: JSONObject): UpdateInfo {
            if (o.optBoolean("hasUpdate", false)) {
                return UpdateInfo(
                    hasUpdate = true,
                    isForce = o.optBoolean("isForce", false),
                    isIgnorable = o.optBoolean("isIgnorable", false),
                    isShowNotification = o.optBoolean("isShowNotification", false),
                    isSilent = o.optBoolean("isSilent", false),
                    updateContent = o.optString("updateContent"),
                    url = o.optString("url"),
                    hash = o.optString("hash"),
                )
            }
            return UpdateInfo()
        }
    }
}