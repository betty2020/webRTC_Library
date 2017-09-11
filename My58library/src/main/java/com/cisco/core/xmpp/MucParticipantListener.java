package com.cisco.core.xmpp;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.PeerConnectionClient;
import com.cisco.core.entity.Participant;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.xmppextension.XExtension;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.MUCUser;
import org.webrtc.Logging;
import org.webrtc.MediaStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author linpeng
 *         聊天消息监听
 */

public class MucParticipantListener implements PacketListener {
    public static ConcurrentMap<String, Participant> participantMap = new ConcurrentHashMap<>();// userid, Participant

    private String TAG = "MucParticipantListener";
    private CiscoApiInterface.UpdateUIEvents updateEvents;

    public MucParticipantListener(CiscoApiInterface.UpdateUIEvents updateEvents) {
        this.updateEvents = updateEvents;
    }

    @Override
    public void processPacket(Packet packet) {
        Log.d("XMPP", "<-addMessageListener" + packet.toXML());
        Presence msg = (Presence) packet;
        String resultName = msg.getFrom();
        String name = resultName.substring(resultName.indexOf("/") + 1, resultName.length());//例如 meet123  android123 ios123
        Log.d("XMPP", "<-addParticipantListener getExtensions=" + msg.getExtensions().size() + ",name=" + name);
        String type = msg.getType().toString();
        Participant pc = new Participant();
        if (msg.getExtensions().size() == 1) {
            if (!name.equals("focus")) {
                MUCUser mucUser = (MUCUser) msg.getExtension("x", "http://jabber.org/protocol/muc#user");
                String jid = mucUser.getItem().getJid();
                String userid = jid.substring(jid.indexOf("/") + 1, jid.length());
                if ("".equals(Key.afferent_nickname)) {
                    //用于健康云是否传入昵称接口
                    pc.setNickname(XmppConnection.getInstance().username);
                } else {
                    pc.setNickname(Key.afferent_nickname);
                }
                pc.setUserid(name);
                pc.setJid(jid);
                pc.setHost(Key.Moderator);
//                participantMap.put(userid, pc);
                participantMap.put(jid, pc);
            }
        }
        if (msg.getExtensions().size() == 2) {
            XExtension x = (XExtension) msg.getExtension(XExtension.NAMESPACE);
            Log.d("XMPP", "<-addParticipantListener---------------------------------------------------------------=");
            Log.d("XMPP", "<-addParticipantListener-x=" + x.toXML());
            MUCUser mucUser = (MUCUser) msg.getExtension("x", "http://jabber.org/protocol/muc#user");
            String nick = x.getNickValue();// 例如 linpeng
            Log.d("XMPP", "<-addParticipantListener x.getConfigRoleValue()=" + x.getConfigRoleValue() + ",nick=" + nick);
            String ishost = x.getConfigRoleValue() != null ? x.getConfigRoleValue() : null;
            String jid = mucUser.getItem().getJid();
            String userid = jid.substring(jid.indexOf("/") + 1, jid.length());
            String audioMuted = x.getAudioMutedValue();
            List<XExtension.Source> list = x.getSources();
            Log.d("XMPP", "<-addParticipantListener-x_list=" + list.size());
            for (XExtension.Source source : list) {
                String type1 = source.getType();
                String ssrc = source.getSsrc();
                Log.d("XMPP", "<-addParticipantListener-x_type1=" + type1 + ",ssrc=" + ssrc);
            }

            pc.setNickname(nick);
            pc.setUserid(name);
            pc.setJid(jid);
            if (audioMuted == null) {
                pc.setGetMuteMic(false);
            } else {
                if (audioMuted.equals("false")) {
                    pc.setGetMuteMic(false);
                } else if (audioMuted.equals("true")) {
                    pc.setGetMuteMic(true);
                }
            }
            if (ishost != null) {
                if (ishost.equals("CRUO") || ishost.equals("CRUA")) {
                    //是主持人
                    pc.setHost(true);
                } else if (ishost.equals("NONE")) {
                    //不是主持人
                    pc.setHost(false);
                }
            } else {
                //不是主持人
                pc.setHost(false);
            }
            boolean isScreen = false;
            for (XExtension.Source source : list) {
                String type1 = source.getType();
                String ssrc = source.getSsrc();
                Log.d("XMPP", "<-addParticipantListener-x_type1=" + type1 + ",ssrc=" + ssrc + ",indexof=" + list.indexOf(source));
                if (list.indexOf(source) == 2) {
                    //说明是双流
//                    isScreen = true;
                    userid = userid + "_screen";
                    pc.setJid(jid + "_screen");
                }
                String label = XmppConnection.ssrcAndLabel.get(ssrc);
                Log.i(TAG, "-ssrcAndLabel_xmpp _label=" + label);
                if (!TextUtils.isEmpty(label)) {
                    MediaStream videoStream = XmppConnection.streamLabelToUserId.remove(label);
                    Log.i(TAG, "-ssrcAndLabel_xmpp _videoStream=" + videoStream);
                    if (videoStream != null) {
                        Log.i(TAG, "-ssrcAndLabel_xmpp _videoStream.videoTracks.isEmpty()=" + videoStream.videoTracks.isEmpty());
                        Log.i(TAG, "-ssrcAndLabel_xmpp _come on " + pc.getJid());
                        XmppConnection.streamLabelToUserJId.put(label, jid);
//                        pc.setScreen(isScreen);
                        pc.setStream(videoStream);
                        PeerConnectionClient.getInstance().addSurfaceView(pc, videoStream);
                    }
                }
            }
            participantMap.put(userid, pc);
        }
        if (type.equals("unavailable")) {

            XExtension x = (XExtension) msg.getExtension(XExtension.NAMESPACE);
            String ishost = x.getConfigRoleValue() != null ? x.getConfigRoleValue() : null;
            Log.i(TAG, "-unavailable——ishost=" + ishost);
            if (ishost != null) {
                if (ishost.equals("CRUO") || ishost.equals("CRUA")) {
                    //是主持人
                    updateEvents.OnHostLeave();
                }
            }

            Participant pcc = participantMap.remove(name);
            PeerConnectionClient.getInstance().removeSurfaceView(pcc.getJid());
        }
        //实时更新人员列表数据
        updateList(participantMap, type);
    }

    /***
     * 更新人员列表
     */
    private void updateList(Map<String, Participant> participantMap, String type) {
        //实时更新人员列表数据
        List<Participant> listParticipant = new ArrayList<Participant>(participantMap.values());
        updateEvents.updatePartcipantList(listParticipant);
    }

}
