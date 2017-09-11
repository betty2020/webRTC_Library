package com.cisco.core.HttpTask;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.entity.ConferenceRecord;
import com.cisco.core.httpcallback.ConferenceRecordCallback;
import com.cisco.core.util.Constants;
import com.cisco.core.util.Lg;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import java.util.List;

/**
 * Created by qihao on 2017/6/28 19:46
 * 2017 to: 邮箱：sin2t@sina.com
 * androidApp 查询会议
 */

public class searchMeeting {

    private ConferenceRecordCallback callback;

    public void searchConference(int count, int number, String userId, ConferenceRecordCallback callback,String search) {
        this.callback = callback;
        String url = Constants.SERVER + Constants.URL_NOHTTP_Conferences + "count=" + count + "&page=" + number + "&userId=" + userId+"$search="+search;
        Request<String> request = NoHttp.createStringRequest(url);
        CallServer.getRequestInstance().add( 0, request, httpSearchListener, true, true);
    }

    private HttpListener<String> httpSearchListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i("查询会议", "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    int count = Integer.parseInt(rootNode.getString("msg"));
                    String obj = rootNode.getJSONArray("obj").toJSONString();
                    List<ConferenceRecord> conferenceRecordList = JSON.parseArray(obj, ConferenceRecord.class);
                    callback.onSucess(conferenceRecordList, count);
                } else {
                    callback.onFailed(rootNode.getString("msg"));
                }
            }else {
                callback.onFailed("参数错误");
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            callback.onFailed(message.getMessage());
        }
    };

}
