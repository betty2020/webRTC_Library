package com.cisco.core.HttpTask;

import com.alibaba.fastjson.JSON;
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

/**
 * @author linpeng
 */
public class LoginAfterCreateRoomHttp {
    private String tag = "LoginAfterCreateRoomHttp";


    private CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents;

    public String requestMessage = "";
    private String useridtoupper;


    public void requestXmppCreateRoom(String conferencePass,
                                      String hostPass, String xmpp_userid_toupper, String jid, CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents) {
        this.onLoginLafterJoinRoomEvents = onLoginLafterJoinRoomEvents;
        this.useridtoupper=xmpp_userid_toupper;
        Request<String> request = NoHttp.createStringRequest(
                Constants.SERVER
                        + Constants.URL_NOHTTP_CreateConferenceClient
                        + "p=" + conferencePass + "&op=" + hostPass + "&u="
                        + xmpp_userid_toupper + "&j=" + jid,
                RequestMethod.POST);
        CallServer.getRequestInstance().add(0, request,
                httpXmppCreateRoomListener, false, true);
    }

    /**
     * 创建会议接受响应
     */
    private HttpListener<String> httpXmppCreateRoomListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")|| !result.contains("<html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                JSONObject jsonNode = rootNode.getJSONObject("obj");
                if (codeNode.equals("200")) {
                    // 成功
                    String ujid = XmppConnection.getInstance().getConnection().getUser();
                    String op = jsonNode.getString("op");//主持人密码
                    String roomName = jsonNode.getString("r");//会议号
                    String moderator = jsonNode.getString("moderator");//是否是主持人
                    Key.gw = jsonNode.getString("gw");
                    Key.vb = jsonNode.getString("vb");//videobridge
                    Key.roomnumber = roomName;
                    Key.login_iscreateroom = true;
                    Key.op = op;
                    if ("true".equals(moderator) || ujid.equals(moderator)) {
                        Key.Moderator = true;
                    } else {
                        Key.Moderator = false;
                    }
                    Lg.i(tag, "result，Key.Moderator=" + Key.Moderator);
                    RequestLoginLafteCreateRoomResult(true, "");
                }
                if (codeNode.equals("500")) {
                    // 会议已存在,验证会议是否存在等判断result={"code":500,"msg":"Conference exist!","obj":{"r":"100020007"}}
                    String r = jsonNode.getString("r");
                    Key.roomnumber = r;
//                    String ujid = XmppConnection.getInstance().getConnection().getUser();
//                    JoinConferenceHttp gch = new JoinConferenceHttp(onLoginLafterJoinRoomEvents);
//                    gch.outOfXmpp(ujid, Key.roomnumber,useridtoupper);

                    VideoInterface.getInstance().LoginAfterJoinRoom(r,"",onLoginLafterJoinRoomEvents);
                }
            } else {
                RequestLoginLafteCreateRoomResult(false, "请求失败");
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
            RequestLoginLafteCreateRoomResult(false, requestMessage);
        }
    };

    public void RequestLoginLafteCreateRoomResult(boolean issuccess, String requestMessage) {
        if (onLoginLafterJoinRoomEvents != null) {
//            onLoginLafterJoinRoomEvents.LoginLafterCreateRoom(issuccess, op, roomName, requestMessage);
            onLoginLafterJoinRoomEvents.LoginLafterJoinRoom(issuccess,requestMessage);
        }
    }

}
