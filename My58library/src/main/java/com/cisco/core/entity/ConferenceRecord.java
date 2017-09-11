package com.cisco.core.entity;

import java.io.Serializable;
import java.util.List;

/**
 * @author linpeng
 */
public class ConferenceRecord implements Serializable {

//    {"code":200,"msg":"287","obj":
//        [{"mConferenceId":"1309D3930C993B09",
//            "cNumber":206020065,
//            "status":"MSE0",
//            "title":null,
//            "startTime":1487301946387,
//            "lengthTime":209,
//            "number":1,
//            "meetPassword":null,
//            "ownerPassword":"86731050",
//            "cycle":0,
//            "cycleDmy":0,
//            "cycleStartPre":0,
//            "cycleStart":0,
//            "cycleEndDay":1487301946387,
//            "layout":0,
//            "createTime":1487301946387,
//            "updateTime":1487301946387,
//            "type":"CTOT",
//            "notice":null,
//            "mConferenceUsers":[
//                {"mUserId":"72A9830E46F25CE2","mConferenceId":"1309D3930C993B09","owner":"CRUO","userName":"林鹏"}]}

    private static final long serialVersionUID = 1L;
        private String mConferenceId;//
        private String cNumber;//title
        private String status;//
        private String title;//
        private String startTime;//
        private String lengthTime;//
        private String number;//
        private String meetPassword;//
        private String ownerPassword;//
        private String cycle;//
        private String cycleDmy;//
        private String cycleStartPre;//
        private String cycleStart;//
        private String cycleEndDay;//
        private String layout;//
        private String createTime;//
        private String updateTime;//
        private String type;//
        private String notice;//
        private List<ConferenceUser> mConferenceUsers;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getmConferenceId() {
        return mConferenceId;
    }

    public void setmConferenceId(String mConferenceId) {
        this.mConferenceId = mConferenceId;
    }

    public String getcNumber() {
        return cNumber;
    }

    public void setcNumber(String cNumber) {
        this.cNumber = cNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getLengthTime() {
        return lengthTime;
    }

    public void setLengthTime(String lengthTime) {
        this.lengthTime = lengthTime;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getMeetPassword() {
        return meetPassword;
    }

    public void setMeetPassword(String meetPassword) {
        this.meetPassword = meetPassword;
    }

    public String getOwnerPassword() {
        return ownerPassword;
    }

    public void setOwnerPassword(String ownerPassword) {
        this.ownerPassword = ownerPassword;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getCycleDmy() {
        return cycleDmy;
    }

    public void setCycleDmy(String cycleDmy) {
        this.cycleDmy = cycleDmy;
    }

    public String getCycleStartPre() {
        return cycleStartPre;
    }

    public void setCycleStartPre(String cycleStartPre) {
        this.cycleStartPre = cycleStartPre;
    }

    public String getCycleStart() {
        return cycleStart;
    }

    public void setCycleStart(String cycleStart) {
        this.cycleStart = cycleStart;
    }

    public String getCycleEndDay() {
        return cycleEndDay;
    }

    public void setCycleEndDay(String cycleEndDay) {
        this.cycleEndDay = cycleEndDay;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public List<ConferenceUser> getmConferenceUsers() {
        return mConferenceUsers;
    }

    public void setmConferenceUsers(List<ConferenceUser> mConferenceUsers) {
        this.mConferenceUsers = mConferenceUsers;
    }
    public ConferenceRecord(){

    }

    public static class ConferenceUser {
        private String mUserId;//
        private String mConferenceId;//
        private String owner;//
        private String userName;//

        public String getmUserId() {
            return mUserId;
        }

        public void setmUserId(String mUserId) {
            this.mUserId = mUserId;
        }

        public String getmConferenceId() {
            return mConferenceId;
        }

        public void setmConferenceId(String mConferenceId) {
            this.mConferenceId = mConferenceId;
        }

        public String getOwner() {
            return owner;
        }

        public void setOwner(String owner) {
            this.owner = owner;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public ConferenceUser(){

        }
    }

}
