package com.cisco.core.interfaces;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.cisco.core.AppRTCAudioManager;
import com.cisco.core.HttpTask.*;
import com.cisco.core.PeerConnectionClient;
import com.cisco.core.VideoImplement;
import com.cisco.core.httpcallback.ConferenceRecordCallback;
import com.cisco.core.httpcallback.FriendsCallback;
import com.cisco.core.httpcallback.MyCallback;
import com.cisco.core.util.Constants;
import com.cisco.core.util.SdkPublicKey;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon.ScalingType;
import org.webrtc.SurfaceViewRenderer;

import java.util.List;

/**
 * @author jlp
 */

public final class VideoInterface implements CiscoApiInterface.OnCallEvents {

    /////////////////////////////////////对音视频操作////////////////////////////////////////////////
    private boolean micEnabled = true;
    private boolean viewEnabled = true;
    private boolean videoEnabled = true;
    private boolean audioEnabled = true;
    private static VideoInterface videoInterface;

    private String stun;
    private String userId;

    public void setStun(String stun) {
        this.stun = stun;
    }

    private VideoInterface() {
    }

    public static VideoInterface getInstance() {
        if (videoInterface == null) {
            synchronized (VideoInterface.class) {
                videoInterface = new VideoInterface();
            }
        }
        return videoInterface;
    }


    /***
     * (2)login vip登录
     * @return
     */
    public void Login(String username, String password, String addressUrl,
                      CiscoApiInterface.OnLoginEvents OnLoginlistener) {
        Constants.SERVER = addressUrl;
        LoginHttp lh = new LoginHttp();
        lh.requestLogin(username, password, OnLoginlistener, this);
    }

    /***
     * （3）guestlogin 匿名登录
     * @return
     */
    public void GuestLogin(String roomnumber, String username,
                           String conferencePass, String addressUrl,
                           CiscoApiInterface.OnGuestLoginEvents OnGuestLoginlistener) {
        Key.roomnumber = roomnumber;
        Constants.SERVER = addressUrl;
        GuestLoginHttp glh = new GuestLoginHttp();
        glh.requestGuestGetNick(roomnumber, username, conferencePass, OnGuestLoginlistener, this);
    }

    /***
     * （4）登录成功后， 如果房间没有存在 创建自己的房间 （即时会议），如果存在直接进入会议。
     * @return
     */

    public void LoginAfterCreateRoom(
            final String conferencePass,
            final String hostPass,
            final CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents) {
        final LoginAfterCreateRoomHttp lacr = new LoginAfterCreateRoomHttp();
        final Handler handler = new Handler() {
            public void handleMessage(Message msg) {
                Bundle data = (Bundle) msg.obj;
                String getujid = data.getString("getujid");
                if (getujid != null && getujid.length() > 0) {
                    String userIdUpper = userId == null ? null : userId.toUpperCase();
                    lacr.requestXmppCreateRoom(conferencePass, hostPass,
                            userIdUpper, getujid, onLoginLafterJoinRoomEvents);
                }
            }
        };
        new Thread() {
            public void run() {
                Message msg = Message.obtain();
                Bundle bundle = new Bundle();
                String userJid = XmppConnection.getInstance().getConnection().getUser();
                bundle.putString("getujid", userJid);
                msg.obj = bundle;
                handler.sendMessage(msg);
            }
        }.start();

    }

    /***
     * （5）登录成功后 输入会议号加入会议，不带昵称参数的接口
     *
     * @return
     */
    public void LoginAfterJoinRoom(String conferenceNumber,
                                   String conferencePass,
                                   CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents) {
        loginAfter(conferenceNumber, conferencePass, "", onLoginLafterJoinRoomEvents);
    }

    /***
     * （5.01）登录成功后 输入会议号加入会议,d带昵称参数的接口
     * @return
     */
    public void LoginAfterJoinRoomByNickname(String conferenceNumber,
                                             String conferencePass,
                                             String conferenceNickname,
                                             CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents) {
        loginAfter(conferenceNumber, conferencePass, conferenceNickname, onLoginLafterJoinRoomEvents);
    }

