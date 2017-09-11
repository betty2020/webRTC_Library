/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package com.cisco.core.xmppextension;

import org.jivesoftware.smack.packet.IQ;

/**
 * A straightforward extension of the IQ. A <tt>JingleIQ</tt> object is created
 * by smack via the {@link MuteIQProvider}. It contains all the information
 * extracted from a <tt>jingle</tt> IQ.
 *
 * @author Emil Ivov
 */
public class MuteIQ extends IQ
{

    public static final String NAMESPACE = "http://jitsi.org/jitmeet/audio";
    public static final String ELEMENT_NAME = "mute";
    
    private String message;
    
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String getChildElementXML() {
		  StringBuilder bldr = new StringBuilder("<" + ELEMENT_NAME);
	        bldr.append(" xmlns='" + NAMESPACE + "'");
	        bldr.append(">"+message);
	        bldr.append("</" + ELEMENT_NAME + ">");
	        return bldr.toString();
	}

}
