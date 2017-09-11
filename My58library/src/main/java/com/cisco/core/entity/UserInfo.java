package com.cisco.core.entity;

/**
 * @author linpeng
 */
public class UserInfo {
    private String phone;
    private String email;
    private String mUserName;
    private String endpoint;
    private String mJid;
    private String mUserId;

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getmJid() {
        return mJid;
    }

    public void setmJid(String mJid) {
        this.mJid = mJid;
    }
}
