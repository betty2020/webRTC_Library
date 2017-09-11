package com.cisco.core.entity;

/**
 * @author linpeng
 */
public class Stats {
    private int sendBandwidth;//上行宽带
    private int receiveBandwidth;//下行宽带
    private String sendPacketLoss;//上行丢包率
    private String receivePacketLoss;//下行丢包率

    public int getSendBandwidth() {
        return sendBandwidth;
    }

    public void setSendBandwidth(int sendBandwidth) {
        this.sendBandwidth = sendBandwidth;
    }

    public int getReceiveBandwidth() {
        return receiveBandwidth;
    }

    public void setReceiveBandwidth(int receiveBandwidth) {
        this.receiveBandwidth = receiveBandwidth;
    }

    public String getSendPacketLoss() {
        return sendPacketLoss;
    }

    public void setSendPacketLoss(String sendPacketLoss) {
        this.sendPacketLoss = sendPacketLoss;
    }

    public String getReceivePacketLoss() {
        return receivePacketLoss;
    }

    public void setReceivePacketLoss(String receivePacketLoss) {
        this.receivePacketLoss = receivePacketLoss;
    }

    public Stats() {
    }
}
