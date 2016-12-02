# update [![](https://jitpack.io/v/czy1121/update.svg)](https://jitpack.io/#czy1121/update)

清晰灵活简单易用的应用更新库




![update1](screenshot1.png) ![update2](screenshot2.png)
![update3](screenshot3.png) ![update4](screenshot4.png)

## Gradle

``` groovy
repositories { 
    maven { url "https://jitpack.io" }
} 

dependencies {
    compile 'com.github.czy1121:update:1.0.2'
}
```
    
## Usage
  

**基本用法**

默认情况下，查询请求会需要三个参数: 包名(package), 版本号(version), 渠道(channel)
package/version 从应用的 context 获取

``` java
// 设置默认更新接口地址与渠道 
UpdateManager.setUrl(mCheckUrl, "yyb");
``` 

``` java
// 进入应用时查询更新
UpdateManager.check(context);
``` 

``` java 
// 在设置界面点击检查更新
UpdateManager.checkManual(context);
``` 

**设置请求url**

设置url后不会额外添加 package/version/channel 等参数

``` java
UpdateManager.create(this).setUrl(mCheckUrl).check();
```

**解析查询结果**

查询结果需要解析成 UpdateInfo 

``` java 
public class UpdateInfo {
    // 是否有新版本
    public boolean hasUpdate = false;
    // 是否静默下载：有新版本时不提示直接下载，下次启动时安装
    public boolean isSilent = false;
    // 是否强制安装：不安装无法使用app
    public boolean isForce = false;
    // 是否可忽略该版本
    public boolean isIgnorable = true;
    // 是否是增量补丁包，暂不支持
    public boolean isPatch = false;
    
    public int versionCode;
    public String versionName;
    public String updateContent;
    
    public String url;
    public String md5;
    public long size;
    
    public String patchUrl;
    public String patchMd5;
    public long patchSize;
}
```

可以定制解析过程

``` java
UpdateManager.create(this).setUrl(mCheckUrl).setParser(new UpdateAgent.InfoParser() {
    @Override
    public UpdateInfo parse(String source) throws Exception {
        UpdateInfo info = new UpdateInfo(); 
        // todo
        return info;
    }
}).check();
```

**更新版本对话框**

``` java
UpdateManager.create(this).setOnPrompt(new UpdateAgent.OnPromptListener() {
    @Override
    public void onPrompt(UpdateAgent agent) { 
        // todo : 根据 agent.getInfo() 显示更新版本对话框，具体可参考 UpdateAgent.OnPrompt
    }
}).check();
```

**没有新版本或出错**

``` java
UpdateManager.create(this).setOnFailure(new UpdateAgent.OnFailureListener() {
    @Override
    public void onFailure(UpdateError error) {  
        Toast.makeText(mContext, error.toString(), Toast.LENGTH_LONG).show();
    }
}).check();
```

**显示下载进度**

可在通知栏显示下载进度，当 info.isSilent 为 true 显示

默认通知栏进度 

``` java
UpdateManager.create(this).setNotifyId(998).check();
```

定制通知栏进度 

``` java
UpdateManager.create(this).setOnNotify(new UpdateAgent.OnProgressListener() {
    @Override
    public void onStart() {
        // todo: start
    }

    @Override
    public void onProgress(int progress) {
        // todo: progress
    }

    @Override
    public void onFinish() {
        // todo: finish
    }
}).check();
```

定制下载进度的对话框，当 info.isSilent 为 false 显示

``` java
UpdateManager.create(this).setOnProgress(new UpdateAgent.OnProgressListener() {
    @Override
    public void onStart() {
        // todo: start
    }

    @Override
    public void onProgress(int progress) {
        // todo: progress
    }

    @Override
    public void onFinish() {
        // todo: finish
    }
}).check();
```

## License

```
Copyright 2016 czy1121

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```