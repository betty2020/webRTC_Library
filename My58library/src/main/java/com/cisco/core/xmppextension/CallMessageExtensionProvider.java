package com.cisco.core.xmppextension;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class CallMessageExtensionProvider implements PacketExtensionProvider {
	@Override
	public PacketExtension parseExtension(XmlPullParser parser)
			throws Exception {
		CallMessageExtension result = new CallMessageExtension();
		boolean done = false;
		while (!done)
			switch (parser.next()) {
			case XmlPullParser.END_TAG: {
				String name = parser.getName();
				if (XExtension.ELEMENT_NAME.equals(name)) {
					done = true;
				}
				break;
			}
			case XmlPullParser.START_TAG: {
				String elementName = parser.getName();
                if(elementName.equals("call")){
					int count = parser.getAttributeCount();
					if (count > 0) {
						String type = parser.getAttributeValue("", "type");
						String roomID = parser.getAttributeValue("", "roomID");
						String resource = parser.getAttributeValue("", "resource");
						String nickname = parser.getAttributeValue("", "nickname");
						result.setType(type);
						result.setRoomID(roomID);
						result.setNickname(nickname);
						result.setResource(resource);
					}
				}
	            break;
			}

			case XmlPullParser.TEXT:
				break;
			}
		return result;
	}
	
	private static String parseContent(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		int parserDepth = parser.getDepth();
		return parseContentDepth(parser, parserDepth);
	}

	public static String parseContentDepth(XmlPullParser parser, int depth)
			throws XmlPullParserException, IOException {
		StringBuffer content = new StringBuffer();
		while (!(parser.next() == XmlPullParser.END_TAG && parser.getDepth() == depth)) {
			content.append(parser.getText());
			System.out.println("--------------elementName="+parser.getName()+"------values="+parser.getText());
		}
		return content.toString();
	}
}
