package com.github.czy1121.update.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import ezy.boost.update.UpdateAgent;
import ezy.boost.update.UpdateInfo;
import ezy.boost.update.UpdateManager;
import ezy.boost.update.UpdateUtil;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String mCheckUrl = "http://client.waimai.baidu.com/message/updatetag";

    String mUpdateUrl = "http://mobile.ac.qq.com/qqcomic_android.apk";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UpdateManager.setDebuggable(true);
        UpdateManager.setWifiOnly(false);
        UpdateManager.setUrl(mCheckUrl, "yyb");
        UpdateManager.check(this);
        check(false, true, false, false, true, 998);

        findViewById(R.id.check_update).setOnClickListener(this);
        findViewById(R.id.check_update_cant_ignore).setOnClickListener(this);
        findViewById(R.id.check_update_force).setOnClickListener(this);
        findViewById(R.id.check_update_no_newer).setOnClickListener(this);
        findViewById(R.id.check_update_silent).setOnClickListener(this);
        findViewById(R.id.clean).setOnClickListener(this);
    }

    void check(boolean isManual, final boolean hasUpdate, final boolean isForce, final boolean isSilent, final boolean isIgnorable, final int notifyId) {
        UpdateManager.create(this).setUrl(mCheckUrl).setManual(isManual).setNotifyId(notifyId).setParser(new UpdateAgent.InfoParser() {
            @Override
            public UpdateInfo parse(String source) throws Exception {
                UpdateInfo info = new UpdateInfo();
                info.hasUpdate = hasUpdate;
                info.updateContent = "• 支持文字、贴纸、背景音乐，尽情展现欢乐气氛；\n• 两人视频通话支持实时滤镜，丰富滤镜，多彩心情；\n• 图片编辑新增艺术滤镜，一键打造文艺画风；\n• 资料卡新增点赞排行榜，看好友里谁是魅力之王。";
                info.versionCode = 587;
                info.versionName = "v5.8.7";
                info.url = mUpdateUrl;
                info.md5 = "56cf48f10e4cf6043fbf53bbbc4009e3";
                info.size = 10149314;
                info.isForce = isForce;
                info.isIgnorable = isIgnorable;
                info.isSilent = isSilent;
                return info;
            }
        }).check();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.check_update:
            check(true, true, false, false, true, 998);
            break;
        case R.id.check_update_cant_ignore:
            check(true, true, false, false, false, 998);
            break;
        case R.id.check_update_force:
            check(true, true, true, false, true, 998);
            break;
        case R.id.check_update_no_newer:
            check(true, false, false, false, true, 998);
            break;
        case R.id.check_update_silent:
            check(true, true, false, true, true, 998);
            break;
        case R.id.clean:
            UpdateUtil.clean(this);
            Toast.makeText(this, "cleared", Toast.LENGTH_LONG).show();
            break;
        }
    }
}
