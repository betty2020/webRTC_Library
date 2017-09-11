package com.cisco.core.xmppextension;

import org.jivesoftware.extension.AbstractPacketExtension;

/**
 * Created by linpeng
 */
public class X2Extension extends AbstractPacketExtension {

    public static final String ELEMENT_NAME = "x";

    public static final String NAMESPACE = "http://jabber.org/protocol/muc#user";

    public X2Extension() {
        super(NAMESPACE, ELEMENT_NAME);
    }

   private String affiliationValue;
    private String jidValue;
    private String roleValue;

    public String getAffiliationValue() {
        return affiliationValue;
    }

    public void setAffiliationValue(String affiliationValue) {
        this.affiliationValue = affiliationValue;
    }

    public String getJidValue() {
        return jidValue;
    }

    public void setJidValue(String jidValue) {
        this.jidValue = jidValue;
    }

    public String getRoleValue() {
        return roleValue;
    }

    public void setRoleValue(String roleValue) {
        this.roleValue = roleValue;
    }

    @Override
    public String getElementName() {
        return ELEMENT_NAME;
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(ELEMENT_NAME).append(" xmlns=\"" + NAMESPACE).append("\">");
        buf.append("<").append(ItemExtension.ELEMENT_NAME).append(" affiliation=\"" + affiliationValue)
                .append("\"  jid=\"" + jidValue)
                .append("\" role=\"" + roleValue)
                .append("\" >");
        buf.append("</").append(ItemExtension.ELEMENT_NAME).append(">");

        buf.append("</").append(ELEMENT_NAME).append(">");
        return buf.toString();
    }


    public class ItemExtension extends AbstractPacketExtension {
        public static final String ELEMENT_NAME = "item";
        public static final String NAMESPACE = "";

        public ItemExtension() {
            super(NAMESPACE, ELEMENT_NAME);
        }

        @Override
        public String getElementName() {
            return ELEMENT_NAME;
        }

        @Override
        public String getNamespace() {
            return NAMESPACE;
        }
    }



}
