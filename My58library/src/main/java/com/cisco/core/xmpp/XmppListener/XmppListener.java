package com.cisco.core.xmpp.XmppListener;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.Packet;

/**
 * @author linpeng
 */
public class XmppListener implements XmppInterface,PacketListener {
    private XMPPConnection connection;
    private XmppCallback callback;

    public XmppListener(XMPPConnection connection) {
        this.connection = connection;
    }
    public void XmppListenerAdapter(XmppCallback callback) {
        this.callback=callback;
    }

    @Override
    public void sendPacket(Packet packet) {
        connection.sendPacket(packet);

    }

    @Override
    public Packet sendPacketAndGetReply(Packet packet) {
        PacketCollector packetCollector
                = connection.createPacketCollector(
                new PacketIDFilter(packet.getPacketID()));

        connection.sendPacket(packet);
        //FIXME: retry allocation on timeout
        Packet response = packetCollector.nextResult(60000);	// BAO

        packetCollector.cancel();


        return response;

    }

    @Override
    public void processPacket(Packet packet) {
        callback.callback(packet);
    }
}
