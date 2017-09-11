package com.cisco.core.xmppextension;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class X2ExtensionProvider implements PacketExtensionProvider {
	/**
	 * {@inheritDoc}
	 */
	@Override
	public PacketExtension parseExtension(XmlPullParser parser)
			throws Exception {
		X2Extension result = new X2Extension();
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
				if(elementName.equals("item")){
						int count = parser.getAttributeCount();
						if (count > 0) {
							String affiliation = parser.getAttributeValue("", "affiliation");
							String jid = parser.getAttributeValue("", "jid");
							String role = parser.getAttributeValue("", "role");
							result.setAffiliationValue(affiliation);
							result.setJidValue(jid);
							result.setRoleValue(role);
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
		}
		return content.toString();
	}
}
