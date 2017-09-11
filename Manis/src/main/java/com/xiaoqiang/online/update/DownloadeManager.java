package com.xiaoqiang.online.update;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xiaoqiang.online.R;
import com.xiaoqiang.online.commonUtils.MLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import static android.app.AlertDialog.THEME_HOLO_LIGHT;


public class DownloadeManager {

    private Context mContext;
    private String apkNames;
    private String apkUrl;
    private AlertDialog noticeDialog;
    private AlertDialog downloadDialog;
    /* 下载包安装路径 */
    private String savePath = "";

    private String saveFileName = "";
    /* 进度条与通知ui刷新的handler和msg常量 */
    private ProgressBar mProgress;

    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    private int progress;
    private Thread downLoadThread;
    private boolean interceptFlag = false;
    public boolean isdown = false;
    private TextView updatePercentTextView;
    private TextView updateCurrentTextView;
    private TextView updateTotalTextView;
    private int apkLength;
    private int apkCurrentDownload;
    private NumberProgressBar mNumProgress;

    public DownloadeManager(Context mContext, String apkNames, String apkUrl) {
        this.mContext = mContext;
        this.apkNames = apkNames;
        this.apkUrl = apkUrl;
        this.savePath = G.CachePath;
        this.saveFileName = savePath + apkNames;
        downloadApk();
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE:
                    mProgress.setProgress(progress);
                    mProgress.setSecondaryProgress((int) (progress * 1.2));
                    mNumProgress.setProgress(progress);
                    updatePercentTextView.setText(progress + "" + "%");
                    try {
                        int currentM, currentK, totalM, totalK;
                        currentM = apkCurrentDownload / 1024 / 1024;
                        currentK = apkCurrentDownload / 1024 - currentM * 1024;
                        totalM = apkLength / 1024 / 1024;
                        totalK = apkLength / 1024 - totalM * 1024;
                        updateCurrentTextView.setText(currentM + "." + currentK + "MB/");
                        updateTotalTextView.setText(totalM + "." + totalK + "MB");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case DOWN_OVER:

                    installApk();
                    break;
                default:
                    break;
            }
        }

        ;
    };


    //外部接口让主Activity调用
    public void checkUpdateInfo(String alert_msg) {
        showNoticeDialog(alert_msg);
    }


    public void showNoticeDialog(String alertInfo) {
        Builder builder = new Builder(mContext);
        builder.setTitle("软件版本更新");
        builder.setMessage(alertInfo);
        builder.setPositiveButton("下载", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                showDownloadDialog();
            }
        });
        builder.setNegativeButton("以后再说", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        noticeDialog = builder.create();
        noticeDialog.show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void showDownloadDialog() {
        Builder builder = new AlertDialog.Builder(mContext, THEME_HOLO_LIGHT);
        builder.setTitle("正在下载");
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.new_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.progress);
        mNumProgress = (NumberProgressBar) v.findViewById(R.id.numberbar1);

        updatePercentTextView = (TextView) v.findViewById(R.id.updatePercentTextView);
        updateCurrentTextView = (TextView) v.findViewById(R.id.updateCurrentTextView);
        updateTotalTextView = (TextView) v.findViewById(R.id.updateTotalTextView);
        builder.setView(v);
        downloadDialog = builder.create();
        downloadDialog.setCanceledOnTouchOutside(false);// 点击提示框外面是否取消提示框
        downloadDialog.setCancelable(false);// 点击返回键是否取消提示框
        downloadDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else {
                    return false; //默认返回 false
                }
            }
        });
        downloadDialog.show();

    }

    private Runnable mdownApkRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                File file = new File(savePath);
                if (!file.exists()) {
                    file.mkdirs();
                }
                String apkFile = saveFileName;
                File ApkFile = new File(apkFile);
                FileOutputStream fos = new FileOutputStream(ApkFile);
                MLog.e(apkUrl);
                URL url = new URL(apkUrl);
                try {
                    SSLContext sc = SSLContext.getInstance("SSL");
                    sc.init(null, new TrustManager[]{new TrustAnyTrustManager()},
                            new java.security.SecureRandom());
                    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
                    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.setDoOutput(true);
//                conn.setDoInput(true);
                conn.connect();
                apkLength = conn.getContentLength();
                InputStream is = conn.getInputStream();
                apkCurrentDownload = 0;
                byte buf[] = new byte[4096];//1024个字节，也就是1kb,可以提高下载速度
                int length = -1;
                while ((length = is.read(buf)) != -1) {
                    apkCurrentDownload += length;
                    progress = (int) (((float) apkCurrentDownload / apkLength) * 100);
                    //更新进度
                    mHandler.sendEmptyMessage(DOWN_UPDATE);
                    fos.write(buf, 0, length);
                    if (apkCurrentDownload == apkLength) {
                        //下载完成通知安装
                        mHandler.sendEmptyMessage(DOWN_OVER);
                        break;
                    }
                    if (interceptFlag) {
                        ApkFile.delete();
                        break;
                    }
                }
                fos.close();
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }
    /**
     * 下载apk
     *
     * @param
     */

    private void downloadApk() {
        downLoadThread = new Thread(mdownApkRunnable);
        downLoadThread.start();
    }

    /**
     * 安装apk
     *
     * @param
     */
    private void installApk() {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);
        ShowNotifaction(i);
        android.os.Process.killProcess(android.os.Process.myPid());
        try {
            if (downloadDialog != null && downloadDialog.isShowing()) {
                //downloadDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void ShowNotifaction(Intent intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                mContext,
                R.string.app_name,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle("下载完成!")
                        .setContentText("点击安装新版本!");
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(R.string.app_name, mBuilder.build());
    }

}  
