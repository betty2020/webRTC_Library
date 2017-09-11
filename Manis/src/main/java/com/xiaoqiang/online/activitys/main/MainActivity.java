package com.xiaoqiang.online.activitys.main;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.settings.SettingActivity;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.fragment.main.ContactFragment;
import com.xiaoqiang.online.fragment.main.HomeFragment;
import com.xiaoqiang.online.fragment.main.MeetingFragment;
import com.xiaoqiang.online.update.UpdateManager;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

/**
 * Ceate author: xiaoqiang on 2017/4/27 10:57
 * MainActivity (TODO)
 * 主要功能：登陆成功后->首页面
 * 邮箱：yugu88@163.com
 */

@RuntimePermissions
public class MainActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    private RadioButton home, metting, contact;
    private FrameLayout layout, layout_base;
    private HomeFragment homeFragment;
    private MeetingFragment meetingFragment;
    private ContactFragment contactFragment;
    private FragmentTransaction ft;
    private TextView tv_head;
    private ImageView btn_back;
    private ImageView add_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitComm.Guest=false;//已登录非游客
        MainActivityPermissionsDispatcher.showStorageWriteWithCheck(this);
        ToastUtils.show(this, "当前版本号：" + InitComm.getVersion(this));
        //有真实接口时调用
        new UpdateManager(this).update();//检测新版本，并升级
        initSetting();
    }

    private void initSetting() {
        tv_head.setText("主页");
        changeFragment(homeFragment);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setImageResource(R.mipmap.head_setup);

    }

    @Override
    public void findViews() {// 此方法在onCreate方法之前调用
        setContentView(R.layout.activity_main);
        // 管理activity
        //        ActivityUtil.getInstance().popOtherActivity(MainActivity.class);
        //        ActivityUtil.getInstance().addActivity(this);
        InitComm.init().getScreenSize(this);//进入应用时初始化屏幕数据
        home = (RadioButton) this.findViewById(R.id.btn_home);
        metting = (RadioButton) this.findViewById(R.id.btn_huiyijilu);
        contact = (RadioButton) this.findViewById(R.id.btn_tongxunlu);
        layout = (FrameLayout) this.findViewById(R.id.fragment_group);
        layout_base = (FrameLayout) this.findViewById(R.id.fragment_base);
        tv_head = (TextView) this.findViewById(R.id.tv_head);
        btn_back = (ImageView) this.findViewById(R.id.btn_back);
        add_img = (ImageView) findViewById(R.id.add_img);
    }

    @Override
    public void init() {// 此方法会在onCreate方法之前调用
        home.setOnCheckedChangeListener(this);
        metting.setOnCheckedChangeListener(this);
        contact.setOnCheckedChangeListener(this);
        btn_back.setOnClickListener(this);
        InitComm.init().startCloudMove(MainActivity.this, layout_base);// 云动画
        homeFragment = new HomeFragment();
        meetingFragment = new MeetingFragment();
        contactFragment = new ContactFragment();
        changeFragment(homeFragment);
    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fm = MainActivity.this.getSupportFragmentManager();
        ft = fm.beginTransaction();
        ft.replace(R.id.fragment_group, fragment);
        ft.commitAllowingStateLoss();
        InitComm.page = 2;
        InitComm.sum = 0;
    }

    public final int Contact_REQUEST_CODE = 3;// 联系人页requestCode
    public final int Meeting_REQUEST_CODE = 2;// 会议页requestCode

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (isChecked) {
            switch (id) {
                case R.id.btn_home:
                    add_img.setVisibility(View.GONE);
                    initSetting();
                    break;
                case R.id.btn_huiyijilu:
                    tv_head.setText("会议记录");
                    add_img.setVisibility(View.VISIBLE);
                    add_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(new Intent(MainActivity.this, AddBookingActivity.class));
                            //overridePendingTransition(R.animator.push_up_in, R.animator.push_up_out);//下往上推出效果
                        }
                    });
                    changeFragment(meetingFragment);
                    break;
                case R.id.btn_tongxunlu:
                    tv_head.setText("通讯录");
                    add_img.setVisibility(View.VISIBLE);
                    add_img.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(new Intent(MainActivity.this, AddContactActivity.class), Contact_REQUEST_CODE);
                            //overridePendingTransition(R.animator.push_up_in, R.animator.push_up_out);//下往上推出效果
                        }
                    });
                    changeFragment(contactFragment);
                    break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 根据上面发送过去的请求吗来区别
        switch (requestCode) {
            case Contact_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    changeFragment(contactFragment);
                }
                break;
            case Meeting_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    changeFragment(meetingFragment);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && InitComm.isFirstClick()) {
            InitComm.init().showDialog(LayoutInflater.from(this).inflate(R.layout.activity_main, null), this, 0);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                startActivity(new Intent(this, SettingActivity.class));
                //overridePendingTransition(R.animator.scale_translate,R.animator.my_alpha_action);//左
                break;
        }
    }

    @Override
    protected void onDestroy() {
        InitComm.init().stopCloud();// 云动画 关闭
        super.onDestroy();
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showStorageWrite() {
        //有真实接口时调用
        // new UpdateManager(this).update();//检测新版本，并升级
        //模拟接口时调用
        // new UpdateManager(this).showNoticeDialog();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleStorageWrite(final PermissionRequest request) {
        showRationaleDialog(R.string.permission_storage_write, request);
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }
}
