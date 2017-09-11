package com.cisco.core.xmpp;

import java.util.UUID;


public class Key {

    public static String afferent_nickname = "";
    public static String create_conference_host_pass = "";//主持人密码
    public static String create_conference_pass = "";//会议密码
    public static String conferencePass="";//会议密码

    public static boolean isguest = true;//ture 代表guest登录  false代表账号登录
    public static boolean login_iscreateroom = true;//ture 代表创建会议  false代表账号参加会议

//    public static Map<String, String> map = new HashMap<String, String>();//根据name 取nick
//    public static final String friendName = "friendName";
//    public static final String infoText = "infoText";
//    public static final String ACTION = "com.manis.chatting";

//    public static String ice_stun;
//    public static String ice_credential;
//    public static String ice_credential_turn;
//    public static String ice_credential_username;

//    public static String xmpp_userid_toupper;// 大写userid
//    public static String xmpp_userid_toLower;// 小写userid
//    public static String xmpp_password;// 密码
//    public static String xmpp_username;// 用户名，
//    public static String xmpp_nick;// 昵称是android+随机数
//    public static String xmpp_url;// xmpp 地址 60.206.107.181

    public static String roomnumber;// 房间号 guest房间和 登录成功后的都用这个变量



//
    public static String vb;//预约会议用到的videobridge,可能为空
    public static String gw;//gartway 地址
    public static String title;
    public static String stageJid;// 主屏标志

    public static boolean Moderator=false;//是否是主持人
    public static String op;//主持人密码


//    public static String username;//用户名
//    public static String phone; //用户手机
//    public static String email;//用户邮箱
    public static void clear() {
//        lp.clear();
//        map.clear();
//        videoStreamMap.clear();
    }

    /*
     * 获取uuid
     */
    public static String getUUID() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }


//    public static   Map<String ,VideoRenderCanvas> videoStreamMap=new HashMap<String ,VideoRenderCanvas>();

    public static String SAVE_USER_KEY = "SAVE_USER_KEY";//用户名
    public static String SAVE_PASS_ET_KEY = "SAVE_PASS_ET_KEY";//密码
    public static String SAVE_ADDRESS_ET_KEY = "SAVE_ADDRESS_ET_KEY";//存地址
    public static String SAVE_MIC_KEY = "SAVE_MIC_KEY";//麦克风
    public static String SAVE_SPEAKER_KEY = "SAVE_SPEAKER_KEY";//扬声器
    public static String SAVE_CAMERA_KEY = "SAVE_CAMERA_KEY";//麦克风
    public static String SAVE_VIDEO_NUMBER_KEY = "SAVE_VIDEO_NUMBER_KEY";//视频数量
    public static String SAVE_VIDEO_QUALITY_KEY = "SAVE_VIDEO_QUALITY_KEY";//视频质量

}
