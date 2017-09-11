package com.cisco.core.HttpTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.entity.UserInfo;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.interfaces.VideoInterface;
import com.cisco.core.util.Constants;
import com.cisco.core.util.Lg;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import java.util.Random;

/**
 * @author linpeng
 */
public class LoginHttp {

    private String tag = "LoginHttp";

    public boolean issuccess;
    private CiscoApiInterface.OnLoginEvents OnLoginlistener;

    public String requestMessage = "";
    private VideoInterface videoInterface;
    private UserInfo userInfo;

    public void requestLogin(String username, String password, CiscoApiInterface.OnLoginEvents OnLoginlistener, VideoInterface videoInterface) {
        this.OnLoginlistener = OnLoginlistener;
        this.videoInterface = videoInterface;
        Request<String> request = NoHttp.createStringRequest(Constants.SERVER + Constants.URL_NOHTTP_LOGIN, RequestMethod.POST);
        request.add("username", username);
        request.add("password", password);
        userInfo =new UserInfo();
        CallServer.getRequestInstance().add(0, request, httpListener, false, false);
    }

    /**
     * login接受响应
     */
    private String username,phone,email,endpoint,mUserId;
    public HttpListener<String> httpListener = new HttpListener<String>() {

        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                if (rootNode != null) {
                    String codeNode = rootNode.getString("code");
                    if (codeNode.equals("200")) {
                        JSONObject jsonNode = rootNode.getJSONObject("obj");
                        username = jsonNode.getString("mUserName");
                        mUserId = jsonNode.getString("mUserId");
                        String password = jsonNode.getString("token");
                        String xmppUrl = jsonNode.getString("xmpp");
                        phone = jsonNode.getString("phone");
                        email = jsonNode.getString("email");

                        JSONObject turnNode = jsonNode.getJSONObject("turn");
                        JSONArray iceNode = turnNode.getJSONArray("iceServers");
                        String ice_stun = iceNode.getJSONObject(0).getString("url");
//                        String ice_credential = iceNode.getJSONObject(1).getString("credential");
//                        String ice_credential_turn = iceNode.getJSONObject(1).getString("url");
//                        String ice_credential_username = iceNode.getJSONObject(1).getString("username");
                        if (ice_stun != null)
                            videoInterface.setStun(ice_stun);
                        if (mUserId != null)
                            videoInterface.setUserId(mUserId);
                        Lg.i(tag, "result=username=" + username);
                        Lg.i(tag, "result=mUserId=" + mUserId);
                        Lg.i(tag, "result=password=" + password);
                        Lg.i(tag, "result=xmppUrl=" + xmppUrl);
                        // 登录xmpp服务器
                        requestLoginXmpp(xmppUrl, username, password, mUserId);
                    }
                    if (codeNode.equals("403")) {
                        requestMessage = "用户名或密码不正确";
                        RequestLoginResult(false, requestMessage,userInfo);
                    }
                }
            } else {
                RequestLoginResult(false, "请求失败",userInfo);
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            if (message.getMessage().contains("time out")) {
                requestMessage = "请求超时";
            } else {
                requestMessage = "请求失败";
            }
            RequestLoginResult(false, requestMessage,userInfo);
        }
    };

    /**
     * 会员登录 xmpp服务器
     *
     * @param xmppUrl
     * @param username
     * @param password
     */
    public void requestLoginXmpp(final String xmppUrl, final String username, final String password, final String userId) {
        new Thread() {
            public void run() {
                endpoint = "android" + Integer.toString((new Random()).nextInt(1000));
                issuccess = XmppConnection.getInstance().login(username, password, endpoint, xmppUrl, userId);
                Lg.i(tag, "issuccess=" + issuccess);
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putBoolean("xmpp_login_result", issuccess);
                msg.obj = bundle;
                handler.sendMessage(msg);
            }
        }.start();
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Bundle data = (Bundle) msg.obj;
            boolean issuccess = data.getBoolean("xmpp_login_result");
            Key.isguest = false;//会员登录

            userInfo.setEmail(email);
            userInfo.setEndpoint(endpoint);
            userInfo.setmUserName(username);
            userInfo.setPhone(phone);
            userInfo.setmUserId(mUserId);
            if (issuccess) {
                userInfo.setmJid( XmppConnection.getInstance().getConnection().getUser());
                RequestLoginResult(issuccess, "登录成功",userInfo);
            } else {
                RequestLoginResult(issuccess, "登录失败",userInfo);
            }
        }
    };

    public void RequestLoginResult(boolean issuccess, String requestMessage, UserInfo userInfo) {
        if (OnLoginlistener != null) {
            OnLoginlistener.LoginResult(issuccess, requestMessage,userInfo);
        }
    }

}
