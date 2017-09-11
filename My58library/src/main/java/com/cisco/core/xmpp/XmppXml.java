package com.cisco.core.xmpp;


import com.cisco.core.meet.util.SdpSsrcVariable;

public class XmppXml {
    public static String StirngXml;

    public static String presenceMessage(String roomURL, String userid_tolower, String nick, boolean audiomuted, boolean videomuted, String ice_cd1, String ice_cd2, int googAvailableReceiveBandwidth, int googAvailableSendBandwidth, String packetLoss_download, String packetLoss_upload, String configRole) {
        String messageXML = String.format(
                "<presence xmlns=\"jabber:client\" to=\"%1$s\">"
                        + "<x xmlns=\"http://igniterealtime.org/protocol/ofmeet\">"
                        + "<user-agent xmlns=\"http://jitsi.org/jitmeet/user-agent'>Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36\">" + "</user-agent>"
                        + "<userId>%2$s</userId>"
                        + "<configRole>%12$s</configRole>"
                        + "<nick xmlns=\"http://jabber.org/protocol/nick\">%3$s</nick>"
                        + "<audiomuted xmlns=\"http://jitsi.org/jitmeet/audio\">%4$s</audiomuted>"
                        + "<videomuted xmlns=\"http://jitsi.org/jitmeet/video\">%5$s</videomuted>"
                        + "<stats xmlns=\"http://jitsi.org/jitmeet/stats\">"
                        + "<stat name=\"bitrate_download\" value=\"%8$s\"/>"
                        + "<stat name=\"bitrate_upload\" value=\"%9$s\"/>"
                        + "<stat name=\"packetLoss_total\" value=\"0\"/>"
                        + "<stat name=\"packetLoss_download\" value=\"%10$s\"/>"
                        + "<stat name=\"packetLoss_upload\" value=\"%11$s\"/>"
                        + "</stats>"
                        + "<media xmlns=\"http://estos.de/ns/mjs\">"
                        + " <source type=\"audio\" ssrc=\"%6$s\" direction=\"sendrecv\"/>"
                        + "<source type=\"video\" ssrc=\"%7$s\" direction=\"sendrecv\"/>"
                        + "</media>"
                        + "</x>"
                        + "</presence>"
//			  , key.xmpp_room + "@conference." + key.xmpp_url,key.xmpp_userid_toLower,key.xmpp_username
//			  ,key.audiomuted,key.videomuted
//			  ,SdpSsrcVariable.ice_cd1,SdpSsrcVariable.ice_cd2,key.googAvailableReceiveBandwidth/1000,key.googAvailableSendBandwidth/1000);
                , roomURL, userid_tolower, nick
                , audiomuted, videomuted
                , ice_cd1, ice_cd2, googAvailableReceiveBandwidth, googAvailableSendBandwidth, packetLoss_download, packetLoss_upload, configRole);


        return messageXML;
    }

    public static String SendVideoBridge(String roomurl, String videobridge, String xmppurl) {
        String messageXML = String.format(
                "<iq xmlns=\"jabber:client\" type=\"set\" to=\"%4$s\" id=\"%1$s\">"
                        + "<conference xmlns=\"http://jitsi.org/protocol/focus\" room=\"%2$s\">"
                        + "<property name=\"bridge\" value=\"%3$s\"/>"
                        + "<property name=\"channelLastN\" value=\"-1\"/>"
                        + "<property name=\"adaptiveLastN\" value=\"false\"/>"
                        + "<property name=\"adaptiveSimulcast\" value=\"false\"/>"
                        + "<property name=\"openSctp\" value=\"true\"/>"
                        + "<property name=\"enableFirefoxHacks\" value=\"false\"/>"
                        + "</conference>"
                        + "</iq>"
                , SdpSsrcVariable.getUUID(), roomurl, videobridge, "ofmeet-focus." + xmppurl);
        return messageXML;
    }

    public static String SendIsRoom(String roomurl) {
        String messageXML = String.format(
                "<iq type=\"get\" to=\"%1$s\" id=\"%2$s\">"
                        + "<query xmlns=\"http://jabber.org/protocol/disco#info\"></query>"
                        + "</iq>"
                , roomurl, SdpSsrcVariable.getUUID());
        return messageXML;
    }

    /***
     * 发送呼叫sip 消息
     * @param roomURL
     * @param jid
     * @return
     */
    public static String SendCallSip(String roomURL, String jid, String sipNumber) {

//		"gw": "manis-cluster.10.20.10.139.61.149.55.22",
//				"moderator": "0fee067c96365c4e@61.149.55.22/meet7065",
//				"vb": "jitsi-videobridge.61.149.55.23.61.149.55.22",
//				"stageJid": "0fee067c96365c4e@61.149.55.22/meet7065"
//		<iq to="manis-cluster.10.30.4.11.210.14.128.236" from="b7a98b9df249bcdd@210.14.128.236/web3469" type="set" id="6653:sendIQ">
        //<dial xmlns="urn:xmpp:rayo:1" to="sip:61005@10.30.4.10" from="100020005@conference.210.14.128.236">
        //<header name="JvbRoomName" value="100020005@conference.210.14.128.236"/>
        //<header name="JvbRoomInfo" value="小强的会议"/>
        //</dial>
        //</iq>
        String messageXML = String.format(
                "<iq  to=\"%5$s\"  from=\"%3$s\" type=\"set\" id=\"6653:sendIQ\">"
                        + "<dial xmlns=\"urn:xmpp:rayo:1\" to=\"%4$s\" from=\"%1$s\">"
                        + "<header name=\"JvbRoomName\" value=\"%2$s\"/>"
                        + "<header name=\"JvbRoomInfo\" value=\"%6$s\"/>"
                        + "</dial>"
                        + "</iq>", roomURL, roomURL, jid, sipNumber, Key.gw, Key.title);
        return messageXML;
    }

