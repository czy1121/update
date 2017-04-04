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

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateInfo {
    // 是否有新版本
    public boolean hasUpdate = false;
    // 是否静默下载：有新版本时不提示直接下载
    public boolean isSilent = false;
    // 是否强制安装：不安装无法使用app
    public boolean isForce = false;
    // 是否下载完成后自动安装
    public boolean isAutoInstall = true;
    // 是否可忽略该版本
    public boolean isIgnorable = true;
    // 一天内最大提示次数，<1时不限
    public int maxTimes = 0;

    public int versionCode;
    public String versionName;

    public String updateContent;

    public String url;
    public String md5;
    public long size;

    public static UpdateInfo parse(String s) throws JSONException {
        JSONObject o = new JSONObject(s);
        return parse(o.has("data") ? o.getJSONObject("data") : o);
    }

    private static UpdateInfo parse(JSONObject o) {
        UpdateInfo info = new UpdateInfo();
        if (o == null) {
            return info;
        }
        info.hasUpdate = o.optBoolean("hasUpdate", false);
        if (!info.hasUpdate) {
            return info;
        }
        info.isSilent = o.optBoolean("isSilent", false);
        info.isForce = o.optBoolean("isForce", false);
        info.isAutoInstall = o.optBoolean("isAutoInstall", !info.isSilent);
        info.isIgnorable = o.optBoolean("isIgnorable", true);

        info.versionCode = o.optInt("versionCode", 0);
        info.versionName = o.optString("versionName");
        info.updateContent = o.optString("updateContent");

        info.url = o.optString("url");
        info.md5 = o.optString("md5");
        info.size = o.optLong("size", 0);

        return info;
    }
}