    private void loginAfter(String conferenceNumber, String conferencePass, String conferenceNickname, CiscoApiInterface.OnLoginLafterJoinRoomEvents onLoginLafterJoinRoomEvents) {
        Key.roomnumber = conferenceNumber;
        Key.afferent_nickname = conferenceNickname;
        JoinConferenceHttp gch = new JoinConferenceHttp(onLoginLafterJoinRoomEvents);
        String userIdUpper = userId == null ? null : userId.toUpperCase();
        gch.joinConferenceClient(conferenceNumber, conferencePass, userIdUpper);
    }

    /***
     * （6）验证判断等操作完成后，连接会议
     * @return
     */
    public void connectToRoom(Activity activity, CiscoApiInterface.UpdateUIEvents updateEvents) {
        VideoImplement.getInstance().onConnectToRoom(activity, updateEvents, stun);
    }

    /***
     * （6.01）设置会议中摄像头，分辨率
     * @return
     */
    public void setVidyoInfo(boolean cameraSP, String videoQuality) {
        VideoImplement.getInstance().setVidyoInfo(cameraSP, videoQuality);
    }

    /***
     * 断开连接
     */
    public void DisConnect() {
        //断开xmpp连接
        XmppConnection.getInstance().closeConnection();
    }

    /***
     * 结束会议
     */
    @Override
    public void onCallHangUp() {
        VideoImplement.videoImplement.disconnect();
    }

    /***
     * ice连接断开后，会议重连。
     */
    public void onReconnection() {
        VideoImplement.videoImplement.onMeetReconnection();
    }

    /***
     * 切换摄像头
     */
    @Override
    public void onCameraSwitch() {
        PeerConnectionClient.getInstance().switchCamera();
    }

    @Override
    public void onVideoScalingSwitch(ScalingType scalingType) {
        //刷新view
        //		this.scalingType = scalingType;
        //		updateVideoView();
    }

    @Override
    public void onCaptureFormatChange(int width, int height, int framerate) {
        PeerConnectionClient.getInstance().changeCaptureFormat(width, height, framerate);
    }

    @Override
    public void onChatChange() {

    }

    /***
     * 好友列表
     */
    @Override
    public void onFriendsList() {
        List<String> list = XmppConnection.getInstance().findMulitUser();
    }

    /***
     * 打开或关闭麦克风
     */
    @Override
    public boolean onToggleMic() {
        micEnabled = !micEnabled;
        PeerConnectionClient.getInstance().setAudioEnabled(micEnabled);
        return micEnabled;
    }

    //设置麦克风
    public void setMic(boolean ismute) {
        PeerConnectionClient.getInstance().setAudioEnabled(ismute);
    }

    //打开或关闭视频
    public void setVideo(boolean videoEnabled) {
        if (videoEnabled) {
            PeerConnectionClient.getInstance().stopVideoSource();
        } else {
            PeerConnectionClient.getInstance().startVideoSource();
        }
    }

    //获取自己的jid
    public String getUserJid() {
        return XmppConnection.getInstance().getConnection().getUser();
    }

//    public void sendPresenceMessageUpdateState(boolean videomuted, int type) {
//        XmppConnection.getInstance().SendPresenceMessage();
//        if (type == 1) {
//            //video
//            XmppConnection.getInstance().SendBannedVideo(Key.xmpp_nick, videomuted);
//        } else if (type == 2) {
//            //麦克风
//            XmppConnection.getInstance().SendMuteMessage(Key.xmpp_nick, videomuted);
//        }
//    }

    /***
     *  不显示自己画面
     */
    @Override
    public boolean onHiddenView() {
        viewEnabled = !viewEnabled;
        PeerConnectionClient.getInstance().setHiddenView(viewEnabled);
        return viewEnabled;
    }

