package com.cisco.core.HttpTask;

import android.content.Context;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.util.Constants;
import com.cisco.core.util.Lg;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

/**
 * @author linpeng
 */
public class AdminHttp {
    private String jid;
    private String roomnumber;
    private String userid;
    private String tag = "AdminHttp";

    public AdminHttp(String jid, String roomnumber, String coverUserid) {
        this.jid = jid;
        this.roomnumber = roomnumber;
        this.userid = coverUserid;
    }

    public void GrantAdmin() {
//        https://manis.fdclouds.com//user/grantAdmin?j=4ae280ded6703e2f@60.206.107.181/android24&r=101120003              //别人的jid
        Request<String> request = NoHttp.createStringRequest(Constants.SERVER + Constants.URL_NOHTTP_GrantAdmin + "j=" + jid + "&r=" + roomnumber);
        CallServer.getRequestInstance().add(0, request, httpGrantAdminListener, false, true);
    }

    private HttpListener<String> httpGrantAdminListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    //授权成功！
                    XmppConnection.getInstance().SendGrantAdminByMessage(jid, userid);
                    Key.Moderator = false;
                    XmppConnection.getInstance().changeConfigRole("NONE");//改变自己状态
//                    Toast.makeText(context, "授权成功！", Toast.LENGTH_SHORT).show();
                } else {
//                    Toast.makeText(context, "授权失败！", Toast.LENGTH_SHORT).show();
                }
            } else {
//                Toast.makeText(context, "请求失败！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
//            Toast.makeText(context, "请求失败！", Toast.LENGTH_SHORT).show();
        }
    };


}
