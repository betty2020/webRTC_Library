/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.cisco.core;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.cisco.core.AppRTCClient.SignalingParameters;
import com.cisco.core.entity.Participant;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.interfaces.CiscoApiInterface.PeerConnectionEvents;
import com.cisco.core.interfaces.CiscoApiInterface.videoOperationOrStatusEtc;
import com.cisco.core.meet.util.SdpSsrcVariable;
import com.cisco.core.util.SdkPublicKey;
import com.cisco.core.xmpp.XmppConnection;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import java.io.File;
import java.io.IOException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.id;


/**
 * Peer connection client implementation.
 * All public methods are routed to local looper thread. All
 * PeerConnectionEvents callbacks are invoked from the same looper thread. This
 * class is a singleton.
 */
public class PeerConnectionClient {

    public static final String VIDEO_TRACK_ID = SdpSsrcVariable.getUUID();
    public static final String AUDIO_TRACK_ID = SdpSsrcVariable.getUUID();

    private static final String TAG = "PCRTCClient";
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_VP9 = "VP9";
    private static final String VIDEO_CODEC_H264 = "H264";
    private static final String AUDIO_CODEC_OPUS = "opus";
    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";
    private static final String AUDIO_ECHO_CANCELLATION_CONSTRAINT = "googEchoCancellation";
    private static final String AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT = "googAutoGainControl";
    private static final String AUDIO_HIGH_PASS_FILTER_CONSTRAINT = "googHighpassFilter";
    private static final String AUDIO_NOISE_SUPPRESSION_CONSTRAINT = "googNoiseSuppression";
    private static final String MAX_VIDEO_WIDTH_CONSTRAINT = "maxWidth";
    private static final String MIN_VIDEO_WIDTH_CONSTRAINT = "minWidth";
    private static final String MAX_VIDEO_HEIGHT_CONSTRAINT = "maxHeight";
    private static final String MIN_VIDEO_HEIGHT_CONSTRAINT = "minHeight";
    private static final String MAX_VIDEO_FPS_CONSTRAINT = "maxFrameRate";
    private static final String MIN_VIDEO_FPS_CONSTRAINT = "minFrameRate";
    private static final String DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT = "DtlsSrtpKeyAgreement";
    private static final int HD_VIDEO_WIDTH = 1280;
    private static final int HD_VIDEO_HEIGHT = 720;
    private static final int MAX_VIDEO_WIDTH = 1280;
    private static final int MAX_VIDEO_HEIGHT = 1280;
    private static final int MAX_VIDEO_FPS = 15;

    private static PeerConnectionClient instance;
    private final PCObserver pcObserver = new PCObserver();
    private final SDPObserver sdpObserver = new SDPObserver();
    private final ExecutorService executor;

    private Context context;
    private PeerConnectionFactory factory;
    private PeerConnection peerConnection;
    PeerConnectionFactory.Options options = null;
    private AudioSource audioSource;
    private VideoSource videoSource;
    private boolean videoCallEnabled;
    private boolean preferIsac;
    private String preferredVideoCodec;
    private boolean videoSourceStopped;
    private boolean isError;
    private Timer statsTimer;
    private SignalingParameters signalingParameters;
    private MediaConstraints pcConstraints;
    private MediaConstraints videoConstraints;
    private MediaConstraints audioConstraints;
    private ParcelFileDescriptor aecDumpFileDescriptor;
    private MediaConstraints sdpMediaConstraints;
    private PeerConnectionParameters peerConnectionParameters;
    private LinkedList<IceCandidate> queuedRemoteCandidates;
    private SessionDescription localSdp; // either offer or answer SDP
    private MediaStream mediaStream;
    private int numberOfCameras;
    private CameraVideoCapturer videoCapturer;
    // enableVideo is set to true if video should be rendered and sent.
    private boolean renderVideo;
    private VideoTrack localVideoTrack;
    private VideoTrack remoteVideoTrack;
    private VideoTrack viewVideoTrack;
    // enableAudio is set to true if audio should be sent.
    private boolean enableAudio;
    private AudioTrack localAudioTrack;

    private PeerConnectionEvents events;
    private videoOperationOrStatusEtc videoEvents;
    private CiscoApiInterface.UpdateUIEvents updateEvents;//lpadd
    private Handler UI_Handler;
    private Handler mHandler;
    private String stun;

