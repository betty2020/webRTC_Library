package com.cisco.nohttp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.cisco.core.interfaces.CiscoApiInterface;

/**
 * 
 * @author Mathew
 * 
 */
public class NetWorkUtil {

	/**
	 * 
	 * @param context
	 * @return
	 */
	public static boolean hasNetwork(final Context context, CiscoApiInterface.UpdateUIEvents updateEvents) {
		ConnectivityManager con = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workinfo = con.getActiveNetworkInfo();
		if (workinfo == null || !workinfo.isAvailable()) {
//			updateEvents.onNetWorkFailed();
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					Toast.makeText(context, "请检查网络！", Toast.LENGTH_SHORT).show();
//				}
//			}).start();
//			Toast.makeText(context, "请检查网络！",Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	// 是否网络
	public static boolean ONLINESTATUS = false;
	private final String NETSETING = "netSetting.properties";
	/**
	 * 判断网络情况
	 *
	 * @param context
	 *            上下文
	 * @return false 表示没有网络 true 表示有网络
	 */
	public static boolean isNetworkAvalible(Context context) {
		boolean isOk = false;

		// 获得网络状态管理器
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			isOk = false;
		} else {
			// 建立网络数组
			NetworkInfo[] net_info = connectivityManager.getAllNetworkInfo();
			if (net_info != null) {
				for (int i = 0; i < net_info.length; i++) {
					// 判断获得的网络状态是否是处于连接状态
					if (net_info[i].getState() == NetworkInfo.State.CONNECTED) {
						isOk = true;
					}
				}
			}
		}
		if (!isOk) {
			Toast.makeText(context, "没有网络", Toast.LENGTH_SHORT)
					.show();
		}
		return isOk;
	}

	public static boolean isNetworkAvalibleService(Context context) {
		boolean isOk = false;

		// 获得网络状态管理器
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager == null) {
			isOk = false;
		} else {
			// 建立网络数组
			NetworkInfo[] net_info = connectivityManager.getAllNetworkInfo();
			if (net_info != null) {
				for (int i = 0; i < net_info.length; i++) {
					// 判断获得的网络状态是否是处于连接状态
					if (net_info[i].getState() == NetworkInfo.State.CONNECTED) {
						isOk = true;
					}
				}
			}
		}
		if (!isOk) {
		}
		return isOk;
	}

}
