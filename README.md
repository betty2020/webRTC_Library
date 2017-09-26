### 初次提交 2017/4/28

***包名： com.webrtc.manis***

* 已经适配了思科所有会议终端，大麦盒子，共享汽车，及ATM机和各种支持VP9硬解码的开发主板

|Manis|更新 | 版本 |
|------------|-----------|--------|
| 小强在线 | Markdown| 1.0.0 |
| ![image](https://github.com/yugu88/webRTC_Library/blob/master/loading1.png) | ![image](https://github.com/yugu88/webRTC_Library/blob/master/loading2.png) |
| ![image](https://github.com/yugu88/webRTC_Library/blob/master/loading3.png) | ![image](https://github.com/yugu88/webRTC_Library/blob/master/loading4.png) |

#### Log日志控制
* MyAppAplication 
    - MLog.init(true);// Log日志控制

#### 目录结构
* activity
    - main 主页面的类
        - -login 登陆页
        - -settings  设置页
    - welcome   欢迎页的几个类
* adapter   所有的适配器
* base      基类
* commonUtils 常用工具类
* customView  自定义view
* fragment 所有的fragment
 - main 主页面fragment
* javaBeen 所有的解析数据对象
* listener 所有的自定义的接口
* util 工具类

#### 所有引用

    compile project(':ManisCiscoLibrary')
    compile files('libs/bugly_crash_release.jar')
    compile files('libs/java-stubs.jar')
    compile files('libs/SocialSDK_email.jar')
    compile files('libs/SocialSDK_QQ_Simplify.jar')
    compile files('libs/SocialSDK_sms.jar')
    compile files('libs/SocialSDK_WeChat_Simplify.jar')
    compile files('libs/umeng_shareboard_widget.jar')
    compile files('libs/umeng_social_api.jar')
    compile files('libs/umeng_social_net.jar')
    compile files('libs/umeng_social_shareboard.jar')
    compile files('libs/umeng_social_tool.jar')
    compile 'com.android.support:support-v4:25.3.1'
    compile 'com.android.support:appcompat-v7:25.3.1'
    compile 'com.android.support:recyclerview-v7:25.3.1'
    compile 'com.android.support:cardview-v7:25.3.1'
    compile 'com.yanzhenjie:recyclerview-swipe:1.0.4'
    compile 'com.android.support:design:25.3.1'
    compile 'com.android.support.constraint:constraint-layout:1.0.2'
    compile 'com.timehop.stickyheadersrecyclerview:library:0.4.3'

#### 编译版本
    compileSdkVersion 25
    buildToolsVersion "25.0.3"
    useLibrary 'org.apache.http.legacy'

    defaultConfig {
        applicationId "com.webrtc.manis"
        minSdkVersion 15
        targetSdkVersion 25

        jackOptions {
            enabled true
        }
    }
#### gradle版本

    buildscript {
        repositories {
            jcenter()
        }
        dependencies {
            classpath 'com.android.tools.build:gradle:2.3.1'
        }
    }

#### settings依赖
    include ':ManisXmpplibrary'
    include ':ManisCiscoLibrary'
    include ':Manis'

#### gradle-wrapper.properties
    distributionUrl=https\://services.gradle.org/distributions/gradle-3.3-all.zip

#### 关于退出和栈中Activity实时管理
```
    /**
     * 作为良心开发者，不能因为用户多安装一个app而增加一丝卡顿，app退出时必须调用此方法。
     * 彻底退出应用，清空相关的所有进程和堆栈内存
     * 注意：可用内存查看工具查看app退出后是否在堆栈中被彻底清除
     */
        public void AppExit();
    // 栈中移除其他Activity
        ActivityUtil.getInstance().popOtherActivity(MainActivity.class);
    // 把当前Activity添加进管理集合
        ActivityUtil.getInstance().addActivity(this);
        
        
   /**
    * 并非每次从栈中移除都节省CPU和内存的消耗，根据跳转逻辑斟酌使用。
    */
```



