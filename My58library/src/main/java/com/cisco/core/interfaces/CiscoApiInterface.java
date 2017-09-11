package com.cisco.core.interfaces;

import android.app.Application;
import com.cisco.core.AppRTCClient.SignalingParameters;
import com.cisco.core.entity.Participant;
import com.cisco.core.entity.Stats;
import com.cisco.core.entity.UserInfo;
import com.cisco.core.util.SdkPublicKey;
import com.yolanda.nohttp.Logger;
import com.yolanda.nohttp.NoHttp;
import org.webrtc.*;
import org.webrtc.RendererCommon.ScalingType;

import java.util.List;

/**
 * jlp
 */
public class CiscoApiInterface {
    public static VideoInterface app = VideoInterface.getInstance();

    public static void init(Application application) {
        SdkPublicKey.rootEglBase = EglBase.create();// 生成rootEglBase对象传给SDK
        NoHttp.initialize(application);
        Logger.setTag("NoHttp");
        Logger.setDebug(true);// 开始NoHttp的调试模式, 这样就能看到请求过程和日志
    }

    public interface OnLoginEvents {
        void LoginResult(boolean result, String requestMessage, UserInfo userInfo);// 登录回掉
    }

    public interface OnGuestLoginEvents {
        void GuestLoginResult(boolean result, String requestMessage);// Guest登录回掉
    }

    public interface OnLoginLafterJoinRoomEvents {
        void LoginLafterJoinRoom(boolean result, String requestMessage);// 用户会员登录成功后  输入会议号参加会议

//        void LoginLafterCreateRoom(boolean result, String hostpass, String roomid, String requestMessage);// 用户会员登录成功后  及时会议，如果存在 直接加入，否知创建
    }

    public interface OnIMCallEvents {
        void acceptCallback(String roomID, String friendid, String resource);//呼叫- 同意

        void rejectCallback(String roomID, String friendid, String resource);//呼叫-拒绝

        void inviteCallback(String roomID, String friendid, String resource);//呼叫-邀请
    }

    public interface UpdateUIEvents {
        void callConnected();//呼叫连接

        void onIceFailed();//ice失败

        void IMMessageRecever(String Message, String name);//用于接收im消息

        void onAddStream(Participant participant);

        void onRemoveStream(String jid);

        void onAddLocalStream(Participant participant);

        void onOccupantLeftRoom(String username);//成员离开回掉

        void beHostToOperate(boolean opreate, int type);//opreate,true静音 false打开，，type，1代表被主持人静音，2代表被主持人禁视频，3代表被主持人剔除，4代表被移交主持人

//        /** @deprecated */
//        @Deprecated
//        void beHuiKongToOperate(boolean allaudio, boolean allvideo, boolean muted, boolean videoClosed, boolean hangup, String userJid, boolean lostModerator);

        void beMeetingSuperAdministratorControl(boolean allaudio, boolean allvideo, boolean muted, boolean videoClosed, boolean hangup, String userJid, boolean lostModerator);

        void OnTheMainScreen(String jid);//上主屏  根据jid 让相应人员上主屏

        void updatePartcipantList(List<Participant> listParticipant);//实时更新人员状态

        //        void updatePartcipantList(Map<String,Participant> participantMap);//实时更新人员状态
        void sipDialBack();//sip回拨

        void onStatsInformation(Stats stat);//

        void peerConnectionStatsReady(StatsReport[] reports);

        void onWhiteBoard(String action, String url);//共享白板

        void OnHostLeave();//主持人离开
    }

    public interface OnCallEvents {
        void onCallHangUp();

        void onCameraSwitch();

        void onVideoScalingSwitch(ScalingType scalingType);

        void onCaptureFormatChange(int width, int height, int framerate);

        void onChatChange();

        void onFriendsList();

        boolean onToggleMic();

        boolean onHiddenView();

        boolean notSendAudioStream();

        boolean notSendVideoStream();
    }

    /**
     * Peer connection events.
     */
    public interface PeerConnectionEvents {
        /**
         * Callback fired once local SDP is created and set.
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);// �°���ӵ�

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is closed (IceConnectionState is
         * DISCONNECTED).
         */
//		void onIceDisconnected();
        void onIceFailed();//ice连接失败 lpadd

        /**
         * Callback fired once peer connection is closed.
         */
        void onPeerConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);

//		void onVideoView();//lp add
    }

    public interface videoOperationOrStatusEtc {
        void GetStats(PeerConnection peerConnection);
    }

    public interface MeetRtcClientEvents {
        void onConnectedToRoomLocal(SignalingParameters signalingParameters);

        void setRemoteDescription(SessionDescription modifiedOffer);

        void onConnectedToRoomRemote(SignalingParameters signalingParameters);


    }
}
