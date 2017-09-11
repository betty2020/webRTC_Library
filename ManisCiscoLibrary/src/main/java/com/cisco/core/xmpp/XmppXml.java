package com.cisco.core.xmpp;


import com.cisco.core.interfaces.CiscoApiInterface;

public class XmppXml {
	public static  String StirngXml;
	public static String presenceMessage(String roomURL,String userid_tolower,String nick ,boolean audiomuted,boolean videomuted,String ice_cd1,String ice_cd2 ,int googAvailableReceiveBandwidth,int googAvailableSendBandwidth,String packetLoss_download,String packetLoss_upload,String configRole){
		String messageXML = String.format(
				"<presence xmlns=\"jabber:client\" to=\"%1$s\">"
						+"<x xmlns=\"http://igniterealtime.org/protocol/ofmeet\">"
						+"<user-agent xmlns=\"http://jitsi.org/jitmeet/user-agent'>Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36\">"+"</user-agent>"
						+"<userId>%2$s</userId>"
						+"<configRole>%12$s</configRole>"
						+"<nick xmlns=\"http://jabber.org/protocol/nick\">%3$s</nick>"
						+"<audiomuted xmlns=\"http://jitsi.org/jitmeet/audio\">%4$s</audiomuted>"
						+"<videomuted xmlns=\"http://jitsi.org/jitmeet/video\">%5$s</videomuted>"
						+"<stats xmlns=\"http://jitsi.org/jitmeet/stats\">"
						+"<stat name=\"bitrate_download\" value=\"%8$s\"/>"
						+"<stat name=\"bitrate_upload\" value=\"%9$s\"/>"
						+"<stat name=\"packetLoss_total\" value=\"0\"/>"
						+"<stat name=\"packetLoss_download\" value=\"%10$s\"/>"
						+"<stat name=\"packetLoss_upload\" value=\"%11$s\"/>"
						+"</stats>"
						+"<media xmlns=\"http://estos.de/ns/mjs\">"
						+" <source type=\"audio\" ssrc=\"%6$s\" direction=\"sendrecv\"/>"
						+"<source type=\"video\" ssrc=\"%7$s\" direction=\"sendrecv\"/>"
						+"</media>"
						+"</x>"
						+"</presence>"
//			  , key.xmpp_room + "@conference." + key.xmpp_url,key.xmpp_userid_toLower,key.xmpp_username
//			  ,key.audiomuted,key.videomuted
//			  ,SdpSsrcVariable.ice_cd1,SdpSsrcVariable.ice_cd2,key.googAvailableReceiveBandwidth/1000,key.googAvailableSendBandwidth/1000);
				, roomURL,userid_tolower,nick
				,audiomuted,videomuted
				,ice_cd1,ice_cd2,googAvailableReceiveBandwidth,googAvailableSendBandwidth,packetLoss_download,packetLoss_upload,configRole);


		return messageXML;
	}
	public static String SendVideoBridge(String roomurl,String videobridge,String xmppurl){
		String messageXML = String.format(
			"<iq xmlns=\"jabber:client\" type=\"set\" to=\"%4$s\" id=\"%1$s\">"
			+"<conference xmlns=\"http://jitsi.org/protocol/focus\" room=\"%2$s\">"
			+"<property name=\"bridge\" value=\"%3$s\"/>"
			+"<property name=\"channelLastN\" value=\"-1\"/>"
			+"<property name=\"adaptiveLastN\" value=\"false\"/>"
			+"<property name=\"adaptiveSimulcast\" value=\"false\"/>"
			+"<property name=\"openSctp\" value=\"true\"/>"
			+"<property name=\"enableFirefoxHacks\" value=\"false\"/>"
			+"</conference>"
			+"</iq>"
			,SdpSsrcVariable.getUUID(),roomurl,videobridge,"ofmeet-focus."+xmppurl);
	return messageXML;
}

	public static String SendIsRoom(String roomurl){
		String messageXML = String.format(
				"<iq type=\"get\" to=\"%1$s\" id=\"%2$s\">"
						+"<query xmlns=\"http://jabber.org/protocol/disco#info\"></query>"
						+"</iq>"
				,roomurl,SdpSsrcVariable.getUUID());
		return messageXML;
	}

