package com.cisco.core.xmpp;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import net.java.sip.communicator.impl.protocol.jabber.extensions.colibri.SourcePacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.JingleIQ;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jitsimeet.MediaPresenceExtension;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketInterceptor;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
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
import org.jivesoftware.smackx.muc.ParticipantStatusListener;
import org.jivesoftware.smackx.packet.MUCUser;
import org.jivesoftware.smackx.packet.Nick;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.MeetRTCClient;
import com.cisco.core.PeerConnectionClient;
import com.cisco.core.VideoImplement;
import com.cisco.core.entity.Participant;
import com.cisco.core.entity.ParticipantMedia;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.meet.util.MediaSSRCMap;
import com.cisco.core.meet.util.SdpSsrcVariable;
import com.cisco.core.meet.util.SmackInit;
import com.cisco.core.meet.*;
import com.cisco.core.util.Lg;
import com.cisco.core.util.Tools;
import com.cisco.core.xmppextension.AppShareExtension;
import com.cisco.core.xmppextension.CallMessageExtension;
import com.cisco.core.xmppextension.MuteIQ;
import com.cisco.core.xmppextension.XExtension;
import com.cisco.nohttp.NetWorkUtil;

public class XmppConnection implements PacketListener {
	private String xmppDomain;
	private String roomURL;
	private String nickname;// android+随机数
	private String username;// 用户名
	private String xmpp_userid_toLower;
	private String password;

	private int PORT = 5222;
	private Context context;
	public static XMPPConnection connection = null;
	private static XmppConnection xmppConnection ;

	private static final String TAG = "XmppConnection";
	public MultiUserChat muc;
	private MeetRTCClient rtcClient;
	private String offererJid = null;
	private String sid;
	private String StirngXml;

	private Timer mTimer2;
	private TimerTask mTimerTask2;

    static String videobridge = "jitsi-videobridge.192.168.2.224.192.168.2.223";

	public boolean isMeetExist;

	private XmppConnecionListener xmppConnecionListener;
	private Timer timer;
	private TimerTask xmppTimetask;
	private CiscoApiInterface.UpdateUIEvents updateEvents;
	private List<Participant> listParticipant;
	public String configRole="NONE";
	public CiscoApiInterface.OnIMCallEvents onIMCallEvents;
	/**
	 * 创建单实例
	 *
	 * @return
	 */
	synchronized public static XmppConnection getInstance() {
		if (xmppConnection == null) {
			xmppConnection = new XmppConnection();
		}
		return xmppConnection;
	}
	/**
	 * 获取连接
	 */
	public XMPPConnection getConnection(Context context) {
		this.context = context;
		if (connection == null || !connection.isConnected()) {
			openConnection();
		}
		return connection;
	}

