package com.xiaoqiang.online.activitys.main.settings;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.MainActivity;
import com.xiaoqiang.online.activitys.main.login.LoginActivity;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.customview.DialogStyle;
import com.xiaoqiang.online.customview.IsShowPassEditText;
import com.xiaoqiang.online.util.SPUtil;

import static com.xiaoqiang.online.commonUtils.InitComm.drawableToBitmap;
import static com.xiaoqiang.online.util.SPUtil.Init;

/**
 * linpeng
 */
public class SettingActivity extends BaseActivity {

    private TextView tv_head;
    private ImageView btn_back;
    private LinearLayout pass_type, userinfo_type;
    private TextView textView, tv_xianshi;
    private FrameLayout frame_cloud;
    private RelativeLayout video_cif, video_number;

    private int width, height;
    private DialogStyle dialog_videoquality, dialog_videonumber;
    private TextView video_number_text, tv_video_quality;
    private ToggleButton setting_camera, setting_speaker, setting_microphone;
    private Button sign_out;
    private IsShowPassEditText password_conference, password_host;
    private String url = "https://gd2.alicdn.com/imgextra/i3/827264851/TB2Pp8pXF55V1Bjy0FpXXXhDpXa_!!827264851.png";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 管理activity
        //        ActivityUtil.getInstance().popOtherActivity(SettingActivity.class);
        //        ActivityUtil.getInstance().addActivity(this);
        //
        //        Log.d("setting","测试登录--管理activity--setting----"+ActivityUtil.getInstance().activityStack.size()+",当前activity="+ActivityUtil.getInstance().getCurrentActivity());

        // 视频数量
        int videoNumber = SPUtil.Init(this).getVideoNumberSP();
        //视频质量
        String videoQuality = SPUtil.Init(this).getVideoQualitySP();
        //默认摄像头
        Boolean cameraSP = SPUtil.Init(this).getCameraSP();
        setting_camera.setChecked(cameraSP);
        //扬声器
        Boolean speakerSP = SPUtil.Init(this).getSpeakerSP();
        setting_speaker.setChecked(speakerSP);
        //麦克风
        Boolean micSP = SPUtil.Init(this).getMicSP();
        setting_microphone.setChecked(micSP);

