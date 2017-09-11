package com.xiaoqiang.online.commonUtils;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaoqiang.online.R;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:04
 * ToastUtils (TODO)
 * 主要功能：为了管理所有的Toast
 * 邮箱：yugu88@163.com
 */
public class ToastUtils {
    private static Toast mToast;
    private static View view;
    private static TextView textView;

    /* Toast优化 */
    public static void show(Context ctx, String text) {
        cancelToast();//清除Toast
        view = (View) View.inflate(ctx, R.layout.my_toast, null);
        textView = (TextView) view.findViewById(R.id.textView);
        mToast = new Toast(ctx);
        textView.setText(text);
        mToast.setDuration(Toast.LENGTH_SHORT);
        mToast.setView(view);//使用View的静态方法inflate()将XML转化为View;
        mToast.setGravity(Gravity.BOTTOM, 0, InitComm.init().screenHeiht / 10);//设置toast的显示位置;
        mToast.show();
        MLog.e(InitComm.init().screenHeiht/11);
    }

    public static void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }
}
