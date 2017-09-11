package com.xiaoqiang.online.base;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.xiaoqiang.online.R;
import com.xiaoqiang.online.commonUtils.ActivityUtil;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;


/**
 * Ceate author: xiaoqiang on 2017/4/24 11:03
 * BaseActivity (TODO)
 * 主要功能：所有Activity的父类
 * 邮箱：yugu88@163.com
 */
@RuntimePermissions
public abstract class BaseActivity extends FragmentActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.tab_bottom_font1));
            requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        }
        MLog.e("base___:onCreate()");//追踪视频页回收崩溃
        BaseActivityPermissionsDispatcher.showCameraWithCheck(this);
        ActivityUtil.getInstance().addActivity(this);
    }

    public abstract void findViews();

    public abstract void init();

    /***************************************************************************
     *
     * 操作管理Activity生命周期
     *
     ***************************************************************************/
    @Override
    protected void onDestroy() {
        MLog.e("base___:onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        MLog.e("base___:onPause()");
        super.onPause();
    }

    @Override
    protected void onResume() {
        MLog.e("base___:onResume()");
        super.onResume();
    }

    @Override
    protected void onStart() {
        MLog.e("base___:onStart()");
        super.onStart();
    }

    @Override
    protected void onStop() {
        MLog.e("base___:onStop()");
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        BaseActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showCamera() {
        findViews();
        init();
    }
    @OnShowRationale(Manifest.permission.CAMERA)
    void showRationaleForCamera(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.permission_camera_rationale, request);
    }
    @OnPermissionDenied(Manifest.permission.CAMERA)
    void onCameraDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
        ToastUtils.show(this,""+ R.string.permission_camera_denied);
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    void onCameraNeverAskAgain() {
        ToastUtils.show(this,""+ R.string.permission_camera_never_askagain);
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
                        //ActivityUtil.getInstance().AppExit(BaseActivity.this);
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    @NeedsPermission(Manifest.permission.WRITE_SETTINGS)
    void showWriteSettings() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        BaseActivityPermissionsDispatcher.onActivityResult(this, requestCode);
    }

    @OnShowRationale(Manifest.permission.WRITE_SETTINGS)
    void showRationaleWriteSettings(final PermissionRequest request) {
        showRationaleDialog(R.string.permission_write_settings_rationale, request);
    }
}
