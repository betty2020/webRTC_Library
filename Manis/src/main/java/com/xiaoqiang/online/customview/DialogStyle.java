package com.xiaoqiang.online.customview;


import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.xiaoqiang.online.R;
import com.xiaoqiang.online.listener.DialogOnKeyDownListener;

public class DialogStyle extends Dialog {

	private static int default_width = 160; // 默认宽度
	private static int default_height = 120;// 默认高度
	private DialogOnKeyDownListener dialogOnKeyDownListener;

	public void setDialogOnKeyDownListener(DialogOnKeyDownListener dialogOnKeyDownListener) {
		this.dialogOnKeyDownListener=dialogOnKeyDownListener;
	}

	public DialogStyle(Context context, int layout, int style) {
		this(context, default_width, default_height, layout, style);
	}
    //显示全屏
	
	public DialogStyle(Context context, int layout,
			int style,int a) {
		super(context, style);
		setContentView(layout);
		Window window = getWindow();
		window.setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	}
	
	public DialogStyle(Context context, int width, int height, int layout,
			int style, int location) {
		super(context, style);
		// set content
		setContentView(layout);
		// set window params
		Window window = getWindow();
		LayoutParams params = window.getAttributes();
		// set width,height by density and gravity
		params.width = (int) width;
		params.height = (int) height;
		window.setAttributes(params);
		window.setGravity(Gravity.BOTTOM);
		window.setWindowAnimations(R.style.Theme_dialogstyle); // 添加下->上动画
	}
	public DialogStyle(Context context, int width, int height, int layout,
			int style, String location) {
		super(context, style);
		setContentView(layout);
		Window window = getWindow();
		LayoutParams params = window.getAttributes();
		params.width = (int) width;
		params.height = (int) height;
		window.setAttributes(params);
		window.setGravity(Gravity.LEFT);
		window.setWindowAnimations(R.style.Theme_dialogstyle); // 添加左->右动画
	}


	public DialogStyle(Context context, int width, int height, int layout,
			int style) {
		super(context, style);
		// set content
		setContentView(layout);
		setCanceledOnTouchOutside(false);
		// set window params
		Window window = getWindow();
		LayoutParams params = window.getAttributes();
		// set width,height by density and gravity
		float density = getDensity(context);
		params.width = (int) (width * density);
		params.height = (int) (height * density);
		params.gravity = Gravity.CENTER;
		window.setAttributes(params);
	}

	private float getDensity(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.density;
	}


	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(dialogOnKeyDownListener!=null) {
			dialogOnKeyDownListener.onKeyDownListener(keyCode, event);
		}
		return super.onKeyDown(keyCode, event);
	}
}