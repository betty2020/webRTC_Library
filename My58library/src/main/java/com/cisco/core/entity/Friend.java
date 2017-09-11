package com.cisco.core.entity;

import java.io.Serializable;

/**
 * @author linpeng
 */
public class Friend implements Serializable {
    private static final long serialVersionUID = 1L;
    private String mUserId;//
    private String userName;//
    private String loginName;//
    private String loginPassword;//
    private String status;//
    private String accessToken;//
    private String createTime;//
    private String lastTime;//
    private String email;//
    private String phone;//
    private String tel;//
    private String level;//
    private String attribution;//
    private String type;//
    private String ipSip;//
    private String protocol;//
    private String creater;//
    private String info;//
    private String code;//
    private String mOrganizationId;//
    private String mRoleId;//
    private String oname;//
    private String label;//
    private String friendUserId;//
    private String fUserId;//
    private String serialUser;//

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(String loginPassword) {
        this.loginPassword = loginPassword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastTime() {
        return lastTime;
    }

    public void setLastTime(String lastTime) {
        this.lastTime = lastTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAttribution() {
        return attribution;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIpSip() {
        return ipSip;
    }

    public void setIpSip(String ipSip) {
        this.ipSip = ipSip;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getCreater() {
        return creater;
    }

    public void setCreater(String creater) {
        this.creater = creater;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getmOrganizationId() {
        return mOrganizationId;
    }

    public void setmOrganizationId(String mOrganizationId) {
        this.mOrganizationId = mOrganizationId;
    }

    public String getmRoleId() {
        return mRoleId;
    }

    public void setmRoleId(String mRoleId) {
        this.mRoleId = mRoleId;
    }

    public String getOname() {
        return oname;
    }

    public void setOname(String oname) {
        this.oname = oname;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public String getfUserId() {
        return fUserId;
    }

    public void setfUserId(String fUserId) {
        this.fUserId = fUserId;
    }

    public String getSerialUser() {
        return serialUser;
    }

    public void setSerialUser(String serialUser) {
        this.serialUser = serialUser;
    }
    public Friend(){

    }

    public Friend(String mUserId, String userName, String loginName, String loginPassword, String status, String accessToken, String createTime, String lastTime, String email, String phone, String tel, String level, String attribution, String type, String ipSip, String protocol, String creater, String info, String code, String mOrganizationId, String mRoleId, String oname, String label, String friendUserId, String fUserId, String serialUser) {
        this.mUserId = mUserId;
        this.userName = userName;
        this.loginName = loginName;
        this.loginPassword = loginPassword;
        this.status = status;
        this.accessToken = accessToken;
        this.createTime = createTime;
        this.lastTime = lastTime;
        this.email = email;
        this.phone = phone;
        this.tel = tel;
        this.level = level;
        this.attribution = attribution;
        this.type = type;
        this.ipSip = ipSip;
        this.protocol = protocol;
        this.creater = creater;
        this.info = info;
        this.code = code;
        this.mOrganizationId = mOrganizationId;
        this.mRoleId = mRoleId;
        this.oname = oname;
        this.label = label;
        this.friendUserId = friendUserId;
        this.fUserId = fUserId;
        this.serialUser = serialUser;
    }

    @Override
    public String toString() {
        return "Friend{" +
                "mUserId='" + mUserId + '\'' +
                ", userName='" + userName + '\'' +
                ", loginName='" + loginName + '\'' +
                ", loginPassword='" + loginPassword + '\'' +
                ", status='" + status + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", createTime='" + createTime + '\'' +
                ", lastTime='" + lastTime + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", tel='" + tel + '\'' +
                ", level='" + level + '\'' +
                ", attribution='" + attribution + '\'' +
                ", type='" + type + '\'' +
                ", ipSip='" + ipSip + '\'' +
                ", protocol='" + protocol + '\'' +
                ", creater='" + creater + '\'' +
                ", info='" + info + '\'' +
                ", code='" + code + '\'' +
                ", mOrganizationId='" + mOrganizationId + '\'' +
                ", mRoleId='" + mRoleId + '\'' +
                ", oname='" + oname + '\'' +
                ", label='" + label + '\'' +
                ", friendUserId='" + friendUserId + '\'' +
                ", fUserId='" + fUserId + '\'' +
                ", serialUser='" + serialUser + '\'' +
                '}';
    }
}
