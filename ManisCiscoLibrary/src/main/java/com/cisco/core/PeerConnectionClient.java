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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaCodecVideoEncoder;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaConstraints.KeyValuePair;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnection.IceConnectionState;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioManager;
import org.webrtc.voiceengine.WebRtcAudioUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.cisco.core.AppRTCClient.SignalingParameters;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.interfaces.CiscoApiInterface.PeerConnectionEvents;
import com.cisco.core.interfaces.CiscoApiInterface.videoOperationOrStatusEtc;
import com.cisco.core.meet.util.SdpSsrcVariable;
import com.cisco.core.xmpp.Key;

/**
 * Peer connection client implementation.
 * <p/>
 * <p/>
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

    private static final PeerConnectionClient instance = new PeerConnectionClient();
    private final PCObserver pcObserver = new PCObserver();
    private final SDPObserver sdpObserver = new SDPObserver();
    private final ScheduledExecutorService executor;

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
    private VideoRenderer.Callbacks localRender;
    List<VideoRenderer.Callbacks> remoteRenders = new ArrayList<VideoRenderer.Callbacks>();
    private SignalingParameters signalingParameters;
    private MediaConstraints pcConstraints;
    private MediaConstraints videoConstraints;
    private MediaConstraints audioConstraints;
    private ParcelFileDescriptor aecDumpFileDescriptor;
    private MediaConstraints sdpMediaConstraints;
    private PeerConnectionParameters peerConnectionParameters;
    // Queued remote ICE candidates are consumed only after both local and
    // remote descriptions are set. Similarly local ICE candidates are sent to
    // remote peer after both local and remote description are set.
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
    private boolean cameraType;// lp add 获取摄像头是前置后置
    private String cameraStatus;


    public SessionDescription getRemoteDescription() {
        return peerConnection.getRemoteDescription();
    }

    /**
     * Peer connection parameters.
     */
    public static class PeerConnectionParameters {
        public final boolean videoCallEnabled;
        public final boolean loopback;
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

        public PeerConnectionParameters(boolean videoCallEnabled,
                                        boolean loopback, boolean tracing, boolean useCamera2,
                                        int videoWidth, int videoHeight, int videoFps,
                                        int videoStartBitrate, String videoCodec,
                                        boolean videoCodecHwAcceleration, boolean captureToTexture,
                                        int audioStartBitrate, String audioCodec,
                                        boolean noAudioProcessing, boolean aecDump,
                                        boolean useOpenSLES, boolean disableBuiltInAEC) {
            this.videoCallEnabled = videoCallEnabled;
            this.useCamera2 = useCamera2;
            this.loopback = loopback;
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
        return instance;
    }

    public void setPeerConnectionFactoryOptions(
            PeerConnectionFactory.Options options) {
        this.options = options;
    }

    public void createPeerConnectionFactory(final Context context,
                                            final PeerConnectionParameters peerConnectionParameters,
                                            final PeerConnectionEvents events,
                                            videoOperationOrStatusEtc videoEvents,
                                            CiscoApiInterface.UpdateUIEvents updateEvents,
                                            String cameraStatus) {
        this.peerConnectionParameters = peerConnectionParameters;
        this.events = events;
        this.cameraStatus = cameraStatus;//lp add
        this.videoEvents = videoEvents;// lp add
        this.updateEvents = updateEvents;
        videoCallEnabled = peerConnectionParameters.videoCallEnabled;
        // Reset variables to initial states.
        this.context = null;
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
        statsTimer = new Timer();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                createPeerConnectionFactoryInternal(context);
            }
        });
    }

    public void createPeerConnection(final EglBase.Context renderEGLContext,
                                     final VideoRenderer.Callbacks localRender,
                                     final List<SurfaceViewRenderer> remoteRenders,
//			final VideoRenderer.Callbacks viewRender,
                                     final SignalingParameters signalingParameters) {
        if (peerConnectionParameters == null) {
            Log.e(TAG, "Creating peer connection without initializing factory.");
            return;
        }
//		this.viewRender = viewRender;
        this.localRender = localRender;
        this.remoteRenders.clear();
        for (int i = 0; i < remoteRenders.size(); i++) {
            this.remoteRenders.add(remoteRenders.get(i));
        }

        // this.remoteRenders = remoteRenders;
        this.signalingParameters = signalingParameters;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    createMediaConstraintsInternal();
                    createPeerConnectionInternal(renderEGLContext);
                } catch (Exception e) {
//					reportError("Failed to create peer connection: "+ e.getMessage());
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
        PeerConnectionFactory.initializeInternalTracer();
        if (peerConnectionParameters.tracing) {
            PeerConnectionFactory.startInternalTracingCapture(Environment
                    .getExternalStorageDirectory().getAbsolutePath()
                    + File.separator + "webrtc-trace.txt");
        }
        Log.d(TAG, "Create peer connection factory. Use video: "
                + peerConnectionParameters.videoCallEnabled);
        isError = false;

        // Initialize field trials.
        PeerConnectionFactory.initializeFieldTrials("");

        // Check preferred video codec.
        // preferredVideoCodec = VIDEO_CODEC_VP8;
        preferredVideoCodec = VIDEO_CODEC_VP9;
//		preferredVideoCodec = VIDEO_CODEC_H264;

        if (videoCallEnabled && peerConnectionParameters.videoCodec != null) {
            if (peerConnectionParameters.videoCodec.equals(VIDEO_CODEC_VP9)) {
                preferredVideoCodec = VIDEO_CODEC_VP9;
            } else if (peerConnectionParameters.videoCodec
                    .equals(VIDEO_CODEC_H264)) {
                preferredVideoCodec = VIDEO_CODEC_H264;
            }
        }
        Log.d(TAG, "Pereferred video codec: " + preferredVideoCodec);

        // Check if ISAC is used by default.
        preferIsac = peerConnectionParameters.audioCodec != null
                && peerConnectionParameters.audioCodec.equals(AUDIO_CODEC_ISAC);

        // Enable/disable OpenSL ES playback.
        if (!peerConnectionParameters.useOpenSLES) {
            Log.d(TAG, "Disable OpenSL ES audio even if device supports it");
            WebRtcAudioManager
                    .setBlacklistDeviceForOpenSLESUsage(true /* enable */);
        } else {
            Log.d(TAG, "Allow OpenSL ES audio if device supports it");
            WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(false);
        }

        if (peerConnectionParameters.disableBuiltInAEC) {
            Log.d(TAG, "Disable built-in AEC even if device supports it");
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(true);
        } else {
            Log.d(TAG, "Enable built-in AEC if device supports it");
            WebRtcAudioUtils.setWebRtcBasedAcousticEchoCanceler(false);
        }

        // Create peer connection factory.
        if (!PeerConnectionFactory.initializeAndroidGlobals(context, true,
                true, peerConnectionParameters.videoCodecHwAcceleration)) {
            events.onPeerConnectionError("Failed to initializeAndroidGlobals");
        }
        if (options != null) {
            Log.d(TAG, "Factory networkIgnoreMask option: "
                    + options.networkIgnoreMask);
        }
        this.context = context;
        options=new PeerConnectionFactory.Options();
        options.disableNetworkMonitor=true;
        factory = new PeerConnectionFactory(options);
        Log.d(TAG, "Peer connection factory created.");

    }

    private void createMediaConstraintsInternal() {
        // Create peer connection constraints.
        pcConstraints = new MediaConstraints();
        // Enable DTLS for normal calls and disable for loopback calls.
//		if (peerConnectionParameters.loopback) {
//			pcConstraints.optional.add(new MediaConstraints.KeyValuePair(
//					DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "false"));
//		} else {
        pcConstraints.optional.add(new KeyValuePair(
                DTLS_SRTP_KEY_AGREEMENT_CONSTRAINT, "true"));
//		}

        // Check if there is a camera on device and disable video call if not.
        numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
        Log.d(TAG,"linpeng,numberOfCameras="+numberOfCameras);
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
            audioConstraints.mandatory.add(new KeyValuePair(
                    AUDIO_ECHO_CANCELLATION_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new KeyValuePair(
                    AUDIO_AUTO_GAIN_CONTROL_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new KeyValuePair(
                    AUDIO_HIGH_PASS_FILTER_CONSTRAINT, "false"));
            audioConstraints.mandatory.add(new KeyValuePair(
                    AUDIO_NOISE_SUPPRESSION_CONSTRAINT, "false"));
        }
        // Create SDP constraints.
        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new KeyValuePair(
                "OfferToReceiveAudio", "true"));
        if (videoCallEnabled || peerConnectionParameters.loopback) {
            sdpMediaConstraints.mandatory
                    .add(new KeyValuePair(
                            "OfferToReceiveVideo", "true"));
        } else {
            sdpMediaConstraints.mandatory
                    .add(new KeyValuePair(
                            "OfferToReceiveVideo", "false"));
        }
    }

    private void createCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        //linpeng  add 判断初始设置前置 或者后置摄像头。
