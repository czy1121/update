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
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.nio.charset.Charset;


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
        UpdateUtil.ensureExternalCacheDir(context);
        return new Builder(context).setWifiOnly(sIsWifiOnly);
    }

    public static class Builder {
        private Context mContext;
        private String mUrl;
        private byte[] mPostData;
        private boolean mIsManual;
        private boolean mIsWifiOnly;
        private int mNotifyId = 0;

        private OnDownloadListener mOnNotificationDownloadListener;
        private OnDownloadListener mOnDownloadListener;
        private IUpdatePrompter mPrompter;
        private OnFailureListener mOnFailureListener;

        private IUpdateParser mParser;
        private IUpdateChecker mChecker;
        private IUpdateDownloader mDownloader;

        public Builder(Context context) {
            mContext = context;
        }

        public Builder setUrl(String url) {
            mUrl = url;
            return this;
        }
        public Builder setPostData(@NonNull byte[] data) {
            mPostData = data;
            return this;
        }
        public Builder setPostData(@NonNull String data) {
            mPostData = data.getBytes(Charset.forName("UTF-8"));
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

        public Builder setParser(@NonNull IUpdateParser parser) {
            mParser = parser;
            return this;
        }
        public Builder setChecker(@NonNull IUpdateChecker checker) {
            mChecker = checker;
            return this;
        }
        public Builder setDownloader(@NonNull IUpdateDownloader downloader) {
            mDownloader = downloader;
            return this;
        }

        public Builder setPrompter(@NonNull IUpdatePrompter prompter) {
            mPrompter = prompter;
            return this;
        }

        public Builder setOnNotificationDownloadListener(@NonNull OnDownloadListener listener) {
            mOnNotificationDownloadListener = listener;
            return this;
        }

        public Builder setOnDownloadListener(@NonNull OnDownloadListener listener) {
            mOnDownloadListener = listener;
            return this;
        }

        public Builder setOnFailureListener(@NonNull OnFailureListener listener) {
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

            UpdateAgent agent = new UpdateAgent(mContext, mUrl, mIsManual, mIsWifiOnly, mNotifyId);
            if (mOnNotificationDownloadListener != null) {
                agent.setOnNotificationDownloadListener(mOnNotificationDownloadListener);
            }
            if (mOnDownloadListener != null) {
                agent.setOnDownloadListener(mOnDownloadListener);
            }
            if (mOnFailureListener != null) {
                agent.setOnFailureListener(mOnFailureListener);
            }
            if (mChecker != null) {
                agent.setChecker(mChecker);
            } else {
                agent.setChecker(new UpdateChecker(mPostData));
            }
            if (mParser != null) {
                agent.setParser(mParser);
            }
            if (mDownloader != null) {
                agent.setDownloader(mDownloader);
            }
            if (mPrompter != null) {
                agent.setPrompter(mPrompter);
            }
            agent.check();
        }
    }

}