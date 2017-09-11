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

/**
 * @author linpeng
 */
public class DeleteConferenceHttp {

    private String tag = "DeleteConferenceHttp";
    private MyCallback callback;


    public void deleteConference(String mConferenceId, MyCallback callback) {
        this.callback = callback;
        String url = Constants.SERVER + Constants.URL_NOHTTP_Conferences2 + mConferenceId;
        Request<String> request = NoHttp.createStringRequest(url, RequestMethod.DELETE);
        CallServer.getRequestInstance().add(0, request, httpdeleteListener, true, true);
    }

    private HttpListener<String> httpdeleteListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "result=" + result);
            if (!result.contains("<!DOCTYPE html>")) {
                JSONObject rootNode = JSON.parseObject(result);
                String codeNode = rootNode.getString("code");
                if (codeNode.equals("200")) {
                    callback.onSucess("删除成功");
                } else {
                    callback.onFailed("删除失败");
                }
            } else {
                callback.onFailed("异常请求");
                Log.e("异常", "上送参数错误");
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            callback.onFailed(message.toString());
//            Toast.makeText(context, "请求失败！", Toast.LENGTH_SHORT).show();
        }
    };


}
