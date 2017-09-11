package com.cisco.core.util;

import android.util.Log;
/**
 * 打log的工具类
 * @author Administrator
 *
 */

public class Lg {

	private static boolean isLog = true;

	public static void e(String tag, String msg) {
		if (isLog) {
			Log.e(tag, msg);
		}
	}

	public static void v(String tag, String msg) {
		if (isLog) {
			Log.v(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (isLog) {
			Log.i(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (isLog) {
			Log.d(tag, msg);
		}
	}
	
}
