package com.cisco.core.xmppextension;

import android.util.Log;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.PacketExtensionProvider;
import org.xmlpull.v1.XmlPullParser;

public class AppShareExtensionProvider implements PacketExtensionProvider {

	@Override
	public PacketExtension parseExtension(XmlPullParser parser)
			throws Exception {
		AppShareExtension result = new AppShareExtension();
		int count = parser.getAttributeCount();
		Log.d("appshare1","linpeng,count:"+count);
		for(int i=0;i<count;i++){
			Log.d("appshare1","linpeng,getAttributeName:"+parser.getAttributeName(i)+",getAttributeValue:"+ parser.getAttributeValue(i));
			if(parser.getAttributeName(i).equals("action")){
				String action = parser.getAttributeValue(i).trim();
				result.setAction(action);
			}
			if(parser.getAttributeName(i).equals("url")){
				String url = parser.getAttributeValue(i).trim();
				result.setUrl(url);
			}
		}
		return result;
	}

}
