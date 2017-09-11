package com.cisco.core.xmpp;

import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.interfaces.CiscoApiInterface;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * @author linpeng
 *         聊天消息监听
 */

public class MucMessageListener implements PacketListener {
    private final String endpoint;
    private CiscoApiInterface.UpdateUIEvents updateEvents;

    public MucMessageListener(CiscoApiInterface.UpdateUIEvents updateEvents, String endpoint) {
        this.updateEvents = updateEvents;
        this.endpoint = endpoint;
    }

    @Override
    public void processPacket(Packet packet) {
        Log.d("XMPP", "<-addMessageListener" + packet.toXML());
        Message message = (Message) packet;
        String infoText = message.getBody();
        String friendName = message.getFrom();
        String xmlns = message.getXmlns();
        String isonly = friendName.substring(friendName.indexOf("/") + 1, friendName.length());
        if (isonly.equals("focus")) {
            Log.d("XMPP", "<-addMessageListener_infoText" + infoText);
            //会控发的消息
            // 会控操作
            // {"allaudio":false,"allvideo":true,"muted":false,"videoClosed":false,"userJid":null,
            // "lock":false,"hangup":false,"msg":null,"role":null,"toClose":false,"lostModerator":false}
            JSONObject rootNode = JSON.parseObject(infoText);
            boolean allaudio = Boolean.parseBoolean(rootNode.getString("allaudio"));
            boolean allvideo = Boolean.parseBoolean(rootNode.getString("allvideo"));
            boolean muted = Boolean.parseBoolean(rootNode.getString("muted"));
            boolean videoClosed = Boolean.parseBoolean(rootNode.getString("videoClosed"));
            String userJid = rootNode.getString("userJid");
            boolean lock = Boolean.parseBoolean(rootNode.getString("lock"));

            boolean hangup = Boolean.parseBoolean(rootNode.getString("hangup"));
            String msg = rootNode.getString("msg");
            boolean role = Boolean.parseBoolean(rootNode.getString("role"));
            boolean toClose = Boolean.parseBoolean(rootNode.getString("toClose"));
            boolean lostModerator = Boolean.parseBoolean(rootNode.getString("lostModerator"));
            if (msg != null) {
                if (updateEvents != null) {
                    updateEvents.IMMessageRecever(msg, "focus");
                }
            } else {
                updateEvents.beMeetingSuperAdministratorControl(allaudio, allvideo, muted, videoClosed, hangup, userJid, lostModerator);
            }
        } else if (!isonly.equals(endpoint)) {
            if (updateEvents != null) {
                if (!"".equals(infoText)) {
                    updateEvents.IMMessageRecever(infoText, isonly);
                    if (MucParticipantListener.participantMap.size() > 0) {
                        updateEvents.IMMessageRecever(infoText, MucParticipantListener.participantMap.get(isonly).getNickname());
                    }
                }
            }
        }
    }
}
