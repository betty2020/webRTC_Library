package com.cisco.core.xmppextension;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class XExtensionProvider implements PacketExtensionProvider {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public PacketExtension parseExtension(XmlPullParser parser)
			throws Exception {
		XExtension result = new XExtension();
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
				if(elementName.equals("user-agent")){
					String context = parseContent(parser);
					result.setUserAgentValue(context);
				}
				if(elementName.equals("userId")){
					String context = parseContent(parser);
					result.setUserIdValue(context);
				}
				if(elementName.equals("configRole")){
					String context = parseContent(parser);
					result.setConfigRoleValue(context);
				}
				if(elementName.equals("nick")){
					String context = parseContent(parser);
					result.setNickValue(context);
				}
				if(elementName.equals("audiomuted")){
					String context = parseContent(parser);
					result.setAudioMutedValue(context);
				}
				if(elementName.equals("videomuted")){
					String context = parseContent(parser);
					result.setVideoMutedValue(context);
				}
				if(elementName.equals("media")){
				}
				if(elementName.equals("source")){
						int count = parser.getAttributeCount();
						if (count > 0) {
							String type = parser.getAttributeValue("", "type");
							String ssrc = parser.getAttributeValue("", "ssrc");
							String direction = parser.getAttributeValue("", "direction");
							result.addSource(new XExtension.Source(type,ssrc,direction));
					}
				}
				if(elementName.equals("stat")){
					int count = parser.getAttributeCount();
					if (count > 0) {
						String name = parser.getAttributeValue("", "name");
						String value = parser.getAttributeValue("", "value");
						result.addStats(new XExtension.Stat(name,value));
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
//			System.out.println("--------------elementName="+parser.getName()+"------values="+parser.getText());
		}
		return content.toString();
	}
}
