package com.xiaoqiang.online.activitys.main;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.cisco.core.httpcallback.MyCallback;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.customview.DateTimeDialog;
import com.xiaoqiang.online.customview.DateTimeDialogOnlyTime;
import com.xiaoqiang.online.customview.DateTimeDialogOnlyYMD;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:01
 * AddBookingActivity (TODO)
 * 主要功能：会议记录->预约会议页面
 * 邮箱：yugu88@163.com
 */
public class AddBookingActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener,
        View.OnTouchListener, View.OnFocusChangeListener, DateTimeDialogOnlyTime.MyOnDateSetListener, DateTimeDialog.MyOnDateSetListener {
    RadioButton button_one, day, week, month;
    RadioButton button_rey;
    FrameLayout cloud_back2;
    private TextView tv_head;
    private ImageView btn_back;
    private EditText start_time;
    private EditText join_time;
    private Button save_info;
    private EditText meeting_theme;
    private EditText meeting_password;
    private EditText master_password, end_time;
    private Long start_time_L, day_time_L, week_time_L, month_time_L, end_time_L;
    private Long lengthTime;
    private LinearLayout linear_time;
    private TextView text_day, text_hours, text_minutes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void findViews() {
        setContentView(R.layout.activity_booking);
        // 管理activity
//        ActivityUtil.getInstance().addActivity(this);
        button_one = (RadioButton) findViewById(R.id.button_one);
        button_rey = (RadioButton) findViewById(R.id.button_rey);
        day = (RadioButton) findViewById(R.id.day);
        week = (RadioButton) findViewById(R.id.week);
        month = (RadioButton) findViewById(R.id.month);
        cloud_back2 = (FrameLayout) findViewById(R.id.cloud_back2);
        tv_head = (TextView) this.findViewById(R.id.tv_head);
        btn_back = (ImageView) this.findViewById(R.id.btn_back);
        start_time = (EditText) this.findViewById(R.id.start_time);
        join_time = (EditText) this.findViewById(R.id.join_time);
        meeting_theme = (EditText) this.findViewById(R.id.meeting_theme);
        meeting_password = (EditText) this.findViewById(R.id.meeting_password);
        master_password = (EditText) this.findViewById(R.id.master_password);
        end_time = (EditText) this.findViewById(R.id.end_time);
        save_info = (Button) this.findViewById(R.id.save_info);
        linear_time = (LinearLayout) findViewById(R.id.linear_time);
        text_day = (TextView) findViewById(R.id.text_day);
        text_hours = (TextView) findViewById(R.id.text_hours);
        text_minutes = (TextView) findViewById(R.id.text_minutes);
        start_time.setInputType(InputType.TYPE_NULL);// <span style = “font-family：Arial，Helvetica，sans-serif;” > //不显示系统输入键盘</ span>

    }

    @Override
    public void init() {
        InitComm.init().startCloudMove(this, cloud_back2);// 云动画
        tv_head.setText("预约会议");
        btn_back.setOnClickListener(this);
        button_rey.setOnCheckedChangeListener(this);
        button_one.setOnCheckedChangeListener(this);
        day.setOnCheckedChangeListener(this);
        week.setOnCheckedChangeListener(this);
        month.setOnCheckedChangeListener(this);
        start_time.setOnTouchListener(this);
        start_time.setOnFocusChangeListener(this);
        join_time.setOnTouchListener(this);
        join_time.setOnFocusChangeListener(this);
        save_info.setOnClickListener(this);
        meeting_theme.setOnClickListener(this);
        end_time.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                startActivity(new Intent(AddBookingActivity.this, MainActivity.class));
                //overridePendingTransition(R.animator.zoom_enter, R.animator.zoom_exit);//缩小到左上角效果
                finish();
                break;
            case R.id.save_info:
                getAllInfoPush();
                break;
        }
    }

    private void getAllInfoPush() {
        String cycle = "0";
        String theme_text = meeting_theme.getText().toString().trim();
        String meetingPassword = meeting_password.getText().toString().trim();
        InitComm.meetingPassword = meetingPassword;
        String masterPassword = master_password.getText().toString().trim();
        InitComm.masterPassword = masterPassword;
        boolean cycleN = button_one.isChecked();
        if (cycleN) {
            cycle = "0";
        } else {
            cycle = "1";
        }
        String cycleDmy = "2";
        String cycleStartPre = "0";
        //0:日循环,1:周循环,2:月循环
        boolean d = day.isChecked();
        boolean w = week.isChecked();
        boolean m = month.isChecked();
        if (d) {
            cycleDmy = "0";
            cycleStartPre = "0";
        }
        if (w) {
            cycleDmy = "1";
            String s = text_day.getText().toString();
            switch (s) {
                case "周一":
                    cycleStartPre = "1";
                    break;
                case "周二":
                    cycleStartPre = "2";
                    break;
                case "周三":
                    cycleStartPre = "3";
                    break;
                case "周四":
                    cycleStartPre = "4";
                    break;
                case "周五":
                    cycleStartPre = "5";
                    break;
                case "周六":
                    cycleStartPre = "6";
                    break;
                case "周日":
                    cycleStartPre = "0";
                    break;
                // 由于android不同版本的操作系统格式化后 时间格式是不同的，
                case "星期一":
                    cycleStartPre = "1";
                    break;
                case "星期二":
                    cycleStartPre = "2";
                    break;
                case "星期三":
                    cycleStartPre = "3";
                    break;
                case "星期四":
                    cycleStartPre = "4";
                    break;
                case "星期五":
                    cycleStartPre = "5";
                    break;
                case "星期六":
                    cycleStartPre = "6";
                    break;
                case "星期日":
                    cycleStartPre = "0";
                    break;
            }
        }
        if (m) {
            cycleDmy = "2";
            cycleStartPre = text_day.getText().toString().substring(0, 1);
        }
        if (TextUtils.isEmpty(theme_text)) {
            ToastUtils.show(this, "请输入会议主题");
        } else if (start_time_L == null) {
            ToastUtils.show(this, "请选择开始时间");
        } else if (lengthTime == null) {
            ToastUtils.show(this, "请选择参会时长");
        } else {
            InitComm.init().showView(this, null, false);
            //获取网络数据
            String userid = InitComm.userInfo.getmUserId();
            if ("0".equals(cycle)) {// 0:一次性会议，1:周期会议
                CiscoApiInterface.app.ReservationMeeting(userid, new MyCallback() {
                            @Override
                            public void onSucess(String message) {
                                MLog.e(message);
                                InitComm.init().closeView();
                                ToastUtils.show(AddBookingActivity.this, "预约成功");
                                startActivity(new Intent(AddBookingActivity.this,MainActivity.class));
                            }

                            @Override
                            public void onFailed(String message) {
                                MLog.e(message);
                                InitComm.init().closeView();
                                ToastUtils.show(AddBookingActivity.this, "预约失败");
                            }
                        }, theme_text, start_time_L.toString(), lengthTime.toString(), "1", meetingPassword, masterPassword, cycle,
                        null, null, null,null);
            } else {
                CiscoApiInterface.app.ReservationMeeting(userid, new MyCallback() {
                            @Override
                            public void onSucess(String message) {
                                MLog.e(message);
                                InitComm.init().closeView();
                                ToastUtils.show(AddBookingActivity.this, "预约成功");
                                startActivity(new Intent(AddBookingActivity.this,MainActivity.class));
                            }

                            @Override
                            public void onFailed(String message) {
                                MLog.e(message);
                                InitComm.init().closeView();
                                ToastUtils.show(AddBookingActivity.this, "预约失败");
                            }
                        }, theme_text, start_time_L.toString(), lengthTime.toString(), "1", meetingPassword, masterPassword, cycle,
                        cycleDmy, cycleStartPre, day_time_L.toString(), end_time_L.toString());
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        if (isChecked) {
            switch (id) {
                case R.id.button_rey:
                    linear_time.setVisibility(View.VISIBLE);
                    break;
                case R.id.button_one:
                    linear_time.setVisibility(View.GONE);
                    break;
                case R.id.day:
                    text_day.setVisibility(View.GONE);
                    new DateTimeDialogOnlyTime(this, new DateTimeDialogOnlyTime.MyOnDateSetListener() {
                        @Override
                        public void onDateOnlyTimeSet(Date date) {
                            day_time_L = date.getTime();
                            text_hours.setText(date.getHours() + "时");
                            text_minutes.setText(date.getMinutes());
                        }
                    }, true, false, false).hideOrShow();
                    break;
                case R.id.week:
                    text_day.setVisibility(View.VISIBLE);
                    new DateTimeDialog(this, null, new DateTimeDialog.MyOnDateSetListener() {
                        @Override
                        public void onDateSet(Date date) {
                            week_time_L = date.getTime();
                            SimpleDateFormat formatE = new SimpleDateFormat("E");
                            text_day.setText(formatE.format(date));
                            SimpleDateFormat formatH = new SimpleDateFormat("HH" + "时");
                            text_hours.setText(formatH.format(date));
                            SimpleDateFormat formatM = new SimpleDateFormat("MM" + "分");
                            text_minutes.setText(formatM.format(date));
                        }
                    }).hideOrShow();
                    break;
                case R.id.month:
                    text_day.setVisibility(View.VISIBLE);
                    new DateTimeDialog(this, null, new DateTimeDialog.MyOnDateSetListener() {
                        @Override
                        public void onDateSet(Date date) {
                            month_time_L = date.getTime();
                            SimpleDateFormat formatd = new SimpleDateFormat("dd" + "日");
                            text_day.setText(formatd.format(date));
                            SimpleDateFormat formatH = new SimpleDateFormat("HH" + "时");
                            text_hours.setText(formatH.format(date));
                            SimpleDateFormat formatM = new SimpleDateFormat("MM" + "分");
                            text_minutes.setText(formatM.format(date));
                        }
                    }).hideOrShow();
                    break;
            }
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.join_time:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    showDatePickDlg(join_time);
                    return true;
                }
                break;
            case R.id.start_time:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    DateTimeDialog dateTimeDialog = new DateTimeDialog(this, null, this);
                    dateTimeDialog.hideOrShow();
                    return true;
                }
                break;
            case R.id.end_time:
                new DateTimeDialogOnlyYMD(this, new DateTimeDialogOnlyYMD.MyOnDateSetListener() {
                    @Override
                    public void onDateSet(Date date) {
                        end_time_L = date.getTime();
                        SimpleDateFormat formatE = new SimpleDateFormat("yyyy-MM-dd");
                        end_time.setText(formatE.format(date));
                    }
                }, true, true, true).hideOrShow();
                break;
        }
        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && InitComm.isFirstClick()) {
            startActivity(new Intent(AddBookingActivity.this, MainActivity.class));
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.join_time:
                if (hasFocus) {
                    showDatePickDlg(join_time);
                }
                break;
            case R.id.start_time:
                if (hasFocus) {
                    DateTimeDialog dateTimeDialog = new DateTimeDialog(this, null, this);
                    dateTimeDialog.hideOrShow();
                }
                break;
            case R.id.end_time:
                new DateTimeDialogOnlyYMD(this, new DateTimeDialogOnlyYMD.MyOnDateSetListener() {
                    @Override
                    public void onDateSet(Date date) {
                        end_time_L = date.getTime();
                        SimpleDateFormat formatE = new SimpleDateFormat("yyyy-MM-dd");
                        end_time.setText(formatE.format(date));
                    }
                }, true, true, true).hideOrShow();
                break;
        }
    }

    protected void showDatePickDlg(final EditText editText) {
        TimePickerDialog timeDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    //从这个方法中取得获得的时间
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        lengthTime = Long.valueOf(hourOfDay * 60 * 60 + minute * 60);
                        editText.setText(hourOfDay + "小时 " + minute + "分钟");
                    }
                }, 0, 0, true);
        timeDialog.show();
        /**
         * 0：初始化小时
         * 0：初始化分
         * true:是否采用24小时制
         */
    }

    @Override
    public void onDateSet(Date date) {
        date.toString();
        MLog.e(date.getTime() + ":" + date.toString());
        SimpleDateFormat mFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        start_time_L = date.getTime();
        start_time.setText(mFormatter.format(date));
    }

    @Override
    public void onDateOnlyTimeSet(Date date) {

    }

    @Override
    protected void onDestroy() {
        InitComm.init().stopCloud();// 云动画 关闭
        super.onDestroy();
    }
}