//				String cameraStatus = SPUtil.getCameraSP(context);
//        for (String deviceName : deviceNames) {
//            Log.d(TAG,"linpeng,deviceName="+deviceName);
//        }
        if (numberOfCameras == 1) {
            //只有一个摄像头的话， 例如7i
            //后置Front facing camera not found, try something else
            Logging.d(TAG, "Looking for other cameras.");
//            RearCamera(enumerator, deviceNames);//华为7i 初始后置
            frontCamera(enumerator, deviceNames);
            //  https://zhidao.baidu.com/question/1669424088437834307.html
        }
        if (numberOfCameras == 2) {
            if (!"".equals(cameraStatus.trim())) {
                if (cameraStatus.equals("true")) {
                    //前置
                    // First, try to find front facing camera
                    Logging.d(TAG, "Looking for front facing cameras.");
                    frontCamera(enumerator, deviceNames);

                } else {
                    //后置
                    // Front facing camera not found, try something else
                    Logging.d(TAG, "Looking for other cameras.");
                    RearCamera(enumerator, deviceNames);
                }

            } else {
                frontCamera(enumerator, deviceNames);
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
    private void createPeerConnectionInternal(EglBase.Context renderEGLContext) {
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
            Log.d(TAG, "EGLContext: " + renderEGLContext);
            factory.setVideoHwAccelerationOptions(renderEGLContext,
                    renderEGLContext);
        }
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        iceServers.add(new PeerConnection.IceServer(Key.ice_stun, "", ""));
        iceServers.add(new PeerConnection.IceServer(Key.ice_credential_turn, Key.ice_credential_username, Key.ice_credential));
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(
                iceServers);
//		PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(
//				signalingParameters.iceServers);
        // TCP candidates are only useful when connecting to a server that
        // supports
        // ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        peerConnection = factory.createPeerConnection(rtcConfig, pcConstraints,
                pcObserver);
//		isInitiator = false;

        // Set default WebRTC tracing and INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!---------webrtc日志
//        Logging.enableTracing("logcat:", EnumSet.of(Logging.TraceLevel.TRACE_ALL));
//        Logging.enableLogToDebugOutput(Logging.Severity.LS_INFO);

        // mediaStream = factory.createLocalMediaStream("ARDAMS");
        mediaStream = factory.createLocalMediaStream(SdpSsrcVariable.getUUID());
        if (videoCallEnabled) {
            if (peerConnectionParameters.useCamera2) {
                if (!peerConnectionParameters.captureToTexture) {
//					reportError(context
//							.getString(R.string.camera2_texture_only_error));
                    return;
                }

                Logging.d(TAG, "Creating capturer using camera2 API.");
                createCapturer(new Camera2Enumerator(context));
            } else {
                Logging.d(TAG, "Creating capturer using camera1 API.");
                createCapturer(new Camera1Enumerator(
                        peerConnectionParameters.captureToTexture));
            }

            if (videoCapturer == null) {
//				reportError("Failed to open camera");
                return;
            }
            mediaStream.addTrack(createVideoTrack(videoCapturer));
        }

        mediaStream.addTrack(createAudioTrack());
        peerConnection.addStream(mediaStream);
        //林鹏添加，有时初始黑屏，从新restart
//        videoSource.restart();
        updateEvents.onAddLocalStream(peerConnection, mediaStream, localVideoTrack);

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
//		Key.remoteNumber=0;
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

    public void createOffer() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (peerConnection != null && !isError) {
                    Log.d(TAG, "PC Create OFFER");
//					isInitiator = true;
                    peerConnection
                            .createOffer(sdpObserver, sdpMediaConstraints);
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
                    peerConnection.createAnswer(sdpObserver, sdpMediaConstraints);
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
                // Drain the queued remote candidates if there is any so that
                // they are processed in the proper order.
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
                Log.d(TAG, "linpeng,--videoCallEnabled=" + videoCallEnabled+",videoStartBitrate="+peerConnectionParameters.videoStartBitrate+",audioStartBitrate="+peerConnectionParameters.audioStartBitrate);
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
                Log.d(TAG, "linpeng,--开始设置远端sdpDescription=" + sdpDescription);
                SessionDescription sdpRemote = new SessionDescription(sdp.type,  sdpDescription);
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
                    videoSource.stop();
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
                    videoSource.restart();
                    videoSourceStopped = false;
                    localVideoTrack.setEnabled(true);// lp add
                }
            }
        });
    }