	/***
	 * 发送呼叫sip 消息
	 * @param roomURL
	 * @param jid
	 * @return
	 */
	public static String SendCallSip(String roomURL,String jid,String sipNumber){
		String messageXML = String.format(
				"<iq type=\"set\" to=\"%5$s\" id=\"8175:sendIQ\" from=\"%3$s\" >"
						+"<dial xmlns=\"urn:xmpp:rayo:1\" to=\"%4$s\" from=\"%1$s\">"
						+"<header name=\"JvbRoomName\" value=\"%2$s\"/>"
						+"</dial>"
						+"</iq>",roomURL,roomURL,jid,sipNumber,Key.gw);
		return messageXML;
	}

	/****
	 * 发送授权主持人
	 * @return
	 */
	public static String SendGrantAdmin(String nickname,String coverJid,String coverUserid){
//		"<message xmlns=\"jabber:client\" to=\"5e41dd5b17e6a8ee@192.168.2.235/meet2849\" type=\"chat\" from=\"100020000@conference.192.168.2.235/meet3694\">"
//				+"<body xmlns=\"http://igniterealtime.org/protocol/grantAdmin\" jid=\"100020000@conference.192.168.2.235/meet2849\" gateway_jid=\"null\">true</body>"
//				+"</message>",toJid,roomURL,jid);
		String form=CiscoApiInterface.app.roomnumber+"@conference."+ CiscoApiInterface.app.xmpp_url+"/"+nickname;
		String jid=CiscoApiInterface.app.roomnumber+"@conference."+ CiscoApiInterface.app.xmpp_url+"/"+coverUserid;
		String messageXML = String.format(
				"<message xmlns=\"jabber:client\" to=\"%1$s\" type=\"chat\" from=\"%2$s\">"
						+"<body xmlns=\"http://igniterealtime.org/protocol/grantAdmin\" jid=\"%3$s\" gateway_jid=\"null\">true</body>"
						+"</message>",jid,form,jid);
		return messageXML;
	}

	/****
	 * 主持人 踢人
	 * @param coverTirenjid
	 * @param ownJid
	 * @return
	 */
	public static String SendTiren(String coverTirenjid ,String ownJid){
//		<message to="39df48694f9af987@60.206.107.181/android975" from="101120003@conference.60.206.107.181/meet233" type="chat">"
//				+"<body jid=\"101120003@conference.60.206.107.181/android975\">true</body>"
//				+"</message>
		String messageXML = String.format(
				"<message xmlns=\"jabber:client\" to=\"%1$s\" from=\"%2$s\" type=\"chat\">"
						+"<body xmlns=\"http://igniterealtime.org/protocol/eject\" jid=\"%3$s\">true</body>"
						+"</message>"
				,CiscoApiInterface.app.roomnumber+"@conference."+ CiscoApiInterface.app.xmpp_url+"/"+coverTirenjid,ownJid,CiscoApiInterface.app.roomnumber+"@conference."+ CiscoApiInterface.app.xmpp_url+"/"+coverTirenjid);
		return messageXML;
	}
	/****
	 * 主持人 静音某一人
	 * @param coverMutejid
	 * @param ownJid
	 * @return
	 */
	public static String SendMute(String coverMutejid ,String ownJid,boolean isMute){
//		<iq xmlns="jabber:client" id="BhTOe-3539" to="39df48694f9af987@60.206.107.181/ios941" type="set" from="109920008@conference.60.206.107.181/focus"><mute xmlns="http://jitsi.org/jitmeet/audio">true</mute></iq>
//		<iq xmlns="jabber:client" id="BhTOe-3539" to="101120003@conference.60.206.107.181/focus" type="set" from="4ae280ded6703e2f@60.206.107.181/android217"><mute xmlns="http://jitsi.org/jitmeet/audio" jid="101120003@conference.60.206.107.181/android217" >true</mute></iq>
		String publicPart=CiscoApiInterface.app.roomnumber+"@conference."+ CiscoApiInterface.app.xmpp_url;

		String messageXML = String.format(
				"<iq xmlns=\"jabber:client\" id=\"BhTOe-3539\" to=\"%1$s\" type=\"set\" from=\"%2$s\">" +
						"<mute xmlns=\"http://jitsi.org/jitmeet/audio\" jid=\"%3$s\" userjid=\"%5$s\" >%4$s</mute>" +
						"</iq>"
				,publicPart+"/focus",ownJid,publicPart+"/"+coverMutejid,isMute,ownJid);
		return messageXML;
	}
	/****
	 * 关闭自己画面
	 * @return
	 */
	public static String SendBannedVideo(String coverMutejid ,String ownJid,boolean isMute){
		String publicPart=CiscoApiInterface.app.roomnumber+"@conference."+ CiscoApiInterface.app.xmpp_url;
		String messageXML = String.format(
				"<iq xmlns=\"jabber:client\" id=\"BhTOe-3539\" to=\"%1$s\" type=\"set\" from=\"%2$s\">" +
						"<mute xmlns=\"http://jitsi.org/jitmeet/video\" jid=\"%3$s\" userjid=\"%5$s\"  >%4$s</mute>" +
						"</iq>"
				,publicPart+"/focus",ownJid,publicPart+"/"+coverMutejid,isMute,ownJid);
		return messageXML;
	}