    /***
     * 打开/关闭音量
     */
    @Override
    public boolean notSendAudioStream() {
        audioEnabled = !audioEnabled;
        if (audioEnabled) {
            //				VideoImplement.videoImplement.audioManager.setSpeakerByLin(audioEnabled);
            VideoImplement.videoImplement.audioManager.setSpeakerPhoneOn(true);
        } else {
            //				VideoImplement.videoImplement.audioManager.setSpeakerByLin(audioEnabled);
            VideoImplement.videoImplement.audioManager.setSpeakerPhoneOn(false);
        }
        return audioEnabled;
    }

    public AppRTCAudioManager audioManager() {
        return VideoImplement.videoImplement.audioManager;
    }

    /***
     * 打开/关闭画面，对方看不到自己,不发流
     */
    @Override
    public boolean notSendVideoStream() {
        videoEnabled = !videoEnabled;
        if (videoEnabled) {
            PeerConnectionClient.getInstance().startVideoSource();
        } else {
            PeerConnectionClient.getInstance().stopVideoSource();
        }
        return videoEnabled;
    }


    //    //获取运行时状态信息
    //	@Override
    //	public Stats getStatsInformation() {
    //		return VideoImplement.videoImplement.onGetStats();
    //	}

    /***
     * 发送聊天消息
     */
    public void sendImMessage(String contString) {
        XmppConnection.getInstance().SendGroupMessage(contString);
    }
    /***
     * 获取人员列表
     */
    //	public List<Participant> getParticipant() {
    //		List<Participant> list = null ;
    //		if (PeerConnectionClient.getInstance() != null) {
    //			 list=	PeerConnectionClient.getInstance().getPartivipant();
    //		}
    //
    //		return list;
    //	}

    /***
     * 授权主持人，前提是 自己是主持人
     */
    public void grantAdmin(String coverJid, String coverUserid) {
//        System.out.print("---getUserJid------" + getUserJid());
        String ujid = coverJid;
        //		String ujid=getUserJid();
        AdminHttp adminHttp = new AdminHttp(ujid, Key.roomnumber, coverUserid);
        adminHttp.GrantAdmin();
    }

    /***
     * 获取主持人，前提是会议中没有主持人。
     */
    public void getModerator(String hostPass) {
        String ujid = XmppConnection.getInstance().getConnection().getUser();
        GetModeratorHttp adminHttp = new GetModeratorHttp(ujid, Key.roomnumber, hostPass);
        adminHttp.GetModerator();
    }

    /***
     * 我是主持人--踢人。
     */
    public void hostTiren(String coverTirenid) {
        XmppConnection.getInstance().SendTirenByMessage(coverTirenid);
    }

    /***
     * 我是主持人--静音。
     */
    public void hostMute(String coverMuteid, boolean isMute) {
        XmppConnection.getInstance().SendMuteMessage(coverMuteid, isMute);
    }

    /***
     * 呼叫sip
     */
    public void CallSip(String sipNumber) {
        XmppConnection.getInstance().SendCallSipByIq(sipNumber);
    }

    /***
     * 接收 sip
     */
    public void inviteSip() {
        XmppConnection.getInstance().SendInviteSipByIq();
    }

    /***
     * 拒绝sip
     */
    public void rejectSip() {
        XmppConnection.getInstance().SendRejectSipByIq();
    }

    /***
     * 呼叫好友
     */
    public void CallFriend(String friendid, String type, String roomNumber) {
        XmppConnection.getInstance().SendCallFriend(friendid, type, roomNumber);
    }

    /***
     * 传入事件
     */
    public void OnCallEvent(CiscoApiInterface.OnIMCallEvents onIMCallEvents) {
        XmppConnection.getInstance().OnIMCallEvents(onIMCallEvents);
    }

//    /***
//     * 获取对方好友id
//     */
//    public String getFriendid() {
//        String friendid = "";
//        if (xmppConnection != null) {
//            friendid = xmppConnection.getInstance().getRoster();
//        }
//        return friendid;
//    }