//	private void reportError(final String errorMessage) {
//		Log.e(TAG, "Peerconnection error: " + errorMessage);
//		executor.execute(new Runnable() {
//			@Override
//			public void run() {
//				if (!isError) {
//					events.onPeerConnectionError(errorMessage);
//					isError = true;
//				}
//			}
//		});
//	}

    private AudioTrack createAudioTrack() {
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        localAudioTrack.setEnabled(enableAudio);
        return localAudioTrack;
    }

    private VideoTrack createVideoTrack(VideoCapturer capturer) {
        videoSource = factory.createVideoSource(capturer, videoConstraints);
        localVideoTrack = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        localVideoTrack.setEnabled(renderVideo);
        localVideoTrack.addRenderer(new VideoRenderer(localRender));
        return localVideoTrack;
    }

    private static String setStartBitrate(String codec, boolean isVideoCodec,
                                          String sdpDescription, int bitrateKbps) {
        String[] lines = sdpDescription.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        // Search for codec rtpmap in format
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding
        // parameters>]
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
        Log.d(TAG, "Found " + codec + " rtpmap " + codecRtpMap + " at "
                + lines[rtpmapLineIndex]);

        // Check if a=fmtp string already exist in remote SDP for this codec and
        // update it with new bitrate parameter.
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " + codec + " " + lines[i]);
                if (isVideoCodec) {
                    lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE + "="
                            + bitrateKbps;
                } else {
                    lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE + "="
                            + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            // Append new a=fmtp line if no such line exist for a codec.
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                if (isVideoCodec) {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " "
                            + VIDEO_CODEC_PARAM_START_BITRATE + "="
                            + bitrateKbps;
                } else {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " "
                            + AUDIO_CODEC_PARAM_BITRATE + "="
                            + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }

        }
        return newSdpDescription.toString();
    }
    private static String preferCodec(
            String sdpDescription, String codec, boolean isAudio) {
        String[] lines = sdpDescription.split("\r\n");
        int mLineIndex = -1;
        int addIndexAudio=-1,addIndeVideo=-1;
        String codecRtpMap = null;
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        String mediaDescription = "m=video ";
        if (isAudio) {
            mediaDescription = "m=audio ";
        }
        for (int i = 0; (i < lines.length)
                && (mLineIndex == -1 || codecRtpMap == null); i++) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i;
                continue;
            }