        if (videoNumber >= 0) {
            video_number_text.setText(videoNumber + "");
        }
        if (!TextUtils.isEmpty(videoQuality)) {
            tv_video_quality.setText(videoQuality);
        }
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        width = wm.getDefaultDisplay().getWidth();
        height = wm.getDefaultDisplay().getHeight();
        InitDialog();
        InitDialog2();
    }

    @Override
    public void findViews() {
        setContentView(R.layout.activity_setting);

        tv_head = (TextView) findViewById(R.id.tv_head);
        btn_back = (ImageView) findViewById(R.id.btn_back);
        userinfo_type = (LinearLayout) findViewById(R.id.userinfo_type);
        userinfo_type.setVisibility(View.GONE);
        pass_type = (LinearLayout) findViewById(R.id.pass_type);
        pass_type.setVisibility(View.GONE);
        textView = (TextView) findViewById(R.id.bianji);
        tv_xianshi = (TextView) findViewById(R.id.tv_xianshi);
        frame_cloud = (FrameLayout) findViewById(R.id.cloud_back);
        // 设置按键
        video_cif = (RelativeLayout) findViewById(R.id.video_cif);
        video_number = (RelativeLayout) findViewById(R.id.video_number);
        video_number_text = (TextView) findViewById(R.id.video_number_text);
        tv_video_quality = (TextView) findViewById(R.id.tv_video_quality);
        sign_out = (Button) findViewById(R.id.sign_out1);
        setting_camera = (ToggleButton) findViewById(R.id.setting_camera);
        setting_speaker = (ToggleButton) findViewById(R.id.setting_speaker);
        setting_microphone = (ToggleButton) findViewById(R.id.setting_microphone);

        //用户信息
        TextView user_name = (TextView) findViewById(R.id.user_name);
        TextView user_phone = (TextView) findViewById(R.id.user_phone);
        TextView user_email = (TextView) findViewById(R.id.user_email);
        user_name.setText("用户名：" + InitComm.userInfo.getmUserName());
        user_phone.setText("手    机：" + InitComm.userInfo.getPhone());
        user_email.setText("邮    箱：" + InitComm.userInfo.getEmail());

        //设置会议密码 主持人密码
        password_conference = (IsShowPassEditText) findViewById(R.id.password_conference);
        password_host = (IsShowPassEditText) findViewById(R.id.password_host);
        //初始 会议密码 主持人密码
        String conferencePass = SPUtil.Init(this).getConferencePassSP();//取 即时会议密码
        String hostPass = SPUtil.Init(this).getHostPassSP();//主持人密码
        if (!TextUtils.isEmpty(conferencePass)) {
            password_conference.setText(conferencePass);
        }
        if (!TextUtils.isEmpty(hostPass)) {
            password_host.setText(hostPass);
        }
        //头像
        final ImageView my_Photo = (ImageView) findViewById(R.id.setting_userinfo);
        Glide.with(this).load(url).centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).into(new SimpleTarget<GlideDrawable>() {
            @Override
            public void onResourceReady(GlideDrawable drawable, GlideAnimation<? super GlideDrawable> glideAnimation) {
                Bitmap bitmap = drawableToBitmap(drawable);// 原图bitmap
                my_Photo.setImageBitmap(createFramedPhoto(100, 100, bitmap, 17));//圆角和边长，都要按比例增加和减少
                MLog.e("解析头像已完成");
                my_Photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(SettingActivity.this, BigPhotoActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    /**
     * 创建圆角图片
     *
     * @param x              图片的大小-->X方向
     * @param y              图片的大小-->Y方向
     * @param image          需要裁剪的图片Bitmap对象
     * @param outerRadiusRat 椭圆形的x-radius角部弧形的半径
     * @return 裁剪之后的Bitmap对象
     */
    public Bitmap createFramedPhoto(int x, int y, Bitmap image, float outerRadiusRat) {
        //根据源文件新建一个darwable对象
        Drawable imageDrawable = new BitmapDrawable(image);
        // 新建一个新的输出图片
        Bitmap output = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        // 新建一个矩形
        RectF outerRect = new RectF(0, 0, x, y);
        // 产生一个指定颜色的圆角矩形
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(0xFF05BF9D);
        canvas.drawRoundRect(outerRect, outerRadiusRat, outerRadiusRat, paint);
        // 将源图片绘制到这个圆角矩形上
        //详解见http://lipeng88213.iteye.com/blog/1189452
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        imageDrawable.setBounds(0, 0, x, y);
        canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
        imageDrawable.draw(canvas);
        canvas.restore();
        return output;
    }

    private boolean isHidePwd = true;// 输入框密码是否是隐藏的，默认为true

    @Override
    public void init() {
        InitComm.init().startCloudMove(this, frame_cloud);
        tv_head.setText("设置");
        btn_back.setImageResource(R.mipmap.head_back);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(this);
        textView.setOnClickListener(this);
        tv_xianshi.setOnClickListener(this);
        // 设置按键点击
        video_cif.setOnClickListener(this);
        video_number.setOnClickListener(this);
        video_number_text.setOnClickListener(this);
        tv_video_quality.setOnClickListener(this);
        setting_camera.setOnClickListener(this);
        setting_speaker.setOnClickListener(this);
        setting_microphone.setOnClickListener(this);
        sign_out.setOnClickListener(this);
    }

    private void InitDialog() {
        dialog_videoquality = new DialogStyle(this, width, height,
                R.layout.dialog_video_quality, R.style.Theme_dialog, 1);
        dialog_videoquality.setCancelable(false);
        dialog_videoquality.findViewById(R.id.dialog_cif).setOnClickListener(this);
        dialog_videoquality.findViewById(R.id.dialog_vga).setOnClickListener(this);
        dialog_videoquality.findViewById(R.id.dialog_720p).setOnClickListener(this);
        dialog_videoquality.findViewById(R.id.dialog_qx).setOnClickListener(this);
    }

    private void InitDialog2() {
        dialog_videonumber = new DialogStyle(this, width, height,
                R.layout.dialog_video_number, R.style.Theme_dialog, 1);
        dialog_videonumber.setCancelable(false);
        dialog_videonumber.findViewById(R.id.dialog_number1).setOnClickListener(this);
        dialog_videonumber.findViewById(R.id.dialog_number2).setOnClickListener(this);
        dialog_videonumber.findViewById(R.id.dialog_number3).setOnClickListener(this);
        dialog_videonumber.findViewById(R.id.dialog_number4).setOnClickListener(this);
        dialog_videonumber.findViewById(R.id.dialog_qx2).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.setting_camera:
                //默认摄像头
                Init(this).setCameraSP(setting_camera.isChecked());
                break;
            case R.id.setting_speaker:
                //扬声器
                Init(this).setSpeakerSP(setting_speaker.isChecked());
                break;
            case R.id.setting_microphone:
                //麦克风
                Init(this).setMicSP(setting_microphone.isChecked());
                break;
            case R.id.video_cif:
                //视频质量
                dialog_videoquality.show();
                break;
            case R.id.video_number:
                //接收视频数量
                dialog_videonumber.show();
                break;
            case R.id.dialog_cif:
                tv_video_quality.setText(R.string.setting_video_2);
                Init(this).setVideoQualitySP(getResources().getString(R.string.setting_video_2));
                dialog_videoquality.cancel();
                break;
            case R.id.dialog_vga:
                tv_video_quality.setText(R.string.setting_video_3);
                Init(this).setVideoQualitySP(getResources().getString(R.string.setting_video_3));
                dialog_videoquality.cancel();
                break;
            case R.id.dialog_720p:
                tv_video_quality.setText(R.string.setting_video_4);
                Init(this).setVideoQualitySP(getResources().getString(R.string.setting_video_4));
                dialog_videoquality.cancel();
                break;
            case R.id.dialog_number1:
                video_number_text.setText(R.string.setting_video_number2);
                Init(this).setVideoNumberSP(1);
                dialog_videonumber.cancel();
                break;
            case R.id.dialog_number2:
                video_number_text.setText(R.string.setting_video_number3);
                Init(this).setVideoNumberSP(2);
                dialog_videonumber.cancel();
                break;
            case R.id.dialog_number3:
                video_number_text.setText(R.string.setting_video_number4);
                Init(this).setVideoNumberSP(3);
                dialog_videonumber.cancel();
                break;
            case R.id.dialog_number4:
                video_number_text.setText(R.string.setting_video_number5);
                Init(this).setVideoNumberSP(4);
                dialog_videonumber.cancel();
                break;
            case R.id.dialog_qx:
                //取消
                dialog_videoquality.cancel();
                break;
            case R.id.dialog_qx2:
                //取消
                dialog_videonumber.cancel();
                break;
            case R.id.btn_back:
                startActivity(new Intent(this, MainActivity.class));
                // overridePendingTransition(R.animator.zoom_enter, R.animator.zoom_exit);//缩小到左上角效果
                break;// 由于launchMode="singleTask" 所以此类中严禁使用finish()。
            case R.id.sign_out1:
                //退出登录
                Log.d("setting", "测试登录--退出登录------");
                CiscoApiInterface.app.DisConnect();
                SPUtil.Init(this).setAutoLogin(false);
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case R.id.bianji:
                if (pass_type.getVisibility() == View.GONE) {
                    pass_type.setVisibility(View.VISIBLE);
                    textView.setText("完成");
                    password_conference.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            SPUtil.Init(SettingActivity.this).setConferencePassSP(s.toString());
                        }
                    });
                    password_host.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            SPUtil.Init(SettingActivity.this).setHostPassSP(s.toString());
                        }
                    });
                } else {
                    pass_type.setVisibility(View.GONE);
                    textView.setText("编辑");
                }
                break;
            case R.id.tv_xianshi:
                if (userinfo_type.getVisibility() == View.GONE) {
                    userinfo_type.setVisibility(View.VISIBLE);
                    tv_xianshi.setText("收起");
                } else {
                    userinfo_type.setVisibility(View.GONE);
                    tv_xianshi.setText("显示");
                }
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(new Intent(this, MainActivity.class));
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        InitComm.init().stopCloud();// 云动画 关闭
        super.onDestroy();
    }
}
