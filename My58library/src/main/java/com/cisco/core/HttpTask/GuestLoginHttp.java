package com.cisco.core.HttpTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
public class GuestLoginHttp {
    private String tag = "LoginHttp";


    public boolean issuccess;
    private XmppConnection xmppConnection = XmppConnection.getInstance();
    private CiscoApiInterface.OnGuestLoginEvents OnGuestLoginlistener;

    public String roomnumber;// 房间号 guest房间和 登录成功后的都用这个变量
    public String username;// 登录的用户名
    public String conferencePass;//会议密码

    public String requestMessage = "";
    private VideoInterface videoInterface;

    public void requestGuestGetNick(String roomnumber, String guest_nickname,
                                    String conferencePass, CiscoApiInterface.OnGuestLoginEvents OnGuestLoginlistener, VideoInterface videoInterface) {
        this.OnGuestLoginlistener = OnGuestLoginlistener;
        this.roomnumber = roomnumber;
        this.username = guest_nickname;
        this.conferencePass = conferencePass;
        this.videoInterface = videoInterface;

        Request<String> request = NoHttp.createStringRequest(
                Constants.SERVER + Constants.URL_NOHTTP_GUEST_LOGIN,
                RequestMethod.POST);
        request.add("nickname", guest_nickname);
        CallServer.getRequestInstance().add(0, request,
                guesthttpListener, false, true);
    }

    private HttpListener<String> guesthttpListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
//                String codeNode = rootNode.getString("code");
                JSONObject jsonNode = rootNode.getJSONObject("obj");
                String guest_username = jsonNode.getString("mUserName");
                String guest_mUserId = jsonNode.getString("mUserId");
                String guest_password = jsonNode.getString("token");
                String guest_xmpp_url = jsonNode.getString("xmpp");
                JSONObject turnNode = jsonNode.getJSONObject("turn");
                JSONArray iceNode = turnNode.getJSONArray("iceServers");
                String ice_stun = iceNode.getJSONObject(0).getString("url");
//                String ice_credential = iceNode.getJSONObject(1).getString("credential");
//                String ice_credential_turn = iceNode.getJSONObject(1).getString("url");
//                String ice_credential_username = iceNode.getJSONObject(1).getString("username");
                if (ice_stun != null)
                    videoInterface.setStun(ice_stun);
                if (guest_mUserId != null)
                    videoInterface.setUserId(guest_mUserId);

                // guest登录会控系统
                requestGuestLoginXmpp(guest_username, guest_password, guest_xmpp_url, guest_mUserId);
            } else {
                RequestGuestLoginResult(false, "请求失败");
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
            RequestGuestLoginResult(false, requestMessage);
        }
    };

    /**
     * 登录xmpp服务器
     *
     * @param guestPassword
     * @param guestXmppUrl
     * @param guestMUserId
     */
    public void requestGuestLoginXmpp(final String guestUsername, final String guestPassword, final String guestXmppUrl, final String guestMUserId) {
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle data = (Bundle) msg.obj;
                boolean issuccess = data.getBoolean("guest_xmpp_login_result");
                if (issuccess) {
                    // 判断资源是否超出限制https://manis.fdclouds.com//user/join?u=39df48694f9af987@60.206.107.181/android806&r=109920016$c=71FB08A415371A3A
//					String ujid = xmppConnection.getConnection(context).getUser();
//					outOfXmpp(ujid);
                    Key.isguest = true;//GUEST登录
//                joinConferenceClient(roomnumber, conferencePass,xmpp_userid_toupper);
                    JoinConferenceHttp gch = new JoinConferenceHttp(OnGuestLoginlistener);
                    String type = "0";// 用于区分验证会议是否存在接口，0代表guest匿名参会，1代表登录成功后用户参会
                    gch.joinConferenceClient(roomnumber, conferencePass, guestMUserId.toUpperCase());
                } else {
                    requestMessage = "加入房间失败!";
                    RequestGuestLoginResult(false, requestMessage);
                }
            }
        };
        new Thread() {
            public void run() {
                String endpoint = "android" + Integer.toString((new Random()).nextInt(1000));
                issuccess = xmppConnection.login(guestUsername, guestPassword, endpoint, guestXmppUrl, guestMUserId.toLowerCase());
                Lg.i(tag, "issuccess=" + issuccess);
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putBoolean("guest_xmpp_login_result", issuccess);
                msg.obj = bundle;
                handler.sendMessage(msg);
            }
        }.start();
    }

    public void RequestGuestLoginResult(boolean issuccess, String requestMessage) {
        if (OnGuestLoginlistener != null) {
            OnGuestLoginlistener.GuestLoginResult(issuccess, requestMessage);
        }
    }
}


