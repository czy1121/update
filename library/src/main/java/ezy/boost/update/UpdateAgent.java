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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class UpdateAgent {

    public interface OnProgressListener {

        void onStart();

        void onProgress(int progress);

        void onFinish();
    }

    public interface OnFailureListener {
        void onFailure(UpdateError error);
    }

    public interface OnPromptListener {
        void onPrompt(UpdateAgent agent);
    }

    public interface InfoParser {
        UpdateInfo parse(String source) throws Exception;
    }

    private Context mContext;
    private String mUrl;
    private File mTmpFile;
    private File mApkFile;
    private boolean mIsManual = false;
    private boolean mIsWifiOnly = false;

    private UpdateInfo mInfo;
    private UpdateError mError = null;

    private InfoParser mParser;

    private OnFailureListener mOnFailureListener;
    private OnPromptListener mOnPromptListener;
    private OnProgressListener mOnProgressListener;
    private OnProgressListener mOnNotificationListener;

    public UpdateAgent(Context context, String url, boolean isManual, boolean isWifiOnly) {
        mContext = context;
        mUrl = url;
        mIsManual = isManual;
        mIsWifiOnly = isWifiOnly;
        mOnPromptListener = new OnPrompt(context);
        mOnFailureListener = new OnFailure(context);

        mParser = new DefaultParser();
    }

    public String getUrl() {
        return mUrl;
    }

    public UpdateInfo getInfo() {
        return mInfo;
    }

    public void setInfo(UpdateInfo info) {
        mInfo = info;
    }

    public UpdateError getError() {
        return mError;
    }

    public void setError(UpdateError error) {
        mError = error;
    }

    public void setInfoParser(InfoParser parser) {
        if (parser != null) {
            mParser = parser;
        }
    }

    public void check() {
        if (mIsWifiOnly) {
            if (UpdateUtil.checkWifi(mContext)) {
                onCheck();
            } else {
                onFailure(new UpdateError(UpdateError.CHECK_NO_WIFI));
            }
        } else {
            if (UpdateUtil.checkNetwork(mContext)) {
                onCheck();
            } else {
                onFailure(new UpdateError(UpdateError.CHECK_NO_NETWORK));
            }
        }
    }

    public void parse(String source) {

        try {
            setInfo(mParser.parse(source));
        } catch (Exception e) {
            e.printStackTrace();
            setError(new UpdateError(UpdateError.CHECK_PARSE));
        }
    }

    public void checkFinish() {
        UpdateError error = getError();
        if (error != null) {
            onFailure(error);
        } else {
            UpdateInfo info = getInfo();
            if (info == null) {
                onFailure(new UpdateError(UpdateError.CHECK_UNKNOWN));
            } else if (!info.hasUpdate) {
                onFailure(new UpdateError(UpdateError.UPDATE_NO_NEWER));
            } else if (UpdateUtil.isIgnore(mContext, info.md5)) {
                onFailure(new UpdateError(UpdateError.UPDATE_IGNORED));
            } else {
                UpdateUtil.setUpdate(mContext, mInfo.md5);
                mTmpFile = new File(mContext.getExternalCacheDir(), info.md5);
                mApkFile = new File(mContext.getExternalCacheDir(), info.md5 + ".apk");
                if (UpdateUtil.verify(mApkFile, mInfo.md5)) {
                    onInstall();
                } else if (info.isSilent) {
                    onDownload();
                } else {
                    mOnPromptListener.onPrompt(this);
                }
            }
        }

    }

    public void update() {
        mApkFile = new File(mContext.getExternalCacheDir(), mInfo.md5 + ".apk");
        if (UpdateUtil.verify(mApkFile, mInfo.md5)) {
            onInstall();
        } else {
            onDownload();
        }
    }

    public void ignore() {
        UpdateUtil.setIgnore(mContext, getInfo().md5);
    }

    public void downloadStart() {
        if (mInfo.isSilent) {
            mOnNotificationListener.onStart();
        } else {
            mOnProgressListener.onStart();
        }
    }

    public void downloadProgress(int progress) {
        if (mInfo.isSilent) {
            mOnNotificationListener.onProgress(progress);
        } else {
            mOnProgressListener.onProgress(progress);
        }
    }

    public void downloadFinish() {
        if (mInfo.isSilent) {
            mOnNotificationListener.onFinish();
        } else {
            mOnProgressListener.onFinish();
        }
        if (mError != null) {
            mOnFailureListener.onFailure(mError);
        } else {
            mTmpFile.renameTo(mApkFile);
            if (!mInfo.isSilent) {
                onInstall();
            }
        }

    }

    public void setNotifyListener(OnProgressListener listener) {
        if (listener != null) {
            mOnNotificationListener = listener;
        }
    }

    public void setProgressListener(OnProgressListener listener) {
        if (listener != null) {
            mOnProgressListener = listener;
        }
    }

    public void setPromptListener(OnPromptListener prompt) {
        if (prompt != null) {
            mOnPromptListener = prompt;
        }
    }

    public void setFailureListener(OnFailureListener failure) {
        if (failure != null) {
            mOnFailureListener = failure;
        }
    }


    private void onFailure(UpdateError error) {
        if (mIsManual || error.isError()) {
            mOnFailureListener.onFailure(error);
        }
    }

    protected void onCheck() {
        new UpdateChecker(this).execute();
    }

    protected void onDownload() {
        if (mOnNotificationListener == null) {
            mOnNotificationListener = new EmptyProgress();
        }
        if (mOnProgressListener == null) {
            mOnProgressListener = new DialogProgress(mContext);
        }
        new UpdateDownloader(this, mContext, mInfo.url, mTmpFile).execute();
    }

    protected void onInstall() {

        UpdateUtil.install(mContext, mApkFile, mInfo.isForce);
    }


    private static class DefaultParser implements InfoParser {
        @Override
        public UpdateInfo parse(String source) throws Exception {
            return UpdateInfo.parse(source);
        }
    }


    private static class OnFailure implements OnFailureListener {

        private Context mContext;

        public OnFailure(Context context) {
            mContext = context;
        }

        @Override
        public void onFailure(UpdateError error) {
            UpdateUtil.log(error.toString());
            Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private static class OnPrompt implements UpdateAgent.OnPromptListener {

        private Context mContext;

        public OnPrompt(Context context) {
            mContext = context;
        }

        @Override
        public void onPrompt(UpdateAgent agent) {
            final UpdateInfo info = agent.getInfo();
            String size = Formatter.formatShortFileSize(mContext, info.size);
            String content = String.format("最新版本：%1$s\n新版本大小：%2$s\n\n更新内容\n%3$s", info.versionName, size, info.updateContent);

            final AlertDialog dialog = new AlertDialog.Builder(mContext).create();

            dialog.setTitle("应用更新");
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);


            float density = mContext.getResources().getDisplayMetrics().density;
            TextView tv = new TextView(mContext);
            tv.setMovementMethod(new ScrollingMovementMethod());
            tv.setVerticalScrollBarEnabled(true);
            tv.setTextSize(14);
            tv.setMaxHeight((int) (250 * density));

            dialog.setView(tv, (int) (25 * density), (int) (15 * density), (int) (25 * density), 0);


            DialogInterface.OnClickListener listener = new OnPromptClick(agent, true);

            if (info.isForce) {
                tv.setText("您需要更新应用才能继续使用\n\n" + content);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", listener);
            } else {
                tv.setText(content);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, "立即更新", listener);
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "以后再说", listener);
                if (info.isIgnorable) {
                    dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "忽略该版", listener);
                }
            }
            dialog.show();
        }
    }

    public static class EmptyProgress implements UpdateAgent.OnProgressListener {
        @Override
        public void onStart() {

        }

        @Override
        public void onFinish() {

        }

        @Override
        public void onProgress(int progress) {

        }
    }

    public static class DialogProgress implements UpdateAgent.OnProgressListener {
        private Context mContext;
        private ProgressDialog mDialog;

        public DialogProgress(Context context) {
            mContext = context;
        }

        @Override
        public void onStart() {
            mDialog = new ProgressDialog(mContext);
            mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mDialog.setMessage("下载中...");
            mDialog.setIndeterminate(false);
            mDialog.setCancelable(false);
            mDialog.show();
        }

        @Override
        public void onProgress(int i) {
            if (mDialog != null) {
                mDialog.setProgress(i);
            }
        }

        @Override
        public void onFinish() {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        }
    }

    public static class NotificationProgress implements UpdateAgent.OnProgressListener {
        private Context mContext;
        private int mNotifyId;
        private NotificationCompat.Builder mBuilder;

        public NotificationProgress(Context context, int notifyId) {
            mContext = context;
            mNotifyId = notifyId;
        }

        @Override
        public void onStart() {
            if (mBuilder == null) {
                String title = "下载中 - " + mContext.getString(mContext.getApplicationInfo().labelRes);
                mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setOngoing(true)
                        .setAutoCancel(false)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setSmallIcon(mContext.getApplicationInfo().icon)
                        .setTicker(title)
                        .setContentTitle(title);
            }
            onProgress(0);
        }

        @Override
        public void onProgress(int progress) {
            if (mBuilder != null) {
                if (progress > 0) {
                    mBuilder.setPriority(Notification.PRIORITY_DEFAULT);
                    mBuilder.setDefaults(0);
                }
                mBuilder.setProgress(100, progress, false);

                NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                nm.notify(mNotifyId, mBuilder.build());
            }
        }

        @Override
        public void onFinish() {
            NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(mNotifyId);
        }
    }

    public static class OnPromptClick implements DialogInterface.OnClickListener {
        private final UpdateAgent mAgent;
        private final boolean mIsAutoDismiss;

        public OnPromptClick(UpdateAgent agent, boolean isAutoDismiss) {
            mAgent = agent;
            mIsAutoDismiss = isAutoDismiss;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {

            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                mAgent.update();
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                mAgent.ignore();
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                // not now
                break;
            }
            if (mIsAutoDismiss) {
                dialog.dismiss();
            }
        }
    }
}