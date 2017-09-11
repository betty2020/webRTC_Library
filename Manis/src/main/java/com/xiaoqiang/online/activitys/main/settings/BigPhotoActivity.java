package com.xiaoqiang.online.activitys.main.settings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.customview.TouchImageView;

public class BigPhotoActivity extends BaseActivity {

    private TouchImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏android系统的状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 隐藏应用程序的标题栏，即当前activity的标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawableResource(R.color.black);// 黑色背景

        Intent intent = getIntent();
        String url = intent.getStringExtra("url");
        MLog.e(url);
        Glide.with(this).load(url).centerCrop().into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable drawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                Bitmap bitmap = InitComm.drawableToBitmap(drawable);
                imageView = new TouchImageView(BigPhotoActivity.this, bitmap);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                imageView.setLayoutParams(params);
                setContentView(imageView);
            }
        });
    }

    @Override
    public void findViews() {

    }

    @Override
    public void init() {

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    protected void onDestroy() {
        Glide.get(this).clearMemory();//清理内存缓存  可以在UI主线程中进行
        super.onDestroy();
    }
}
