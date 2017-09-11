package com.cisco.core;

import android.content.Context;
import android.util.Log;
import com.cisco.core.entity.Participant;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.meet.JingleToSdp;
import com.cisco.core.meet.util.MediaSSRCMap;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriConferenceIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.ColibriIQProvider;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.LinkedList;
import java.util.Map;

/**
 *
 */
public class MeetRTCClient implements AppRTCClient {
    private final static String TAG = "MeetRTCClient";

    private Context context;
    private boolean answerSent;
    private XmppConnection xmppConnection = XmppConnection.getInstance();
    private static final MeetRTCClient instance = new MeetRTCClient();

    public MeetRTCClient(Context context) {
        this.context = context;
    }


    public MeetRTCClient() {
    }

    public static MeetRTCClient getInstance() {
        return instance;
    }

    @Override
    public void connectToRoom(final CiscoApiInterface.UpdateUIEvents updateEvents) {
        ProviderManager.getInstance().addIQProvider(ColibriConferenceIQ.ELEMENT_NAME, ColibriConferenceIQ.NAMESPACE, new ColibriIQProvider());
        ProviderManager.getInstance().addIQProvider(JingleIQ.ELEMENT_NAME, JingleIQ.NAMESPACE, new JingleIQProvider());
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (xmppConnection != null) {
                    String roomURL = Key.roomnumber + "@conference." + xmppConnection.getXmppDomain();
                    xmppConnection.joinMuc(MeetRTCClient.this, roomURL, updateEvents);
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
        SignalingParameters params = new SignalingParameters(iceServers
                //				,initiator
                , bridgeOfferSdp, iceCandidates);
        return params;
    }

    public void acceptSessionInit(final SessionDescription bridgeOfferSdp) {

        SignalingParameters signalingParameters = prepareSignalingParams(bridgeOfferSdp);
        //		callActivity.onConnectedToRoomRemote(signalingParameters);
        VideoImplement.videoImplement.onConnectedToRoomRemote(signalingParameters);

    }

    // MediaSSRCMap 是访问和操作数据的工具类
    public void onSourceAdd(MediaSSRCMap addedSSRCs) {
        SessionDescription rsd = VideoImplement.videoImplement.getRemoteDescription();// 获取C的会议简介数据对象
        Log.d(TAG, "会议简介加SSRC前-->会议描述=" + rsd.description);
        SessionDescription modifiedOffer = JingleToSdp.addSSRCs(rsd, addedSSRCs);// 对会议简介数据拼接成具有SSRC数据的会议简介对象
        Log.d(TAG, "会议简介加SSRC后-->会议描述=" + modifiedOffer.description);

        VideoImplement.videoImplement.setRemoteDescription(modifiedOffer);
    }

    public void onSourceRemove(MediaSSRCMap removedSSRCs) {
        SessionDescription rsd = VideoImplement.videoImplement.getRemoteDescription();
        SessionDescription modifiedOffer = JingleToSdp.removeSSRCs(rsd, removedSSRCs);
        Log.i(TAG, "SOURCE REMOVE OFFER: " + modifiedOffer.description);
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

    @Override
    public void disconnectFromRoom() {
        if (xmppConnection != null) {
            xmppConnection.disconnect();
        }
    }


    @Override
    public void sendLocalIceCandidateRemovals(IceCandidate[] candidates) {

    }

    @Override
    public void onCreateLocalRoom() {
        boolean initiator = false;
        SessionDescription offerSdp = null;
        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();
        LinkedList<IceCandidate> iceCandidates = new LinkedList<>();
        SignalingParameters signalingParameters = new SignalingParameters(iceServers, offerSdp, iceCandidates);
        VideoImplement.videoImplement.onConnectedToRoomLocal(signalingParameters);
    }

    public void onParticipantInfo(Map<String, Participant> participantMap) {
    }
}
