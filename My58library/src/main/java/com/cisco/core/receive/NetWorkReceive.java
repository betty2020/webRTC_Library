package com.cisco.core.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * @author linpeng
 */
public class NetWorkReceive extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        //Toast.makeText(context, intent.getAction(), 1).show();
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo activeInfo = manager.getActiveNetworkInfo();
        //Toast.makeText(context, "mobile:"+mobileInfo.isConnected()+"\n"+"wifi:"+wifiInfo.isConnected() +"\n"+"active:"+activeInfo.getTypeName(),Toast.LENGTH_SHORT).show();
        if (!mobileInfo.isConnected() && !wifiInfo.isConnected()) {
            Toast.makeText(context, "网络不可以用",Toast.LENGTH_SHORT).show();
        }else {
        }
    }  //如果无网络连接activeInfo为null
}
