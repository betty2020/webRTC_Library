package com.xiaoqiang.online.activitys.main;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.xiaoqiang.online.R;
import com.xiaoqiang.online.adapter.MeetingStatePagerAdapter;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.fragment.DriverFragment;
import com.xiaoqiang.online.fragment.VideoFragment;

import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;

/**
 * Ceate author: xiaoqiang on 2017/5/22 12:29
 * (位置：) com.webrtc.manis.activitys.main (Context)MeetingActivity
 * TODO->主要功能：视频会议页面-内部两个fragment左右滑动
 * 邮箱：sin2t@sina.com
 */
public class MeetingActivity extends BaseActivity {


    private ViewPager mViewPager;
    private DriverFragment driverFragment;
    private VideoFragment videoFragment2;
    ArrayList<Fragment> fragmentList = new ArrayList<>();
    private RelativeLayout tou_layout, rl_top;
    private SurfaceViewRenderer big_surface;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int mCurrentOrientation = getResources().getConfiguration().orientation;
        if (mCurrentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            MLog.e("竖屏---> portrait"); // 竖屏
            //setContentView(R.layout.meeting_viewpager);
        } else if (mCurrentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            MLog.e("横屏---> landscape"); // 横屏
            //setContentView(R.layout.activity_meeting);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meeting_viewpager);
        driverFragment = new DriverFragment();
        videoFragment2 = new VideoFragment();
        mViewPager = (ViewPager) findViewById(R.id.pager_view);
        fragmentList.clear();
        fragmentList.add(driverFragment);
        fragmentList.add(videoFragment2);
        MeetingStatePagerAdapter pagerAdapter = new MeetingStatePagerAdapter(getSupportFragmentManager(), fragmentList);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void findViews() {

    }

    @Override
    public void init() {

    }

    @Override
    protected void onStop() {
        MLog.e("onStop():");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        MLog.e("activity成功退出释放资源");
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && InitComm.isFirstClick()) {
            InitComm.init().showDialog(LayoutInflater.from(this).inflate(R.layout.meeting_viewpager, null), this, 1);
        }
        return false;
    }

    @Override
    public void onClick(View v) {

    }
}
