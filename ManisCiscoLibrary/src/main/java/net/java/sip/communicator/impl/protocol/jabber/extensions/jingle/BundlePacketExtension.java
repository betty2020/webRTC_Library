/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

/**
 * An 'ssrc' extension.
 * @author jilinpeng
 */
public class BundlePacketExtension
        extends AbstractPacketExtension
{
	
	/**
     * The namespace.
     */
    public static final String NAMESPACE =
        "http://estos.de/ns/bundle";
	
    /**
     * The name of the "encryption" element.
     */
    public static final String ELEMENT_NAME = "bundle";
//    /**
//     * The name of the label attribute.
//     */
//    public static final String LABEL_ATTR_NAME = "label";
//    /**
//     * The name of the mslabel attribute.
//     */
//    public static final String MSLABEL_ATTR_NAME = "mslabel";
//    
//    /**
//     * The name of the cname attribute.
//     */
//    public static final String CNAME_ATTR_NAME = "cname";
//    /**
//     * The name of the msid attribute.
//     */
//    public static final String MSID_ATTR_NAME = "msid";
    
    
    
    
    
    
    /**
     * Creates a new instance of <tt>RtcpmuxPacketExtension</tt>.
     */
    public BundlePacketExtension()
    {
        super(NAMESPACE, ELEMENT_NAME);
    }
    
  

    
}

