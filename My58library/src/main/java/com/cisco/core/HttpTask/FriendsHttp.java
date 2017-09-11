package com.cisco.core.HttpTask;

import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.entity.Friend;
import com.cisco.core.httpcallback.FriendsCallback;
import com.cisco.core.util.Constants;
import com.cisco.core.util.Lg;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.RequestMethod;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

import java.util.List;

/**
 * @author linpeng
 */
public class FriendsHttp {
    private String tag = "FriendsHttp";
    private FriendsCallback callback;

    //------------------------------------t通讯录 好友列表---------------------------------------------
    public void GetFriendsList(String userId, FriendsCallback callback) {
        this.callback = callback;
        String url = Constants.SERVER + Constants.URL_NOHTTP_Friends + userId;
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.GET);
        CallServer.getRequestInstance().add(0, request, httpfriendsListener, true, true);
    }

    private HttpListener<String> httpfriendsListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    String obj = rootNode.getJSONArray("obj").toJSONString();
                    List<Friend> friendsList = JSON.parseArray(obj, Friend.class);
                    callback.onSucess(friendsList);
                } else {
                    Log.e("异常", "获取失败");
                    callback.onFailed();
                }
            } else {
                callback.onFailed();
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            callback.onFailed();
        }
    };


    //------------------------------------十、	查询可用好友---------------------------------------------
    public void SelectFriendsList(String userId, FriendsCallback callback, String search) {
        this.callback = callback;
        String url;
        if (TextUtils.isEmpty(search)) {
            url = Constants.SERVER + Constants.URL_NOHTTP_SelectFriends + userId;
        } else {
            url = Constants.SERVER + Constants.URL_NOHTTP_SelectFriends + userId + "?search=" + search;
        }
        Request<String> request = NoHttp.createStringRequest(url);
        CallServer.getRequestInstance().add(0, request, httpselectfriendsListener, true, true);
    }

    private HttpListener<String> httpselectfriendsListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    String obj = rootNode.getJSONArray("obj").toJSONString();
                    List<Friend> friendsList = JSON.parseArray(obj, Friend.class);
                    callback.onSucess(friendsList);
                } else {
                    callback.onFailed();
                }
            } else {
                callback.onFailed();
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            callback.onFailed();
        }
    };

    //------------------------------------添加好友---------------------------------------------
    public void AddFriendsList(String friendId, String userId, FriendsCallback callback) {
        this.callback = callback;
        String url = Constants.SERVER + Constants.URL_NOHTTP_Friends + userId + "/" + friendId;
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.POST);
        CallServer.getRequestInstance().add(0, request, httpaddfriendsListener, true, true);
    }

    private HttpListener<String> httpaddfriendsListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    callback.onSucess(null);
                } else {
                    callback.onFailed();
                }
            } else {
                callback.onFailed();
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            callback.onFailed();
        }
    };

    //------------------------------------删除好友---------------------------------------------
    public void DeleteFriendsList(String friendId, String userId, FriendsCallback callback) {
        this.callback = callback;
        String url = Constants.SERVER + Constants.URL_NOHTTP_Friends + userId + "/" + friendId;
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.DELETE);
        CallServer.getRequestInstance().add(0, request, httpdeletefriendsListener, true, true);
    }

    private HttpListener<String> httpdeletefriendsListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    callback.onSucess(null);
                } else {
                    callback.onFailed();
                }
            } else {
                callback.onFailed();
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            callback.onFailed();
        }
    };


}
