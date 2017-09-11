package com.cisco.core;

import android.app.Activity;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import com.cisco.core.AppRTCClient.RoomConnectionParameters;
import com.cisco.core.AppRTCClient.SignalingParameters;
import com.cisco.core.PeerConnectionClient.PeerConnectionParameters;
import com.cisco.core.entity.Stats;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.meet.util.SdpSsrcVariable;
import com.cisco.core.xmpp.XmppConnection;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.StatsReport.Value;

import java.text.DecimalFormat;

public class VideoImplement implements AppRTCClient.SignalingEvents, CiscoApiInterface.PeerConnectionEvents, CiscoApiInterface.videoOperationOrStatusEtc {

    private Activity activity;
    private String TAG = "VideoImplement";
    private PeerConnectionClient peerConnectionClient;// 获取链接客户端PeerConnectionClient对象;
    private AppRTCClient appRtcClient;
    private SignalingParameters signalingParameters;
    public AppRTCAudioManager audioManager = null;
    private boolean isError;
    private boolean activityRunning;
    //    private RoomConnectionParameters roomConnectionParameters;
    private PeerConnectionParameters peerConnectionParameters;
    private boolean iceConnected;
    private long callStartedTimeMs = 0;
    //private EglBase.Context renderEGLContext;
//    List<VideoRenderer.Callbacks> remoteRenders = new ArrayList<>();
//    private List<SurfaceViewRenderer> listRemoteRenderer = new ArrayList<>();
    public CiscoApiInterface.UpdateUIEvents updateEvents;
    public static VideoImplement videoImplement;


    public static VideoImplement getInstance() {
        if (videoImplement == null) {
            synchronized (VideoImplement.class) {
                videoImplement = new VideoImplement();
            }
        }
        return videoImplement;
    }

    public VideoImplement() {
    }

    public void onConnectToRoom(Activity activity,
                                CiscoApiInterface.UpdateUIEvents updateEvents, String stun) {
        videoImplement = this;
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.setStun(stun);
        this.activity = activity;
        this.updateEvents = updateEvents;
        //--------------------------------------------------- 以上 <---> 接收数据 -----------------》
        peerConnectionParameters = initParameters();// peerConnectionParameters是否后台运行
        appRtcClient = new MeetRTCClient(activity);// 此 MeetRTCClient 对象实现了AppRTCClient接口

        // 初始化音频管理器
        callStartedTimeMs = System.currentTimeMillis();//获取此刻时间值
        audioManager = AppRTCAudioManager.create(activity.getApplicationContext(), new Runnable() {
            @Override
            public void run() { //创建音频管理器AppRTCAudioManager对象,监听设备状态改变回调
                onAudioManagerChangedState();//音频管理器状态改变的操作
            }
        });
        Log.d(TAG, "初始化音频管理器...");
        audioManager.init();//初始化音频管理器

        peerConnectionClient.createPeerConnectionFactory(activity, peerConnectionParameters, this, this, updateEvents); // 创建连接工厂

        appRtcClient.connectToRoom(updateEvents);// 连接服务器房间

    }

    public void setVidyoInfo(boolean cameraSP, String videoQuality) {
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.setVidyoInfo(cameraSP, videoQuality);
    }

    private PeerConnectionClient.PeerConnectionParameters initParameters() {
        boolean useCamera2, hwCodec, captureToTexture;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Added in API level 21.
            useCamera2 = true;// 使用camera2选项 默认false;
            hwCodec = true;// 检查硬件编解码
            captureToTexture = true;// 检查捕获到纹理。
        } else {
            useCamera2 = false;// 使用camera2选项 默认false;
            hwCodec = false;// 检查硬件编解码
            captureToTexture = false;// 检查捕获到纹理。
        }

        //        useCamera2 = false;// 使用camera2选项 默认false;
        //        hwCodec = true;// 检查硬件编解码
        //        captureToTexture = false;// 检查捕获到纹理。


