package com.cisco.nohttp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.util.Lg;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mathew
 */
public class NetWorkUtil {

    /**
     * @param context
     * @return
     */
    public static boolean hasNetwork(final Context context, CiscoApiInterface.UpdateUIEvents updateEvents) {
        ConnectivityManager con = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo workinfo = con.getActiveNetworkInfo();
        if (workinfo == null || !workinfo.isAvailable()) {
            //			updateEvents.onNetWorkFailed();
            new Thread(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(context, "请检查网络！", Toast.LENGTH_SHORT).show();
                }
            }).start();
//            Toast.makeText(context, "请检查网络！", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private static BasicHeader[] headers = new BasicHeader[10];
    static {
        headers[0] = new BasicHeader("Appkey", "");
    }
    public static String post(String url, HashMap<String, String> requestDataMap) {
        DefaultHttpClient client = new DefaultHttpClient();

        HttpPost post = new HttpPost(url);
        HttpParams params = new BasicHttpParams();//
        params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 3000);
        HttpConnectionParams.setSoTimeout(params, 3000);
        post.setParams(params);
        // 设置请求头
        // post.setHeaders(headers);
        Object obj = null;
        try {
            if (requestDataMap != null) {
                HashMap<String, String> map = requestDataMap;
                ArrayList<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    BasicNameValuePair pair = new BasicNameValuePair(
                            entry.getKey(), entry.getValue());
                    pairList.add(pair);
                }
                HttpEntity entity = new UrlEncodedFormEntity(pairList, "UTF-8");
                post.setEntity(entity);
            }
            HttpResponse response = client.execute(post);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(),
                        "UTF-8");
                Lg.e(NetWorkUtil.class.getSimpleName(), result);

                return result;
            }
        } catch (ClientProtocolException e) {
            Lg.e(NetWorkUtil.class.getSimpleName(), e.getLocalizedMessage());
        } catch (IOException e) {
            Lg.e(NetWorkUtil.class.getSimpleName(), e.getLocalizedMessage());
        }
        return null;
    }

    /**
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        Lg.e("----url", url);
        DefaultHttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        HttpParams params = new BasicHttpParams();//
        params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, 30000);
        HttpConnectionParams.setSoTimeout(params, 30000);
        get.setParams(params);
        // get.setHeaders(headers);
        Object obj = null;
        try {
            HttpResponse response = client.execute(get);
            // Lg.e("----response.getStatusLine().getStatusCode()",
            // response.getStatusLine().getStatusCode()+"");
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(),
                        "UTF-8");
                Lg.e("----result", url + "\n" + result);
                return result;

            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
            // Lg.e("网络——————————", e.getMessage());
        } catch (IOException e) {
            // Lg.e("网络——————————11111111111111", e.getMessage());
            e.printStackTrace();
        }
        return "null";
    }

    /**
     *
     * @param context
     * @return
     */
    public static boolean hasNetwork(Context context) {
        ConnectivityManager con = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo workinfo = con.getActiveNetworkInfo();
        if (workinfo == null || !workinfo.isAvailable()) {
            return false;
        }
        return true;
    }
}
