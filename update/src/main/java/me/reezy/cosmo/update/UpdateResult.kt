package me.reezy.cosmo.update
 
import android.content.Context


class UpdateResult(val code: Int = 0, extra: String? = null): Throwable("[$code]") {
    val isError: Boolean get() =  code >= 2000

    val extraMessage: String = if (extra == null) "" else "($extra)"

    fun getFullMessage(context: Context): String = "[$code]${getCodeString(context)}$extraMessage"

    fun getCodeString(context: Context) = when(code) {

        UPDATE_NO_NEWER -> context.getString(R.string.update_no_newer)
        UPDATE_IGNORED -> context.getString(R.string.update_ignored)
        UPDATE_CANCELLED -> context.getString(R.string.update_cancelled)

        CHECK_NO_WIFI -> context.getString(R.string.update_check_no_wifi)
        CHECK_NO_NETWORK -> context.getString(R.string.update_check_no_network)
        CHECK_NETWORK_IO -> context.getString(R.string.update_check_network_io)
        CHECK_HTTP_STATUS -> context.getString(R.string.update_check_http_status)

        DOWNLOAD_DISK_NO_SPACE -> context.getString(R.string.update_download_disk_no_space)
        DOWNLOAD_DISK_IO -> context.getString(R.string.update_download_disk_io)
        DOWNLOAD_NETWORK_IO -> context.getString(R.string.update_download_network_io)
        DOWNLOAD_NETWORK_TIMEOUT -> context.getString(R.string.update_download_network_timeout)
        DOWNLOAD_HTTP_STATUS -> context.getString(R.string.update_download_http_status)
        DOWNLOAD_INCOMPLETE -> context.getString(R.string.update_download_incomplete)

        INSTALL_VERIFY -> context.getString(R.string.update_install_verify)

        UNKNOWN_EXCEPTION -> context.getString(R.string.update_unknown_exception)

        else -> ""
    }

    companion object {

        const val UPDATE_NO_NEWER = 1001
        const val UPDATE_IGNORED = 1002
        const val UPDATE_CANCELLED = 1003

        const val CHECK_NO_WIFI = 2002
        const val CHECK_NO_NETWORK = 2003
        const val CHECK_NETWORK_IO = 2004
        const val CHECK_HTTP_STATUS = 2005

        const val DOWNLOAD_DISK_NO_SPACE = 3003
        const val DOWNLOAD_DISK_IO = 3004
        const val DOWNLOAD_NETWORK_IO = 3005
        const val DOWNLOAD_NETWORK_TIMEOUT = 3007
        const val DOWNLOAD_HTTP_STATUS = 3008
        const val DOWNLOAD_INCOMPLETE = 3009

        const val INSTALL_VERIFY = 4001

        const val UNKNOWN_EXCEPTION = 5000

    }
}