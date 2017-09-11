package com.xiaoqiang.online.customview;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import com.xiaoqiang.online.R;

import java.util.Calendar;
import java.util.Date;

/**
 * 自定义 日期 选择 器  只能选择 日月年
 * Created by Administrator on 2016/6/22.
 */
public class DateTimeDialogOnlyTime extends AlertDialog implements View.OnClickListener {

    private TimePicker timePicker;
    private MyOnDateSetListener myOnDateSetListener;

    private Button cancleButton, okButton;

    // 控制 日期
    private int measureWidth;

    private boolean is24HourView;


    // 是否 显示 年选择器   true 显示 ，false 隐藏
    private boolean isHourVisible = true;

    // 是否 显示 月选择器   true 显示 ，false 隐藏
    private boolean isMinuteVisible = true;


    protected DateTimeDialogOnlyTime(Context context) {
        super(context);
    }

    public DateTimeDialogOnlyTime(Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DateTimeDialogOnlyTime(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    /**
     * @param context         上下文对象
     * @param is24HourView    选择 监听器
     * @param isHourVisible   月 是否可见
     * @param isMinuteVisible 年 是否可见
     */
    public DateTimeDialogOnlyTime(Context context, boolean is24HourView, boolean isHourVisible, boolean isMinuteVisible) {
        super(context);
        this.is24HourView = is24HourView;
        this.isHourVisible = isHourVisible;
        this.isMinuteVisible = isMinuteVisible;
        init();
    }

    public DateTimeDialogOnlyTime(Context context, MyOnDateSetListener myOnDateSetListener, boolean is24HourView, boolean isHourVisible, boolean isMinuteVisible) {
        super(context);
        this.myOnDateSetListener = myOnDateSetListener;
        this.is24HourView = is24HourView;
        this.isHourVisible = isHourVisible;
        this.isMinuteVisible = isMinuteVisible;
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Context contextThemeWrapper = new ContextThemeWrapper(
                getContext(), android.R.style.Theme_Holo_Light);
        LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);
        View view = localInflater.inflate(R.layout.view_time_picker_dialog, null, false);
        setView(view);

        timePicker = (TimePicker) view.findViewById(R.id.timePicker);
        LinearLayout buttonGroup = (LinearLayout) view.findViewById(R.id.buttonGroup);
        cancleButton = (Button) view.findViewById(R.id.cancelButton);
        okButton = (Button) view.findViewById(R.id.okButton);

        cancleButton.setOnClickListener(this);
        okButton.setOnClickListener(this);


        // 设置 是否 是 24 小时 格式
        timePicker.setIs24HourView(is24HourView);

        // 设置 默认 显示 的 时间
//        timePicker.setCurrentHour(18);
//        timePicker.setCurrentMinute(18);

        // 设置 显示 宽度
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        buttonGroup.measure(width, height);
        timePicker.measure(width, height);
        if (buttonGroup.getMeasuredWidth() > timePicker.getMeasuredWidth()) {
            this.measureWidth = buttonGroup.getMeasuredWidth();
        } else {
            this.measureWidth = timePicker.getMeasuredWidth();
        }
    }


    public void hideOrShow() {
        if (this == null) {
            return;
        }
        if (!this.isShowing()) {
            this.show();
            // 设置 显示 的 宽高
//            WindowManager.LayoutParams attributes = this.getWindow().getAttributes();
//            this.getWindow().setAttributes(attributes);
        } else {
            this.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancelButton:
                dismiss();
                break;
            case R.id.okButton:
                onOkButtonClick();
                dismiss();
                break;
        }

    }

    /**
     * 确认 按钮 点击 事件
     */
    private void onOkButtonClick() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timePicker.getCurrentHour());
        calendar.set(Calendar.MINUTE, timePicker.getCurrentMinute());
        if (this.myOnDateSetListener != null) {
            this.myOnDateSetListener.onDateOnlyTimeSet(calendar.getTime());
        }
        Log.i("test", "ok==hour==" + timePicker.getCurrentHour() + "==min===" + timePicker.getCurrentMinute());
    }

    public interface MyOnDateSetListener {
        void onDateOnlyTimeSet(Date date);
    }

}
