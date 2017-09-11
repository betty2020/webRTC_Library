package com.cisco.core.xmpp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import com.cisco.core.MeetRTCClient;
import com.cisco.core.PeerConnectionClient;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.meet.JingleToSdp;
import com.cisco.core.meet.SdpToJingle;
import com.cisco.core.meet.util.MediaSSRCMap;
import com.cisco.core.meet.util.SdpSsrcVariable;
import com.cisco.core.meet.util.SmackInit;
import com.cisco.core.xmpp.XmppListener.XmppListener;
import com.cisco.core.xmppextension.AppShareExtension;
import com.cisco.core.xmppextension.CallMessageExtension;
import com.cisco.core.xmppextension.ConferenceIq;
import com.cisco.core.xmppextension.MuteIQ;

import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.SourcePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jitsimeet.MediaPresenceExtension;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.Nick;
import org.webrtc.IceCandidate;
import org.webrtc.MediaStream;
import org.webrtc.SessionDescription;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class XmppConnection implements PacketListener {
    private String xmppDomain;//xmpp url
    private String roomURL;
    private String endpoint;// android+随机数
    public String username;// 用户名
    private String userId;
    private String password;
    private int PORT = 5222;
    private static XMPPConnection connection = null;
    private static XmppConnection xmppConnect = new XmppConnection();
    private static final String TAG = "XmppConnection";
    public MultiUserChat muc;
    private MeetRTCClient rtcClient;
    private String offererJid = null;
    private String sid;
    private String StirngXml;
    private Timer mTimer2;
    private TimerTask mTimerTask2;
    private XmppConnecionListener xmppConnecionListener;
    private Timer tExit;
    private CiscoApiInterface.UpdateUIEvents updateEvents;
    public String configRole = "NONE";
    //    public static ConcurrentMap<String, Participant> participantMap = new ConcurrentHashMap<>();// userid, Participant
    public static Map<String, MediaStream> streamLabelToUserId = new ConcurrentHashMap<>();// streamId, ResourceId
    public static Map<String, String> streamLabelToUserJId = new ConcurrentHashMap<>();// streamId, ResourceId
    public static Map<String, String> ssrcAndLabel = new ConcurrentHashMap<>();// ssrc, laber

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();


    private MucMessageListener mucMessageListener;//聊天消息监听；
    private MucParticipantListener mucParticipantListener;//聊天室中成员监听；

    private XmppConnection() {
    }

    /**
     * 创建单实例
     *
     * @return
     */
    synchronized public static XmppConnection getInstance() {
        return xmppConnect;
    }

    /**
     * 获取连接
     */
    public XMPPConnection getConnection() {
        if (connection == null || !connection.isConnected()) {
            openConnection();
        }
        return connection;
    }

    /**
     * 打开连接
     */
    private boolean openConnection() {
        try {
            SmackInit.init();
            if (null == connection || !connection.isAuthenticated()) {
                ConnectionConfiguration config = new ConnectionConfiguration(xmppDomain, PORT, xmppDomain);//非法参数异常
                config.setSASLAuthenticationEnabled(true);// 是否启用安全验证
                config.setSecurityMode(ConnectionConfiguration.SecurityMode.enabled);
                config.setReconnectionAllowed(true);//自动重连
                config.setSendPresence(true); // 状态设为离线，目的为了取离线消息
                config.setDebuggerEnabled(true); // 调试模式
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    config.setTruststoreType("AndroidCAStore");
                    config.setTruststorePassword(null);
                    config.setTruststorePath(null);
                } else {
                    config.setTruststoreType("BKS");
                    String path = System.getProperty("javax.net.ssl.trustStore");
                    if (path == null)
                        path = System.getProperty("java.home") + File.separator + "etc"
                                + File.separator + "security" + File.separator
                                + "cacerts.bks";
                    config.setTruststorePath(path);
                }
                connection = new XMPPConnection(config);
                connection.addPacketListener(
                        new PacketListener() {
                            @Override
                            public void processPacket(Packet packet) {
                                logLongString("XMPP", "<-----jilinpeng- " + packet.toXML());
                                if (packet instanceof ConferenceIq) {
                                    if (((ConferenceIq) packet).isReady()) {
                                        joinRoom();
                                    } else {
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    Thread.sleep(300);
                                                    sendConferenceIQ();
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }).start();

                                    }
                                }
                                //静音
                                if (packet instanceof MuteIQ) {
                                    String message = ((MuteIQ) packet).getMessage();
                                    if (message.equals("true")) {
                                        updateEvents.beHostToOperate(true, 1);
                                    } else {
                                        updateEvents.beHostToOperate(false, 1);
                                    }
                                }
                                //剔除
                                if (packet instanceof Message) {
                                    Message m = (Message) packet;
                                    if (m.toXML().contains("body")) {
                                        Message.Body me = m.getBodys();
                                        String message = me.getMessage();
                                        String xmlns = me.getXmlns();
                                        logLongString("XMPP", "<--message- " + m.toXML());
                                        if (xmlns.equals("http://igniterealtime.org/protocol/eject")) {
                                            //被主持人剔除
                                            updateEvents.beHostToOperate(true, 3);
                                        } else if (xmlns.equals("http://igniterealtime.org/protocol/grantAdmin")) {
                                            //移交主持人
                                            String hostJid = me.getJid();
                                            if (hostJid.equals(Key.roomnumber + "@conference." + xmppDomain + "/" + endpoint)) {
                                                configRole = "CRUO";
                                                Key.Moderator = true;
                                                SendPresenceMessage();
                                                updateEvents.beHostToOperate(true, 4);
                                            }
                                        } else if (xmlns.equals("http://igniterealtime.org/protocol/stage")) {
                                            //上主屏
                                            String jid = me.getJid();
                                            Log.i(TAG, "SOURCE ADD 上主屏jid: " + jid);
                                            updateEvents.OnTheMainScreen(jid);
                                        } else if (xmlns.equals("http://igniterealtime.org/protocol/ringing")) {
                                            Call(packet, m);//web端呼叫带 body
                                        }

                                        //SIP 回拨我
                                        CallMessageExtension cme = (CallMessageExtension) packet.getExtension(CallMessageExtension.ELEMENT_NAME, CallMessageExtension.NAMESPACE);
                                        if (cme != null) {
                                            logLongString("XMPP", "<--cme-- " + cme.toXML());
                                            String type = cme.getType();
                                            updateEvents.sipDialBack();
                                        }

                                    } else if (m.toXML().contains("appshare")) {
                                        //共享白板
                                        AppShareExtension ase = (AppShareExtension) m.getExtension(AppShareExtension.ELEMENT_NAME, AppShareExtension.NAMESPACE);
                                        logLongString("XMPP", "linpeng,<--ase--共享白板 " + ase.toXML());
                                        String action = ase.getAction().trim();
                                        String url = ase.getUrl().trim();
                                        updateEvents.onWhiteBoard(action, url);
                                    } else if (m.toXML().contains("call")) {
                                        Call(packet, m);//ios呼叫我 不带body
                                    }
                                }
                            }
                        },
                        new PacketFilter() {
                            @Override
                            public boolean accept(Packet packet) {
                                return true;
                            }
                        });

                connection.addPacketSendingListener(new PacketListener() {
                    @Override
                    public void processPacket(Packet packet) {

                    }
                }, new PacketFilter() {
                    @Override
                    public boolean accept(Packet packet) {
                        return true;
                    }
                });

                connection.addPacketListener(this, new PacketFilter() {
                    public boolean accept(Packet packet) {
                        return (packet instanceof JingleIQ);
                    }
                });
                connection.connect();
                if (connection.isConnected()) {
                    xmppConnecionListener = new XmppConnecionListener();
                    connection.addConnectionListener(xmppConnecionListener);//连接监听
                }
                ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
                discoManager.addFeature("urn:xmpp:delay");
                discoManager.addFeature("urn:xmpp:jingle:1");
                discoManager.addFeature("urn:xmpp:jingle:apps:rtp:1");
                discoManager.addFeature("urn:xmpp:jingle:apps:rtp:audio");
                discoManager.addFeature("urn:xmpp:jingle:apps:rtp:video");
                discoManager.addFeature("urn:xmpp:jingle:transports:ice-udp:1");
                discoManager.addFeature("urn:xmpp:jingle:transports:dtls-sctp:1");
                discoManager.addFeature("urn:ietf:rfc:5761");
                discoManager.addFeature("urn:ietf:rfc:5888");
                discoManager.addFeature("urn:xmpp:jingle:apps:rtp:rtcp-fb:0");
                discoManager.addFeature("urn:xmpp:jingle:apps:rtp:rtp-hdrext:0");
                discoManager.addFeature("http://igniterealtime.org/protocol/ofmeet");
                discoManager.addFeature("http://jabber.org/protocol/nick");
                discoManager.addFeature("http://jitsi.org/jitmeet/stats");
                discoManager.addFeature("http://estos.de/ns/mjs");
                discoManager.addFeature("http://jabber.org/protocol/muc#user");
                discoManager.addFeature("http://jabber.org/protocol/disco#info");
                discoManager.addFeature("http://igniterealtime.org/protocol/ringing");
                return true;
            }
        } catch (XMPPException xe) {
            Log.e("fail", "连接失败");
            xe.printStackTrace();
            connection = null;
        }
        return false;
    }

    private void Call(Packet packet, Message m) {
        //SIP 回拨我/好友呼叫我
        CallMessageExtension cme = (CallMessageExtension) packet.getExtension(CallMessageExtension.ELEMENT_NAME, CallMessageExtension.NAMESPACE);
        if (cme != null) {
            logLongString("XMPP", "<--cme--" + cme.toXML());
            String type = cme.getType();
            //updateEvents.sipDialBack();//sip回掉接口
            logLongString("XMPP", "<--cme--muc=" + muc + ",type=" + type + ",onIMCallEvents=" + onIMCallEvents);
            String roomID = cme.getRoomID();
            String resource = cme.getResource();
            String friendid = m.getFrom();
            String nickname = cme.getNickname();

            if (onIMCallEvents != null) {
                if (type.equals("accept")) {
                    onIMCallEvents.acceptCallback(roomID, friendid, resource);
                } else if (type.equals("reject")) {
                    onIMCallEvents.rejectCallback(roomID, friendid, resource);
                } else if (type.equals("invite")) {
                    if (muc == null) {
                        onIMCallEvents.inviteCallback(roomID, friendid, resource);
                    } else {
                        type = "busy";//被叫开会中，像主叫发他在忙
                        SendCallFriend(friendid, type, roomID);
                    }
                } else if (type.equals("busy")) {
                    //被叫正在会议中，
//                    onIMCallEvents.busyCallback(endpoint);
                }

            }
        }
    }

    /*
     * 登录
     */
    public boolean login(String username, String password, String endpoint, String xmppurl, String userId) {
        this.username = username;
        this.endpoint = endpoint;
        this.userId = userId;
        this.xmppDomain = xmppurl;
        this.password = password;
        // Log.e("Xmappconnection", "connection.=" + connection);
        try {
            closeConnection();
            if (getConnection() == null)
                return false;
            getConnection().login(userId, password, endpoint);
            return true;
        } catch (XMPPException e) {
            Log.e("Xmappconnection", "fail!, Not connected to server, close connection");
            closeConnection();
            e.printStackTrace();
        } catch (IllegalStateException e) {
            Log.e("Xmappconnection", "Not connected to server.=");
        }
        return false;
    }

    public void closeConnection() {
        if (connection != null && connection.isConnected()) {
            connection.disconnect();
            connection = null;
        } else {
            connection = null;
        }
    }

    public void disconnect() {
        if (muc != null) {
//            muc.leave();
            Presence leavePresence = new Presence(Presence.Type.unavailable);
            leavePresence.setTo(roomURL);
            leavePresence.setFrom(Key.roomnumber + "@conference." + this.xmppDomain + "/" + endpoint);
            connection.sendPacket(leavePresence);//离开会议
            Key.clear();//清空人员集合
            muc.removeMessageListener(mucMessageListener);//移除聊天消息监听
            muc.removeParticipantListener(mucParticipantListener);//移除成员监听
            muc = null;
        }
        if (mTimer2 != null) {
            mTimer2.cancel();
            mTimerTask2.cancel();
        }
        if (xmppConnecionListener != null) {
            connection.removeConnectionListener(xmppConnecionListener);
        }
        if (Key.isguest) {
            closeConnection();
        }
        SdpSsrcVariable.audiomuted = false;
        SdpSsrcVariable.videomuted = false;
        Log.e("Xmappconnection", "linpeng,disconnect_connection.=" + connection + ",muc=" + muc);
    }

    /*
     * 加入会议
     */
    public void joinMuc(MeetRTCClient rtcClient, String roomURL, final CiscoApiInterface.UpdateUIEvents updateEvents) {
        this.rtcClient = rtcClient;
        this.roomURL = roomURL;
        this.updateEvents = updateEvents;
        //手机第一个进入及时会议 更改主持人状态
        if (Key.Moderator) {
            configRole = "CRUO";
        } else {
            configRole = "NONE";
        }
        if (!connection.isConnected()) {
            return;
        }
        String afferent_nickname = "";
        if ("".equals(Key.afferent_nickname)) {
            //用于健康云是否传入昵称接口
            afferent_nickname = username;

        } else {
            afferent_nickname = Key.afferent_nickname;
        }
        Packet presencePacket = new Presence(Presence.Type.available);
        presencePacket.setTo(connection.getUser());

        presencePacket.addExtension(new Nick(afferent_nickname));
        connection.sendPacket(presencePacket);

        // 发送videobridge消息
        sendConferenceIQ();
    }


    public void sendConferenceIQ() {
        final Packet videoBridgeXml = SendVideoBridge(roomURL, Key.vb, xmppDomain);
//        connection.sendPacket(videoBridgeXml);
        final XmppListener xl = new XmppListener(connection);
        xl.sendPacketAndGetReply(videoBridgeXml);
    }

    public void joinRoom() {
        Log.d("", "--------------joinRoom-------------");
        try {
            String afferent_nickname = "";
            if ("".equals(Key.afferent_nickname)) {
                //用于健康云是否传入昵称接口
                afferent_nickname = username;
            } else {
                afferent_nickname = Key.afferent_nickname;
            }
            muc = new MultiUserChat(connection, roomURL);
            muc.addPresenceInterceptor(new PresenceInterceptor());
            // 发送presence消息   会控端显示自己名字
            final Packet xml = SendExtension(endpoint, afferent_nickname);
            connection.sendPacket(xml);
            // 聊天室服务将会决定要接受的历史记录数量
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxChars(0);
//            muc.join(endpoint);
            mTimer2 = new Timer();
            mTimerTask2 = new TimerTask() {
                @Override
                public void run() {
                    Packet presencePacket = new Presence(
                            Presence.Type.available);
                    getConnection().sendPacket(presencePacket);
                }
            };
            mTimer2.schedule(mTimerTask2, 10000, 10000);
            // webrtc create本地流
            rtcClient.onCreateLocalRoom();
        } catch (Exception e) {
            Log.e(TAG, endpoint + " : could not enter MUC " + e);
        }
        // 成员监听
        mucParticipantListener = new MucParticipantListener(updateEvents);
        muc.addParticipantListener(mucParticipantListener);
        // message监听
        mucMessageListener = new MucMessageListener(updateEvents, endpoint);
        muc.addMessageListener(mucMessageListener);
        muc.addPresenceInterceptor(new PresenceInterceptor());
    }

    /**
     * 查询会议室成员名字 * @param muc 人员列表
     */
    public List<String> findMulitUser() {
        List<String> listUser = new ArrayList<String>();
        Iterator<String> it = muc.getOccupants(); // 遍历出聊天室人员名称

        while (it.hasNext()) { // 聊天室成员名字
            String name = StringUtils.parseResource(it.next());
            if (!name.equals("focus")) {
                listUser.add(name);
            }
        }
        return listUser;
    }

    @Override
    public void processPacket(Packet packet) {
        try {
            JingleIQ jiq = (JingleIQ) packet;
            ackJingleIQ(jiq);
            switch (jiq.getAction()) {
                case SESSION_INITIATE:
                    Log.i(TAG, " : Jingle session-initiate received");
                    Log.i(TAG, "SOURCE SESSION_INITIATE: " + jiq.toXML());
                    offererJid = jiq.getFrom();
                    sid = jiq.getSID();
                    SessionDescription bridgeOfferSdp = JingleToSdp.toSdp(jiq,
                            SessionDescription.Type.OFFER);
                    Log.i(TAG, "linpeng,, bridgeOfferSdp: " + bridgeOfferSdp.description);
                    rtcClient.acceptSessionInit(bridgeOfferSdp);
                    break;
                case SOURCEADD:
                case ADDSOURCE:
                    Log.i(TAG, "SOURCE ADD: ----" + jiq.toXML());
                    final MediaSSRCMap addedSSRCs = MediaSSRCMap.getSSRCsFromContent(jiq
                            .getContentList());
                    String labelValue = addedSSRCs.getSsrcs().get("video").get(0).getParameter("label");// 参数
                    String mslabelValue = addedSSRCs.getSsrcs().get("video").get(0).getParameter("mslabel");// 参数
                    if (!mslabelValue.equals("mixedmslabel")) {
                        Long videoSSRCs = addedSSRCs.getSsrcs().get("video").get(0).getSSRC();
                        Log.i(TAG, "-ssrcAndLabel_add _ssrc=" + videoSSRCs + ",label=" + labelValue);
                        ssrcAndLabel.put("" + videoSSRCs, labelValue);
                    }

                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            rtcClient.onSourceAdd(addedSSRCs);
                        }
                    });
                    break;
                case REMOVESOURCE:
                case SOURCEREMOVE:
                    Log.i(TAG, "REMOVE SOURCE: " + jiq.toXML());
                    final MediaSSRCMap removedSSRCs = MediaSSRCMap
                            .getSSRCsFromContent(jiq.getContentList());
                    final String removelabelValue = removedSSRCs.getSsrcs().get("video").get(0).getParameter("label");// 参数

                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            String jid = XmppConnection.streamLabelToUserJId.get(removelabelValue);
                            PeerConnectionClient.getInstance().removeSurfaceView(jid);
                            rtcClient.onSourceRemove(removedSSRCs);
                        }
                    });
                    break;
                default:
                    System.err.println(" : Unknown Jingle IQ received : "
                            + jiq.toString());
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error", e);
        }
    }

    /**
     * This function simply create an ACK packet to acknowledge the Jingle IQ
     * packet <tt>packetToAck</tt>.
     *
     * @param packetToAck the <tt>JingleIQ</tt> that need to be acknowledge.
     */
    private void ackJingleIQ(JingleIQ packetToAck) {
        if (packetToAck.getType().equals("error")) {
            //linpeng add
            updateEvents.onIceFailed();
        } else {
            IQ ackPacket = IQ.createResultIQ(packetToAck);
            connection.sendPacket(ackPacket);
        }

    }

    public void sendSessionAccept(SessionDescription sdp) {
        JingleIQ sessionAccept = SdpToJingle.toJingle(sdp);
        sessionAccept.setTo(offererJid);
        sessionAccept.setSID(sid);
        sessionAccept.setType(IQ.Type.SET);
        sessionAccept.setInitiator(connection.getUser());// linpeng add
        Log.i(TAG, sessionAccept.toXML());
        addStreamsToPresence(sessionAccept.getContentList());
        connection.sendPacket(sessionAccept);
        SendPresenceMessage();//发送presence消息
    }

    public Packet SendVideoBridge(String roomURL, String videobridge,
                                  String xmppurl) {
        final String videoBridgeXml = XmppXml.SendVideoBridge(roomURL,
                videobridge, xmppurl);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return videoBridgeXml;
            }
        };
        return xmlPacket;
    }

    // 发送presenece
    public void SendPresenceMessage() {
        String afferent_nickname = "";
        if ("".equals(Key.afferent_nickname)) {
            afferent_nickname = username;
        } else {
            afferent_nickname = Key.afferent_nickname;
        }
        StirngXml = XmppXml.presenceMessage(roomURL, userId,
                afferent_nickname, SdpSsrcVariable.audiomuted, SdpSsrcVariable.videomuted, SdpSsrcVariable.ice_cd1,
                SdpSsrcVariable.ice_cd2,
                SdpSsrcVariable.googAvailableReceiveBandwidth,
                SdpSsrcVariable.googAvailableSendBandwidth, SdpSsrcVariable.packetLoss_download, SdpSsrcVariable.packetLoss_upload, configRole);
        logLongString("XMPP", "--SendPresenceMessage--2> " + StirngXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return StirngXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }

    private void addStreamsToPresence(List<ContentPacketExtension> contentList) {
        MediaSSRCMap mediaSSRCs = MediaSSRCMap.getSSRCsFromContent(contentList);
        MediaPresenceExtension mediaPresence = new MediaPresenceExtension();
        for (SourcePacketExtension audioSSRC : mediaSSRCs.getSSRCsForMedia("audio")) {
            MediaPresenceExtension.Source ssrc = new MediaPresenceExtension.Source();
            ssrc.setMediaType("audio");
            ssrc.setSSRC(String.valueOf(audioSSRC.getSSRC()));
            mediaPresence.addChildExtension(ssrc);
        }

        for (SourcePacketExtension videoSSRC : mediaSSRCs.getSSRCsForMedia("video")) {
            MediaPresenceExtension.Source ssrc = new MediaPresenceExtension.Source();
            ssrc.setMediaType("video");
            ssrc.setSSRC(String.valueOf(videoSSRC.getSSRC()));

            mediaPresence.addChildExtension(ssrc);
        }

        sendPresenceExtension(mediaPresence);
    }

    private Presence lastPresenceSent = null;

    public String getXmppDomain() {
        return xmppDomain;
    }


    private class PresenceInterceptor implements PacketInterceptor {
        /**
         * {@inheritDoc}
         * <p/>
         * Adds <tt>this.publishedConferenceExt</tt> as the only
         * <tt>ConferenceAnnouncementPacketExtension</tt> of <tt>packet</tt>.
         */
        @Override
        public void interceptPacket(Packet packet) {
            if (packet instanceof Presence) {
                lastPresenceSent = (Presence) packet;
            }
        }
    }

    /**
     * Adds given <tt>PacketExtension</tt> to the MUC presence and publishes it
     * immediately.
     *
     * @param extension the <tt>PacketExtension</tt> to be included in MUC presence.
     */
    public void sendPresenceExtension(PacketExtension extension) {
        if (lastPresenceSent != null) {
            setPacketExtension(lastPresenceSent, extension,
                    extension.getNamespace());

            connection.sendPacket(lastPresenceSent);
        }
    }

    @SuppressLint("NewApi")
    private static void setPacketExtension(Packet packet,
                                           PacketExtension extension, String namespace) {
        if (namespace == null || namespace.isEmpty()) {
            return;
        }

        // clear previous announcements
        PacketExtension pe;
        while (null != (pe = packet.getExtension(namespace))) {
            packet.removeExtension(pe);
        }

        if (extension != null) {
            packet.addExtension(extension);
        }
    }

    public void sendTransportInfo(IceCandidate candidate) {
        JingleIQ iq = SdpToJingle.createTransportInfo(offererJid, candidate);
        Log.i(TAG, "transport-info-sid: " + sid);
        Log.i(TAG, "transport-info-offererJid: " + offererJid);
        if (iq != null) {
            iq.setSID(sid);
            // iq.setInitiator(offererJid);//add
            connection.sendPacket(iq);
            Log.i(TAG, "transport-info: " + iq.toXML());
        }

    }

    private void logLongString(String tag, String message) {
        int partLen = 1024;
        if (message.length() < partLen) {
            Log.d(tag, message);
        } else {
            int parts = message.length() / partLen;
            int mod = message.length() % partLen;
            for (int i = 0; i < parts; i++) {
                Log.d(tag,
                        "PART "
                                + i
                                + ": "
                                + message.substring(i * partLen, (i + 1)
                                * partLen));
            }
            if (mod > 0) {
                Log.d(tag,
                        "PART "
                                + parts
                                + ": "
                                + message.substring(parts * partLen,
                                message.length()));
            }
        }
    }

   /* **
     * 连接异常监听
*/

    private class XmppConnecionListener implements ConnectionListener {
        @Override
        public void connectionClosed() {
            Log.i(TAG, "ConnectionListener-connectionClosed");
            try {
                disconnect();
            } catch (Exception e) {
                Log.i(TAG, "ConnectionListener-connectionClosed=" + e.toString());
            } finally {
                tExit = new Timer();
                tExit.schedule(new XmppTimetask(), 5000);
            }
        }

        @Override
        public void connectionClosedOnError(Exception e) {
            Log.i(TAG, "ConnectionListener-connectionClosedOnError=" + e.toString());
            boolean error = e.getMessage().equals("stream:error (conflict)");
            if (!error) {
                try {
                    disconnect();
                } catch (Exception e2) {
                    Log.i(TAG, "ConnectionListener-e=" + e2.toString());
                    updateEvents.onIceFailed();
                } finally {
                    tExit = new Timer();
                    tExit.schedule(new XmppTimetask(), 5000);
                }
            }
        }

        @Override
        public void reconnectingIn(int seconds) {
            Log.i(TAG, "ConnectionListener-reconnectingIn");
        }

        @Override
        public void reconnectionSuccessful() {
            Log.i(TAG, "ConnectionListener-reconnectionSuccessful");
        }

        @Override
        public void reconnectionFailed(Exception e) {
            Log.i(TAG, "ConnectionListener-reconnectionFailed");
        }

    }

    /**
     * 重连
     */
    private class XmppTimetask extends TimerTask {

        public XmppTimetask() {
        }

        @Override
        public void run() {
            try {
                getConnection().login(userId, password, endpoint);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * CreateSubmitform
     */
    private void CreateSubmitform() throws XMPPException {
        // // 获得聊天室的配置表单
        Form form = muc.getConfigurationForm();
        // 根据原始表单创建一个要提交的新表单。
        Form submitForm = form.createAnswerForm();
        // 向要提交的表单添加默认答复
        for (Iterator<FormField> fields = form.getFields(); fields
                .hasNext(); ) {
            FormField field = (FormField) fields.next();
            if (!FormField.TYPE_HIDDEN.equals(field.getType())
                    && field.getVariable() != null) {
                // 设置默认值作为答复
                submitForm.setDefaultAnswer(field.getVariable());
            }
        }
        // 设置聊天室的新拥有者
        // List<String> owners = new ArrayList<String>();
        // owners.add(connection.getUser());// 用户JID
        // submitForm.setAnswer("muc#roomconfig_roomowners",
        // owners);
        // 设置聊天室是持久聊天室，即将要被保存下来
        submitForm.setAnswer("muc#roomconfig_persistentroom", false);
        // 房间仅对成员开放
        submitForm.setAnswer("muc#roomconfig_membersonly", false);
        // 允许占有者邀请其他人
        submitForm.setAnswer("muc#roomconfig_allowinvites", true);
        // 进入是否需要密码
        // submitForm.setAnswer("muc#roomconfig_passwordprotectedroom",
        // true);
        // 设置进入密码
        // submitForm.setAnswer("muc#roomconfig_roomsecret",
        // "password");
        // 能够发现占有者真实 JID 的角色
        // submitForm.setAnswer("muc#roomconfig_whois", "anyone");
        // 登录房间对话
        submitForm.setAnswer("muc#roomconfig_enablelogging", true);
        // 仅允许注册的昵称登录
        submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
        // 允许使用者修改昵称
        submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
        // 允许用户注册房间
        submitForm.setAnswer("x-muc#roomconfig_registration", false);
        // 发送已完成的表单（有默认值）到服务器来配置聊天室
        submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", true);
        // 发送已完成的表单（有默认值）到服务器来配置聊天室
        muc.sendConfigurationForm(submitForm);
    }

    /***
     * 发送呼叫sip iq消息
     */
    public void SendCallSipByIq(String sipNumber) {
        final String callSipXml = XmppXml.SendCallSip(roomURL, connection.getUser(), sipNumber);
        System.out.println("<-callSipXml-" + callSipXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return callSipXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }

    /***
     * 发送授权主持人消息
     */
    public void SendGrantAdminByMessage(String coverJid, String coverUserid) {
        final String grantAdminXml = XmppXml.SendGrantAdmin(endpoint, coverJid, coverUserid, this.xmppDomain);
        System.out.println("<-grantAdminXml-" + grantAdminXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return grantAdminXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }

    /***
     * 主持人发送踢人 message消息
     */
    public void SendTirenByMessage(String coverTirenid) {
        final String tirenXml = XmppXml.SendTiren(coverTirenid, connection.getUser(), this.xmppDomain);
        System.out.println("<-tirenXml-" + tirenXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return tirenXml;
            }
        };
        connection.sendPacket(xmlPacket);
        //踢人的同时 移除此人
//        participantMap.remove(coverTirenid);
    }

    /***
     * 主持人发送静音某一成员 message消息
     */
    public void SendMuteMessage(String coverMuteid, boolean isMute) {
        if (connection != null) {
            final String muteXml = XmppXml.SendMute(coverMuteid, connection.getUser(), isMute, xmppDomain);
            System.out.println("<-muteXml-" + muteXml);
            final Packet xmlPacket = new Packet() {
                @Override
                public String toXML() {
                    return muteXml;
                }
            };
            connection.sendPacket(xmlPacket);
        }
    }

    /***
     * 禁止自己本地视频，同时向服务器端发送iq消息
     */
    public void SendBannedVideo(String coverMuteid, boolean isBannedVideo) {
        final String bannedVideoXml = XmppXml.SendBannedVideo(coverMuteid, connection.getUser(), isBannedVideo, xmppDomain);
        System.out.println("<-bannedVideoXml-" + bannedVideoXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return bannedVideoXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }

    /***
     * sip回拨  -接收
     */
    public void SendInviteSipByIq() {
        final String InviteXml = XmppXml.SendInviteSip(username);
        System.out.println("<-InviteXml-" + InviteXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return InviteXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }

    /***
     * sip回拨  -拒绝
     */
    public void SendRejectSipByIq() {
        final String rejectXml = XmppXml.SendRejectSip(username);
        System.out.println("<-rejectXml-" + rejectXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return rejectXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }

    /***
     * 发送SendIsRoom
     */
    public Packet SendIsRoom(String roomURL) {
        final String isroomXml = XmppXml.SendIsRoom(roomURL);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return isroomXml;
            }
        };
        return xmlPacket;
    }

    /***
     * 发送presence扩展消息
     */
    public Packet SendExtension(String nickname, String username) {
        final String xml = XmppXml.SendExtension(nickname, username, this.xmppDomain);
        Log.d("xmpp", "xmpp-SendExtension-" + xml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return xml;
            }
        };
        return xmlPacket;
    }

    /***
     * 获取主持人成功后  configRole 状态 变成CRUO或CRUA
     */
    public void changeConfigRole(String configRole) {
        this.configRole = configRole;
        Log.d("xmpp", "changeConfigRole-configRole-" + configRole);
        SendPresenceMessage();
    }


    /***
     * 呼叫好友
     */
    public void SendCallFriend(String friendid, String type, String roomNumber) {
        String resource = roomNumber + "@" + xmppDomain;
        final String callFriendXml = XmppXml.SendInviteFriend(friendid, type, roomNumber, resource, username);
        System.out.println("<-callfriendsXml-" + callFriendXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return callFriendXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }

    public CiscoApiInterface.OnIMCallEvents onIMCallEvents;

    public void OnIMCallEvents(CiscoApiInterface.OnIMCallEvents onIMCallEvents) {
        this.onIMCallEvents = onIMCallEvents;
    }

//
//    /***
//     * 更新人员列表
//     */
//    private void updateList(Map<String, Participant> participantMap, String type) {
//        //实时更新人员列表数据
//        // 将Map values 转化为List
//        List<Participant> listParticipant = new ArrayList<Participant>(participantMap.values());
//        updateEvents.updatePartcipantList(listParticipant);
////        SdkPublicKey.participantMap=participantMap;
////        PeerConnectionClient.getInstance().updatePartcipantMap(listParticipant);
//    }

    /***
     * 发送上主屏消息
     */
    public void SendUpperScreen(String screenJid) {
//        to="39df48694f9af987@60.206.107.181/android216"
//        from="101120003@conference.60.206.107.181/meet7938"
//        jid="101120003@conference.60.206.107.181/android216"
        String from = Key.roomnumber + "@conference." + xmppDomain + "/" + endpoint;
        String to = roomURL;
//      String to=Key.roomnumber+"@conference."+ Key.xmpp_url+"/";//friendsjid
        final String screenXml = XmppXml.SendUpperScreen(to, from, screenJid);
        System.out.println("<-screenXml-" + screenXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return screenXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }

    /***
     * 发送群聊消息
     */
    public void SendGroupMessage(String contString) {
        String to = roomURL;
        String from = Key.roomnumber + "@conference." + xmppDomain + "/" + endpoint;
        final String messageXml = XmppXml.SendGroupMessage(to, from, contString, username);
        System.out.println("<-messageXml-" + messageXml);
        final Packet xmlPacket = new Packet() {
            @Override
            public String toXML() {
                return messageXml;
            }
        };
        connection.sendPacket(xmlPacket);
    }
}
