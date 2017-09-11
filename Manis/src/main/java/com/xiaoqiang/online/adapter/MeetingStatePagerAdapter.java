package com.xiaoqiang.online.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by xiaoqiang on 2017/5/16 16:31
 * 2017 to: 邮箱：sin2t@sina.com
 * androidApp
 */

public class MeetingStatePagerAdapter extends FragmentStatePagerAdapter {
    ArrayList<Fragment> fragmentList;
    public MeetingStatePagerAdapter(FragmentManager fm, ArrayList<Fragment> fragmentList) {
        super(fm);
        this.fragmentList=fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }
}
