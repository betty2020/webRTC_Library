package com.cisco.core.HttpTask;

import android.content.Context;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.util.Constants;
import com.cisco.core.util.Lg;
import com.cisco.core.util.Tools;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;
import com.cisco.nohttp.CallServer;
import com.cisco.nohttp.HttpListener;
import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.Response;

/**
 * @author linpeng
 * 获取主持人
 */
public class GetModeratorHttp {
    private String jid;
    private String roomnumber;
    private String hostPass;
    private Context context;
    private String tag="AdminHttp";

    public GetModeratorHttp(Context context, String jid, String roomnumber, String hostPass) {
        this.context=context;
        this.jid = jid;
        this.roomnumber = roomnumber;
        this.hostPass=hostPass;
    }
    public void GetModerator(){
        Request<String> request = NoHttp.createStringRequest( Constants.SERVER + Constants.URL_NOHTTP_GetModerator +"j="+jid+"&r=" + roomnumber+ "&p=" + Tools.get32MD5Str(hostPass)  );
        CallServer.getRequestInstance().add(context, 0, request,
                httpGetModeratorListener, false, true);
    }
    private HttpListener<String> httpGetModeratorListener = new HttpListener<String>() {
        @Override
        public void onSucceed(int what, Response<String> response) {
            String result = response.get();
            Lg.i(tag, "GetModerator,result=" + result);
            if(result.contains("code")){
                JSONObject rootNode= JSON.parseObject(result);
                String codeNode =rootNode.getString("code");
                if (codeNode.equals("200")) {
                    //授权成功！
                    Toast.makeText(context, "获取主持人成功！", Toast.LENGTH_SHORT).show();
                    Key.Moderator=true;
                    XmppConnection.getInstance().changeConfigRole("CRUO");
                }else {
                    Toast.makeText(context, "获取主持人失败！", Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(context, "获取主持人失败！", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailed(int what, String url, Object tag,
                             Exception message, int responseCode, long networkMillis) {
            Toast.makeText(context, "请求失败！", Toast.LENGTH_SHORT).show();
        }
    };



}
