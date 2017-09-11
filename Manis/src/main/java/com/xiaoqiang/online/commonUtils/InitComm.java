package com.xiaoqiang.online.commonUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.cisco.core.entity.Participant;
import com.cisco.core.entity.UserInfo;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.util.Constants;
import com.cisco.core.xmpp.Key;
import com.xiaoqiang.online.BroadcastReceivers.GetBroadcast;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.login.GuestLoginActivity;
import com.xiaoqiang.online.activitys.main.login.LoginActivity;
import com.xiaoqiang.online.customview.DialogStyle;
import com.xiaoqiang.online.customview.DynamicWeatherCloudyView;
import com.xiaoqiang.online.customview.MyProgressDialog;

import java.util.ArrayList;
import java.util.List;

import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.OnekeyShareTheme;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:20
 * InitComm (TODO)
 * 主要功能：单例设计，变量和接口以及公共调用方法的存储位置
 * 邮箱：sin2t@sina.com
 */
public class InitComm {
    private static InitComm initComm;
    /* 登录状态 */
    public static boolean Guest = false;//游客进入
    public static String et_username;
    public static String et_password;
    public static String meetingPassword = "";
    public static String masterPassword = "";
    public static boolean MyisHost = false;
    public static int sum = 0;//总数
    public static int page = 2;//页数
    public static String mainJid ;//存主屏jid
    public static UserInfo userInfo=null;// 登陆成功后返回的用户信息
    public static Participant localParticipant;
    private MyProgressDialog dialog;
    public int screenHeiht, screenWidth;
    private DynamicWeatherCloudyView cloudyView;


    private InitComm() {
    }

    public static InitComm init() {

        if (initComm == null) {
            initComm = new InitComm();
        }
        return initComm;
    }