    /***
     *
     * @author 切换分辨率
     *
     */
    public void onSwitchResolution(int videoWidth, int videoHeight, int framerate) {
        if (videoWidth <= 0 || videoHeight <= 0) {
            videoWidth = 320;
            videoHeight = 240;
        }
        if (framerate < 15) {
            framerate = 15;
        }
        PeerConnectionClient.getInstance().changeCaptureFormat(videoWidth, videoHeight, framerate);
    }

    public void enableStatsEvents() {
        int STAT_CALLBACK_PERIOD = 5000;
        PeerConnectionClient.getInstance().enableStatsEvents(true, STAT_CALLBACK_PERIOD);
    }

    public void setAudioEnabled(boolean audioEnabled) {
        PeerConnectionClient.getInstance().setAudioEnabled(audioEnabled);
    }

    /**
     * （20）获取会议列表
     *
     * @param count
     * @param page
     * @param userId
     * @param callback
     */
    public void GetConferencesList(int count, int page, String userId, ConferenceRecordCallback callback) {
        ConferencesHttp cah = new ConferencesHttp();
        cah.GetConferenceList(count, page, userId, callback);
    }

    /***
     * （20.1）预约会议
     *
     * @return
     */
    public void ReservationMeeting(String userId, MyCallback callback
            , String title, String startTime,
                                   String lengthTime, String number, String meetPassword,
                                   String ownerPassword, String cycle,
                                   String cycleDmy, String cycleStartPre,
                                   String cycleStart, String cycleEndDay) {
        ReservationHttp fh = new ReservationHttp();
        fh.ReservationInfo(userId, callback, title, startTime, lengthTime, number, meetPassword, ownerPassword, cycle, cycleDmy, cycleStartPre, cycleStart, cycleEndDay);
    }

    /***
     * （20.2）删除会议
     *
     * @return
     */
    public void DeleteMeeting(String conferenceId, MyCallback callback) {
        DeleteConferenceHttp dh = new DeleteConferenceHttp();
        dh.deleteConference(conferenceId, callback);
    }

    /***
     * （20.3）搜索会议
     *
     * @return
     */
    public void SearchMeeting(int count, int page, String userId, ConferenceRecordCallback callback, String search) {
        searchMeeting searchmeet = new searchMeeting();
        searchmeet.searchConference(count, page, userId, callback, search);
    }

    /***
     * （21）获取好友列表
     *
     * @return
     */
    public void GetFriendsList(String userId, FriendsCallback callback) {
        FriendsHttp fh = new FriendsHttp();
        fh.GetFriendsList(userId, callback);
    }

    /***
     * （21。1）查询可用好友
     *
     * @return
     */
    public void SelectFriendsList(String userId, FriendsCallback callback, String search) {
        FriendsHttp fh = new FriendsHttp();
        fh.SelectFriendsList(userId, callback, search);
    }

    /**
     * 添加好友
     *
     * @param userId   用户ID
     * @param callback 回调
     */
    public void AddFriends(String friendId, String userId, FriendsCallback callback) {
        FriendsHttp fh = new FriendsHttp();
        fh.AddFriendsList(friendId, userId, callback);
    }

    /**
     * 删除好友
     *
     * @param userId   用户ID
     * @param callback 回调
     */
    public void DeleteFriends(String friendId, String userId, FriendsCallback callback) {
        FriendsHttp fh = new FriendsHttp();
        fh.DeleteFriendsList(friendId, userId, callback);
    }

    /**
     * 添加好友
     *
     * @param screenJid 谁主屏id
     */
    public void SendUpperScreen(String screenJid) {
        XmppConnection.getInstance().SendUpperScreen(screenJid);
    }


    //()创建SurfaceView
    public void createEglBase(Context context) {
        SdkPublicKey.rootEglBase = EglBase.create();// 生成rootEglBase对象传给SDK
    }

    //()创建SurfaceView
    public SurfaceViewRenderer createSurfaceView(Context context) {
        return SdkPublicKey.createSurfaceView(context);
    }

    // 初始化SurfaceView配置参数
    public void initSurfaceView(SurfaceViewRenderer surfaceView) {
        SdkPublicKey.initSurfaceView(surfaceView);
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
