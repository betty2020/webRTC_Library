/*
 *  Copyright 2013 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.cisco.core;

import com.cisco.core.interfaces.CiscoApiInterface;

import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;

import java.util.List;

public interface AppRTCClient {

    void connectToRoom(CiscoApiInterface.UpdateUIEvents updateEvents);

    void sendOfferSdp(final SessionDescription sdp);

    void sendAnswerSdp(final SessionDescription sdp);

    void sendLocalIceCandidate(final IceCandidate candidate);

    void sendLocalIceCandidateRemovals(final IceCandidate[] candidates);

    void disconnectFromRoom();

    void onCreateLocalRoom();

    class RoomConnectionParameters {
        public final String roomId;
        public final boolean loopback;

        public RoomConnectionParameters(String roomId, boolean loopback) {
            this.roomId = roomId;
            this.loopback = loopback;
        }
    }

    class SignalingParameters {
        public final List<PeerConnection.IceServer> iceServers;
        public final SessionDescription offerSdp;
        public final List<IceCandidate> iceCandidates;

        public SignalingParameters(List<PeerConnection.IceServer> iceServers,
                                   SessionDescription offerSdp,
                                   List<IceCandidate> iceCandidates) {
            this.iceServers = iceServers;
            this.offerSdp = offerSdp;
            this.iceCandidates = iceCandidates;
        }
    }

    interface SignalingEvents {
        void onConnectedToRoomLocal(final SignalingParameters params);//lp add

        void onConnectedToRoomRemote(final SignalingParameters params);//lp add
    }
}
