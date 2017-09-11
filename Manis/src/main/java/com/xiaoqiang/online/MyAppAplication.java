package com.xiaoqiang.online;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.text.TextUtils;

import com.cisco.core.interfaces.CiscoApiInterface;
import com.mob.MobSDK;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaoqiang.online.BroadcastReceivers.GetBroadcast;
import com.xiaoqiang.online.commonUtils.ActivityUtil;
import com.xiaoqiang.online.commonUtils.MLog;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:07
 * MyAppAplication (TODO)
 * 主要功能：按照标准设计模式，重写的Application类，方便后续在此类中做部分操作
 * 邮箱：yugu88@163.com
 */
public class MyAppAplication extends android.app.Application {

    private String packgeName;
    protected boolean isNeedCaughtExeption = true;// 是否捕获未知异常
    private MyUncaughtExceptionHandler uncaughtExceptionHandler;
    private PendingIntent restartIntent;
    @Override
    public Context getApplicationContext() {
        return super.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // ------------------------------------------------------------------------------------//
        packgeName = getPackageName();
        if (isNeedCaughtExeption) {
            cauchException();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {// 读写权限，升级使用
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }
        //腾讯bug
        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        // 初始化Bugly
        CrashReport.initCrashReport(context, "fea7e6fe7b", false);
        GetBroadcast.registerReceiver(getApplicationContext());//注册广播，用于监听应用是否安装完成

        // -------------------------初始化----------------------------------------------------//
        CiscoApiInterface.init(this);// 初始化SDK
        MobSDK.init(this, null, null);
        MLog.init(true);// Log日志控制
        NoHttp.initialize(this); // NoHttp默认初始化。
        Logger.setDebug(true); // 开启NoHttp调试模式。
        Logger.setTag("webRTC_NoHttp"); // 设置NoHttp打印Log的TAG。

    }

    /**
     * 获取进程号对应的进程名
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    // -------------------异常捕获-----捕获异常后重启系统-----------------//
    @SuppressWarnings("WrongConstant")
    private void cauchException() {
        Intent intent = new Intent();
        // 参数1：包名，参数2：程序入口的activity
        intent.setClassName(packgeName, packgeName + ".activitys.welcome.WelcomeActivity");
        restartIntent = PendingIntent.getActivity(getApplicationContext(), -1, intent,Intent.FLAG_ACTIVITY_NEW_TASK);

        // 程序崩溃时触发线程
        uncaughtExceptionHandler = new MyUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

    // 创建服务用于捕获崩溃异常
    private class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable ex) {
            // 保存错误日志
            // saveCatchInfo2File(ex);
            // 1秒钟后重启应用
//            AlarmManager mgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);
            // 关闭当前应用
            ActivityUtil.getInstance().AppExit(getApplicationContext());

//            finishProgram();
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @return 返回文件名称
     */
    private String saveCatchInfo2File(Throwable ex) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String sb = writer.toString();
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String time = formatter.format(new Date());
            String fileName = time + "buglog" + ".txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String filePath = Environment.getExternalStorageDirectory() + "/Xiaoqiang/" + packgeName
                        + "/BugLog/";
                File dir = new File(filePath);
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        // 创建目录失败: 一般是因为SD卡被拔出了
                        return "";
                    }
                }
                MLog.d("xiaoqiang捕捉报错日志=" + filePath + fileName);
                FileOutputStream fos = new FileOutputStream(filePath + fileName);
                fos.write(sb.getBytes());
                fos.close();
                //文件保存完了之后,在应用下次启动的时候去检查错误日志,发现新的错误日志,就发送给开发者
            }
            return fileName;
        } catch (Exception e) {
            System.out.println("an error occured while writing file..." + e.getMessage());
        }
        return null;
    }

    // 结束线程,一般与finishAllActivity()一起使用
    // 例如: finishAllActivity;finishProgram();
    public static void finishProgram() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
