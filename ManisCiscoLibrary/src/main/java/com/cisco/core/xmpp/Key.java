package com.cisco.core.xmpp;

import com.cisco.core.entity.ParticipantMedia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;


public class Key {
    public static String create_conference_host_pass = "";//主持人密码
    public static String create_conference_pass = "";//会议密码

    public static boolean isguest = true;//ture 代表guest登录  false代表账号登录
    public static boolean login_iscreateroom = true;//ture 代表创建会议  false代表账号参加会议


    public static Map<String, String> map = new HashMap<String, String>();//根据name 取nick
    public static final String friendName = "friendName";
    public static final String infoText = "infoText";
    public static final String ACTION = "com.manis.chatting";
    //	  public static boolean onIceDisconnectedType=false;//ice连接断开类型，true代表用户手动关闭的，false代表ice断开后 程序自己断开的。
    public static String ice_stun;
    public static String ice_credential;
    public static String ice_credential_turn;
    public static String ice_credential_username;

    public static List<ParticipantMedia> lp = new ArrayList<ParticipantMedia>();

    public static String vb;//预约会议用到的videobridge,可能为空
    public static String gw;//gartway 地址
    public static boolean Moderator=false;//是否是主持人
    public static String op;//主持人密码
    public static void clear() {
        lp.clear();
        map.clear();
        Moderator=false;
    }
}
