package com.xiaoqiang.online.update;

import android.os.Environment;

import java.io.File;

public class G {
	/** 应用缓存目录 */
	public static final String CachePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
			+ "Android/data/com.example.addemo/file/";
}