    /****
     * 发送授权主持人
     * @return
     */
    public static String SendGrantAdmin(String nickname, String coverJid, String coverUserid, String xmppDomain) {
//		"<message xmlns=\"jabber:client\" to=\"5e41dd5b17e6a8ee@192.168.2.235/meet2849\" type=\"chat\" from=\"100020000@conference.192.168.2.235/meet3694\">"
//				+"<body xmlns=\"http://igniterealtime.org/protocol/grantAdmin\" jid=\"100020000@conference.192.168.2.235/meet2849\" gateway_jid=\"null\">true</body>"
//				+"</message>",toJid,roomURL,jid);
        String form = Key.roomnumber + "@conference." + xmppDomain + "/" + nickname;
        String jid = Key.roomnumber + "@conference." + xmppDomain + "/" + coverUserid;
        String messageXML = String.format(
                "<message xmlns=\"jabber:client\" to=\"%1$s\" type=\"chat\" from=\"%2$s\">"
                        + "<body xmlns=\"http://igniterealtime.org/protocol/grantAdmin\" jid=\"%3$s\" gateway_jid=\"null\">true</body>"
                        + "</message>", jid, form, jid);
        return messageXML;
    }

    /****
     * 主持人 踢人
     * @param coverTirenjid
     * @param ownJid
     * @return
     */
    public static String SendTiren(String coverTirenjid, String ownJid, String xmppDomain) {
//		<message to="39df48694f9af987@60.206.107.181/android975" from="101120003@conference.60.206.107.181/meet233" type="chat">"
//				+"<body jid=\"101120003@conference.60.206.107.181/android975\">true</body>"
//				+"</message>
        String messageXML = String.format(
                "<message xmlns=\"jabber:client\" to=\"%1$s\" from=\"%2$s\" type=\"chat\">"
                        + "<body xmlns=\"http://igniterealtime.org/protocol/eject\" jid=\"%3$s\">true</body>"
                        + "</message>"
                , Key.roomnumber + "@conference." + xmppDomain + "/" + coverTirenjid, ownJid, Key.roomnumber + "@conference." + xmppDomain + "/" + coverTirenjid);
        return messageXML;
    }

    /****
     * 主持人 静音某一人，
     * 静音自己 同时向服务器发送iq消息
     * @param coverMutejid
     * @param ownJid
     * @return
     */
    public static String SendMute(String coverMutejid, String ownJid, boolean isMute, String xmppDomain) {
//		<iq xmlns="jabber:client" id="BhTOe-3539" to="101100014@conference.60.206.107.181/focus" type="set" from="4ae280ded6703e2f@60.206.107.181/android892">
// <mute xmlns="http://jitsi.org/jitmeet/audio" jid="101100014@conference.60.206.107.181/android892" userjid=="4ae280ded6703e2f@60.206.107.181/android892" >true</mute></iq>

        String publicPart = Key.roomnumber + "@conference." + xmppDomain;
        String messageXML = String.format(
                "<iq xmlns=\"jabber:client\" id=\"BhTOe-3539\" to=\"%1$s\" type=\"set\" from=\"%2$s\">" +
                        "<mute xmlns=\"http://jitsi.org/jitmeet/audio\" jid=\"%3$s\" userjid=\"%5$s\" >%4$s</mute>" +
                        "</iq>"
                , publicPart + "/focus", ownJid, publicPart + "/" + coverMutejid, isMute, ownJid);
        return messageXML;
    }

    /****
     * 关闭自己画面
     * @return
     */
    public static String SendBannedVideo(String coverMutejid, String ownJid, boolean isMute, String xmppDomain) {
        String publicPart = Key.roomnumber + "@conference." + xmppDomain;
        String messageXML = String.format(
                "<iq xmlns=\"jabber:client\" id=\"BhTOe-3539\" to=\"%1$s\" type=\"set\" >" +
                        "<mute xmlns=\"http://jitsi.org/jitmeet/video\" jid=\"%2$s\" userjid=\"%4$s\"  >%3$s</mute>" +
                        "</iq>"
                , publicPart + "/focus", publicPart + "/" + coverMutejid, isMute, ownJid);
        return messageXML;
    }

