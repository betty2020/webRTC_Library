/*
 * Copyright © YOLANDA. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cisco.nohttp;

import android.content.Context;

import com.yolanda.nohttp.NoHttp;
import com.yolanda.nohttp.download.DownloadQueue;
import com.yolanda.nohttp.rest.Request;
import com.yolanda.nohttp.rest.RequestQueue;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created in Oct 23, 2015 1:00:56 PM
 *
 * @author YOLANDA
 */
public class CallServer {

    private static CallServer callServer;

    /**
     * 请求队列
     */
    private RequestQueue requestQueue;

    /**
     * 下载队列
     */
    private static DownloadQueue downloadQueue;

    private CallServer() {
        requestQueue = NoHttp.newRequestQueue();
    }

    /**
     * 请求队列
     */
    public synchronized static CallServer getRequestInstance() {
        if (callServer == null)
            callServer = new CallServer();
        return callServer;
    }

    /**
     * 下载队列
     */
    public static DownloadQueue getDownloadInstance() {
        if (downloadQueue == null)
            downloadQueue = NoHttp.newDownloadQueue();
        return downloadQueue;
    }
    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }

    public <T> void add(int what, Request<T> request, HttpListener<T> callback, boolean canCancel, boolean isLoading) {
        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new TrustAnyTrustManager()},
                    new java.security.SecureRandom());
            request.setSSLSocketFactory(sc.getSocketFactory());
            request.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        requestQueue.add(what, request, new HttpResponseListener<T>(request, callback, canCancel, isLoading));
    }

    public void cancelBySign(Object sign) {
        requestQueue.cancelBySign(sign);
    }

    public void cancelAll() {
        requestQueue.cancelAll();
    }

    public void stopAll() {
        requestQueue.stop();
    }

}
