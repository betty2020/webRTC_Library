package com.xiaoqiang.online.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.xiaoqiang.online.R;

@SuppressLint("AppCompatCustomView")
public class IsShowPassEditText extends EditText implements View.OnTouchListener{
    private Drawable[] drawables;
    private int eyeWidth;
    private Drawable drawableEyeOpen;
    private boolean isHidePwd = true;// 输入框密码是否是隐藏的，默认为true
    private Context mContext;

    public IsShowPassEditText(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public IsShowPassEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        init();
    }

    public IsShowPassEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public void init() {
        drawables = this.getCompoundDrawables();
        eyeWidth = drawables[2].getBounds().width();// 眼睛图标的宽度
        drawableEyeOpen = mContext.getResources().getDrawable(R.mipmap.eyes_open);
        drawableEyeOpen.setBounds(drawables[2].getBounds());//这一步不能省略
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // getWidth,getHeight必须在这里处理
            float et_pwdMinX = v.getWidth() - eyeWidth - this.getPaddingRight();
            float et_pwdMaxX = v.getWidth();
            float et_pwdMinY = 0;
            float et_pwdMaxY = v.getHeight();
            float x = event.getX();
            float y = event.getY();
            if (x < et_pwdMaxX && x > et_pwdMinX && y > et_pwdMinY && y < et_pwdMaxY) {
                // 点击了眼睛图标的位置
                isHidePwd = !isHidePwd;
                if (isHidePwd) {
                    this.setCompoundDrawables(drawables[0], drawables[1],
                            drawables[2],
                            drawables[3]);

                    this.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    this.setCompoundDrawables(drawables[0], drawables[1],
                            drawableEyeOpen,
                            drawables[3]);
                    this.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        }
        return false;
    }
}
