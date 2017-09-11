package com.xiaoqiang.online.activitys.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.login.LoginActivity;
import com.xiaoqiang.online.adapter.DepthPageTransformer;
import com.xiaoqiang.online.adapter.ViewPagerAdatper;
import com.xiaoqiang.online.base.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 引导
 *
 * @author linpeng
 */
public class GuideActivity extends BaseActivity {

    private ViewPager mIn_vp;
    private LinearLayout mIn_ll;
    private List<View> mViewList;
    private ImageView mLight_dots;
    private int mDistance;
    private ImageView mOne_dot;
    private Button mBtn_next;
    private RelativeLayout mRl_dots;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        // 管理activity
//        ActivityUtil.getInstance().addActivity(this);
    }

    @Override
    public void findViews() {
        setContentView(R.layout.activity_guide);

        mIn_vp = (ViewPager) findViewById(R.id.in_viewpager);
        mIn_ll = (LinearLayout) findViewById(R.id.in_ll);
        mRl_dots = (RelativeLayout) findViewById(R.id.rl_dots);
        mLight_dots = (ImageView) findViewById(R.id.iv_light_dots);
        mBtn_next = (Button) findViewById(R.id.bt_next);
    }

    @Override
    public void init() {
        mBtn_next.setOnClickListener(this);
        mViewList = new ArrayList<View>();
        LayoutInflater lf = getLayoutInflater().from(GuideActivity.this);
        View view1 = lf.inflate(R.layout.we_indicator1, null);
        View view2 = lf.inflate(R.layout.we_indicator2, null);
        View view3 = lf.inflate(R.layout.we_indicator3, null);
        View view4 = lf.inflate(R.layout.we_indicator4, null);
        mViewList.add(view1);
        mViewList.add(view2);
        mViewList.add(view3);
        mViewList.add(view4);
        mIn_vp.setAdapter(new ViewPagerAdatper(mViewList));
        addDots();
        moveDots();
        mIn_vp.setPageTransformer(true, new DepthPageTransformer());
    }

    private void moveDots() {
        mLight_dots.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //获得两个圆点之间的距离
                mDistance = mIn_ll.getChildAt(1).getLeft() - mIn_ll.getChildAt(0).getLeft();
                mLight_dots.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        mIn_vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //页面滚动时小白点移动的距离，并通过setLayoutParams(params)不断更新其位置
                float leftMargin = mDistance * (position + positionOffset);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLight_dots.getLayoutParams();
                params.leftMargin = (int) leftMargin;
                //layoutParams.leftMargin = (int) leftMargin;
                mLight_dots.setLayoutParams(params);
            }

            @Override
            public void onPageSelected(int position) {
                //页面跳转时，设置小圆点的margin
                float leftMargin = mDistance * position;
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mLight_dots.getLayoutParams();
                params.leftMargin = (int) leftMargin;
                mLight_dots.setLayoutParams(params);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void addDots() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 40, 0);
        for (int i = 0; i < 4; i++) {
            mOne_dot = new ImageView(this);
            mOne_dot.setImageResource(R.drawable.gray_dot);
            mOne_dot.setId(i * 2);
            mOne_dot.setOnClickListener(this);
            mOne_dot.setLayoutParams(layoutParams);
            mIn_ll.addView(mOne_dot);
        }
        mRl_dots.setPadding(40, 0, 0, 0);// 弥补最后一个点右边多像素无法居中的问题
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case 0:
                mIn_vp.setCurrentItem(0);
                break;
            case 2:
                mIn_vp.setCurrentItem(1);
                break;
            case 4:
                mIn_vp.setCurrentItem(2);
                break;
            case 6:
                mIn_vp.setCurrentItem(3);
                break;
            case R.id.bt_next:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }
}
