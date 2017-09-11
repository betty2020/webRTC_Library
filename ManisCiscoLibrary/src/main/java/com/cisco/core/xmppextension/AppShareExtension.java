package com.cisco.core.xmppextension;

import org.jivesoftware.extension.AbstractPacketExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linpeng
 */
public class AppShareExtension extends AbstractPacketExtension {

    public static final String ELEMENT_NAME = "appshare";

    public static final String NAMESPACE = "http://igniterealtime.org/protocol/appshare";

    public AppShareExtension() {
        super(NAMESPACE, ELEMENT_NAME);
    }
    public String action;
    public String url;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(ELEMENT_NAME).append(" xmlns='").append(NAMESPACE).append("'")
                .append("action='").append(getAction()).append("'")
                .append("url='").append(getUrl()).append("'")
                .append("'>");
        buf.append("</").append(ELEMENT_NAME).append(">");
        return buf.toString();
    }
}