        boolean videoCallEnabled = true;// 视频呼叫默认true
        String videoCodec = "VP9";// 默认视频编码
        String audioCodec = "OPUS";// 默认音频编码
        boolean noAudioProcessing = false;// 检查禁用音频处理标志
        boolean aecDump = false;// AEC转储
        boolean useOpenSLES = false;// 检查OpenSL ES的启用标志。
        boolean disableBuiltInAEC = false;// 检查禁用内置的AEC
        int videoWidth = 0;
        int videoHeight = 0;
        // 获取视频分辨率。
        //        String resolution = "320 x 240";//"1280 x 720","640 x 480"
        String resolution = "320 x 240";
        String[] dimensions = resolution.split("[ x]+");
        if (dimensions.length == 2) {
            try {
                videoWidth = Integer.parseInt(dimensions[0]);
                videoHeight = Integer.parseInt(dimensions[1]);
            } catch (NumberFormatException e) {
                videoWidth = 0;
                videoHeight = 0;
            }
        }
        // 设置相机的FPS
        int cameraFps = 12;//cameraFps=videoFps
        boolean captureQualitySlider = false;// 检查采集质量
        // 获取音频和视频的比特率值
        int videoStartBitrate = 0;
        int audioStartBitrate = 0;

        boolean displayHud = false;// //检查统计显示选项
        boolean tracing = false;// //检查统计显示选项


