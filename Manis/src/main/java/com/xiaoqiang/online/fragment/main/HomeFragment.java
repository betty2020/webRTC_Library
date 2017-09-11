package com.xiaoqiang.online.fragment.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.util.Constants;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.JoinMeetingActivity;
import com.xiaoqiang.online.activitys.main.MeetingActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.util.SPUtil;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:06
 * HomeFragment (TODO)
 * 主要功能：首页的Fragment
 * 邮箱：yugu88@163.com
 */
public class HomeFragment extends Fragment implements View.OnClickListener, CiscoApiInterface.OnLoginLafterJoinRoomEvents {
    private View view;
    private RadioButton now_meeting, join_metting;
    private TextView name_text, tv_greetings,dizhi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        now_meeting = (RadioButton) view.findViewById(R.id.now_meeting);
        join_metting = (RadioButton) view.findViewById(R.id.join_metting);
        name_text = (TextView) view.findViewById(R.id.name_text);
        dizhi = (TextView) view.findViewById(R.id.dizhi);
        tv_greetings = (TextView) view.findViewById(R.id.tv_greetings);
        initView();
        return view;
    }

    private void initView() {
        dizhi.setText(Constants.SERVER+"暂时显示,方便测试时查看");
        if (!TextUtils.isEmpty(InitComm.et_username)) {
            name_text.setText(InitComm.et_username);
        }else if (!TextUtils.isEmpty(SPUtil.Init(getActivity()).getUserSP())){
            name_text.setText(SPUtil.Init(getActivity()).getUserSP());
        }else if (!TextUtils.isEmpty(InitComm.userInfo.getmUserId())){
            name_text.setText(InitComm.userInfo.getmUserId());
        }else {
            name_text.setText("hello");
        }
        Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料
        t.setToNow(); // 取得系统时间。
        int mHour = t.hour;    // 0-23
        if (mHour < 11) {
            tv_greetings.setText("早上好");
        } else if (mHour >= 11 && mHour < 13) {
            tv_greetings.setText("中午好");
        } else if (mHour >= 13 && mHour < 19) {
            tv_greetings.setText("下午好");
        } else {
            tv_greetings.setText("晚上好");
        }
        now_meeting.setOnClickListener(this);
        join_metting.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.now_meeting:
                //即时会议
                MLog.d("即时会议");
                InitComm.init().showView(getActivity(), "", true);
                CiscoApiInterface.app.LoginAfterCreateRoom("", "", this);
                break;
            case R.id.join_metting:
                //参加会议
                getActivity().startActivity(new Intent(getActivity(), JoinMeetingActivity.class));
                break;
        }
    }

    @Override
    public void LoginLafterJoinRoom(boolean result, String requestMessage) {
        MLog.d("LoginLafterJoinRoom" + result);
        InitComm.init().closeView();
        if (result) {
            getActivity().startActivity(new Intent(getActivity(), MeetingActivity.class));
        } else {
            ToastUtils.show(getActivity(), "加入会议失败:" + requestMessage);
        }
    }
//
//    @Override
//    public void LoginLafterCreateRoom(boolean result, String hostpass, String roomid, String requestMessage) {
//        MLog.d("即时会议" + result);
//        InitComm.init().closeView();
//        if (result) {
//            getActivity().startActivity(new Intent(getActivity(), MeetingActivity.class));
//        } else {
//            ToastUtils.show(getActivity(), requestMessage);
//        }
//    }
}
