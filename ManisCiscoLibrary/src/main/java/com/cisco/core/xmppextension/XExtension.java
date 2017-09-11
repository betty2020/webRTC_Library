package com.cisco.core.xmppextension;

import org.jivesoftware.extension.AbstractPacketExtension;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by linpeng
 */
public class XExtension extends AbstractPacketExtension {

    public static final String ELEMENT_NAME = "x";

    public static final String NAMESPACE = "http://igniterealtime.org/protocol/ofmeet";


    public String userAgentValue;
    public String userIdValue;
    public String configRoleValue;
    public String nickValue;
    public String audioMutedValue;
    public String videoMutedValue;
    public List<Source> sources = new ArrayList<>();
    public List<Stat> stats = new ArrayList<>();

    public List<Stat> getStats() {
        return stats;
    }

    public void setStats(List<Stat> stats) {
        this.stats = stats;
    }

    public void addStats(Stat stat) {
        this.stats.add(stat);
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public void addSource(Source source) {
        this.sources.add(source);
    }

    public String getUserAgentValue() {
        return userAgentValue;
    }


    public void setUserAgentValue(String userAgentValue) {
        this.userAgentValue = userAgentValue;
    }


    public String getUserIdValue() {
        return userIdValue;
    }


    public void setUserIdValue(String userIdValue) {
        this.userIdValue = userIdValue;
    }


    public String getConfigRoleValue() {
        return configRoleValue;
    }


    public void setConfigRoleValue(String configRoleValue) {
        this.configRoleValue = configRoleValue;
    }


    public String getNickValue() {
//        return nickValue !=null? nickValue: null;
        return nickValue !=null? nickValue: null;
    }


    public void setNickValue(String nickValue) {
        this.nickValue = nickValue;
    }


    public String getAudioMutedValue() {
        return audioMutedValue;
    }


    public void setAudioMutedValue(String audioMutedValue) {
        this.audioMutedValue = audioMutedValue;
    }


    public String getVideoMutedValue() {
        return videoMutedValue;
    }


    public void setVideoMutedValue(String videoMutedValue) {
        this.videoMutedValue = videoMutedValue;
    }


    public XExtension() {
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

    @Override
    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<").append(ELEMENT_NAME).append(" xmlns='" + NAMESPACE).append("'>");

        buf.append("<").append(UserAgentExtension.ELEMENT_NAME).append(" xmlns='" + UserAgentExtension.NAMESPACE).append("'>");
        //value
        buf.append(userAgentValue);
        buf.append("</").append(UserAgentExtension.ELEMENT_NAME).append(">");

        buf.append("<").append(UserId.ELEMENT_NAME).append(">");
        // UserId.value
        buf.append(userIdValue);
        buf.append("</").append(UserId.ELEMENT_NAME).append(">");

        buf.append("<").append(ConfigRole.ELEMENT_NAME).append(">");
        //ConfigRole value
        buf.append(configRoleValue);
        buf.append("</").append(ConfigRole.ELEMENT_NAME).append(">");

        buf.append("<").append(Nick.ELEMENT_NAME).append(" xmlns='" + Nick.NAMESPACE).append("'>");
        //nick value
        buf.append(nickValue);
        buf.append("</").append(Nick.ELEMENT_NAME).append(">");

        buf.append("<").append(AudioMuted.ELEMENT_NAME).append(" xmlns='" + AudioMuted.NAMESPACE).append("'>");
        //AudioMuted value
        buf.append(audioMutedValue);
        buf.append("</").append(AudioMuted.ELEMENT_NAME).append(">");

        buf.append("<").append(VideoMuted.ELEMENT_NAME).append(" xmlns='" + VideoMuted.NAMESPACE).append("'>");
        //VideoMuted value
        buf.append(videoMutedValue);
        buf.append("</").append(VideoMuted.ELEMENT_NAME).append(">");


        buf.append("<").append(StatsExtension.ELEMENT_NAME).append(" xmlns='").append(StatsExtension.NAMESPACE).append("'>");
//        statsvalues
        List<Stat> statList = this.getStats();
        for (Stat stat : statList) {
            buf.append("<stat name='").append(stat.getName())
                    .append("' value='").append(stat.getValue())
                    .append("' />");
        }
        buf.append("</").append(StatsExtension.ELEMENT_NAME).append(">");


        buf.append("<").append(MediaExtension.ELEMENT_NAME).append(" xmlns='").append(MediaExtension.NAMESPACE).append("'>");
        List<Source> list = this.getSources();
        for (Source source : list) {
            buf.append("<source type='").append(source.getType())
                    .append("' ssrc='").append(source.getSsrc())
                    .append("' direction='").append(source.getDirection())
                    .append("' />");
        }
        buf.append("</").append(MediaExtension.ELEMENT_NAME).append(">");
        buf.append("</").append(ELEMENT_NAME).append(">");
        return buf.toString();
    }


    public class UserAgentExtension extends AbstractPacketExtension {
        public static final String ELEMENT_NAME = "user-agent";
        public static final String NAMESPACE = "http://jitsi.org/jitmeet/user-agent";

        public UserAgentExtension() {
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

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append("<").append(ELEMENT_NAME).append(" xmlns=" + NAMESPACE).append(">");
            buf.append(this.getText());
            buf.append("</").append(ELEMENT_NAME).append(">");
            return buf.toString();
        }

    }

    public static class UserId extends AbstractPacketExtension {
        public static final String ELEMENT_NAME = "userId";
        public static final String NAMESPACE = "";

        public UserId() {
            super(NAMESPACE, ELEMENT_NAME);
        }

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append("<").append(ELEMENT_NAME).append(">");
            buf.append(this.getText());
            buf.append("</").append(ELEMENT_NAME).append(">");
            return buf.toString();
        }
    }