        peerConnectionParameters = new PeerConnectionClient.PeerConnectionParameters(videoCallEnabled, tracing, useCamera2,
                videoWidth, videoHeight, cameraFps, videoStartBitrate, videoCodec, hwCodec,
                captureToTexture, audioStartBitrate, audioCodec, noAudioProcessing, aecDump, useOpenSLES, disableBuiltInAEC);
        return peerConnectionParameters;
    }

    private void onAudioManagerChangedState() {
    }

    public SessionDescription getRemoteDescription() {
        return peerConnectionClient.getRemoteDescription();
    }

    public void setRemoteDescription(SessionDescription remoteDescription) {
        peerConnectionClient.setRemoteDescription(remoteDescription);
    }

    @Override
    public void GetStats(PeerConnection peerConnection) {

        if (peerConnection == null || isError) {
            return;
        }
        boolean success = peerConnection.getStats(new StatsObserver() {
            @Override
            public void onComplete(final StatsReport[] reports) {

                onPeerConnectionStatsReady(reports);

                float packetsLost = 0, packetsReceived = 0, packetsSent = 0;
                //通过rtc获取上行宽带下行宽带等
                DecimalFormat df = new DecimalFormat("0.00");//格式化小数
                // 获取values 数组
                for (StatsReport statsReport : reports) {
                    if (statsReport.type.equals("VideoBwe")) {
                        StatsReport.Value[] srv = statsReport.values;
                        for (Value value : srv) {
                            String info = value.toString().trim();
                            if (info.contains("googAvailableReceiveBandwidth")) {
                                String garbw = info.substring(info.indexOf(":") + 1, info.lastIndexOf("]")).trim();
                                SdpSsrcVariable.googAvailableReceiveBandwidth = Integer.parseInt(garbw) / 1000;
                            }
                            if (value.toString().contains("googAvailableSendBandwidth")) {
                                String galsbw = info.substring(info.indexOf(":") + 1, info.lastIndexOf("]")).trim();
                                SdpSsrcVariable.googAvailableSendBandwidth = Integer.parseInt(galsbw) / 1000;
                            }
                        }
                    }
                    if (statsReport.type.equals("ssrc")) {
                        StatsReport.Value[] srv = statsReport.values;
                        for (Value value : srv) {
                            String ssrc_info = value.toString();
                            if (ssrc_info.contains("packetsLost")) {
                                String re = ssrc_info.substring(ssrc_info.indexOf(":") + 1, ssrc_info.lastIndexOf("]")).trim();
                                packetsLost = Integer.parseInt(re);
                            }
                            if (ssrc_info.contains("packetsSent")) {
                                String re = ssrc_info.substring(ssrc_info.indexOf(":") + 1, ssrc_info.lastIndexOf("]")).trim();
                                packetsSent = Integer.parseInt(re);
                            }
                            if (ssrc_info.contains("packetsReceived")) {
                                String re = ssrc_info.substring(ssrc_info.indexOf(":") + 1, ssrc_info.lastIndexOf("]")).trim();
                                packetsReceived = Integer.parseInt(re);
                            }
                            if (ssrc_info.contains("googFrameRateSent")) {
                                // 上行丢包率
                                float count = packetsLost + packetsSent;
                                if (count > 0) {
                                    float aa = packetsLost / count * 100;
                                    String s = df.format(aa);
                                    SdpSsrcVariable.packetLoss_upload = s;
                                }
                            }
                            if (ssrc_info.contains("googFrameRateReceived")) {
                                float count = packetsLost + packetsReceived;
                                // 下行丢包率
                                if (count > 0) {
                                    float bb = packetsLost / count * 100;
                                    SdpSsrcVariable.packetLoss_download = df.format(bb);
                                }
                            }
                        }

                    }
                }
                XmppConnection.getInstance().SendPresenceMessage();
                Stats stat = new Stats();
                stat.setSendBandwidth(SdpSsrcVariable.googAvailableSendBandwidth);
                stat.setReceiveBandwidth(SdpSsrcVariable.googAvailableReceiveBandwidth);
                stat.setSendPacketLoss(SdpSsrcVariable.packetLoss_upload);
                stat.setReceivePacketLoss(SdpSsrcVariable.packetLoss_download);
                updateEvents.onStatsInformation(stat);
            }
        }, null);
        if (!success) {
            Log.e(TAG, "getStats() returns false!");
        }
    }

    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendAnswerSdp(sdp);
                }
            }
        });
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidate(candidate);
                }
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (appRtcClient != null) {
                    appRtcClient.sendLocalIceCandidateRemovals(candidates);
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                iceConnected = true;
                final long delta = System.currentTimeMillis() - callStartedTimeMs;
                Log.i(TAG, "电话连接: delay=" + delta + "ms");
                if (peerConnectionClient == null || isError) {
                    Log.w(TAG, "电话连接在封闭的或错误的状态。。");
                    return;
                }
                updateEvents.callConnected();
            }
        });
    }

    @Override
    public void onIceFailed() {
        updateEvents.onIceFailed();
    }

    @Override
    public void onPeerConnectionClosed() {
    }

    @Override
    public void onPeerConnectionStatsReady(final StatsReport[] reports) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isError && iceConnected) {
                    updateEvents.peerConnectionStatsReady(reports);
                }
            }
        });
    }

    @Override
    public void onPeerConnectionError(String description) {
    }

    @Override
    public void onConnectedToRoomLocal(final SignalingParameters params) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //先创建本地流
                if (params != null) {
                    onConnectedToRoomInternalLocal(params);
                }
            }
        });
    }

    private void onConnectedToRoomInternalLocal(final SignalingParameters params) {
        signalingParameters = params;
        peerConnectionClient.createPeerConnection(signalingParameters);
    }

    @Override
    public void onConnectedToRoomRemote(final SignalingParameters params) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 收到SESSION_INITIATE 设置remot 端sdp
                onConnectedToRoomInternal(params);
            }
        });
    }

    private void onConnectedToRoomInternal(final SignalingParameters params) {
        signalingParameters = params;
        if (params.offerSdp != null) {
            peerConnectionClient.setRemoteDescription(params.offerSdp);
            peerConnectionClient.createAnswer();
        }
        if (params.iceCandidates != null) {
            for (IceCandidate iceCandidate : params.iceCandidates) {
                peerConnectionClient.addRemoteIceCandidate(iceCandidate);
            }
        }
    }

    public void disconnect() {
        activityRunning = false;

        if (appRtcClient != null) {
            appRtcClient.disconnectFromRoom();
            appRtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }
    }

    //  createPeerConnection
    public void onMeetReconnection() {
        appRtcClient.connectToRoom(updateEvents);
    }
}
