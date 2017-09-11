package com.xiaoqiang.online.activitys.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.nohttp.NetWorkUtil;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.ToastUtils;


/**
 * Ceate author: xiaoqiang on 2017/4/28 11:01
 * JoinMeetingActivity (TODO)
 * 主要功能：主页->参加会议页面
 * 邮箱：yugu88@163.com
 */
public class JoinMeetingActivity extends BaseActivity implements CiscoApiInterface.OnLoginLafterJoinRoomEvents {
    private TextView tv_head;
    private ImageView btn_back;
    private Button join_meet;
    private EditText conference_no_et, conference_pass_et;
    private String conferenceNumber, conferencePass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void findViews() {
        setContentView(R.layout.activity_join_meeting);
        tv_head = (TextView) this.findViewById(R.id.tv_head);
        btn_back = (ImageView) this.findViewById(R.id.btn_back);
        join_meet = (Button) this.findViewById(R.id.join_meet);
        conference_no_et = (EditText) this.findViewById(R.id.conference_no_et);
        conference_pass_et = (EditText) this.findViewById(R.id.conference_pass_et);

    }

    @Override
    public void init() {
        tv_head.setText(R.string.main_join);
        btn_back.setOnClickListener(this);
        join_meet.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
            case R.id.join_meet:
                //
                conferenceNumber = conference_no_et.getText().toString();
                conferencePass = conference_pass_et.getText().toString();
                if (conferenceNumber.trim().equals("")) {
                    ToastUtils.show(this, getResources().getString(R.string.login_emptyNameTip3));
                } else if (!NetWorkUtil.hasNetwork(this)) {
                    ToastUtils.show(this, getResources().getString(R.string.login_noNetWork));
                } else {
                    //参加会议
                    CiscoApiInterface.app.LoginAfterJoinRoom(conferenceNumber, conferencePass, this);
                    // type=1400 audit(0.0:2336): avc: denied { read } for name="online" dev="sysfs" ino=42203
                    // scontext=u:r:untrusted_app:s0:c512,c768 tcontext=u:object_r:sysfs:s0 tclass=file permissive=0
                }

                break;
        }
    }

    @Override
    public void LoginLafterJoinRoom(boolean result, String requestMessage) {
        if (result) {
            Intent intent = new Intent(this, MeetingActivity.class);
            startActivity(intent);
        } else {
            ToastUtils.show(this, requestMessage);
        }
    }



    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return false;
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }
}
