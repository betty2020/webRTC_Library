package com.xiaoqiang.online.BroadcastReceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.xiaoqiang.online.commonUtils.ToastUtils;

/**
 * Ceate author: xiaoqiang on 2017/6/28 16:30
 * (位置：) com.webrtc.manis.BroadcastReceivers (Context)GetBroadcast
 * TODO->主要功能：此类为广播接收者，接收apk已安装完成的系统广播，从广播中寻找到当前apk的包名，重启当前应用。
 * 邮箱：sin2t@sina.com
 */
public class GetBroadcast extends BroadcastReceiver {

    private static GetBroadcast mReceiver = new GetBroadcast();
    private static IntentFilter mIntentFilter;
    public static void registerReceiver(Context context) {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addDataScheme("package");
        mIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        // mIntentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        // mIntentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED); context.registerReceiver(mReceiver, mIntentFilter);
    }
    public static void unregisterReceiver(Context context) { context.unregisterReceiver(mReceiver);
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String packageName = intent.getData().getSchemeSpecificPart();
        if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
            ToastUtils.show(context,"添加了新的应用");
            PackageManager pm = context.getPackageManager();
            Intent intent1 = new Intent();
            try {
                intent1 = pm.getLaunchIntentForPackage(packageName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        } //else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
        //// Toast.makeText(context, "有应用被删除", Toast.LENGTH_LONG).show();
        // } else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
        //// Toast.makeText(context, "有应用被替换", Toast.LENGTH_LONG).show();
        // }
    }
}
