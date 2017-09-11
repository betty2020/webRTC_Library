package com.cisco.core.HttpTask;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.util.Constants;
import com.cisco.core.util.Lg;
import com.cisco.core.util.Tools;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

/**
 * @author linpeng
 */
public class JoinConferenceHttp {
    private String tag = "JoinConferenceHttp";

    private CiscoApiInterface.OnGuestLoginEvents OnGuestLoginlistener;
    private CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents;
    private String requestMessage = "";
    private String useridtoupper;

    public JoinConferenceHttp(CiscoApiInterface.OnGuestLoginEvents OnGuestLoginlistener) {
        this.OnGuestLoginlistener = OnGuestLoginlistener;
    }

    public JoinConferenceHttp(CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents) {
        this.onLoginLafterJoinRoomEvents = onLoginLafterJoinRoomEvents;
    }

    /**
     * guest JoinConferenceClient
     */
    public void joinConferenceClient(String roomnumber, String conferencePass, String useridtoupper) {
        this.useridtoupper = useridtoupper;
        Request<String> request = NoHttp.createStringRequest(
                Constants.SERVER
                        + Constants.URL_NOHTTP_JoinConferenceClient + "r="
                        + roomnumber + "&p=" + Tools.get32MD5Str(conferencePass) + "&u="
                        + useridtoupper + "&j=" + XmppConnection.getInstance().getConnection().getUser(), RequestMethod.POST);
        CallServer.getRequestInstance().add(0, request, httpJoinConferenceListener, false, true);
    }

    /**
     * 登录响应
     */
    private HttpListener<String> httpJoinConferenceListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            // TODO: 2017/7/24 这个接口可能存在问题，输入会议号仍无法入会。也可能是回调函数逻辑的问题。
            Lg.i(tag, "result=" + result);// result={"code":200,"msg":null,"obj":null},type=1
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    JSONObject jsonNode = rootNode.getJSONObject("obj");//"obj":null 是否需要判断屏蔽
                    Key.vb = jsonNode.getString("vb");
                    String moderator = jsonNode.getString("moderator");//主持人
                    Key.gw = jsonNode.getString("gw");
                    Key.title = jsonNode.getString("title");
                    Key.stageJid = jsonNode.getString("stageJid");//主屏 null
                    Log.e("Key.stageJid=", Key.stageJid);
                    String ujid = XmppConnection.getInstance().getConnection().getUser();

                    Lg.e(tag, "Moderator_result=" + moderator + ",ujid=" + ujid);

                    if ("true".equals(moderator) || ujid.equals(moderator)) {
                        Key.Moderator = true;
                    } else {
                        Key.Moderator = false;
                    }
                    Lg.e(tag, "主持人Key.Moderator=" + Key.Moderator);
                    outOfXmpp(ujid, Key.roomnumber, useridtoupper);
                } else if (codeNode.equals("401")) {
                    requestMessage = "会议室不存在或密码错误!";
                    RequestOnFailed();
                } else if (codeNode.equals("403")) {
                    requestMessage = "会议室已经被锁定!";
                    RequestOnFailed();
                }
            } else {
                requestMessage = "请求失败";
                RequestOnFailed();
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
            RequestOnFailed();
        }
    };

    private void RequestOnFailed() {
        if (!RequestGuestLoginResult(false, requestMessage))//如果不是guest的会议，则回调正常用户
            RequestLoginLafterJoinRoomResult(false, requestMessage);
    }


    private boolean RequestGuestLoginResult(boolean issuccess, String requestMessage) {
        if (OnGuestLoginlistener != null) {
            OnGuestLoginlistener.GuestLoginResult(issuccess, requestMessage);
            return true;
        }
        return false;
    }

    private boolean RequestLoginLafterJoinRoomResult(boolean issuccess, String requestMessage) {
        if (onLoginLafterJoinRoomEvents != null) {
            onLoginLafterJoinRoomEvents.LoginLafterJoinRoom(issuccess, requestMessage);
            return true;
        }
        return false;
    }


    /**
     * 验证超出会议资源使用限制
     */
    public void outOfXmpp(String ujid, String roomnumber, String useridtoupper) {
        // https://manis.fdclouds.com/user/join?j=39df48694f9af987@60.206.107.181/ios424&r=109920008&u=39DF48694F9AF987
        Request<String> request = NoHttp.createStringRequest(
                Constants.SERVER + Constants.URL_NOHTTP_OutOfResources
                        + "j=" + ujid + "&r=" + roomnumber + "&u="
                        + useridtoupper, RequestMethod.POST);
        CallServer.getRequestInstance().add(0, request,
                httpOutOfListener, false, true);
    }

    /**
     * 验证超出会议资源使用限制,禁止使用会议 响应
     */
    private HttpListener<String> httpOutOfListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (result.contains("code")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    if (!RequestGuestLoginResult(true, "入会成功"))//如果不是guest的会议，则回调正常用户
                        RequestLoginLafterJoinRoomResult(true, "入会成功");
                } else {
                    //						logAndToast("超出会议资源使用限制,禁止使用会议");
                    requestMessage = "超出会议资源使用限制,禁止使用会议";
                    RequestOnFailed();
                }
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            //				logAndToast("请求失败");
            if (message.getMessage().contains("time out")) {
                requestMessage = "请求超时";
            } else {
                requestMessage = "请求失败";
            }
            RequestOnFailed();
        }
    };
}
