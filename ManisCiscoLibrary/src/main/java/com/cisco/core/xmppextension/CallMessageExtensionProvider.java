package com.cisco.core.xmppextension;

import android.util.Log;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class CallMessageExtensionProvider implements PacketExtensionProvider {
	@Override
	public PacketExtension parseExtension(XmlPullParser parser)
			throws Exception {
		CallMessageExtension result = new CallMessageExtension();
		int count = parser.getAttributeCount();
		Log.d("CallMessage","linpeng,count:"+count);
		for(int i=0;i<count;i++){
			Log.d("CallMessage","linpeng,getAttributeName:"+parser.getAttributeName(i)+",getAttributeValue:"+ parser.getAttributeValue(i));
			if(parser.getAttributeName(i).equals("type")){
				String type = parser.getAttributeValue(i).trim();
				result.setType(type);
			}
			if(parser.getAttributeName(i).equals("roomID")){
				String roomID = parser.getAttributeValue(i).trim();
				result.setRoomID(roomID);
			}
			if(parser.getAttributeName(i).equals("resource")){
				String resource = parser.getAttributeValue(i).trim();
				result.setResource(resource);
			}
			if(parser.getAttributeName(i).equals("nickname")){
				String nickname = parser.getAttributeValue(i).trim();
				result.setNickname(nickname);
			}
		}



//		boolean done = false;
//		while (!done)
//			switch (parser.next()) {
//			case XmlPullParser.END_TAG: {
//				String name = parser.getName();
//				if (XExtension.ELEMENT_NAME.equals(name)) {
//					done = true;
//				}
//				break;
//			}
//			case XmlPullParser.START_TAG: {
//				String elementName = parser.getName();
//                if(elementName.equals("call")){
//					int count = parser.getAttributeCount();
//					if (count > 0) {
//						String type = parser.getAttributeValue("", "type");
//						String roomID = parser.getAttributeValue("", "roomID");
//						String resource = parser.getAttributeValue("", "resource");
//						String nickname = parser.getAttributeValue("", "nickname");
//						result.setType(type);
//						result.setRoomID(roomID);
//						result.setNickname(nickname);
//						result.setResource(resource);
//					}
//				}
//	            break;
//			}
//
//			case XmlPullParser.TEXT:
//				break;
//			}
		return result;
	}
	
}
