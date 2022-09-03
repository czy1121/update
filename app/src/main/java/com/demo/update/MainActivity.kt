package com.demo.update

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel
import me.reezy.cosmo.update.HttpUtil
import me.reezy.cosmo.update.UpdateInfo
import me.reezy.cosmo.update.UpdateManager
import me.reezy.cosmo.update.UpdatePromptDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MMKV.initialize(applicationContext, if (BuildConfig.DEBUG) MMKVLogLevel.LevelDebug else MMKVLogLevel.LevelNone)

//        UpdateManager.setChecker("http://yourdomain.com/path")

        UpdateManager.setChecker {
            UpdateInfo(
                hasUpdate = true,
                isIgnorable = true,
                updateContent = "• 支持文字、贴纸、背景音乐，尽情展现欢乐气氛；\n• 两人视频通话支持实时滤镜，丰富滤镜，多彩心情；\n• 图片编辑新增艺术滤镜，一键打造文艺画风；\n• 资料卡新增点赞排行榜，看好友里谁是魅力之王。",
                url = "https://dldir1v6.qq.com/dmpt/apkSet/10.2.7/qqcomic_android_10.2.7_dm2017_arm32.apk",
                hash = "f32382019552738f6cd369439f8ca965"
            )
        }

        UpdateManager.check(this)
    }
}