    public static String SendInviteSip(String username) {
//		<message type="chat" to="call@60.206.107.181/gateway" iq="4A186207-3E21-4A51-85B7-8736A1C1017F"><call xmlns="http://igniterealtime.org/protocol/ringing" type="accept" roomID="100020008" resource="100020008@conference.60.206.107.181sip:1013" nickname="&#x5C0F;&#x9896;"/></message>
        String messageXML = String.format(
                "<message type=\"chat\" to=\"call@60.206.107.181/gateway\" iq=\"4A186207-3E21-4A51-85B7-8736A1C1017F\">" +
                        "<call xmlns=\"http://igniterealtime.org/protocol/ringing\" type=\"%1$s\" roomID=\"%2$s\" resource=\"%3$s\" nickname=\"%4$s\"/>" +
                        "</message>"
                , "invite", Key.roomnumber, Key.roomnumber + "@conference.60.206.107.181sip:1013", username);
        return messageXML;
    }

    public static String SendRejectSip(String username) {
//		<message type="chat" to="call@60.206.107.181/gateway" iq="4A186207-3E21-4A51-85B7-8736A1C1017F"><call xmlns="http://igniterealtime.org/protocol/ringing" type="accept" roomID="100020008" resource="100020008@conference.60.206.107.181sip:1013" nickname="&#x5C0F;&#x9896;"/></message>
        String messageXML = String.format(
                "<message type=\"chat\" to=\"call@60.206.107.181/gateway\" iq=\"4A186207-3E21-4A51-85B7-8736A1C1017F\">" +
                        "<call xmlns=\"http://igniterealtime.org/protocol/ringing\" type=\"%1$s\" roomID=\"%2$s\" resource=\"%3$s\" nickname=\"%4$s\"/>" +
                        "</message>"
                , "reject", Key.roomnumber, Key.roomnumber + "@conference.60.206.107.181sip:1013", username);
        return messageXML;
    }

    public static String SendExtension(String userid, String nickname, String xmppDomain) {
        String form = Key.roomnumber + "@conference." + xmppDomain + "/" + userid;
        String messageXML = String.format(
                "<presence to=\"%1$s\">" +
                        "<x xmlns=\"http://jabber.org/protocol/muc\"><history maxstanzas=\"0\"/><password/></x>" +
                        "<x xmlns=\"http://igniterealtime.org/protocol/ofmeet\">" +
                        "<nick xmlns=\"http://jabber.org/protocol/nick\">%2$s</nick></x></presence>"
                , form, nickname);
        return messageXML;
    }

    /***
     * 发送呼叫好友 消息
     * @return
     */
    public static String SendInviteFriend(String friendid, String type, String roomNumber, String resource, String username) {
//		invite
//		<message type="chat" to="call@60.206.107.181/gateway" iq="4A186207-3E21-4A51-85B7-8736A1C1017F"><call xmlns="http://igniterealtime.org/protocol/ringing" type="accept" roomID="100020008" resource="100020008@conference.60.206.107.181sip:1013" nickname="&#x5C0F;&#x9896;"/></message>
        String messageXML = String.format(
                "<message type=\"chat\" to=\"%1$s\" iq=\"4A186207-3E21-4A51-85B7-8736A1C1017F\">" +
                        "<call xmlns=\"http://igniterealtime.org/protocol/ringing\" type=\"%2$s\" roomID=\"%3$s\" resource=\"%4$s\" nickname=\"%5$s\"/>" +
                        "</message>"
                , friendid, type, roomNumber, resource, username);
        return messageXML;
    }

    /***
     * android是主持人   发送上主屏消息
     * @return
     */
    public static String SendUpperScreen(String to, String from, String friendsjid) {
//						<message to="39df48694f9af987@60.206.107.181/android216" from="101120003@conference.60.206.107.181/meet7938" type="groupchat">
//                      <body xmlns="http://igniterealtime.org/protocol/stage" jid="101120003@conference.60.206.107.181/android216" max="720" min="180" videobandwidth="1024">
//                      </body></message>
        String messageXML = String.format(
                "<message xmlns=\"jabber:client\" to=\"%1$s\" type=\"groupchat\" from=\"%2$s\">"
                        + "<body xmlns=\"http://igniterealtime.org/protocol/stage\" jid=\"%3$s\" max=\"720\" min=\"180\" videobandwidth=\"1024\">true</body>"
                        + "</message>", to, from, friendsjid);
        return messageXML;
    }

    /***
     * 发送呼叫好友 消息
     * @return
     */
    public static String SendGroupMessage(String to, String from, String text, String nickname) {
//		<message to="821e9433b8ece9a6@61.149.55.22/android172" from="206120002@conference.61.149.55.22/web8785" type="groupchat">
//		<body xmlns="jabber:client">aaaaa</body>
//		<nick xmlns="http://jabber.org/protocol/nick"></nick>
//		</message>
        String messageXML = String.format(
                "<message xmlns=\"jabber:client\" to=\"%1$s\" type=\"groupchat\" from=\"%2$s\"  >" +
                        "<body >" + text +
                        "</body>" +
                        "<nick xmlns=\"http://jabber.org/protocol/nick\">" + nickname +
                        "</nick>" +
                        "</message>"
                , to, from);
        return messageXML;
    }
}