    public SessionDescription getRemoteDescription() {
        return peerConnection.getRemoteDescription();
    }

    public void setStun(String stun) {
        this.stun = stun;
    }

    public String getStun() {
        return stun;
    }

    /**
     * Peer connection parameters.
     */
    public static class PeerConnectionParameters {
        public final boolean videoCallEnabled;
        public final boolean tracing;
        public final boolean useCamera2;
        public final int videoWidth;
        public final int videoHeight;
        public final int videoFps;
        public final int videoStartBitrate;
        public final String videoCodec;
        public final boolean videoCodecHwAcceleration;
        public final boolean captureToTexture;
        public final int audioStartBitrate;
        public final String audioCodec;
        public final boolean noAudioProcessing;
        public final boolean aecDump;
        public final boolean useOpenSLES;
        public final boolean disableBuiltInAEC;

        public PeerConnectionParameters(boolean videoCallEnabled, boolean tracing, boolean useCamera2,
                                        int videoWidth, int videoHeight, int videoFps,
                                        int videoStartBitrate, String videoCodec,
                                        boolean videoCodecHwAcceleration, boolean captureToTexture,
                                        int audioStartBitrate, String audioCodec,
                                        boolean noAudioProcessing, boolean aecDump,
                                        boolean useOpenSLES, boolean disableBuiltInAEC) {
            this.videoCallEnabled = videoCallEnabled;
            this.useCamera2 = useCamera2;
            this.tracing = tracing;
            this.videoWidth = videoWidth;
            this.videoHeight = videoHeight;
            this.videoFps = videoFps;
            this.videoStartBitrate = videoStartBitrate;
            this.videoCodec = videoCodec;
            this.videoCodecHwAcceleration = videoCodecHwAcceleration;
            this.captureToTexture = captureToTexture;
            this.audioStartBitrate = audioStartBitrate;
            this.audioCodec = audioCodec;
            this.noAudioProcessing = noAudioProcessing;
            this.aecDump = aecDump;
            this.useOpenSLES = useOpenSLES;
            this.disableBuiltInAEC = disableBuiltInAEC;
        }
    }

    private PeerConnectionClient() {
        // Executor thread is started once in private ctor and is used for all
        // peer connection API calls to ensure new peer connection factory is
        // created on the same thread as previously destroyed factory.
        executor = Executors.newSingleThreadScheduledExecutor();
    }

    public static PeerConnectionClient getInstance() {
        if (instance == null) {
            synchronized (PeerConnectionClient.class) {
                instance = new PeerConnectionClient();
            }
        }
        return instance;
    }

    //    public static PeerConnectionClient getInstance() {
    //        return instance;
    //    }

    public void setPeerConnectionFactoryOptions(PeerConnectionFactory.Options options) {
        this.options = options;
    }

    //创建对等连接工厂
    public void createPeerConnectionFactory(final Context context,
                                            final PeerConnectionParameters peerConnectionParameters,
                                            final PeerConnectionEvents events,
                                            videoOperationOrStatusEtc videoEvents,
                                            CiscoApiInterface.UpdateUIEvents updateEvents
    ) {
        //        this.renderEGLContext=renderEGLContext;
        this.context = context;
        //callback.setContext(context);
        this.peerConnectionParameters = peerConnectionParameters;
        this.events = events;
        this.videoEvents = videoEvents;// lp add
        this.updateEvents = updateEvents;
        //callback.setUpdateEvents(updateEvents);
        //-------------------------------------------传递数据----------》
        videoCallEnabled = peerConnectionParameters.videoCallEnabled;
        //        this.context = null;
        factory = null;
        peerConnection = null;
        preferIsac = false;
        videoSourceStopped = false;
        isError = false;
        queuedRemoteCandidates = null;
        localSdp = null; // either offer or answer SDP
        mediaStream = null;
        videoCapturer = null;
        renderVideo = true;
        localVideoTrack = null;
        remoteVideoTrack = null;
        viewVideoTrack = null;
        enableAudio = true;
        localAudioTrack = null;
        statsTimer = new Timer();//计时器
        UI_Handler = new Handler();
        executor.execute(new Runnable() {// 计划执行者服务
            @Override
            public void run() {
                createPeerConnectionFactoryInternal(context);//创建内部链接工厂
            }
        });
    }

