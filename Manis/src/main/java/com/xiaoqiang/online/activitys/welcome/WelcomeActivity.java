package com.xiaoqiang.online.activitys.welcome;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;

import com.cisco.core.entity.UserInfo;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.util.Constants;
import com.cisco.nohttp.NetWorkUtil;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.MainActivity;
import com.xiaoqiang.online.activitys.main.login.LoginActivity;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.util.SPUtil;


/**
 * 引导
 *
 * @author jilinpeng
 */
public class WelcomeActivity extends BaseActivity implements CiscoApiInterface.OnLoginEvents {

    private final int SPLASH_DISPLAY_LENGHT = 2000;
    private String TAG = "WelcomeActivity";
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle bundle) {
        MLog.e("onCreate1:" + System.currentTimeMillis());
        super.onCreate(bundle);
        MLog.e("onCreate2:" + System.currentTimeMillis());
    }

    @Override
    public void findViews() {
        MLog.e("findViews:" + System.currentTimeMillis());
        setContentView(R.layout.activity_welcome);
        InitComm.init().getScreenSize(this);//进入应用时初始化屏幕数据
        // 管理activity
        //		ActivityUtil.getInstance().addActivity(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        //取 sp中账号密码
        username = SPUtil.Init(WelcomeActivity.this).getUserSP();
        password = SPUtil.Init(WelcomeActivity.this).getPassSP();
        if (!NetWorkUtil.hasNetwork(WelcomeActivity.this)) {
            ToastUtils.show(WelcomeActivity.this, "请连接网络");
        }
    }

    @Override
    public void init() {
        MLog.e("init_start:" + System.currentTimeMillis());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!TextUtils.isEmpty(username) & !TextUtils.isEmpty(password) & SPUtil.Init(WelcomeActivity.this).getAutoLogin()) {
                    MLog.e("init_run:" + System.currentTimeMillis());
                    CiscoApiInterface.app.Login(username, password, Constants.SERVER,WelcomeActivity.this);
                } else {
                    checkNextStep(false);
                }
            }
        }, SPLASH_DISPLAY_LENGHT);
        //        new Handler(new Handler.Callback() {
        //            //处理接收到的消息的方法
        //            @Override
        //            public boolean handleMessage(Message arg0) {
        //                MLog.e("init_run:" + System.currentTimeMillis());
        //                CiscoApiInterface.app.Login(username, password, Constants.SERVER, WelcomeActivity.this, WelcomeActivity.this);
        //                return false;
        //            }
        //        }).sendEmptyMessageDelayed(0, SPLASH_DISPLAY_LENGHT); //表示延时三秒进行任务的执行
        MLog.e("init_end:" + System.currentTimeMillis());
    }

    /**
     * 判断下一步行动
     */
    private void checkNextStep(boolean isLogin) {
        Intent intent = new Intent();
        if (SPUtil.Init(this).getFirstOpenSP()) {
            intent.setClass(this, GuideActivity.class);
        } else if (isLogin) {
            intent.setClass(this, MainActivity.class);
        } else if (!isLogin) {
            intent.setClass(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void LoginResult(boolean result, String requestMessage, UserInfo userInfo) {
        MLog.e("LoginResult:" + System.currentTimeMillis());
        MLog.e("userInfo="+userInfo);
        InitComm.userInfo = userInfo;
        checkNextStep(result);
    }
}
