package com.xiaoqiang.online.activitys.main.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.cisco.core.entity.UserInfo;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.util.Constants;
import com.cisco.nohttp.NetWorkUtil;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.MainActivity;
import com.xiaoqiang.online.activitys.main.settings.SettingUrlActivity;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.util.SPUtil;

/**
 * @author linpeng
 */
public class LoginActivity extends BaseActivity implements CiscoApiInterface.OnLoginEvents {

    private FrameLayout flLayout;
    private FrameLayout rl;
    private EditText et_username, et_password;
    private ImageView btn_login, btn_guest;
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 管理activity
        //        ActivityUtil.getInstance().popOtherActivity(LoginActivity.class);
        //        ActivityUtil.getInstance().addActivity(this);
        SPUtil.Init(this).setFirstOpenSP(false);// 存储登陆状态

    }

    @Override
    public void findViews() {
        setContentView(R.layout.activity_login);
        rl = (FrameLayout) this.findViewById(R.id.rl);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        btn_login = (ImageView) findViewById(R.id.btn_login);
        btn_guest = (ImageView) findViewById(R.id.btn_guest);

        String username = SPUtil.Init(this).getUserSP();//取 sp中账号
        String password = SPUtil.Init(this).getPassSP();//取 sp中密码
        String address = SPUtil.Init(this).getAddressSP();//取address地址
        if (!TextUtils.isEmpty(username)) {
            et_username.setText(username);
        }
        if (!TextUtils.isEmpty(password)) {
            et_password.setText(password);
        }
        if (!TextUtils.isEmpty(address)) {
            Constants.SERVER = address;
        }
        InitComm.init().startCloudMove(LoginActivity.this, rl);// 云动画
    }

    @Override
    public void init() {
        btn_login.setOnClickListener(this);
        btn_guest.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        username = et_username.getText().toString();
        password = et_password.getText().toString();
        switch (v.getId()) {
            case R.id.btn_login:
                if (username.trim().equals("*#123!?")) {
                    startActivity(new Intent(this, SettingUrlActivity.class));
                } else if (password.trim().equals("") || username.trim().equals("")) {
                    ToastUtils.show(this, this.getResources().getString(R.string.login_emptyNameTip));
                } else if (!NetWorkUtil.hasNetwork(this)) {
                    ToastUtils.show(this, this.getResources().getString(R.string.login_noNetWork));
                } else {
                    InitComm.et_username = username;
                    InitComm.init().showView(this, "", true);
                    CiscoApiInterface.app.Login(username, password, Constants.SERVER, this);// 调取登陆接口回调LoginResult
                }
                break;

            case R.id.btn_guest:
                //匿名登录
                startActivity(new Intent(this, GuestLoginActivity.class));
                break;
        }
    }

    @Override
    public void LoginResult(boolean result, String requestMessage, UserInfo userInfo) {
        MLog.e("userInfo="+userInfo);
        InitComm.userInfo = userInfo;
        InitComm.init().closeView();
        if (result) {
            MLog.d("username=" + username + ",password=" + password);
            SPUtil.Init(this).setUserSP(username);//存 账号
            SPUtil.Init(this).setPassSP(password);//存 密码
            SPUtil.Init(this).setAddressSP(Constants.SERVER);//存address地址
            SPUtil.Init(this).setAutoLogin(true);//自动登录
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            ToastUtils.show(this, requestMessage);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && InitComm.isFirstClick()) {
            InitComm.init().showDialog(LayoutInflater.from(this).inflate(R.layout.activity_main, null), this, 0);
        }
        return false;
    }
}
