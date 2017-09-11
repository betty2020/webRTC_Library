package com.cisco.core.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author linpeng
 */
public class Participant {
    private String nickname;//昵称
    private String userid;//id
    private boolean isHost;//是否主持人
    private boolean getMuteMic;//麦克风状态
    private String jid;//每个人的jid

    private Map<String ,String> map=new HashMap<String ,String>();//video 和audio ssrc

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public boolean isHost() {
        return isHost;
    }

    public void setHost(boolean host) {
        isHost = host;
    }

    public boolean isGetMuteMic() {
        return getMuteMic;
    }

    public void setGetMuteMic(boolean getMuteMic) {
        this.getMuteMic = getMuteMic;
    }

//    @Override
//    public boolean equals(Object obj) {
//        Participant p=(Participant)obj;
//        return  nickname.equals(p.nickname)&& userid.equals(p.userid)&& isHost==(p.isHost)&& getMuteMic==(p.getMuteMic) ;
//    }
//    @Override
//    public int hashCode() {
//        String in = nickname+userid+isHost+getMuteMic;
//        return in.hashCode();
//    }
    public Participant(String nickname, String userid, boolean isHost, boolean getMuteMic) {
        this.nickname = nickname;
        this.userid = userid;
        this.isHost = isHost;
        this.getMuteMic = getMuteMic;
    }

    public Participant() {
    }
}
