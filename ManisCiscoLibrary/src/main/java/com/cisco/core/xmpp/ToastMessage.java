package com.cisco.core.xmpp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
/**
 * 打log的工具类
 * @author Administrator
 *
 */

public class ToastMessage {
	
	public static Toast logToast;

	// Log |msg| and Toast about it.
		public static void logAndToast(String TAG,Context context,String msg) {
			Log.d(TAG, "manis:"+msg);
			if (logToast != null) {
				logToast.cancel();
			}
			logToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
			logToast.show();
		}
	// Log |msg|
	public static void log(String TAG,String msg) {
		Log.d(TAG, "manis:"+msg);
	}
	
}
