package com.cisco.core.entity;

import org.webrtc.MediaStream;

/**
 * @author linpeng
 */
public class ParticipantMedia {
    private String jid;//每个人的jid
//    private MediaSSRCMap addedSSRCs;//包含ssrc
    private String videoSsrcMedia;
    private String audioSsrcMedia;
    private MediaStream mediaStream;
    private String streamId;//streamId==label 值   <parameter name="label" value="b202d883-5794-4293-9e0e-1d2339cc76a7"></parameter>
    private String cName;

    public String getcName() {
        return cName;
    }

    public void setcName(String cName) {
        this.cName = cName;
    }

    public String getStreamId() {
        return streamId;
    }

    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getVideoSsrcMedia() {
        return videoSsrcMedia;
    }

    public void setVideoSsrcMedia(String videoSsrcMedia) {
        this.videoSsrcMedia = videoSsrcMedia;
    }

    public String getAudioSsrcMedia() {
        return audioSsrcMedia;
    }

    public void setAudioSsrcMedia(String audioSsrcMedia) {
        this.audioSsrcMedia = audioSsrcMedia;
    }

    public MediaStream getMediaStream() {
        return mediaStream;
    }

    public void setMediaStream(MediaStream mediaStream) {
        this.mediaStream = mediaStream;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }


    public ParticipantMedia() {
    }
}
