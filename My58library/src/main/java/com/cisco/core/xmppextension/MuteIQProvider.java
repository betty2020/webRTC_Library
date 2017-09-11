/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package com.cisco.core.xmppextension;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

//import org.jitsi.org.xmlpull.v1.*;

/**
 * An implementation of a Jingle IQ provider that parses incoming Jingle IQs.
 * 
 * @author Emil Ivov
 */
public class MuteIQProvider implements IQProvider {
	
	
	@Override
	public IQ parseIQ(XmlPullParser parser) throws Exception {
		MuteIQ mi = new MuteIQ();
		String elementName = parser.getName();
		if (elementName.equals("mute")) {
			String context = parseContent(parser);
			mi.setMessage(context);
		}
		return mi;
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