    /* 显示遮罩层 */
    public void showView(Context a, String text, boolean b) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        dialog = new MyProgressDialog(a, text, R.drawable.loading, b);
        dialog.setProgressStyle(R.style.CustomDialog);
        dialog.show();
    }

    public void closeView() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * 视频小窗口布局
     * @param videoN
     * @return
     */
    public LinearLayout.LayoutParams initLayout(int videoN) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                InitComm.init().screenHeiht / 4, InitComm.init().screenWidth / 4);
        return layoutParams;
    }

    //云动画 开启
    public void startCloudMove(Activity activity, ViewGroup layout_base) {
        // 临时假图
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView cloud = new ImageView(activity);
        cloud.setImageResource(R.drawable.clouds2);
        cloud.setScaleType(ImageView.ScaleType.FIT_XY);
        cloud.setLayoutParams(params);
        // 创建云动画实例
        //cloudyView = new DynamicWeatherCloudyView(activity, R.drawable.clouds, -screenWidth, screenHeiht, 30);
        layout_base.addView(cloud);
        ImageView imageView = new ImageView(activity);
        imageView.setBackgroundColor(Color.WHITE);
        imageView.setAlpha((float) 0.8);
        layout_base.addView(imageView);
        //cloudyView.move();// 开始移动
    }

    //云动画 关闭
    public void stopCloud() {
        //        if (cloudyView != null) {
        //            cloudyView.stop();
        //        }
    }

    // 获取屏幕数据的方法必须在进入应用时完成初始化
    public void getScreenSize(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        this.screenHeiht = metrics.heightPixels;//返回的是像素
        this.screenWidth = metrics.widthPixels;
        MLog.e(screenHeiht + ":::" + screenWidth);
    }

    private PopupWindow popupWindow;
    private List<PopupWindow> popupWindowList = new ArrayList<>();

    public void showDialog(View view, final Activity activity, final int type) {
        //view为popupWindow所在的页面
        //type -0是退出登录 1是结束会议
        InitComm.init().closeView();
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenHeiht = metrics.heightPixels;//返回的是像素
        int screenWidth = metrics.widthPixels;
        View popView = LayoutInflater.from(activity).inflate(R.layout.pop_dialog, null);
        TextView msg = (TextView) popView.findViewById(R.id.tv_msg1);
        if (type == 0) {
            msg.setText("您好，确定现在退出吗？");
        } else if (type == 1) {
            msg.setText("您确定要离开会议室吗？");
        }
        popupWindow = new PopupWindow(popView, screenWidth, screenHeiht);
        popupWindowList.add(popupWindow);//防止弹出多个窗口
        Button bt_know = (Button) popView.findViewById(R.id.bt_cancel);
        Button bt_back = (Button) popView.findViewById(R.id.bt_ok);
        bt_back.setOnClickListener(new View.OnClickListener() {// 取消
            @Override
            public void onClick(View v) {
                if (popupWindow != null) {
                    for (int i = 0; i < popupWindowList.size(); i++) {
                        if (popupWindowList.get(i) != null) {
                            popupWindowList.get(i).dismiss();
                        }
                    }
                    popupWindowList.clear();
                }
            }
        });
        bt_know.setOnClickListener(new View.OnClickListener() {// 退出
            @Override
            public void onClick(View v) {
                if (type == 0) {
                    //xmpp退出登录
                    CiscoApiInterface.app.DisConnect();
                    ActivityUtil.getInstance().AppExit(activity);
                    GetBroadcast.unregisterReceiver(activity);
                    popupWindow.dismiss();
                } else if (type == 1) {
                    CiscoApiInterface.app.onCallHangUp();// 结束会议
                    if (Guest) {
                        activity.startActivity(new Intent(activity, GuestLoginActivity.class));
                    } else {
                        activity.startActivity(new Intent(activity, LoginActivity.class));
                    }
                    popupWindow.dismiss();
                }
            }
        });
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(false);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    public void showDialogBySip(View view, final Activity activity) {
        //view为popupWindow所在的页面
        InitComm.init().closeView();
        final DialogStyle chatdialog = new DialogStyle(activity, R.layout.pop_dialog_sip, R.style.Theme_dialog, 0);
        final EditText et_sip = (EditText) chatdialog.findViewById(R.id.et_sip);
        Button bt_know = (Button) chatdialog.findViewById(R.id.bt_cancel);
        Button bt_back = (Button) chatdialog.findViewById(R.id.bt_ok);
        chatdialog.show();
        bt_back.setOnClickListener(new View.OnClickListener() {// 取消
            @Override
            public void onClick(View v) {
                chatdialog.cancel();
            }
        });
        bt_know.setOnClickListener(new View.OnClickListener() {// 呼叫
            @Override
            public void onClick(View v) {
                //呼叫
                String sipNumber = et_sip.getText().toString();
                if (sipNumber.trim().equals("")) {
                    ToastUtils.show(activity, "sip号不能为空！");
                } else {
                    CiscoApiInterface.app.CallSip(sipNumber);
                    chatdialog.cancel();
                }
            }
        });
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static int getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String versionName = info.versionName;
            int versionCode = info.versionCode;
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            MLog.e("获取版本号失败，返回 versionCode=0");
            return 0;
        }
    }

    /**
     * 切换软键盘的状态
     * 如当前为收起变为弹出,若当前为弹出变为收起
     */
    public void toggleInput(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 强制隐藏输入法键盘
     */
    public void hideInput(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private static long lastClickTime;
    private static boolean firstClick;

    public synchronized static boolean isFirstClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime >= 1000) {// 禁止1s内频繁点击
            firstClick = true;
        } else {
            firstClick = false;
        }
        lastClickTime = time;
        MLog.e("是否第一次点击：" + firstClick);
        return firstClick;
    }

    /**
     * 演示调用ShareSDK执行分享
     *
     * @param context
     * @param platformToShare 指定直接分享平台名称（一旦设置了平台名称，则九宫格将不会显示）
     * @param showContentEdit 是否显示编辑页
     */
    public void showShare(Context context, String platformToShare, boolean showContentEdit) {
        String text = Constants.SERVER + "meet/?r=" + Key.roomnumber
                + "\r\n"
                + "需要Chrome53或以上版本才可入会";
        OnekeyShare oks = new OnekeyShare();
        oks.setSilent(!showContentEdit);
        if (platformToShare != null) {
            oks.setPlatform(platformToShare);
        }
        //ShareSDK快捷分享提供两个界面第一个是九宫格 CLASSIC  第二个是SKYBLUE
        oks.setTheme(OnekeyShareTheme.CLASSIC);
        oks.disableSSOWhenAuthorize();// 在自动授权时可以禁用SSO方式
        //oks.setAddress("12345678901"); //分享短信的号码和邮件的地址
        oks.setTitle("复制会议地址在浏览器中打开:");
        oks.setTitleUrl(Constants.SERVER + "meet/?r=" + Key.roomnumber);
        oks.setText(text);
        //        oks.setImagePath("/sdcard/test-pic.jpg");  //分享sdcard目录下的图片
        oks.setImageUrl("http://p17.qhimg.com/bdm/64_64_/t017f4613546825c075.png");
        oks.setUrl(Constants.SERVER + "meet/?r=" + Key.roomnumber); //微信不绕过审核分享链接
        //oks.setFilePath(testVideo);  //filePath用于视频分享
        //oks.setComment(context.getString(R.string.app_share_comment)); //我对这条分享的评论，仅在人人网和QQ空间使用，否则可以不提供
        oks.setSite("小强在线");  //QZone分享完之后返回应用时提示框上显示的名称
        oks.setSiteUrl(Constants.SERVER + "meet/?r=" + Key.roomnumber);//QZone分享参数
        oks.setVenueName("小强在线");//分享时的地方名
        oks.setVenueDescription("小强及时会议!");//分享时的地方描述
        oks.setLatitude(23.169f);
        oks.setLongitude(112.908f);
        // oks.setCallback(new OneKeyShareCallback());// 将快捷分享的操作结果将通过OneKeyShareCallback回调
        //oks.setShareContentCustomizeCallback(new ShareContentCustomizeDemo());// 去自定义不同平台的字段内容
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);// 在九宫格设置自定义的图标
        String label = "小强在线好友";
        View.OnClickListener listener = new View.OnClickListener() {
            public void onClick(View v) {
                ToastUtils.show(context, "稍后开发该功能");
            }
        };
        //oks.setCustomerLogo(logo, label, listener);//小强在线好友
        //oks.setEditPageBackground(getPage());// 为EditPage设置一个背景的View
        // oks.addHiddenPlatform(SinaWeibo.NAME);// 隐藏九宫格中的新浪微博
        oks.show(context);// 启动分享
    }

}