    public void createPeerConnection(final SignalingParameters signalingParameters) {
        if (peerConnectionParameters == null) {
            Log.e(TAG, "创建peer connection工厂没有初始化。");
            return;
        }
        this.signalingParameters = signalingParameters;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    createMediaConstraintsInternal();
                    createPeerConnectionInternal();
                } catch (Exception e) {
                    try {
                        throw e;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    public void close() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                closeInternal();
            }
        });
    }

    public boolean isVideoCallEnabled() {
        return videoCallEnabled;
    }

    private void createPeerConnectionFactoryInternal(Context context) {
        // PeerConnectionFactory为C与java交互类
        PeerConnectionFactory.initializeInternalTracer();// C的方法
        if (peerConnectionParameters.tracing) {
            PeerConnectionFactory.startInternalTracingCapture(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "webrtc-trace.txt");
        }
        Log.d(TAG, "创建对等连接工厂，使用视频: " + peerConnectionParameters.videoCallEnabled);
        isError = false;
        PeerConnectionFactory.initializeFieldTrials("");
        if (videoCallEnabled && peerConnectionParameters.videoCodec != null) {
            if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_VP9)) {
                preferredVideoCodec = VIDEO_CODEC_VP9;
            } else if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_H264)) {
                preferredVideoCodec = VIDEO_CODEC_H264;
            }
        }
        Log.d(TAG, "Pereferred 视频编解码器: " + preferredVideoCodec);
        // 检查ISAC是否在默认情况下使用。
        preferIsac = peerConnectionParameters.audioCodec != null && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC);
        // 启用/禁用 OpenSL ES回放。
        if (!peerConnectionParameters.useOpenSLES) {
            Log.d(TAG, "禁用OpenSL ES音频即使设备支持它");
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(true /* enable */);
        } else {
            Log.d(TAG, "允许OpenSL ES音频设备是否支持它");
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
        }
        if (peerConnectionParameters.disableBuiltInAEC) {
            Log.d(TAG, "禁用built-in AEC 即使设备支持它");
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        } else {
            Log.d(TAG, "使用built-in AEC 如果设备支持它");
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false);
        }
        PeerConnectionFactory.initializeAndroidGlobals(context, peerConnectionParameters.videoCodecHwAcceleration);
        if (options != null) {
            Log.d(TAG, "工厂networkIgnoreMask选项: " + options.networkIgnoreMask);
        }
        //this.context = context;
        factory = new PeerConnectionFactory(options);
        Log.d(TAG, "由连接工厂创建完成");
    }

    private void createMediaConstraintsInternal() {
        // Create peer connection constraints.
        pcConstraints = new MediaConstraints();
        // Enable DTLS for normal calls and disable for loopback calls.
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair(
                DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));

        // Check if there is a camera on device and disable video call if not.
        //		numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
        numberOfCameras = (new Camera1Enumerator()).getDeviceNames().length;
        if (numberOfCameras == 0) {
            Log.w(TAG, "No camera on device. Switch to audio only call.");
            videoCallEnabled = false;
        }
        // Create video constraints if video call is enabled.
        if (videoCallEnabled) {
            videoConstraints = new MediaConstraints();
            int videoWidth = peerConnectionParameters.videoWidth;
            int videoHeight = peerConnectionParameters.videoHeight;
            // If VP8 HW video encoder is supported and video resolution is not
            // specified force it to HD.
            if ((videoWidth == 0 || videoHeight == 0)
                    && peerConnectionParameters.videoCodecHwAcceleration
                    && MediaCodecVideoEncoder.isVp8HwSupported()) {
                videoWidth = HD_VIDEO_WIDTH;
                videoHeight = HD_VIDEO_HEIGHT;
            }
            Logging.d(TAG, "jilinpeng,MediaCodecVideoEncoder.isH264HwSupported=" + MediaCodecVideoEncoder.isH264HwSupported());
            //			MediaCodecVideoEncoder.disableH264HwCodec();
            Logging.d(TAG, "jilinpeng,MediaCodecVideoEncoder.isH264HwSupportedUsingTextures=" + MediaCodecVideoEncoder.isH264HwSupportedUsingTextures());

            Logging.d(TAG, "jilinpeng,MediaCodecVideoDecoder.isH264HwSupported=" + MediaCodecVideoDecoder.isH264HwSupported());
            ////			MediaCodecVideoDecoder.disableH264HwCodec();
            Logging.d(TAG, "jilinpeng,MediaCodecVideoDecoder.isH264HwSupportedUsingTextures=" + MediaCodecVideoDecoder.isH264HighProfileHwSupported());
            // Add video resolution constraints.
            if (videoWidth > 0 && videoHeight > 0) {
                videoWidth = Math.min(videoWidth, MAX_VIDEO_WIDTH);
                videoHeight = Math.min(videoHeight, MAX_VIDEO_HEIGHT);
                videoConstraints.mandatory.add(new KeyValuePair(
                        MIN_VIDEO_WIDTH_CONSTRAINT, Integer
                        .toString(videoWidth)));
                videoConstraints.mandatory.add(new KeyValuePair(
                        MAX_VIDEO_WIDTH_CONSTRAINT, Integer
                        .toString(videoWidth)));
                videoConstraints.mandatory.add(new KeyValuePair(
                        MIN_VIDEO_HEIGHT_CONSTRAINT, Integer
                        .toString(videoHeight)));
                videoConstraints.mandatory.add(new KeyValuePair(
                        MAX_VIDEO_HEIGHT_CONSTRAINT, Integer
                        .toString(videoHeight)));
            }

            // Add fps constraints.
            int videoFps = peerConnectionParameters.videoFps;
            if (videoFps > 0) {
                videoFps = Math.min(videoFps, MAX_VIDEO_FPS);
                videoConstraints.mandatory.add(new KeyValuePair(
                        MIN_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
                videoConstraints.mandatory.add(new KeyValuePair(
                        MAX_VIDEO_FPS_CONSTRAINT, Integer.toString(videoFps)));
            }
        }

        // Create audio constraints.
        audioConstraints = new MediaConstraints();
        // added for audio performance measurements
        if (peerConnectionParameters.noAudioProcessing) {
            Log.d(TAG, "Disabling audio processing");
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                    AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }
        // Create SDP constraints.
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair(
                "OfferToReceiveAudio", "true"));
        if (videoCallEnabled) {
            sdpMediaConstraints.mandatory
                    .add(new MediaConstraints.KeyValuePair(
                            "OfferToReceiveVideo", "true"));
        } else {
            sdpMediaConstraints.mandatory
                    .add(new MediaConstraints.KeyValuePair(
                            "OfferToReceiveVideo", "false"));
        }
    }

    private boolean cameraType;// lp add 获取摄像头是前置后置
    private static String videoQuality;//lp add 本地视频数量

    public void setVidyoInfo(boolean cameraSP, String videoQuality) {
        this.cameraType = cameraSP;
        this.videoQuality = videoQuality;
    }

    private void createCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        //        Logging.d(TAG, "jilinpeng,Looking for front facing cameras." + deviceNames.length);
        //
        //        if (cameraType) {
        //            Logging.d(TAG, "Looking for front facing cameras.");
        //            //前置
        //            frontCamera(enumerator, deviceNames);
        //        } else {
        //            Logging.d(TAG, "Looking for other cameras.");
        //            //后置
        //            RearCamera(enumerator, deviceNames);
        //        }
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                videoCapturer = enumerator.createCapturer(deviceName, null);
                if (videoCapturer != null) {
                    return;
                }
            }
        }

        // Front facing camera not found, try something else
        Logging.d(TAG, "Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return;
                }
            }
        }
    }

    private void RearCamera(CameraEnumerator enumerator, String[] deviceNames) {
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating other camera capturer.");
                videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return;
                }
            }
        }
    }

    private void frontCamera(CameraEnumerator enumerator,
                             final String[] deviceNames) {
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Logging.d(TAG, "Creating front facing camera capturer.");
                videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return;
                }
            }
        }
    }

    @SuppressLint("NewApi")
    private void createPeerConnectionInternal() {
        if (factory == null || isError) {
            Log.e(TAG, "Peerconnection factory is not created");
            return;
        }
        Log.d(TAG, "Create peer connection.");
        Log.d(TAG, "PCConstraints: " + pcConstraints.toString());
        if (videoConstraints != null) {
            Log.d(TAG, "VideoConstraints: " + videoConstraints.toString());
        }
        queuedRemoteCandidates = new LinkedList<IceCandidate>();
        if (videoCallEnabled) {
            Log.d(TAG, "EGLContext: " + SdkPublicKey.rootEglBase.getEglBaseContext());
            factory.setVideoHwAccelerationOptions(SdkPublicKey.rootEglBase.getEglBaseContext(), SdkPublicKey.rootEglBase.getEglBaseContext());
        }
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        iceServers.add(new PeerConnection.IceServer(stun, "", ""));
//        iceServers.add(new PeerConnection.IceServer(Key.ice_credential_turn, Key.ice_credential_username, Key.ice_credential));
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        peerConnection = factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
//        Logging.enableTracing("logcat:", EnumSet.of(Logging.TraceLevel.TRACE_WARNING));
//        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO);
        mediaStream = factory.createLocalMediaStream(SdpSsrcVariable.getUUID());
        if (videoCallEnabled) {
            if (peerConnectionParameters.useCamera2) {
                if (!peerConnectionParameters.captureToTexture) {
                    return;
                }
                Logging.d(TAG, "Creating capturer using camera2 API.");
                createCapturer(new Camera2Enumerator(context));
            } else {
                Logging.d(TAG, "Creating capturer using camera1 API.");
                createCapturer(new Camera1Enumerator(peerConnectionParameters.captureToTexture));
            }

            if (videoCapturer == null) {
                return;
            }
            mediaStream.addTrack(createVideoTrack(videoCapturer));
        }
        mediaStream.addTrack(createAudioTrack());
        peerConnection.addStream(mediaStream);
        // 显示大屏 lpadd
        Participant pc = new Participant();
        pc.setStream(mediaStream);
        pc.setJid(XmppConnection.getInstance().getConnection().getUser());

        updateEvents.onAddLocalStream(pc);

        if (peerConnectionParameters.aecDump) {
            try {
                aecDumpFileDescriptor = ParcelFileDescriptor.open(new File(
                                Environment.getExternalStorageDirectory().getPath()
                                        + File.separator + "Download/audio.aecdump"),
                        ParcelFileDescriptor.MODE_READ_WRITE
                                | ParcelFileDescriptor.MODE_CREATE
                                | ParcelFileDescriptor.MODE_TRUNCATE);
                factory.startAecDump(aecDumpFileDescriptor.getFd(), -1);
            } catch (IOException e) {
                Log.e(TAG, "Can not open aecdump file", e);
            }
        }
        Log.d(TAG, "Peer connection created.");
    }

    private void closeInternal() {
        if (factory != null && peerConnectionParameters.aecDump) {
            //			factory.StopAudioRecord();
            factory.stopAecDump();
        }
        Log.d(TAG, "Closing peer connection.");
        statsTimer.cancel();
        if (peerConnection != null) {
            peerConnection.dispose();
            peerConnection = null;
        }
        Log.d(TAG, "Closing audio source.");
        if (audioSource != null) {
            audioSource.dispose();
            audioSource = null;
        }
        Log.d(TAG, "Closing video source.");
        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }
        Log.d(TAG, "Closing peer connection factory.");
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        options = null;
        Log.d(TAG, "Closing peer connection done.");
        events.onPeerConnectionClosed();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
    }


    public boolean isHDVideo() {
        if (!videoCallEnabled) {
            return false;
        }
        int minWidth = 0;
        int minHeight = 0;
        for (KeyValuePair keyValuePair : videoConstraints.mandatory) {
            if (keyValuePair.getKey().equals("minWidth")) {
                try {
                    minWidth = Integer.parseInt(keyValuePair.getValue());
                } catch (NumberFormatException e) {
                    Log.e(TAG,
                            "Can not parse video width from video constraints");
                }
            } else if (keyValuePair.getKey().equals("minHeight")) {
                try {
                    minHeight = Integer.parseInt(keyValuePair.getValue());
                } catch (NumberFormatException e) {
                    Log.e(TAG,
                            "Can not parse video height from video constraints");
                }
            }
        }
        return minWidth * minHeight >= 1280 * 720;
    }

    public void enableStatsEvents(boolean enable, int periodMs) {
        Log.e(TAG, "链接成功进入Peer connection");
        if (enable) {
            try {
                statsTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                videoEvents.GetStats(peerConnection);
                            }
                        });
                    }
                }, 0, periodMs);
            } catch (Exception e) {
                Log.e(TAG, "Can not schedule statistics timer", e);
            }
        } else {
            statsTimer.cancel();
        }
    }

    public void switchCamera() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                switchCameraInternal();
            }
        });
    }

    public void setAudioEnabled(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                enableAudio = enable;
                if (localAudioTrack != null) {
                    localAudioTrack.setEnabled(enableAudio);
                }
            }
        });
    }

    public void setVideoEnabled(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                renderVideo = enable;
                if (localVideoTrack != null) {
                    localVideoTrack.setEnabled(renderVideo);
                }
                if (remoteVideoTrack != null) {
                    remoteVideoTrack.setEnabled(renderVideo);
                }
                if (viewVideoTrack != null) {
                    viewVideoTrack.setEnabled(renderVideo);
                }

            }
        });
    }

    /*
     * lp add
     */
    public void setHiddenView(final boolean enable) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                renderVideo = enable;
                if (localVideoTrack != null) {
                    localVideoTrack.setEnabled(renderVideo);
                }
            }
        });
    }

    public void createAnswer() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection != null && !isError) {
                    Log.d(TAG, "PC create ANSWER");
                    //					isInitiator = false;
                    peerConnection.createAnswer(sdpObserver,
                            sdpMediaConstraints);
                }
            }
        });
    }

    public void addRemoteIceCandidate(final IceCandidate candidate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection != null && !isError) {
                    if (queuedRemoteCandidates != null) {
                        queuedRemoteCandidates.add(candidate);
                    } else {
                        peerConnection.addIceCandidate(candidate);
                    }
                }
            }
        });
    }

    public void removeRemoteIceCandidates(final IceCandidate[] candidates) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection == null || isError) {
                    return;
                }
                drainCandidates();
                peerConnection.removeIceCandidates(candidates);
            }
        });
    }

    public void setRemoteDescription(final SessionDescription sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection == null || isError) {
                    return;
                }
                String sdpDescription = sdp.description;
                if (preferIsac) {
                    sdpDescription = preferCodec(sdpDescription,
                            AUDIO_CODEC_ISAC, true);
                }
                if (videoCallEnabled) {
                    sdpDescription = preferCodec(sdpDescription,
                            preferredVideoCodec, false);
                }
                if (videoCallEnabled
                        && peerConnectionParameters.videoStartBitrate > 0) {
                    sdpDescription = setStartBitrate(VIDEO_CODEC_VP8, true,
                            sdpDescription,
                            peerConnectionParameters.videoStartBitrate);
                    sdpDescription = setStartBitrate(VIDEO_CODEC_VP9, true,
                            sdpDescription,
                            peerConnectionParameters.videoStartBitrate);
                    sdpDescription = setStartBitrate(VIDEO_CODEC_H264, true,
                            sdpDescription,
                            peerConnectionParameters.videoStartBitrate);
                }
                if (peerConnectionParameters.audioStartBitrate > 0) {
                    sdpDescription = setStartBitrate(AUDIO_CODEC_OPUS, false,
                            sdpDescription,
                            peerConnectionParameters.audioStartBitrate);
                }
                Log.d(TAG, "Set remote SDP.");
                SessionDescription sdpRemote = new SessionDescription(sdp.type,
                        sdpDescription);
                peerConnection.setRemoteDescription(sdpObserver, sdpRemote);
            }
        });
    }

    public void stopVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (videoSource != null && !videoSourceStopped) {
                    Log.d(TAG, "Stop video source.");
                    try {
                        videoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                    }
                    videoSourceStopped = true;
                    localVideoTrack.setEnabled(false);// lp add
                }
            }
        });
    }

    public void startVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (videoSource != null && videoSourceStopped) {
                    Log.d(TAG, "Restart video source.");
                    videoCapturer.startCapture(peerConnectionParameters.videoWidth, peerConnectionParameters.videoHeight, peerConnectionParameters.videoFps);
                    videoSourceStopped = false;
                    localVideoTrack.setEnabled(true);// lp add
                }
            }
        });
    }

    private AudioTrack createAudioTrack() {
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(enableAudio);
        return localAudioTrack;
    }

    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        videoSource = factory.createVideoSource(capturer);
        videoCapturer.startCapture(peerConnectionParameters.videoWidth, peerConnectionParameters.videoHeight, peerConnectionParameters.videoFps);
        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(renderVideo);
        return localVideoTrack;
    }

    private static String setStartBitrate(String codec, boolean isVideoCodec, String sdpDescription, int bitrateKbps) {
        String[] lines = sdpDescription.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec + " codec");
            return sdpDescription;
        }
        Log.d(TAG, "Found " + codec + " rtpmap " + codecRtpMap + " at " + lines[rtpmapLineIndex]);
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " + codec + " " + lines[i]);
                if (isVideoCodec) {
                    lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                if (isVideoCodec) {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " " + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " " + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }
        }
        return newSdpDescription.toString();
    }

    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        String[] lines = sdpDescription.split("\r\n");
        int mLineIndex = -1;
        String codecRtpMap = null;
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        String mediaDescription = "m=video ";
        if (isAudio) {
            mediaDescription = "m=audio ";
        }
        for (int i = 0; (i < lines.length) && (mLineIndex == -1 || codecRtpMap == null); i++) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i;
                continue;
            }
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
            }
        }
        if (mLineIndex == -1) {
            Log.w(TAG, "No " + mediaDescription + " line, so can't prefer " + codec);
            return sdpDescription;
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec);
            return sdpDescription;
        }
        Log.d(TAG, "Found " + codec + " rtpmap " + codecRtpMap + ", prefer at " + lines[mLineIndex]);
        String[] origMLineParts = lines[mLineIndex].split(" ");
        if (origMLineParts.length > 3) {
            StringBuilder newMLine = new StringBuilder();
            int origPartIndex = 0;
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(codecRtpMap);
            for (; origPartIndex < origMLineParts.length; origPartIndex++) {
                if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
                    newMLine.append(" ").append(origMLineParts[origPartIndex]);
                }
            }
            lines[mLineIndex] = newMLine.toString();
            Log.d(TAG, "Change media description: " + lines[mLineIndex]);
        } else {
            Log.e(TAG, "Wrong SDP media description format: " + lines[mLineIndex]);
        }
        StringBuilder newSdpDescription = new StringBuilder();
        for (String line : lines) {
            //            if (line.contains("a=mid:audio")) {
            //                line += "\r\n" + "b=AS:64";
            //            }
            //            if (line.contains("a=mid:video")) {
            //
            //                if (!"".equals(videoQuality.trim())) {
            //
            //                    if (videoQuality.equals("CIF")) {
            //                        line += "\r\n" + "b=AS:256";
            //                    }
            //                    if (videoQuality.equals("VGA")) {
            //                        line += "\r\n" + "b=AS:512";
            //                    }
            //                    if (videoQuality.equals("720p")) {
            //                        line += "\r\n" + "b=AS:1024";
            //                    }
            //                } else {
            //                    line += "\r\n" + "b=AS:256";
            //                }
            //            }
            newSdpDescription.append(line).append("\r\n");
        }
        return newSdpDescription.toString();
    }

    private void drainCandidates() {
        if (queuedRemoteCandidates != null) {
            Log.d(TAG, "Add " + queuedRemoteCandidates.size() + " remote candidates");
            for (IceCandidate candidate : queuedRemoteCandidates) {
                peerConnection.addIceCandidate(candidate);
            }
            queuedRemoteCandidates = null;
        }
    }

    private void switchCameraInternal() {
        if (!videoCallEnabled || numberOfCameras < 2 || isError || videoCapturer == null) {
            Log.e(TAG, "Failed to switch camera. Video: " + videoCallEnabled
                    + ". Error : " + isError + ". Number of cameras: "
                    + numberOfCameras);
            return; // No video is sent or only one camera is available or error happened.
        }
        Log.d(TAG, "Switch camera");
        videoCapturer.switchCamera(null);
    }

    public void changeCaptureFormat(final int width, final int height, final int framerate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                changeCaptureFormatInternal(width, height, framerate);
            }
        });
    }

    private void changeCaptureFormatInternal(int width, int height, int framerate) {
        if (!videoCallEnabled || isError || videoCapturer == null) {
            Log.e(TAG, "Failed to change capture format. Video: " + videoCallEnabled + ". Error : " + isError);
            return;
        }
        Log.d(TAG, "changeCaptureFormat: " + width + "x" + height + "@" + framerate);
        videoCapturer.changeCaptureFormat(width, height, framerate);
        videoEvents.GetStats(peerConnection);
    }

    private class PCObserver implements PeerConnection.Observer {
        public PCObserver() {
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    events.onIceCandidate(candidate);
                }
            });
        }

        @Override
        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    events.onIceCandidatesRemoved(candidates);
                }
            });
        }

        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            Log.d(TAG, "SignalingState: " + newState);
        }

        @Override
        public void onIceConnectionChange(final PeerConnection.IceConnectionState newState) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IceConnectionState: " + newState);
                    if (newState == IceConnectionState.CONNECTED) {
                        events.onIceConnected();
                    } else if (newState == IceConnectionState.DISCONNECTED) {
                        //如果收到连接断开事件， 需要做重连机制。
                        CiscoApiInterface.app.onReconnection();
                    } else if (newState == IceConnectionState.FAILED) {
                        events.onIceFailed();
                    }
                    if (newState == IceConnectionState.CHECKING) {
                        //断开会议重新连接

                    }
                }
            });
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Log.d(TAG, "IceGatheringState: " + newState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Log.d(TAG, "IceConnectionReceiving changed to " + receiving);
        }

        @Override
        public void onAddStream(final MediaStream stream) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    String label = stream.videoTracks.get(0).id();
                    Log.d(TAG, "onAddStream.label=" + stream.label() + ",id=" + id);
                    if (peerConnection == null || isError) {
                        return;
                    }
                    if (stream.label().equals("mixedmslabel")) {
                        return;
                    }
                    if (stream.audioTracks.size() > 1 || stream.videoTracks.size() > 1) {
                        return;
                    }
                    if (stream.videoTracks.size() < 1) {
                        return;
                    }
                    if (stream.videoTracks.size() == 1) {
                        Log.i(TAG, "-ssrcAndLabel_peer _label=" + label + ",stream=" + stream);
                        XmppConnection.streamLabelToUserId.put(label, stream);
                    }
                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
            Log.e("videoTracks.size()=", "111111111111打印=" + stream.videoTracks.size());
            if (peerConnection == null || isError) {
                return;
            }
            Log.e("videoTracks.size()=", "22222222222打印=" + stream.videoTracks.size());
            if (stream.videoTracks.size() == 1) {
                stream.videoTracks.get(0).dispose();
            }
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
        }

        @Override
        public void onRenegotiationNeeded() {
        }

        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {

        }
    }

    //动态移除断开的surfaceView
    public void removeSurfaceView(String jid) {
        updateEvents.onRemoveStream(jid);//移除视频的回调 交由客户端处理
    }

    //动态加入surfaceView
    public void addSurfaceView(final Participant pc, final MediaStream videoStream) {
        UI_Handler.post(new Runnable() {
            @Override
            public void run() {
//                SurfaceViewRenderer surfaceView = SdkPublicKey.createSurfaceView(context);
//                SdkPublicKey.initSurfaceView(surfaceView);
//                // 包装流并渲染
//                VideoRenderer vr = new VideoRenderer(surfaceView);
//                VideoTrack remoteVideoTrack = videoStream.videoTracks.get(0);
//                remoteVideoTrack.setEnabled(true);
//                remoteVideoTrack.addRenderer(vr);//渲染视频
//                pc.setSurfaceView(surfaceView);
                updateEvents.onAddStream(pc);
            }
        });
    }

    private class SDPObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription origSdp) {
            if (localSdp != null) {
                return;
            }
            String sdpDescription = origSdp.description;
            if (preferIsac) {
                sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC,
                        true);
            }
            if (videoCallEnabled) {
                sdpDescription = preferCodec(sdpDescription,
                        preferredVideoCodec, false);
            }
            final SessionDescription sdp = new SessionDescription(origSdp.type,
                    sdpDescription);
            localSdp = sdp;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection != null && !isError) {
                        //						Log.d(TAG, "Set local SDP from " + sdp.type);
                        Log.d(TAG, "sdp,onCreateSuccess=setLocalDescription=");
                        peerConnection.setLocalDescription(sdpObserver, sdp);
                    }
                }
            });
        }

        @Override
        public void onSetSuccess() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection == null || isError) {
                        return;
                    }
                    if (peerConnection.getLocalDescription() != null) {
                        Log.d(TAG, "sdp,onSetSuccess=peerConnection.getLocalDescription()!=null ,Local SDP set succesfully=");

                        events.onLocalDescription(localSdp);
                        drainCandidates();
                    } else {
                        Log.d(TAG, "sdp,onSetSuccess=peerConnection.getLocalDescription()==null ,Remote SDP set succesfully=");
                    }
                }
            });
        }

        @Override
        public void onCreateFailure(final String error) {
        }

        @Override
        public void onSetFailure(final String error) {
        }
    }
}