    public static class ConfigRole extends AbstractPacketExtension {

        public static final String ELEMENT_NAME = "configRole";
        public static final String NAMESPACE = "";

        public ConfigRole() {
            super(NAMESPACE, ELEMENT_NAME);
        }

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append("<").append(ELEMENT_NAME).append(">");
            buf.append(this.getText());
            buf.append("</").append(ELEMENT_NAME).append(">");
            return buf.toString();
        }
    }

    public static class Nick extends AbstractPacketExtension {

        public static final String ELEMENT_NAME = "nick";
        public static final String NAMESPACE = "http://jabber.org/protocol/nick";

        public Nick() {
            super(NAMESPACE, ELEMENT_NAME);
        }

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append("<").append(ELEMENT_NAME).append(" xmlns=" + NAMESPACE).append(">");
            buf.append(this.getText());
            buf.append("</").append(ELEMENT_NAME).append(">");
            return buf.toString();
        }
    }

    public static class AudioMuted extends AbstractPacketExtension {

        public static final String ELEMENT_NAME = "audiomuted";
        public static final String NAMESPACE = "http://jitsi.org/jitmeet/audio";

        public AudioMuted() {
            super(NAMESPACE, ELEMENT_NAME);
        }

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append("<").append(ELEMENT_NAME).append(" xmlns=" + NAMESPACE).append(">");
            buf.append(this.getText());
            buf.append("</").append(ELEMENT_NAME).append(">");
            return buf.toString();
        }
    }

    public static class VideoMuted extends AbstractPacketExtension {

        public static final String ELEMENT_NAME = "videomuted";
        public static final String NAMESPACE = "http://jitsi.org/jitmeet/video";

        public VideoMuted() {
            super(NAMESPACE, ELEMENT_NAME);
        }

        public String toXML() {
            StringBuilder buf = new StringBuilder();
            buf.append("<").append(ELEMENT_NAME).append(" xmlns=" + NAMESPACE).append(">");
            buf.append(this.getText());
            buf.append("</").append(ELEMENT_NAME).append(">");
            return buf.toString();
        }
    }

    public class StatsExtension extends AbstractPacketExtension {
        public static final String ELEMENT_NAME = "stats";
        public static final String NAMESPACE = "http://jitsi.org/jitmeet/stats";

        public StatsExtension() {
            super(NAMESPACE, ELEMENT_NAME);
        }

    }

    public static class Stat {

        private String name;
        private String value;

        public Stat() {
        }

        public Stat(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class MediaExtension extends AbstractPacketExtension {

        public static final String ELEMENT_NAME = "media";
        public static final String NAMESPACE = "http://estos.de/ns/mjs";

        public MediaExtension() {
            super(NAMESPACE, ELEMENT_NAME);
        }
    }

    public static class Source {

        private String type;
        private String ssrc;
        private String direction;

        public Source() {
        }

        public Source(String type, String ssrc, String direction) {
            this.type = type;
            this.ssrc = ssrc;
            this.direction = direction;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSsrc() {
            return ssrc;
        }

        public void setSsrc(String ssrc) {
            this.ssrc = ssrc;
        }

        public String getDirection() {
            return direction;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }
    }


}
