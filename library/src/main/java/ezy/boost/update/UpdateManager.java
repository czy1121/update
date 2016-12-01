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

import android.content.Context;
import android.text.TextUtils;

import java.io.File;


public class UpdateManager {

    private static String sUrl;
    private static String sChannel;
    // 非wifi网络不检查更新
    private static boolean sIsWifiOnly = true;

    public static void setWifiOnly(boolean wifiOnly) {
        sIsWifiOnly = wifiOnly;
    }

    public static void setUrl(String url, String channel) {
        sUrl = url;
        sChannel = channel;
    }

    public static void setDebuggable(boolean debuggable) {
        UpdateUtil.DEBUG = debuggable;
    }

    public static void install(Context context) {
        UpdateUtil.install(context, true);
    }

    public static void check(Context context) {
        create(context).check();
    }

    public static void checkManual(Context context) {
        create(context).setManual(true).check();
    }

    public static Builder create(Context context) {
        File file = new File(context.getExternalFilesDir("").getParentFile(), "cache");
        if (file != null && !file.exists()) {
            file.mkdirs();
        }
        UpdateUtil.log("===>>> " + context.getExternalCacheDir());
        return new Builder(context).setWifiOnly(sIsWifiOnly);
    }

    public static class Builder {
        private Context mContext;
        private String mUrl;
        private boolean mIsManual;
        private boolean mIsWifiOnly;
        private int mNotifyId = 0;
        private UpdateAgent.OnProgressListener mOnNotifyListener;
        private UpdateAgent.OnProgressListener mOnProgressListener;
        private UpdateAgent.OnPromptListener mOnPromptListener;
        private UpdateAgent.OnFailureListener mOnFailureListener;
        private UpdateAgent.InfoParser mParser;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setUrl(String url) {
            mUrl = url;
            return this;
        }

        public Builder setNotifyId(int notifyId) {
            mNotifyId = notifyId;
            return this;
        }

        public Builder setManual(boolean isManual) {
            mIsManual = isManual;
            return this;
        }

        public Builder setWifiOnly(boolean isWifiOnly) {
            mIsWifiOnly = isWifiOnly;
            return this;
        }

        public Builder setParser(UpdateAgent.InfoParser parser) {
            mParser = parser;
            return this;
        }

        public Builder setOnNotify(UpdateAgent.OnProgressListener listener) {
            mOnNotifyListener = listener;
            return this;
        }

        public Builder setOnProgress(UpdateAgent.OnProgressListener listener) {
            mOnProgressListener = listener;
            return this;
        }

        public Builder setOnPrompt(UpdateAgent.OnPromptListener listener) {
            mOnPromptListener = listener;
            return this;
        }

        public Builder setOnFailure(UpdateAgent.OnFailureListener listener) {
            mOnFailureListener = listener;
            return this;
        }

        private static long sLastTime;

        public void check() {
            long now = System.currentTimeMillis();
            if (now - sLastTime < 3000) {
                return;
            }
            sLastTime = now;

            if (TextUtils.isEmpty(mUrl)) {
                mUrl = UpdateUtil.toCheckUrl(mContext, sUrl, sChannel);
            }

            UpdateAgent agent = new UpdateAgent(mContext, mUrl, mIsManual, mIsWifiOnly);
            agent.setInfoParser(mParser);
            if (mOnNotifyListener != null) {
                agent.setNotifyListener(mOnNotifyListener);
            } else if (mNotifyId > 0) {
                agent.setNotifyListener(new UpdateAgent.NotificationProgress(mContext, mNotifyId));
            }
            agent.setFailureListener(mOnFailureListener);
            agent.setPromptListener(mOnPromptListener);
            agent.setProgressListener(mOnProgressListener);
            agent.check();
        }
    }

}