package com.cisco.core.xmppextension;

import org.jivesoftware.extension.AbstractPacketExtension;

import java.util.ArrayList;
import java.util.List;

public class CallMessageExtension extends AbstractPacketExtension {

    public static final String ELEMENT_NAME = "call";

    public static final String NAMESPACE = "http://igniterealtime.org/protocol/ringing";

    public String type;
    public String roomID;
    public String resource;
    public String nickname;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public CallMessageExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(ELEMENT_NAME).append(" xmlns='" + NAMESPACE)
                .append(" type='" + type)
                .append(" roomID='" + roomID)
                .append(" resource='" + resource)
                .append(" nickname='" + nickname)
                .append("'>");
        buf.append("</").append(ELEMENT_NAME).append(">");
        return buf.toString();
    }





}