	/**
	 * 打开连接
	 */
	public boolean openConnection() {
		try {
			SmackInit.init();
			if (null == connection || !connection.isAuthenticated()) {
				ConnectionConfiguration config = new ConnectionConfiguration(
						xmppDomain, PORT, xmppDomain);
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

				connection.addPacketListener(new PacketListener() {
					@Override
					public void processPacket(Packet packet) {
						 logLongString("XMPP", "<---- " + packet.toXML());
						String result = packet.toXML();
						if (result.contains("http://jabber.org/protocol/disco#info")   && result.contains("type=\"result\"")) {
							//会议存在  直接进入
							isMeetExist=true;
							joinRoom();
						}else if(result .contains("http://jabber.org/protocol/disco#info") && result.contains("404")){
							//会议不存在  创建
							isMeetExist=false;
							joinRoom();
						}
						//	<iq id="3APJ3-10757" to="39df48694f9af987@60.206.107.181/android523" from="101120003@conference.60.206.107.181/focus"
//								type="set"><mute xmlns='http://jitsi.org/jitmeet/audio'>true</mute></iq>
//						静音
						if (packet instanceof MuteIQ) {
							String message=((MuteIQ) packet).getMessage();
							if(message.equals("true")){
								updateEvents.beHostToOperate(true,1);
							}else{
								updateEvents.beHostToOperate(false,1);
							}
						}

						//剔除
//						<message to="39df48694f9af987@60.206.107.181/android975" from="101120003@conference.60.206.107.181/meet233" type="chat">
//						<body jid="101120003@conference.60.206.107.181/android975">true</body></message>
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
//								<message to="39df48694f9af987@60.206.107.181/android184" from="101120003@conference.60.206.107.181/meet786" type="chat">
//								<body xmlns="http://igniterealtime.org/protocol/grantAdmin" jid="101120003@conference.60.206.107.181/android184">true</body></message>
									String hostJid = me.getJid();
									if (hostJid.equals(CiscoApiInterface.app.roomnumber + "@conference." + CiscoApiInterface.app.xmpp_url + "/" + nickname)) {
										configRole = "CRUO";
										SendPresenceMessage();
										updateEvents.beHostToOperate(true, 4);
									}
								} else if (xmlns.equals("http://igniterealtime.org/protocol/stage")) {
									//上主屏
//						<message to="39df48694f9af987@60.206.107.181/android216" from="101120003@conference.60.206.107.181/meet7938" type="groupchat">
//                      <body xmlns="http://igniterealtime.org/protocol/stage" jid="101120003@conference.60.206.107.181/android216" max="720" min="180" videobandwidth="1024">
//                      </body></message>
									String jid = me.getJid();
									Log.i(TAG, "SOURCE ADD 上主屏jid: " + jid);
									updateEvents.OnTheMainScreen(jid);
								}
//                       <message type="chat" to="call@60.206.107.181/gateway" iq="4A186207-3E21-4A51-85B7-8736A1C1017F">
//						<call xmlns="http://igniterealtime.org/protocol/ringing" type="accept" roomID="100020008" resource="100020008@conference.60.206.107.181sip:1013" nickname="&#x5C0F;&#x9896;"/>
//						</message>
								//SIP 回拨我
								CallMessageExtension cme = (CallMessageExtension) packet.getExtension(CallMessageExtension.ELEMENT_NAME, CallMessageExtension.NAMESPACE);
								if (cme != null) {
									logLongString("XMPP", "<--cme-- " + cme.toXML());
									String type = cme.getType();
									updateEvents.sipDialBack();
								}
							}else if(m.toXML().contains("appshare")){
                            //共享白板
							  AppShareExtension ase=(AppShareExtension)m.getExtension(AppShareExtension.ELEMENT_NAME, AppShareExtension.NAMESPACE);
								logLongString("XMPP", "linpeng,<--ase--共享白板 " + ase.toXML());
								 String  action= ase.getAction().trim();
								  String url=ase.getUrl().trim();
								updateEvents.onWhiteBoard(action,url);
							}else if(m.toXML().contains("call")){
//                          <message to="4ae280ded6703e2f@60.206.107.181/android969" from="f57fcb874fa58539@60.206.107.181/ios555" type="chat">
//                           <call xmlns='http://igniterealtime.org/protocol/ringing' type='accept' roomID='101120002' resource='f57fcb874fa58539@60.206.107.181/ios555' nickname='刘兰''>
//                           </call></message>
								//SIP 回拨我
								CallMessageExtension cme=(CallMessageExtension) packet.getExtension(CallMessageExtension.ELEMENT_NAME, CallMessageExtension.NAMESPACE);
								if(cme!=null) {
									logLongString("XMPP", "<--cme--" + cme.toXML());
									String type=cme.getType();
									String roomID=cme.getRoomID();
									String resource=cme.getResource();
									String friendid= m.getFrom();
									String nickname=cme.getNickname();
									if(onIMCallEvents!=null) {
										if (type.equals("accept")) {
											onIMCallEvents.acceptCallback(roomID, friendid, resource);
										} else if (type.equals("reject")) {
											onIMCallEvents.rejectCallback(roomID, friendid, resource);
										} else if (type.equals("invite")) {
											onIMCallEvents.inviteCallback(roomID, friendid, resource);
										}
									}
								}
							}
						}

					}
				}, new PacketFilter() {
					@Override
					public boolean accept(Packet packet) {
						return true;
					}
				});

