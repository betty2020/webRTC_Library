package com.cisco.core;

import android.content.Context;
import android.util.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;

import org.jivesoftware.smack.provider.*;
import org.jivesoftware.smack.util.StringUtils;
import org.webrtc.*;

import java.util.*;

import com.cisco.core.entity.ParticipantMedia;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.meet.util.MediaSSRCMap;
import com.cisco.core.meet.JingleToSdp;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;

/**
 *
 */
public class MeetRTCClient implements AppRTCClient {
	private final static String TAG = "MeetRTCClient";

	private Context context;
	private boolean answerSent;
	private XmppConnection xmppConnection = XmppConnection.getInstance();

	
	public MeetRTCClient(Context context){
		this.context=context;
	}

	@Override
	public void connectToRoom(RoomConnectionParameters connectionParameters,final CiscoApiInterface.UpdateUIEvents updateEvents) {

		ProviderManager.getInstance().addIQProvider(ColibriConferenceIQ.ELEMENT_NAME,ColibriConferenceIQ.NAMESPACE, new ColibriIQProvider());
		ProviderManager.getInstance().addIQProvider(JingleIQ.ELEMENT_NAME,JingleIQ.NAMESPACE, new JingleIQProvider());

		new Thread(new Runnable() {
			@Override
			public void run() {
				if (xmppConnection != null) {
					xmppConnection.joinMuc(MeetRTCClient.this,
							CiscoApiInterface.app.roomnumber + "@conference." + CiscoApiInterface.app.xmpp_url,
							CiscoApiInterface.app.xmpp_nick, updateEvents);
				}
			}
		}).start();
	}

	private SignalingParameters prepareSignalingParams(
			SessionDescription bridgeOfferSdp) {
		boolean initiator = false;

		// We provide empty ice servers and candidates lists, they should be
		// extracted from the remote description
		LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
		LinkedList<IceCandidate> iceCandidates = new LinkedList<>();
		SignalingParameters params = new SignalingParameters(iceServers,
				initiator,
				// clientId,// wssUrl, wssPostUrl,
				bridgeOfferSdp, iceCandidates);

		return params;
	}

	public void acceptSessionInit(final SessionDescription bridgeOfferSdp) {
		SignalingParameters signalingParameters = prepareSignalingParams(bridgeOfferSdp);
		VideoImplement.videoImplement.onConnectedToRoomRemote(signalingParameters);
	}

	public void onSourceAdd(MediaSSRCMap addedSSRCs) {
		SessionDescription rsd = VideoImplement.videoImplement.getRemoteDescription();
		Log.i(TAG, "linpeng,,SOURCE ADD rsd: " +rsd.description);
		SessionDescription modifiedOffer = JingleToSdp.addSSRCs(rsd, addedSSRCs);
		Log.i(TAG, "linpeng,,SOURCE ADD modifiedOffer: " +modifiedOffer.description);
		VideoImplement.videoImplement.setRemoteDescription(modifiedOffer);

		Log.d(TAG, "linpeng,,SOURCE ADD Key.lp.size: " +Key.lp.size()+"-----------------------------");
		Log.d(TAG, "linpeng,,SOURCE ADD videoSSRCs: " +addedSSRCs.getSsrcs().get("video").get(0).getSSRC());
		Log.d(TAG, "linpeng,,SOURCE ADD video-label: " +addedSSRCs.getSsrcs().get("video").get(0).getParameter("label"));
		Log.d(TAG, "linpeng,,SOURCE ADD audioSSRCs: " +addedSSRCs.getSsrcs().get("audio").get(0).getSSRC());
		Log.d(TAG, "linpeng,,SOURCE ADD audioSSRCs.size: " +addedSSRCs.getSsrcs().size());
		if(addedSSRCs.getSsrcs().size()>1) {
			ParticipantMedia pm = new ParticipantMedia();
			String labelValue = addedSSRCs.getSsrcs().get("video").get(0).getParameter("label");
			String cnameValue = addedSSRCs.getSsrcs().get("video").get(0).getParameter("cname");
			Long videoSSRCs = addedSSRCs.getSsrcs().get("video").get(0).getSSRC();
			Long audioSSRCs = addedSSRCs.getSsrcs().get("audio").get(0).getSSRC();
			pm.setStreamId(labelValue);
			pm.setcName(cnameValue);
			pm.setVideoSsrcMedia(Long.toString(videoSSRCs));
			pm.setAudioSsrcMedia(Long.toString(audioSSRCs));
			Key.lp.add(pm);
		}
		if(addedSSRCs.getSsrcs().size()==1){
			String cnameValue = addedSSRCs.getSsrcs().get("video").get(0).getParameter("cname");
			String labelValue = addedSSRCs.getSsrcs().get("video").get(0).getParameter("label");
			Long videoSSRCs = addedSSRCs.getSsrcs().get("video").get(0).getSSRC();
			for (ParticipantMedia pm:Key.lp ) {
				if(cnameValue.equals(pm.getcName())){
					pm.setVideoSsrcMedia(Long.toString(videoSSRCs));
					pm.setStreamId(labelValue);
				}
			}
		}


	}

	public void onSourceRemove(MediaSSRCMap removedSSRCs) {
		SessionDescription rsd = VideoImplement.videoImplement.getRemoteDescription();
		SessionDescription modifiedOffer = JingleToSdp.removeSSRCs(rsd,
				removedSSRCs);
		Log.i(TAG, "SOURCE REMOVE OFFER: " + modifiedOffer.description);
		System.out.println("----------------------onSourceRemove-description="+modifiedOffer.description);
		VideoImplement.videoImplement.setRemoteDescription(modifiedOffer);
	}

	@Override
	public void sendOfferSdp(SessionDescription sdp) {
		Log.e(TAG, "We do not send offer ! - ever!!");
	}

	@Override
	public void sendAnswerSdp(SessionDescription sdp) {
		if (!answerSent) {
			xmppConnection.sendSessionAccept(sdp);
		}
		answerSent = true;
	}

	@Override
	public void sendLocalIceCandidate(IceCandidate candidate) {
		xmppConnection.sendTransportInfo(candidate);
	}
	/**
	 * Disconnect.
	 */
	private void disconnect() {
		if (xmppConnection != null) {
			xmppConnection.disconnect();
		}
	}

	@Override
	public void disconnectFromRoom() {
		disconnect();
	}

	@Override
	public void sendLocalIceCandidateRemovals(IceCandidate[] candidates) {

	}
	@Override
	public void onCreateLocalRoom() {
		boolean initiator = true;
		SessionDescription offerSdp = null;
		LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
		LinkedList<IceCandidate> iceCandidates = new LinkedList<>();
		SignalingParameters signalingParameters = new SignalingParameters(
				iceServers, initiator, offerSdp, iceCandidates);
		VideoImplement.videoImplement.onConnectedToRoomLocal(signalingParameters);
	}
}
