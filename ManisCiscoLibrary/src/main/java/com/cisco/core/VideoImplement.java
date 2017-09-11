package com.cisco.core;

import android.app.Activity;
import android.util.Log;

import com.cisco.core.AppRTCClient.RoomConnectionParameters;
import com.cisco.core.AppRTCClient.SignalingParameters;
import com.cisco.core.PeerConnectionClient.PeerConnectionParameters;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.meet.util.SdpSsrcVariable;
import com.cisco.core.xmpp.XmppConnection;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.StatsObserver;
import org.webrtc.StatsReport;
import org.webrtc.StatsReport.Value;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class VideoImplement implements AppRTCClient.SignalingEvents,CiscoApiInterface.PeerConnectionEvents,CiscoApiInterface.videoOperationOrStatusEtc {


    private Activity activity;
	private String TAG="VideoImplement";


	// Peer connection statistics callback period in ms.
	private static final int STAT_CALLBACK_PERIOD = 5000;
	public PeerConnectionClient peerConnectionClient = null;
	private AppRTCClient appRtcClient;
	private SignalingParameters signalingParameters;
	public AppRTCAudioManager audioManager = null;

	private boolean commandLineRun;
	private int runTimeMs;
	private boolean activityRunning;
	private RoomConnectionParameters roomConnectionParameters;
	private PeerConnectionParameters peerConnectionParameters;
	private boolean iceConnected;
	private boolean isError;
	private boolean callControlFragmentVisible = true;
	private long callStartedTimeMs = 0;
	private boolean micEnabled = true;
	private boolean viewEnabled = true;
	private boolean videoEnabled = true;
	private boolean audioEnabled = true;


	private EglBase.Context renderEGLContext;
	private VideoRenderer.Callbacks localRender;
//	private VideoRenderer.Callbacks viewRender;
	List<VideoRenderer.Callbacks> remoteRenders = new ArrayList<VideoRenderer.Callbacks>();
	private List<SurfaceViewRenderer> listRemoteRenderer = new ArrayList<SurfaceViewRenderer>();

	public CiscoApiInterface.UpdateUIEvents updateEvents;

	public static VideoImplement videoImplement;
	public String cameraStatus;

	public VideoImplement(Activity activity, EglBase.Context renderEGLContext,
			 VideoRenderer.Callbacks localRender,
			 List<SurfaceViewRenderer> remoteRenders
//			 ,VideoRenderer.Callbacks viewRender
			,CiscoApiInterface.UpdateUIEvents updateEvents,String cameraStatus
			,String videoQuality){
		this.cameraStatus=cameraStatus;
		videoImplement=this;
		this.activity=activity;
		this.renderEGLContext=renderEGLContext;
		this.localRender=localRender;
		this.listRemoteRenderer=remoteRenders;
		for (int i = 0; i < remoteRenders.size(); i++) {
			this.remoteRenders.add(remoteRenders.get(i));
		}
		this.updateEvents=updateEvents;
		peerConnectionParameters=CiscoApiInterface.app.InitParameters(false,videoQuality);
		appRtcClient = new MeetRTCClient(activity.getApplicationContext());
		roomConnectionParameters = new AppRTCClient.RoomConnectionParameters("", false);
		startCall();
		peerConnectionClient = PeerConnectionClient.getInstance();
		peerConnectionClient.createPeerConnectionFactory(activity.getApplicationContext(),peerConnectionParameters, this,this,updateEvents,cameraStatus);
	}
	public void ReconnectionPeer(){
		peerConnectionClient.close();
		peerConnectionClient=null;
		appRtcClient.connectToRoom(roomConnectionParameters,updateEvents);
		peerConnectionClient = PeerConnectionClient.getInstance();
		peerConnectionClient.createPeerConnectionFactory(activity.getApplicationContext(),peerConnectionParameters, this,this,updateEvents,cameraStatus);
	}

	private void startCall() {
		if (appRtcClient == null) {
			Log.e(TAG, "AppRTC client is not allocated for a call.");
			return;
		}
		callStartedTimeMs = System.currentTimeMillis();

		// Start room connection.
		// ToastMessage.logAndToast(TAG,context,getString(R.string.connecting_to,
		// roomConnectionParameters.roomUrl));
		appRtcClient.connectToRoom(roomConnectionParameters,updateEvents);

		// Create and audio manager that will take care of audio routing,
		// audio modes, audio device enumeration etc.
		audioManager = AppRTCAudioManager.create(activity.getApplicationContext(), new Runnable() {
			// This method will be called each time the audio state (number and
			// type of devices) has been changed.
			@Override
			public void run() {
				onAudioManagerChangedState();
			}
		});
		// Store existing audio settings and change audio mode to
		// MODE_IN_COMMUNICATION for best possible VoIP performance.
		Log.d(TAG, "Initializing the audio manager...");
		audioManager.init();
	}

	public SessionDescription getRemoteDescription() {
	return peerConnectionClient.getRemoteDescription();
}
	public void setRemoteDescription(SessionDescription remoteDescription) {
	peerConnectionClient.setRemoteDescription(remoteDescription);
}

	private void onAudioManagerChangedState() {
		// TODO(henrika): disable video if
		// AppRTCAudioManager.AudioDevice.EARPIECE
		// is active.
	}


	@Override
	public void GetStats(PeerConnection peerConnection) {
		if (peerConnection == null || isError) {
		return;
	}
	boolean success = peerConnection.getStats(new StatsObserver() {
		@Override
		public void onComplete(final StatsReport[] reports) {
			//通过rtc获取上行宽带下行宽带等
			 DecimalFormat df = new DecimalFormat("0.00");//格式化小数
			for (StatsReport statsReport : reports) {

				if(statsReport.type.equals("VideoBwe")){
					StatsReport.Value [] srv=statsReport.values;
					for (Value value : srv) {
						String info=value.toString().trim();
						if(info.contains("googAvailableReceiveBandwidth")){
							String garbw=info.substring(info.indexOf(":")+1, info.lastIndexOf("]")).trim();
							SdpSsrcVariable.googAvailableReceiveBandwidth=Integer.parseInt(garbw);
						}
                        if(value.toString().contains("googAvailableSendBandwidth")){
                        	String galsbw=info.substring(info.indexOf(":")+1, info.lastIndexOf("]")).trim();
                        	SdpSsrcVariable.googAvailableSendBandwidth=Integer.parseInt(galsbw);
						}
					}
				}
				if (statsReport.type.equals("ssrc")) {
					// 获取values 数组
					float packetsLost = 0,packetsReceived = 0,packetsSent = 0;
					StatsReport.Value[] srv = statsReport.values;
					for (Value value : srv) {
						String ssrc_info = value.toString();
						if (ssrc_info.contains("packetsLost")) {
							String re=ssrc_info.substring(ssrc_info.indexOf(":")+1, ssrc_info.lastIndexOf("]")).trim();
							 packetsLost=Integer.parseInt(re);
						}

						if (ssrc_info.contains("packetsSent")) {
							String re=ssrc_info.substring(ssrc_info.indexOf(":")+1, ssrc_info.lastIndexOf("]")).trim();
							 packetsSent=Integer.parseInt(re);
						}

						if (ssrc_info.contains("packetsReceived")) {
							String re=ssrc_info.substring(ssrc_info.indexOf(":")+1, ssrc_info.lastIndexOf("]")).trim();
							 packetsReceived=Integer.parseInt(re);
						}

						if (ssrc_info.contains("googFrameRateSent")) {
							// 上行丢包率
							float count=packetsLost+packetsSent;
							 if(count>0){
								 float aa=(float)packetsLost/count;
								 String s = df.format(aa);
								 SdpSsrcVariable.packetLoss_upload=s;
		                         }
						}
						if (ssrc_info.contains("googFrameRateReceived")) {
							float count=packetsLost+packetsReceived;
							// 下行丢包率
							 if(count>0){
								 float bb=(float)packetsLost/count;
								 SdpSsrcVariable.packetLoss_download=df.format(bb);

								 }
						}

					}

				}

			}

			XmppConnection.getInstance().SendPresenceMessage();
			onPeerConnectionStatsReady(reports);
		}
	}, null);
	if (!success) {
		Log.e(TAG, "getStats() returns false!");
	}
	}


	@Override
	public void onLocalDescription(final SessionDescription sdp) {
		final long delta = System.currentTimeMillis() - callStartedTimeMs;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (appRtcClient != null) {
//					ToastMessage.logAndToast(TAG,acti,"Sending " + sdp.type + ", delay=" + delta+ "ms");
//					if (signalingParameters.initiator) {
//						appRtcClient.sendOfferSdp(sdp);
//					} else {
						appRtcClient.sendAnswerSdp(sdp);
//					}
				}
			}
		});
	}

	@Override
	public void onIceCandidate(final IceCandidate candidate) {
		System.out.print("onIceCandidate="+candidate);
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
		final long delta = System.currentTimeMillis() - callStartedTimeMs;
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
//				ToastMessage.logAndToast(TAG,context,"ICE connected, delay=" + delta + "ms");
				iceConnected = true;
//				callConnected();
				final long delta = System.currentTimeMillis() - callStartedTimeMs;
				Log.i(TAG, "Call connected: delay=" + delta + "ms");
				if (peerConnectionClient == null || isError) {
					Log.w(TAG, "Call is connected in closed or error state");
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPeerConnectionStatsReady(final StatsReport[] reports) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!isError && iceConnected) {
//					hudFragment.updateEncoderStatistics(reports);
					updateEvents.peerConnectionStatsReady(reports);
				}
			}
		});		
	}

	@Override
	public void onPeerConnectionError(String description) {
//		reportError(description);
	}

	@Override
	public void onConnectedToRoomLocal(final SignalingParameters params) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//先创建本地流
				onConnectedToRoomInternalLocal(params);
			}
		});		
	}
	private void onConnectedToRoomInternalLocal(final SignalingParameters params) {
		signalingParameters = params;
		peerConnectionClient.createPeerConnection(
				renderEGLContext, localRender,
				listRemoteRenderer
//				,viewRender
				, signalingParameters);
	}
	@Override
	public void onConnectedToRoomRemote(final SignalingParameters params) {
		activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//收到SESSION_INITIATE 设置remote端sdp
				onConnectedToRoomInternal(params);
			}
		});		
	}
	private void onConnectedToRoomInternal(final SignalingParameters params) {
		final long delta = System.currentTimeMillis() - callStartedTimeMs;
		signalingParameters = params;
			if (params.offerSdp != null) {
				peerConnectionClient.setRemoteDescription(params.offerSdp);
//				ToastMessage.logAndToast(TAG,context,"Creating ANSWER...");
				// Create answer. Answer SDP will be sent to offering client in
				// PeerConnectionEvents.onLocalDescription event.
//				peerConnectionClient.createAnswer();
			}
			if (params.iceCandidates != null) {
				// Add remote ICE candidates from room.
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
		if (localRender != null) {
			localRender = null;
		}
		for (int i = 0; i < listRemoteRenderer.size(); i++) {
			SurfaceViewRenderer remoteRender = listRemoteRenderer.get(i);
			if (remoteRender != null) {
				remoteRender.release();
				remoteRender = null;
			}

		}
		listRemoteRenderer.clear();
		if (audioManager != null) {
			audioManager.close();
			audioManager = null;
		}
	}
	public void updataViewAll(){
		for (int j = 0; j < listRemoteRenderer.size(); j++) {
			listRemoteRenderer.get(j).release();
			listRemoteRenderer.get(j).init(renderEGLContext, null);
		}
		
	}
	public void updataView(int i){
		if(i<(listRemoteRenderer.size())){
			listRemoteRenderer.get(i).release();
			listRemoteRenderer.get(i).init(renderEGLContext, null);
		}
	}
	public void onMeetReconnection() {
		appRtcClient.connectToRoom(roomConnectionParameters,updateEvents);
	}
}
