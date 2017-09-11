package com.cisco.core.HttpTask;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.httpcallback.MyCallback;
import com.cisco.core.util.Constants;
import com.cisco.core.util.Lg;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author linpeng
 */
public class ReservationHttp {
    private String tag = "ReservationHttp";
    private MyCallback callback;

    public void ReservationInfo(String userId, MyCallback callback, String title, String startTime,
                                String lengthTime, String number, String meetPassword,
                                String ownerPassword, String cycle,
                                String cycleDmy, String cycleStartPre,
                                String cycleStart, String cycleEndDay) {
        this.callback = callback;
        String url = Constants.SERVER + Constants.URL_NOHTTP_Conferences2;
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.POST);
        List<Map<String, String>> mConferenceUsers = new ArrayList<>();
        Map<String, String> map = new HashMap<String, String>();
        map.put("mUserId", userId);
        mConferenceUsers.add(map);
        FastJsonString users = new FastJsonString(title, startTime, lengthTime, number, meetPassword,
                ownerPassword, cycle, cycleDmy, cycleStartPre, cycleStart, cycleEndDay, mConferenceUsers);
        String jsonString = JSON.toJSONString(users);
        Log.e("qihao", "jsonString:" + jsonString);
        request.setDefineRequestBodyForJson(jsonString);
        CallServer.getRequestInstance().add(0, request, httpfriendsListener, false, false);
    }

    class FastJsonString {
        String title;
        String startTime;
        String lengthTime;
        String number;
        String meetPassword;
        String ownerPassword;
        String cycle;
        String cycleDmy;
        String cycleStartPre;
        String cycleStart;
        String cycleEndDay;
        List<Map<String, String>> mConferenceUsers;

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

        public List<Map<String, String>> getmConferenceUsers() {
            return mConferenceUsers;
        }

        public void setmConferenceUsers(List<Map<String, String>> mConferenceUsers) {
            this.mConferenceUsers = mConferenceUsers;
        }

        public FastJsonString(String title, String startTime, String lengthTime, String number,
                              String meetPassword, String ownerPassword, String cycle,
                              String cycleDmy, String cycleStartPre, String cycleStart,
                              String cycleEndDay, List<Map<String, String>> mConferenceUsers) {
            this.title = title;
            this.startTime = startTime;
            this.lengthTime = lengthTime;
            this.number = number;
            this.meetPassword = meetPassword;
            this.ownerPassword = ownerPassword;
            this.cycle = cycle;
            this.cycleDmy = cycleDmy;
            this.cycleStartPre = cycleStartPre;
            this.cycleStart = cycleStart;
            this.cycleEndDay = cycleEndDay;
            this.mConferenceUsers = mConferenceUsers;

        }

        /**
         * {
         "title": "test Client",//标题
         "startTime": 1492597433536,//一次性会议的开始时间
         "lengthTime": 60,//单位秒,会议时长
         "number": 1,//人数
         "meetPassword": null,//会议室密码，可为空
         "ownerPassword": null,//主持人密码,可为空
         "cycle": 1,//0:一次性会议，1:周期会议
         "cycleDmy": 0,//0:日循环,1:周循环,2:月循环
         "cycleStartPre": 0,//天循环:0;周循环:0"星期日", 1"星期一", 2"星期二", 3"星期三", 4"星期四", 5"星期五", 6"星期六";月循环1~31
         "cycleStart": 60,//单位秒,周期会议每天的开始时间，从0点0分到开始时间的秒数，
         "cycleEndDay": 1492597433536,//会议到期结束时间
         "mConferenceUsers": [
         {
         "mUserId": "5E41DD5B17E6A8EE"//用户id
         }
         ]
         }
         */
    }

    public static class userinfo {
        public userinfo() {

        }

        private String mUserId;

        public String getmUserId() {
            return mUserId;
        }

        public void setmUserId(String mUserId) {
            this.mUserId = mUserId;
        }
    }

    private HttpListener<String> httpfriendsListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            if (!result.contains("<!DOCTYPE html>")) {
                Lg.i(tag, "result=" + result);
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    callback.onSucess("成功");
                } else {
                    callback.onFailed("参数失败");
                }
            } else {
                callback.onFailed("参数失败");
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            callback.onSucess("网络异常");
        }
    };


}
