package com.cisco.core.entity;

import android.content.Context;
import com.cisco.core.util.SdkPublicKey;
import org.webrtc.MediaStream;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

/**
 * @author linpeng
 */
public class Participant {
    private String nickname;//昵称
    private String userid;//endpoint 例如 mett+随机数，android+随机数==endpoint
    private boolean isHost;//是否主持人
    private boolean getMuteMic;//麦克风状态    true代表关闭， false 代表开启
    private String jid;//每个人的jid
    //    private Map<String ,String> map=new HashMap<String ,String>();// ssrc   type_video 和audio
    //-------------换流后如下变量将会发生相应的变化--------需要存储-------------->>>>
    private SurfaceViewRenderer surfaceView;// 带有音视频流的surfaceView
    //    private RelativeLayout surfaceLayout;
    //-----------如下为流---换流后这些必须都要重新设置数据-----------
//    private TextView nameText;
//    private VideoRenderer renderer;
    private MediaStream stream;
    //    private VideoTrack track;
    private boolean isScreen;//是否是双流

    public boolean isScreen() {
        return isScreen;
    }

    public void setScreen(boolean screen) {
        isScreen = screen;
    }

    public MediaStream getStream() {
        return stream;
    }

    public void setStream(MediaStream stream) {
        this.stream = stream;
    }

    public synchronized SurfaceViewRenderer getSurfaceView(Context context) {
        if (surfaceView == null) {
            SurfaceViewRenderer surfaceView = SdkPublicKey.createSurfaceView(context);
            SdkPublicKey.initSurfaceView(surfaceView);
            // 包装流并渲染
            VideoRenderer vr = new VideoRenderer(surfaceView);
            VideoTrack localVideoTrack = stream.videoTracks.get(0);
            localVideoTrack.setEnabled(true);
            localVideoTrack.addRenderer(vr);//渲染视频
            this.surfaceView = surfaceView;
        }
        return surfaceView;
    }

//    public void setSurfaceView(SurfaceViewRenderer surfaceView) {
//        this.surfaceView = surfaceView;
//    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public boolean isGetMuteMic() {
        return getMuteMic;
    }

    public void setGetMuteMic(boolean getMuteMic) {
        this.getMuteMic = getMuteMic;
    }

    public Participant(String nickname, String userid, boolean isHost, boolean getMuteMic) {
        this.nickname = nickname;
        this.userid = userid;
        this.isHost = isHost;
        this.getMuteMic = getMuteMic;
    }

    public Participant() {
    }
}
