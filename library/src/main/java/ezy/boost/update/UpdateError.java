/*
 * Copyright 2016 czy1121
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ezy.boost.update;


import android.util.SparseArray;

public class UpdateError extends Throwable {

    public final int code;
    public UpdateError(int code) {
        this(code, null);
    }
    public UpdateError(int code, String message) {
        super(make(code, message));
        this.code = code;
    }
    public boolean isError() {
        return code >= 2000;
    }

    @Override
    public String toString() {
        if (isError()) {
            return "[" + code + "]" + getMessage();
        }
        return getMessage();
    }

    private static String make(int code, String message) {
        String m = messages.get(code);
        if (m == null) {
            return message;
        }
        if (message == null) {
            return m;
        }
        return m + "(" + message + ")";
    }


    public static final int UPDATE_IGNORED = 1001;
    public static final int UPDATE_NO_NEWER = 1002;

    public static final int CHECK_UNKNOWN = 2001;
    public static final int CHECK_NO_WIFI = 2002;
    public static final int CHECK_NO_NETWORK = 2003;
    public static final int CHECK_NETWORK_IO = 2004;
    public static final int CHECK_HTTP_STATUS = 2005;
    public static final int CHECK_PARSE = 2006;


    public static final int DOWNLOAD_UNKNOWN = 3001;
    public static final int DOWNLOAD_CANCELLED = 3002;
    public static final int DOWNLOAD_DISK_NO_SPACE = 3003;
    public static final int DOWNLOAD_DISK_IO = 3004;
    public static final int DOWNLOAD_NETWORK_IO = 3005;
    public static final int DOWNLOAD_NETWORK_BLOCKED = 3006;
    public static final int DOWNLOAD_NETWORK_TIMEOUT = 3007;
    public static final int DOWNLOAD_HTTP_STATUS = 3008;
    public static final int DOWNLOAD_INCOMPLETE = 3009;
    public static final int DOWNLOAD_VERIFY = 3010;

    public static final SparseArray<String> messages = new SparseArray<>();
    static {

        messages.append(UPDATE_IGNORED, "该版本已经忽略");
        messages.append(UPDATE_NO_NEWER, "已经是最新版了");

        messages.append(CHECK_UNKNOWN, "查询更新失败：未知错误");
        messages.append(CHECK_NO_WIFI, "查询更新失败：没有 WIFI");
        messages.append(CHECK_NO_NETWORK, "查询更新失败：没有网络");
        messages.append(CHECK_NETWORK_IO, "查询更新失败：网络异常");
        messages.append(CHECK_HTTP_STATUS, "查询更新失败：错误的HTTP状态");
        messages.append(CHECK_PARSE, "查询更新失败：解析错误");

        messages.append(DOWNLOAD_UNKNOWN, "下载失败：未知错误");
        messages.append(DOWNLOAD_CANCELLED, "下载失败：下载被取消");
        messages.append(DOWNLOAD_DISK_NO_SPACE, "下载失败：磁盘空间不足");
        messages.append(DOWNLOAD_DISK_IO, "下载失败：磁盘读写错误");
        messages.append(DOWNLOAD_NETWORK_IO, "下载失败：网络异常");
        messages.append(DOWNLOAD_NETWORK_BLOCKED, "下载失败：网络中断");
        messages.append(DOWNLOAD_NETWORK_TIMEOUT, "下载失败：网络超时");
        messages.append(DOWNLOAD_HTTP_STATUS, "下载失败：错误的HTTP状态");
        messages.append(DOWNLOAD_INCOMPLETE, "下载失败：下载不完整");
        messages.append(DOWNLOAD_VERIFY, "下载失败：校验错误");
    }
}