//            if(lines[i].startsWith("a=mid:audio")){
//                addIndexAudio=i;
//                Log.d(TAG, "----------------preferCodec---audio---"+ lines[addIndexAudio].toString());
//            }
//            if(lines[i].startsWith("a=mid:video")){
//                addIndeVideo=i;
//                Log.d(TAG, "----------------preferCodec---video---"+ lines[addIndeVideo].toString());
//            }
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
        Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + ", prefer at "
                + lines[mLineIndex]);
        String[] origMLineParts = lines[mLineIndex].split(" ");
        if (origMLineParts.length > 3) {
            StringBuilder newMLine = new StringBuilder();
            int origPartIndex = 0;
            // Format is: m=<media> <port> <proto> <fmt> ...
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
            newSdpDescription.append(line).append("\r\n");
        }
//        for (int i = 0; i < lines.length; i++) {
//            //linpeng add
////             if(lines[i].equals("a=mid:audio")){
////                lines[i]+="\r\n" +  "b=AS:64";
////            }
////            if(lines[i].equals("\n"+"a=mid:video")){
////                lines[i]+="\r\n" +  "b=AS:128";
////            }
//            newSdpDescription.append(lines[i]).append("\r\n");
//        }
        return newSdpDescription.toString();
    }

    private void drainCandidates() {
        if (queuedRemoteCandidates != null) {
            Log.d(TAG, "Add " + queuedRemoteCandidates.size()
                    + " remote candidates");
            for (IceCandidate candidate : queuedRemoteCandidates) {
                peerConnection.addIceCandidate(candidate);
            }
            queuedRemoteCandidates = null;
        }
    }

    private void switchCameraInternal() {
        if (!videoCallEnabled || numberOfCameras < 2 || isError
                || videoCapturer == null) {
            Log.e(TAG, "Failed to switch camera. Video: " + videoCallEnabled
                    + ". Error : " + isError + ". Number of cameras: "
                    + numberOfCameras);
            return; // No video is sent or only one camera is available or error
            // happened.
        }
        Log.d(TAG, "Switch camera");
        videoCapturer.switchCamera(null);
    }

    public void changeCaptureFormat(final int width, final int height,
                                    final int framerate) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                changeCaptureFormatInternal(width, height, framerate);
            }
        });
    }

    private void changeCaptureFormatInternal(int width, int height,
                                             int framerate) {
        if (!videoCallEnabled || isError || videoCapturer == null) {
            Log.e(TAG, "Failed to change capture format. Video: "
                    + videoCallEnabled + ". Error : " + isError);
            return;
        }
        Log.d(TAG, "changeCaptureFormat: " + width + "x" + height + "@" + framerate);
//		videoCapturer.onOutputFormatRequest(width, height, framerate);
        videoCapturer.changeCaptureFormat(width, height, framerate);
        videoEvents.GetStats(peerConnection);
    }

    // Implementation detail: observe ICE & stream changes and react
    // accordingly.
    private class PCObserver implements PeerConnection.Observer {
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
        public void onIceConnectionChange(
                final IceConnectionState newState) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IceConnectionState: " + newState);
                    if (newState == IceConnectionState.CONNECTED) {
                        events.onIceConnected();

                    } else if (newState == IceConnectionState.DISCONNECTED) {
//						events.onIceDisconnected();
                        //如果收到连接断开事件， 需要做重连机制。
                        CiscoApiInterface.app.onReconnection();
                    } else if (newState == IceConnectionState.FAILED) {
//						reportError("ICE connection failed.");
                        events.onIceFailed();
                    }
                }
            });
        }

        @Override
        public void onIceGatheringChange(
                PeerConnection.IceGatheringState newState) {
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
                    if (peerConnection == null || isError) {
                        return;
                    }
                    stream.videoTracks.get(0);
//                    Log.i(TAG, "linpeng,,onAddStream:stream.label() " + stream.label()+",id="+stream.videoTracks.get(0).id()+",size="+stream.videoTracks.size());
                    if (stream.label().equals("mixedmslabel")) {
                        return;
                    }
                    if (stream.audioTracks.size() > 1 || stream.videoTracks.size() > 1) {
//						reportError("Weird-looking stream: " + stream);
                        return;
                    }
                    if (stream.videoTracks.size() < 1) {
                        return;
                    }
                    if (stream.videoTracks.size() == 1) {
                        updateEvents.onAddStream(stream, remoteRenders, remoteVideoTrack, renderVideo);
                    }

                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
//            Log.i(TAG, "linpeng,,onRemoveStream:stream.label() " + stream.label()+",id="+stream.videoTracks.get(0).id()+",size="+stream.videoTracks.size());
            if (peerConnection == null || isError) {
                return;
            }
            if (stream.videoTracks.size() == 1) {
                updateEvents.onRemoveStream(stream, remoteRenders, remoteVideoTrack, renderVideo);
            }
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
        }

        @Override
        public void onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }
    }

    // Implementation detail: handle offer creation/signaling and answer
    // setting,
    // as well as adding remote ICE candidates once the answer SDP is set.
    private class SDPObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(final SessionDescription origSdp) {
            if (localSdp != null) {
//				reportError("Multiple SDP create.");
                return;
            }
            String sdpDescription = origSdp.description;
            if (preferIsac) {
                sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC,
                        true);
            }
            if (videoCallEnabled) {
                Log.d(TAG, "linpeng,--preferCodec------创建本地sdponCreateSuccess_localSdp:"+sdpDescription);
                sdpDescription = preferCodec(sdpDescription, preferredVideoCodec, false);
            }
            final SessionDescription sdp = new SessionDescription(origSdp.type,
                    sdpDescription);
            localSdp = sdp;
