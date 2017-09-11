package com.cisco.core.xmpp.XmppListener;

import org.jivesoftware.smack.packet.Packet;

public interface XmppCallback {
    void callback(Packet packet);
}