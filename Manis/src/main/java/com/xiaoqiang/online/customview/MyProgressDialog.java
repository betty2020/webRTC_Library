package com.xiaoqiang.online.customview;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaoqiang.online.R;

import java.util.Calendar;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:05
 * MyProgressDialog (TODO)
 * 主题样式 style name="dialog"
 * 主要功能：网络请求或其他耗时操作时的进度对话框
 * 邮箱：yugu88@163.com
 */
public class MyProgressDialog extends ProgressDialog {

	public MyProgressDialog(Context context, int theme) {
		super(context, theme);
	}

	public MyProgressDialog(Context context) {
		super(context);
	}

	private AnimationDrawable mAnimation;
	private ImageView mImageView;
	private TextView mTextView;
	private String loadingTip;
	private int resid;
	// 创建的时间
	public static final int MIN_CLICK_DELAY_TIME = 1000;
	private long time = 0;

	/**
	 * 
	 * @param context
	 *            上下文对象
	 * @param content
	 *            显示文字提示信息内容
	 * @param resid
	 *            动画id
	 * @param b
	 *            点击返回键是否取消提示框
	 */
	public MyProgressDialog(Context context, String content, int resid,
                            boolean b) {
		super(context);
		time = Calendar.getInstance().getTimeInMillis();
		this.loadingTip = content;
		this.resid = resid;
		// 点击提示框外面是否取消提示框
		setCanceledOnTouchOutside(false);
		// 点击返回键是否取消提示框
		setCancelable(b);
		setIndeterminate(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.progress_dialog);// Dialog界面布局

		mTextView = (TextView) findViewById(R.id.loadingTv);
		mImageView = (ImageView) findViewById(R.id.loadingIv);

		mImageView.setBackgroundResource(resid);
		// 通过ImageView对象拿到背景显示的AnimationDrawable
		mAnimation = (AnimationDrawable) mImageView.getBackground();
		mImageView.post(new Runnable() {
			@Override
			public void run() {
				mAnimation.start();
			}
		});
		mTextView.setText(loadingTip);
		long currentTime = Calendar.getInstance().getTimeInMillis();
		if (currentTime - time > MIN_CLICK_DELAY_TIME) {

		}
	}

}