//            final String finalSdpDescription = sdpDescription;
            Log.d(TAG, "linpeng,--创建本地sdponCreateSuccess_localSdp:" + sdpDescription);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (peerConnection != null && !isError) {
                        Log.d(TAG, "linpeng,--Set local SDP from " + sdp.type);
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
                    Log.d(TAG, "linpeng,--onSetSuccess_getLocalDescription=" + peerConnection.getLocalDescription());
                    // For answering peer connection we set remote SDP and
                    // then
                    // create answer and set local SDP.
                    if (peerConnection.getLocalDescription() != null) {
                        // We've just set our local SDP so time to send it,
                        // drain
                        // remote and send local ICE candidates.
                        System.out.println( "linpeng,--onSetSuccess_Local SDP set succesfully description" + peerConnection.getLocalDescription().description);
                        events.onLocalDescription(localSdp);
                        drainCandidates();
                    } else {
                        // We've just set remote SDP - do nothing for now -
                        // answer will be created soon.
                        //远端设置成功后 创建本地流
                        System.out.println("linpeng,--远端设置成功onSetSuccess_description=" + peerConnection.getRemoteDescription().description);
                        createAnswer();

                    }
                }
            });
        }

        @Override
        public void onCreateFailure(final String error) {
//			reportError("createSDP error: " + error);
        }

        @Override
        public void onSetFailure(final String error) {
//			reportError("setSDP error: " + error);
        }
    }

}
