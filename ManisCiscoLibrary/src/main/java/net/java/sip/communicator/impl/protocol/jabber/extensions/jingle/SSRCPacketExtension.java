/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.net.URI;

/**
 * An 'ssrc' extension.
 * @author jilinpeng
 */
public class SSRCPacketExtension
        extends AbstractPacketExtension
{
	
	/**
     * The namespace.
     */
    public static final String NAMESPACE =
        "http://estos.de/ns/ssrc";
	
    /**
     * The name of the "encryption" element.
     */
    public static final String ELEMENT_NAME = "ssrc";
    /**
     * The name of the label attribute.
     */
    public static final String LABEL_ATTR_NAME = "label";
    /**
     * The name of the mslabel attribute.
     */
    public static final String MSLABEL_ATTR_NAME = "mslabel";
    
    /**
     * The name of the cname attribute.
     */
    public static final String CNAME_ATTR_NAME = "cname";
    /**
     * The name of the msid attribute.
     */
    public static final String MSID_ATTR_NAME = "msid";
    
    
    
    
    
    
    /**
     * Creates a new instance of <tt>RtcpmuxPacketExtension</tt>.
     */
    public SSRCPacketExtension()
    {
        super(NAMESPACE, ELEMENT_NAME);
    }
    
    /**
     * Set the Label.
     *
     * @param id ID to Label
     */
    public void setLabel(String label)
    {
        setAttribute(LABEL_ATTR_NAME, label);
    }

    /**
     * Get the Label.
     *
     * @return the Label
     */
    public String getLabel()
    {
        return getAttributeAsString(LABEL_ATTR_NAME);
    }
   
    public void setMslabel(String  mslabel)
    {
        setAttribute(MSLABEL_ATTR_NAME,mslabel);
    }

    
    public URI getURI()
    {
        return getAttributeAsURI(MSLABEL_ATTR_NAME);
    }

    
    public void setCname(String  cname)
    {
        setAttribute(CNAME_ATTR_NAME,cname);
    }

    
    public URI getCname()
    {
        return getAttributeAsURI(CNAME_ATTR_NAME);
    }

    
    public void setMsid(String  msid)
    {
        setAttribute(MSID_ATTR_NAME,msid);
    }

    
    public URI getMsid()
    {
        return getAttributeAsURI(MSID_ATTR_NAME);
    }

    
}

