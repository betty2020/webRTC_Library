package com.xiaoqiang.online.activitys.main.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.util.Constants;
import com.cisco.nohttp.NetWorkUtil;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.MeetingActivity;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.ToastUtils;

/**
 * @author linpeng
 */
public class GuestLoginActivity extends BaseActivity implements CiscoApiInterface.OnGuestLoginEvents {

    private TextView tv_head;
    private ImageView btn_back;
    private EditText conference_no_et, conference_pass_et, conference_nick_et;
    private Button join_meet;
    private String conferenceNumber, conferenceNick, conferencePass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 管理activity
        //        ActivityUtil.getInstance().popOtherActivity(GuestLoginActivity.class);
        //        ActivityUtil.getInstance().addActivity(this);
    }

    @Override
    public void findViews() {
        setContentView(R.layout.activity_guest_join_meeting);
        tv_head = (TextView) this.findViewById(R.id.tv_head);
        btn_back = (ImageView) this.findViewById(R.id.btn_back);
        conference_no_et = (EditText) this.findViewById(R.id.conference_no_et);
        conference_nick_et = (EditText) this.findViewById(R.id.conference_nick_et);
        conference_pass_et = (EditText) this.findViewById(R.id.conference_pass_et);

        join_meet = (Button) findViewById(R.id.join_meet);
    }

    @Override
    public void init() {
        tv_head.setText("加入会议");
        btn_back.setImageResource(R.mipmap.head_back);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(this);

        join_meet.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        conferenceNumber = conference_no_et.getText().toString();
        conferenceNick = conference_nick_et.getText().toString();
        conferencePass = conference_pass_et.getText().toString();
        switch (v.getId()) {
            case R.id.btn_back:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.join_meet:
                if (conferenceNumber.trim().equals("") || conferenceNick.trim().equals("")) {
                    ToastUtils.show(this, "会议号和昵称不能为空");
                } else if (!NetWorkUtil.hasNetwork(this)) {
                    ToastUtils.show(this, "请连接网络");
                } else {
                    CiscoApiInterface.app.GuestLogin(conferenceNumber, conferenceNick, conferencePass, Constants.SERVER, this);
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return false;
    }

    /***
     * guest登录成功回掉
     */
    @Override
    public void GuestLoginResult(boolean result, String requestMessage) {
        if (result) {
            Intent intent = new Intent(this, MeetingActivity.class);
            startActivity(intent);
            InitComm.Guest=true;
        } else {
            ToastUtils.show(this, requestMessage);
        }

    }
}