				connection.addPacketSendingListener(new PacketListener() {
					@Override
					public void processPacket(Packet packet) {
						 logLongString("XMPP", "----> " + packet.toXML());
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
				if(connection.isConnected()){
					xmppConnecionListener = new XmppConnecionListener();
					connection.addConnectionListener(xmppConnecionListener);//连接监听
				}
				ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
				discoManager.addFeature("urn:xmpp:jingle:1");
				discoManager.addFeature("urn:xmpp:jingle:apps:rtp:1");
				discoManager.addFeature("urn:xmpp:jingle:apps:rtp:audio");
				discoManager.addFeature("urn:xmpp:jingle:apps:rtp:video");
				discoManager.addFeature("urn:xmpp:jingle:transports:ice-udp:1");
				discoManager
						.addFeature("urn:xmpp:jingle:transports:dtls-sctp:1");
				discoManager.addFeature("urn:ietf:rfc:5761");
				discoManager.addFeature("urn:ietf:rfc:5888");
				discoManager.addFeature("urn:xmpp:jingle:apps:rtp:rtcp-fb:0");
				discoManager
						.addFeature("urn:xmpp:jingle:apps:rtp:rtp-hdrext:0");

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

	/*
	 * 登录
	 */

	public boolean login(final Context context, String username,
			String password, String nickname, String xmppurl,
			String xmpp_userid_toLower) {
		this.context = context;
		this.username = username;
		this.nickname = nickname;
		this.password=password;
		this.xmpp_userid_toLower = xmpp_userid_toLower;
		this.xmppDomain = xmppurl;
		Log.d(TAG,"xmpp--xmppDomain="+xmppDomain);
		listParticipant = new ArrayList<Participant>();
		try {
			closeConnection();
			if (getConnection(context) == null)
				return false;
			// xmpp_userid_toLower 相当于xmpp的用户名
			getConnection(context).login(xmpp_userid_toLower, password,nickname);
			Log.d(TAG,"xmpp--connection="+connection);
			return true;
		} catch (XMPPException e) {
			Log.e("Xmappconnection","fail!, Not connected to server, close connection");
			closeConnection();
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e("Xmappconnection", "Not connected to server.");
		}
		return false;
	}

	public void closeConnection() {
		if (connection != null&&connection.isConnected()) {
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
			connection.disconnect();
			connection = null;
//                }
//            }).start();
		}else{
			connection=null;
		}
	}

	public void disconnect() {
		if (muc != null&&muc.isJoined()) {
			Log.e("Xmappconnection", "disconnect,muc="+muc+",isjoined="+muc.isJoined()+",muc.getRoom()="+muc.getRoom());
			muc.leave();
			Key.clear();//清空人员集合
			listParticipant.clear();
			muc = null;
		}
		if (mTimer2 != null) {
			mTimer2.cancel();
		}
		if(xmppConnecionListener!=null&& connection!=null){
			connection.removeConnectionListener(xmppConnecionListener);
		}
		if (Key.isguest) {
			closeConnection();
		}
	}

	/*
	 * 加入会议
	 */
	public void joinMuc(MeetRTCClient rtcClient, String roomURL,
			final String nickname,final CiscoApiInterface.UpdateUIEvents updateEvents) {
		this.rtcClient = rtcClient;
		this.roomURL = roomURL;
		this.nickname = nickname;
		this.updateEvents=updateEvents;
		Log.d(TAG, "joinMuc----context-" + context + "=connection=" + connection+",getuser="+connection.getUser());
		if(Key.vb!=null){
			videobridge=Key.vb;
		}
		if(!connection.isConnected()){
			return;
		}
		// 发送room消息 获取会议是否存在
		final Packet isRoomXml = SendIsRoom(roomURL);
		connection.sendPacket(isRoomXml);

		//发送nick消息
		Packet presencePacket = new Presence(Presence.Type.available);
		presencePacket.setTo(connection.getUser());
		presencePacket.addExtension(new Nick(username));
		connection.sendPacket(presencePacket);

		// 发送presence消息   会控端显示自己名字
		final Packet xml = SendExtension(nickname,username);
		connection.sendPacket(xml);
		if(Key.Moderator){
			configRole="CRUO";
		}else{
			configRole="NONE";
		}
		SendPresenceMessage();//发送presence消息
	}

	public void joinRoom(){
		logLongString("XMPP", "<-joinRoom,muc=" +muc);
		logLongString("XMPP", "<-joinRoom,connection=" +connection+",isConnected"+connection.isConnected());
		muc = new MultiUserChat(connection, roomURL);
		muc.addPresenceInterceptor(new PresenceInterceptor());
			try {
				// 聊天室服务将会决定要接受的历史记录数量
				DiscussionHistory history = new DiscussionHistory();
				history.setMaxChars(0);
				if (isMeetExist) {
					muc.join(nickname,"",history, SmackConfiguration.getPacketReplyTimeout());
				} else {
					muc.create(nickname);
					CreateSubmitform();
					mTimer2 = new Timer();
					mTimerTask2 = new TimerTask() {
						@Override
						public void run() {
							Packet presencePacket = new Presence(
									Presence.Type.available);
							if(connection!=null&&connection.isConnected())
							connection.sendPacket(presencePacket);
						}
					};
					mTimer2.schedule(mTimerTask2, 10000, 10000);

					// 发送videobridge消息
					final Packet videoBridgeXml = SendVideoBridge(roomURL, videobridge, xmppDomain);
					connection.sendPacket(videoBridgeXml);
				}
				// webrtc create peerconction 之类的
				rtcClient.onCreateLocalRoom();
			} catch (XMPPException e) {
				Log.e(TAG, nickname + " : could not enter MUC " + e);
			}
		// message监听
		muc.addMessageListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				logLongString("XMPP", "<-addMessageListener" +packet.toXML());
				Message message = (Message) packet;
				String infoText = message.getBody();
				String friendName = message.getFrom();
				String  xmlns=message.getXmlns();
				String isonly = friendName.substring( friendName.indexOf("/") + 1, friendName.length());
				logLongString("XMPP", "<-addMessageListener_infoText" +infoText);
				if (isonly.equals("focus")) {
					//会控发的消息
					// 会控操作
					// {"allaudio":false,"allvideo":true,"muted":false,"videoClosed":false,"userJid":null,
					// "lock":false,"hangup":false,"msg":null,"role":null,"toClose":false,"lostModerator":false}
					JSONObject rootNode= JSON.parseObject(infoText);
					boolean allaudio =Boolean.parseBoolean(rootNode.getString("allaudio"));
					boolean allvideo =Boolean.parseBoolean( rootNode.getString("allvideo"));
					boolean muted = Boolean.parseBoolean(rootNode.getString("muted"));
					boolean videoClosed =Boolean.parseBoolean( rootNode.getString("videoClosed"));
					String userJid = rootNode.getString("userJid");
					boolean lock = Boolean.parseBoolean(rootNode.getString("lock"));

					boolean hangup = Boolean.parseBoolean(rootNode.getString("hangup"));
					String msg = rootNode.getString("msg");
					boolean role = Boolean.parseBoolean(rootNode.getString("role"));
					boolean toClose = Boolean.parseBoolean(rootNode.getString("toClose"));
					boolean lostModerator = Boolean.parseBoolean(rootNode.getString("lostModerator"));
					if(msg!=null){
						if(updateEvents!=null){
							updateEvents.IMMessageRecever(msg, "focus");
						}
					}else{
//					<message to="39df48694f9af987@60.206.107.181/android825" from="101120003@conference.60.206.107.181/meet7130" type="groupchat">
//					<body xmlns="http://igniterealtime.org/protocol/stage" jid="39df48694f9af987@60.206.107.181/android825"
//					max="720" min="180" videobandwidth="1024"></body></message>
						updateEvents.beHuiKongToOperate(allaudio, allvideo,muted,videoClosed,hangup,userJid,lostModerator);
					}
				} else if (!isonly.equals(nickname)) {
//					if(xmlns.equals("http://igniterealtime.org/protocol/stage")) {
//						//上主屏消息
//
//					}else{
						if (updateEvents != null) {
							if (infoText.length()!=0) {
								logLongString("XMPP", "<-addMessageListener---Key.map.size()" +Key.map.size()+"--Key.map.get(isonly)-"+Key.map.get(isonly));
								if (Key.map.size() > 0) {
//									updateEvents.IMMessageRecever(infoText, Key.map.get(isonly));
//								} else {
									updateEvents.IMMessageRecever(infoText, isonly);
								}
							}
						}
//					}
				}
			}
		});

		// 成员监听
		muc.addParticipantListener(new PacketListener() {
			@Override
			public void processPacket(Packet packet) {
				logLongString("XMPP", "<-addParticipantListener" + packet.toXML());
				Presence msg = (Presence) packet;
				String resultName = msg.getFrom();
				String name = resultName.substring(resultName.indexOf("/") + 1, resultName.length());//例如 meet123  android123 ios123
                logLongString("XMPP", "<-addParticipantListener getExtensions=" + msg.getExtensions().size());
				String type=msg.getType().toString();
				if(type.equals("unavailable")){
					for (int i=0;i<listParticipant.size();i++){
						if(listParticipant.get(i).getUserid().equals(name)){
							listParticipant.remove(i);
						}
					}
				}
				if( msg.getExtensions().size()==1) {
					if(!name.equals("focus")) {
						MUCUser mucUser = (MUCUser) msg.getExtension("x", "http://jabber.org/protocol/muc#user");
						String jid = mucUser.getItem().getJid();
						String userid= jid.substring(jid.indexOf("/") + 1, jid.length());
						Participant pc = new Participant();
						pc.setNickname(username);
						pc.setUserid(name);
						pc.setJid(jid);
						if(listParticipant.size()>0){
							for (int i=0;i<listParticipant.size();i++){
								if(listParticipant.get(i).getNickname().equals(pc.getNickname())&& userid.equals(name)){
									listParticipant.set(i,pc);
								}
							}
						}else{
								Key.map.put(name, username);
							    pc.setHost(Key.Moderator);
								listParticipant.add(pc);
						}

					}
				}
				if( msg.getExtensions().size()==2) {
					XExtension x = (XExtension) msg.getExtension(XExtension.NAMESPACE);
					MUCUser mucUser = (MUCUser)msg.getExtension("x", "http://jabber.org/protocol/muc#user");
						String nick = x.getNickValue();// 例如 linpeng
						String ishost =x.getConfigRoleValue() !=null? x.getConfigRoleValue(): null;
						String jid = mucUser.getItem().getJid();
					    String userid= jid.substring(jid.indexOf("/") + 1, jid.length());
						String audioMuted = x.getAudioMutedValue();
						Map<String ,String> map=new HashMap<String ,String>();
						List<XExtension.Source> list = x.getSources();
						for (XExtension.Source source : list) {
							String type1=source.getType();
							String ssrc=source.getSsrc();
							map.put(type1,ssrc);
						}
						Participant pc = new Participant();
						pc.setNickname(nick);
						pc.setUserid(name);
						pc.setJid(jid);
						pc.setMap(map);
						if (audioMuted == null) {
							pc.setGetMuteMic(false);
						} else {
							if (audioMuted.equals("false")) {
								pc.setGetMuteMic(false);
							} else if (audioMuted.equals("true")) {
								pc.setGetMuteMic(true);
							}
						}
						if(ishost!=null) {
							if (ishost.equals("CRUO") || ishost.equals("CRUA")) {
								//是主持人
								pc.setHost(true);
							} else if (ishost.equals("NONE")) {
								//不是主持人
								pc.setHost(false);
							}
						}else{
							//不是主持人
							pc.setHost(false);
							if(Key.Moderator){
								pc.setHost(true);
							}
						}
					            Key.map.put(name, nick);
					            listParticipant.add(pc);
								for (int i=0;i<listParticipant.size();i++){
									if(listParticipant.get(i).getNickname().equals(pc.getNickname())&& listParticipant.get(i).getUserid().equals(userid)){
										listParticipant.set(i,pc);
									}
									for (int  j =  listParticipant.size()  -  1 ; j  >  i; j --){
										if  (listParticipant.get(j).equals(listParticipant.get(i)))  {
											listParticipant.remove(j);
										}
									}
								}
				}
				//实时更新人员列表数据
				updateEvents.updatePartcipantList(listParticipant);
			}
		});


		muc.addParticipantStatusListener(new ParticipantStatusListener() {
			@Override
			public void joined(String s) {
			}
			@Override
			public void left(final String participant) {
				Log.d("xmpp",StringUtils.parseResource(participant)+ " has left the room.1"+"------listParticipant.size------"+listParticipant.size());
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(2000);
							for (int i = 0; i < listParticipant.size(); i++) {
								Log.d("xmpp","集合"+listParticipant.get(i).getUserid()+ " has left the room.1");
								if (listParticipant.get(i).getUserid().equals(StringUtils.parseResource(participant))) {
									listParticipant.remove(listParticipant.get(i));
									Key.map.remove(StringUtils.parseResource(participant));
								}
							}
							Log.d("xmpp"," has left the room.1"+"------------------------------------------listParticipant.size------"+listParticipant.size());
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}).start();

				updateEvents.onOccupantLeftRoom( Key.map.get(StringUtils.parseResource(participant)));
				updateEvents.updatePartcipantList(listParticipant);

			}
			@Override
			public void kicked(String s, String s1, String s2) {
			}
			@Override
			public void voiceGranted(String s) {
			}
			@Override
			public void voiceRevoked(String s) {
			}
			@Override
			public void banned(String s, String s1, String s2) {
			}
			@Override
			public void membershipGranted(String s) {
			}

			@Override
			public void membershipRevoked(String s) {
			}
			@Override
			public void moderatorGranted(String s) {
			}

			@Override
			public void moderatorRevoked(String s) {

			}
			@Override
			public void ownershipGranted(String s) {
			}
			@Override
			public void ownershipRevoked(String s) {
			}
			@Override
			public void adminGranted(String s) {
			}
			@Override
			public void adminRevoked(String s) {
			}
			@Override
			public void nicknameChanged(String s, String s1) {
			}
		});
		muc.addPresenceInterceptor(new PresenceInterceptor());
	}
	/**
	 * 未读消息数量
	 */
	public void getUnreadMsgsCount(){

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

	/*
	 * 创建会议
	 */

	public static void createRoom(String roomnumber, String vb, Context context) {
		if (connection == null) {
			return;
		}
		videobridge = vb;
	}

	@Override
	public void processPacket(Packet packet) {

		try {
			JingleIQ jiq = (JingleIQ) packet;
			ackJingleIQ(jiq);
			switch (jiq.getAction()) {
			case SESSION_INITIATE:
//				Log.i(TAG, "linpeng,, SESSION_INITIATE_toxml: " + jiq.toXML());
				System.out.println("linpeng,, SESSION_INITIATE_toxml: " + jiq.toXML());
				SessionDescription bridgeOfferSdp= JingleToSdp.toSdp(jiq, SessionDescription.Type.OFFER);
				Log.i(TAG, "linpeng,, bridgeOfferSdp: " + bridgeOfferSdp.description);
				offererJid = jiq.getFrom();
				sid = jiq.getSID();
				rtcClient.acceptSessionInit(bridgeOfferSdp);
				break;
			case SOURCEADD:
			case ADDSOURCE:
				Log.i(TAG, "linpeng,, SOURCE ADD-toxml: " + jiq.toXML());
				MediaSSRCMap addedSSRCs = MediaSSRCMap.getSSRCsFromContent(jiq.getContentList());
//				Log.d(TAG, "linpeng,,SOURCE ADD videoSSRCs: " +addedSSRCs.getSsrcs().get("video").get(0).getSSRC()+"---"+ "linpeng,,SOURCE ADD audioSSRCs: " +addedSSRCs.getSsrcs().get("audio").get(0).getSSRC());
//				Log.i(TAG, "linpeng,,SOURCE ADD TYPE: " +addedSSRCs.getMediaTypes());
//				Log.i(TAG, "linpeng,,SOURCE ADD size: " +addedSSRCs.getSsrcs().size());
				Log.i(TAG, "linpeng,,SOURCE ADD addedSSRCs: " +addedSSRCs.getSsrcs()+",size=="+addedSSRCs.getSsrcs().size());
				rtcClient.onSourceAdd(addedSSRCs);
				break;
			case REMOVESOURCE:
			case SOURCEREMOVE:
				Log.i(TAG, "linpeng,,SOURCE REMOVE: " + jiq.toXML());
				Log.d("xmpp","linpeng,,SOURCE REMOVE: has left the room.1");
				MediaSSRCMap removedSSRCs = MediaSSRCMap
						.getSSRCsFromContent(jiq.getContentList());
				rtcClient.onSourceRemove(removedSSRCs);
				break;
			default:
				System.err.println(" : Unknown Jingle IQ received : "+ jiq.toString());
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
	 * @param packetToAck
	 *            the <tt>JingleIQ</tt> that need to be acknowledge.
	 */
	private void ackJingleIQ(JingleIQ packetToAck) {

		if(packetToAck.getType().equals("error")) {
			//linpeng add
			updateEvents.onIceFailed();
		}else{
			IQ ackPacket = IQ.createResultIQ(packetToAck);
//		logLongString("XMPP", "---------ackPacket> " + ackPacket.toXML());
			connection.sendPacket(ackPacket);
		}
	}

	public void sendSessionAccept(SessionDescription sdp) {
		Log.d(TAG, "-----------------sendSessionAccept-description=" + sdp.description);
		JingleIQ sessionAccept = SdpToJingle.toJingle(sdp);
		sessionAccept.setTo(offererJid);
		sessionAccept.setSID(sid);
		sessionAccept.setType(IQ.Type.SET);
		sessionAccept.setInitiator(connection.getUser());// linpeng add
		addStreamsToPresence(sessionAccept.getContentList());
		connection.sendPacket(sessionAccept);

		//手机第一个进入及时会议 更改主持人状态
		if(Key.Moderator){
			configRole="CRUO";
		}else{
			configRole="NONE";
		}
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
		final String StirngXml = XmppXml.presenceMessage(roomURL, xmpp_userid_toLower,
				username,  SdpSsrcVariable.audiomuted,SdpSsrcVariable.videomuted, SdpSsrcVariable.ice_cd1,
				SdpSsrcVariable.ice_cd2,
				SdpSsrcVariable.googAvailableReceiveBandwidth / 1000,
				SdpSsrcVariable.googAvailableSendBandwidth / 1000,SdpSsrcVariable.packetLoss_download,SdpSsrcVariable.packetLoss_upload,configRole);
		final Packet xmlPacket = new Packet() {
			@Override
			public String toXML() {
				return StirngXml;
			}
		};
		if(NetWorkUtil.hasNetwork(context,updateEvents)&& connection!=null&& connection.isConnected()){
			connection.sendPacket(xmlPacket);
		}
	}

	private void addStreamsToPresence(List<ContentPacketExtension> contentList) {
		MediaSSRCMap mediaSSRCs = MediaSSRCMap.getSSRCsFromContent(contentList);

		MediaPresenceExtension mediaPresence = new MediaPresenceExtension();

		for (SourcePacketExtension audioSSRC : mediaSSRCs
				.getSSRCsForMedia("audio")) {
			MediaPresenceExtension.Source ssrc = new MediaPresenceExtension.Source();
			ssrc.setMediaType("audio");
			ssrc.setSSRC(String.valueOf(audioSSRC.getSSRC()));

			mediaPresence.addChildExtension(ssrc);
		}

		for (SourcePacketExtension videoSSRC : mediaSSRCs
				.getSSRCsForMedia("video")) {
			MediaPresenceExtension.Source ssrc = new MediaPresenceExtension.Source();
			ssrc.setMediaType("video");
			ssrc.setSSRC(String.valueOf(videoSSRC.getSSRC()));

			mediaPresence.addChildExtension(ssrc);
		}

		sendPresenceExtension(mediaPresence);
	}

	private Presence lastPresenceSent = null;

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
	 * @param extension
	 *            the <tt>PacketExtension</tt> to be included in MUC presence.
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
		if (iq != null && connection!=null&&connection.isConnected()) {
			iq.setSID(sid);
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
//			try {
//				XmppConnection.getInstance().closeConnection();
//			} catch (Exception e) {
//				Log.i(TAG, "ConnectionListener-connectionClosed="+e.toString());
//			}
//			Reconnection();
		}

		@Override
		public void connectionClosedOnError(Exception e) {
//			java.net.SocketException: Broken pipe//有个异常
//			java.net.SocketException: Software caused connection abort
			Log.i(TAG, "ConnectionListener-connectionClosedOnError重连=" + e.toString());
//			// 这里就是网络不正常或者被挤掉断线激发的事件
			boolean error = e.getMessage().equals("stream:error (conflict)");//// 被挤掉线
			if (!error) {
				try {
//					XmppConnection.getInstance().closeConnection();
				} catch (Exception e2) {
					Log.i(TAG, "ConnectionListener-e=" + e2.toString());
					updateEvents.onIceFailed();
				}
			} else if (e.getMessage().contains("Connection timed out")) {// 连接超时
				// 不做任何操作，会实现自动重连
				Log.i(TAG, "ConnectionListener-connectionClosedOnError=连接超时");
			}
//			//重连服务器
//			Reconnection();
		}
//		class MyTimertask extends TimerTask {
//
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//
//				if (NetWorkUtil.isNetworkAvalible(context)) {
////					synchronized (ACCESSIBILITY_SERVICE) {
//////                    new LoginTask(OpenfirePushReceive.context).execute();
////					}
//					boolean issuccess=login2();
//					Log.d(TAG, "ConnectionListener--重连--正在连接中-issuccess="+issuccess );
//					if(issuccess){
//					joinMuc(rtcClient,CiscoApiInterface.app.roomnumber + "@conference." + CiscoApiInterface.app.xmpp_url,CiscoApiInterface.app.xmpp_nick, updateEvents);
//					}
//
//				}else{
//					Log.d(TAG, "ConnectionListener--重连--当前没有网络-" );
//					timer.schedule(new MyTimertask(), 5000);
//				}
//
//			}
//
//		}
//		private void Reconnection() {
//					try {
//						Log.d(TAG, "ConnectionListener--重连--context-" + context + "=connection=" + connection+",getuser="+connection.getUser());
//						Log.d(TAG, "ConnectionListener--重连--1connection=" + connection.isConnected());
//						Log.d(TAG, "ConnectionListener--重连--rtcClient-" + rtcClient + "=CiscoApiInterface.app.xmpp_url=" + CiscoApiInterface.app.xmpp_url + ",CiscoApiInterface.app.xmpp_nick=" + CiscoApiInterface.app.xmpp_nick);
////                            joinMuc(rtcClient, CiscoApiInterface.app.roomnumber + "@conference." + CiscoApiInterface.app.xmpp_url,CiscoApiInterface.app.xmpp_nick, updateEvents);
//						Log.d(TAG, "ConnectionListener--重连--muc=" + muc+",mucisJoined"+muc.isJoined()+",getRoom"+muc.getRoom()+",getNickname"+muc.getNickname());
//						timer = new Timer();
//						timer.schedule(new MyTimertask(), 5000);
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//		}

		@Override
		public void reconnectingIn(int seconds) {
			Log.i(TAG, "ConnectionListener-reconnectingIn,connection="+connection+",getuser="+connection.getUser()+",isConnected="+connection.isConnected()+",倒计时："+seconds);
		}

		@Override
		public void reconnectionSuccessful() {
			Log.i(TAG, "ConnectionListener-reconnectionSuccessful");
			Log.i(TAG, "ConnectionListener-reconnectionSuccessful,connection="+connection+",getuser="+connection.getUser()+",isConnected="+connection.isConnected());
			Log.d(TAG, "ConnectionListener--重连-1-muc=" + muc+",mucisJoined="+muc.isJoined()+",getRoom="+muc.getRoom()+",getNickname="+muc.getNickname());
//						// 发送room消息 获取会议是否存在
//			final Packet isRoomXml = SendIsRoom(roomURL);
//			connection.sendPacket(isRoomXml);
			if(VideoImplement.videoImplement.peerConnectionClient!= null){
				VideoImplement.videoImplement.ReconnectionPeer();
			}
			Log.d(TAG, "ConnectionListener--重连-2-muc=" + muc+",mucisJoined="+muc.isJoined()+",getRoom="+muc.getRoom()+",getNickname="+muc.getNickname()+",isConnected="+connection.isConnected());
		}

		@Override
		public void reconnectionFailed(Exception e) {
			Log.i(TAG, "ConnectionListener-reconnectionFailed");
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
	public void SendCallSipByIq(String sipNumber){
		final String callSipXml = XmppXml.SendCallSip(roomURL,connection.getUser(),sipNumber);
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
	public void SendGrantAdminByMessage(String coverJid,String coverUserid){
		final String grantAdminXml = XmppXml.SendGrantAdmin(nickname,coverJid,coverUserid);
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
	public void SendTirenByMessage(String coverTirenid){
		final String tirenXml = XmppXml.SendTiren(coverTirenid,connection.getUser());
		System.out.println("<-tirenXml-" + tirenXml);
		final Packet xmlPacket = new Packet() {
			@Override
			public String toXML() {
				return tirenXml;
			}
		};
		connection.sendPacket(xmlPacket);
		//踢人的同时 移除此人
		if(listParticipant.size()>0){
			for (int i=0;i<listParticipant.size();i++){
				if(listParticipant.get(i).getUserid().equals(coverTirenid)){
					listParticipant.remove(listParticipant.get(i));
//					Key.map.remove(listParticipant.get(i).getUserid());
				}

			}
		}
	}
	/***
	 * 主持人发送静音某一成员 message消息
	 * 自己静音时 也发送iq消息
	 */
	public void SendMuteMessage(String coverMuteid,boolean isMute) {
		if(connection!=null) {
			final String muteXml = XmppXml.SendMute(coverMuteid, connection.getUser(), isMute);
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
	public void SendBannedVideo(String coverMuteid,boolean isBannedVideo) {
		final String bannedVideoXml = XmppXml.SendBannedVideo(coverMuteid,connection.getUser(),isBannedVideo);
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
	public Packet SendExtension(String nickname,String username) {
		final String xml = XmppXml.SendExtension(nickname,username);
		Log.d("xmpp","xmpp-SendExtension-"+xml);
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
//		configRole="CRUO";
		this.configRole=configRole;
//		if(configRole.equals("NONE")){
//        updateEvents.updateHostState();
//		}
		SendPresenceMessage();
	}
	/***
	 * 呼叫好友
	 */
	public void SendCallFriend(String friendid,String type,String roomNumber){
		String resource=roomNumber+"@"+ CiscoApiInterface.app.xmpp_url;
		final String callFriendXml = XmppXml.SendInviteFriend(friendid,type,roomNumber,resource,username);
		System.out.println("<-callfriendsXml-" + callFriendXml);
		final Packet xmlPacket = new Packet() {
			@Override
			public String toXML() {
				return callFriendXml;
			}
		};
		connection.sendPacket(xmlPacket);
	}
	public void OnIMCallEvents(CiscoApiInterface.OnIMCallEvents onIMCallEvents) {
		this.onIMCallEvents=onIMCallEvents;
	}
	/*
	 * 登录
	 */

	public boolean login2() {
		try {
			if (getConnection(context) == null)
				return false;
			// xmpp_userid_toLower 相当于xmpp的用户名
			getConnection(context).login(xmpp_userid_toLower, password,nickname);
			Log.d(TAG,"xmpp--connection2="+connection);
			return true;
		} catch (XMPPException e) {
			Log.e("Xmppconnection","fail!, Not connected to server, close connection");
			closeConnection();
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e("Xmppconnection", "Not connected to server.");
		}
		return false;
	}
}