	public static String SendInviteSip(String username){
//		<message type="chat" to="call@60.206.107.181/gateway" iq="4A186207-3E21-4A51-85B7-8736A1C1017F"><call xmlns="http://igniterealtime.org/protocol/ringing" type="accept" roomID="100020008" resource="100020008@conference.60.206.107.181sip:1013" nickname="&#x5C0F;&#x9896;"/></message>
		String messageXML = String.format(
				"<message type=\"chat\" to=\"call@60.206.107.181/gateway\" iq=\"4A186207-3E21-4A51-85B7-8736A1C1017F\">" +
						"<call xmlns=\"http://igniterealtime.org/protocol/ringing\" type=\"%1$s\" roomID=\"%2$s\" resource=\"%3$s\" nickname=\"%4$s\"/>" +
						"</message>"
				,"invite",CiscoApiInterface.app.roomnumber,CiscoApiInterface.app.roomnumber+"@conference.60.206.107.181sip:1013",username);
		return messageXML;
	}

	public static String SendRejectSip(String username){
//		<message type="chat" to="call@60.206.107.181/gateway" iq="4A186207-3E21-4A51-85B7-8736A1C1017F"><call xmlns="http://igniterealtime.org/protocol/ringing" type="accept" roomID="100020008" resource="100020008@conference.60.206.107.181sip:1013" nickname="&#x5C0F;&#x9896;"/></message>
		String messageXML = String.format(
				"<message type=\"chat\" to=\"call@60.206.107.181/gateway\" iq=\"4A186207-3E21-4A51-85B7-8736A1C1017F\">" +
						"<call xmlns=\"http://igniterealtime.org/protocol/ringing\" type=\"%1$s\" roomID=\"%2$s\" resource=\"%3$s\" nickname=\"%4$s\"/>" +
						"</message>"
				,"reject",CiscoApiInterface.app.roomnumber,CiscoApiInterface.app.roomnumber+"@conference.60.206.107.181sip:1013",username);
		return messageXML;
	}
	public static String SendExtension(String userid,String nickname){
		String form=CiscoApiInterface.app.roomnumber+"@conference."+ CiscoApiInterface.app.xmpp_url+"/"+userid;
		String messageXML = String.format(
				"<presence to=\"%1$s\">" +
						"<x xmlns=\"http://jabber.org/protocol/muc\"><history maxstanzas=\"0\"/><password/></x>" +
						"<x xmlns=\"http://igniterealtime.org/protocol/ofmeet\">" +
						"<nick xmlns=\"http://jabber.org/protocol/nick\">%2$s</nick></x></presence>"
				,form,nickname);
		return messageXML;
	}

	/***
	 * 发送呼叫好友 消息
	 * @return
	 */
	public static String SendInviteFriend(String friendid,String type,String roomNumber,String resource,String username){
//		invite
//		<message type="chat" to="call@60.206.107.181/gateway" iq="4A186207-3E21-4A51-85B7-8736A1C1017F"><call xmlns="http://igniterealtime.org/protocol/ringing" type="accept" roomID="100020008" resource="100020008@conference.60.206.107.181sip:1013" nickname="&#x5C0F;&#x9896;"/></message>
		String messageXML = String.format(
				"<message type=\"chat\" to=\"%1$s\" iq=\"4A186207-3E21-4A51-85B7-8736A1C1017F\">" +
						"<call xmlns=\"http://igniterealtime.org/protocol/ringing\" type=\"%2$s\" roomID=\"%3$s\" resource=\"%4$s\" nickname=\"%5$s\"/>" +
						"</message>"
				,friendid,type,roomNumber,resource,username);
		return messageXML;
	}
}
