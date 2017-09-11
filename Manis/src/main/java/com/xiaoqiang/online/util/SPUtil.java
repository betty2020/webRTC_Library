package com.xiaoqiang.online.util;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author linpeng
 */
// SharedPreferences 本身就是单利模式，此封装会徒增过多重复代码。
public class SPUtil {
    private static SharedPreferences preferences;
    private static SharedPreferences.Editor editor;
    private static SPUtil spUtil;

    //初始化  6.0对SharedPreferences有了更严格的要求。开放权限会抛异常。
    public static SPUtil Init (Context context) {
        preferences = context.getSharedPreferences("sp",Context.MODE_PRIVATE);//此处文件类型和名称不标准也会导致无法存储
        editor = preferences.edit();
        if (spUtil == null) {
            spUtil = new SPUtil();
        }
        return spUtil;
    }

    /**
     * 使用示例：
     * SPUtil.Init(this).setFirstOpenSP(false);// 存储首次登陆标记-set
     * SPUtil.Init(this).getFirstOpenSP()     // 获取首次登录标记-get
     */

    // 存用户SP
    public void setUserSP(String name) {
        editor.putString(PublicKey.SAVE_USER_KEY, name);
        editor.commit();
    }

    // 取用户名SP
    public String getUserSP() {
        return preferences.getString(PublicKey.SAVE_USER_KEY, null);
    }

    // 存自动登陆
    public void setAutoLogin(boolean b) {
        editor.putBoolean(PublicKey.SAVE_AUTO_LOGIN_KEY, b);
        editor.commit();
    }

    // 取自动登录
    public boolean getAutoLogin() {
        return preferences.getBoolean(PublicKey.SAVE_AUTO_LOGIN_KEY, false);
    }

    // 存密码SP
    public void setPassSP(String name) {
        editor.putString(PublicKey.SAVE_PASS_ET_KEY, name);
        editor.commit();
    }

    // 取密码SP
    public String getPassSP() {
        return preferences.getString(PublicKey.SAVE_PASS_ET_KEY, null);

    }

    // 存地址
    public void setAddressSP(String name) {
        editor.putString(PublicKey.SAVE_ADDRESS_ET_KEY, name);
        editor.commit();
    }

    // 取地址
    public String getAddressSP() {
        return preferences.getString(PublicKey.SAVE_ADDRESS_ET_KEY, null);

    }


    // 存麦克风状态SP
    public void setMicSP(boolean name) {
        editor.putBoolean(PublicKey.SAVE_MIC_KEY, name);
        editor.commit();
    }

    // 取麦克风状态SP
    public boolean getMicSP() {
        return preferences.getBoolean(PublicKey.SAVE_MIC_KEY, true);

    }

    // 存扬声器状态SP
    public void setSpeakerSP(boolean name) {
        editor.putBoolean(PublicKey.SAVE_SPEAKER_KEY, name);
        editor.commit();
    }

    // 取扬声器状态SP
    public boolean getSpeakerSP() {
        return preferences.getBoolean(PublicKey.SAVE_SPEAKER_KEY, true);

    }

    // 存摄像头状态SP
    public void setCameraSP(boolean name) {
        editor.putBoolean(PublicKey.SAVE_CAMERA_KEY, name);
        editor.commit();
    }

    // 取摄像头状态SP
    public boolean getCameraSP() {
        return preferences.getBoolean(PublicKey.SAVE_CAMERA_KEY, true);

    }

    // 存接收视频数量SP
    public void setVideoNumberSP(int num) {
        editor.putInt(PublicKey.SAVE_VIDEO_NUMBER_KEY, num);
        editor.commit();
    }

    // 取视频数量SP
    public int getVideoNumberSP() {
        return preferences.getInt(PublicKey.SAVE_VIDEO_NUMBER_KEY, 4);

    }

    // 存视频分辨率
    public void setVideoQualitySP(String name) {
        editor.putString(PublicKey.SAVE_VIDEO_QUALITY_KEY, name);
        editor.commit();
    }

    // 取视频分辨率
    public String getVideoQualitySP() {
        return preferences.getString(PublicKey.SAVE_VIDEO_QUALITY_KEY, "CIF");

    }
    // 第一次安装引导页
    public void setFirstOpenSP(boolean name) {
        editor.putBoolean(PublicKey.SAVE_FIRST_OPEN_KEY, name);
        editor.commit();
    }

    // 第一次安装引导页
    public boolean getFirstOpenSP() {
        return preferences.getBoolean(PublicKey.SAVE_FIRST_OPEN_KEY, true);
    }



    // 设置会议密码
    public void setConferencePassSP(String name) {

        editor.putString(PublicKey.SAVE_CONFERENCE_PASS_KEY, name);
        editor.commit();
    }

    // 获取会议密码
    public String getConferencePassSP() {
        return preferences.getString(PublicKey.SAVE_CONFERENCE_PASS_KEY, null);

    }
    // 设置主持人密码
    public void setHostPassSP(String name) {
        editor.putString(PublicKey.SAVE_CONFERENCE_PASS_KEY, name);
        editor.commit();
    }

    // 获取主持人密码
    public String getHostPassSP() {
        return preferences.getString(PublicKey.SAVE_HOST_PASS_KEY, null);
    }
}
