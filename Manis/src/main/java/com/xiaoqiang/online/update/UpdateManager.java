package com.xiaoqiang.online.update;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.KeyEvent;

import com.cisco.core.util.Constants;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.CacheMode;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import org.json.JSONException;
import org.json.JSONObject;

import static android.app.AlertDialog.THEME_HOLO_LIGHT;

public class UpdateManager {

    private Context mContext;
    private String updateUrl = "";
    private long fileSize = 0;

    public UpdateManager(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 检测软件更新
     */

    public void update() {
        InitComm.init().showView(mContext, "正在获取数据...", false);
        Request<String> request = NoHttp.createStringRequest(Constants.UPDATA_VERTION, RequestMethod.GET);
        request.setCacheMode(CacheMode.ONLY_REQUEST_NETWORK);
        CallServer.getRequestInstance().add(0, request, new HttpListener<String>() {
            @Override
            public void onSucceed(int what, Response<String> response) {
                InitComm.init().closeView();
                MLog.e(response.get());
                try {
                    JSONObject jo = new JSONObject(response.get());
                    JSONObject object = jo.getJSONObject("version");
                    String version = object.getString("apkVersion");
                    updateUrl = object.getString("apkPath");
                    MLog.e(updateUrl);
                    if (Integer.parseInt(version) > InitComm.getVersion(mContext)) {
                        showNoticeDialog();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailed(int what, String url, Object tag, Exception message, int responseCode, long networkMillis) {
                InitComm.init().closeView();
                MLog.e(message.getMessage());
            }
        }, false, true);
    }

    /**
     * 显示软件更新对话框
     */
    public void showNoticeDialog() {
        MLog.e(updateUrl);
//        updateUrl = "http://www.ximalaya.com/down?tag=web&client=android";//测试地址
        // 构造对话框
        AlertDialog.Builder builder = new Builder(mContext, THEME_HOLO_LIGHT);
        builder.setTitle("软件更新");
        builder.setMessage("检测到新版本，请立即更新 ");
        // 更新
        builder.setPositiveButton("更新", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 显示下载对话框
                showDownloadDialog();
            }
        });
        AlertDialog noticeDialog = builder.create();
        noticeDialog.setCanceledOnTouchOutside(false);// 点击提示框外面是否取消提示框
        noticeDialog.setCancelable(false);// 点击返回键是否取消提示框
        noticeDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                } else {
                    return false; // 默认返回 false
                }
            }
        });
        noticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    public void showDownloadDialog() {
        DownloadeManager dm = new DownloadeManager(mContext, "小强在线", updateUrl);
        dm.showDownloadDialog();
    }
}
