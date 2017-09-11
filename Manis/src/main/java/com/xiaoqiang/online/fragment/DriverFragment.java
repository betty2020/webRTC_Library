package com.xiaoqiang.online.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.meet.util.SdpSsrcVariable;
import com.cisco.core.xmpp.XmppConnection;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.util.SPUtil;

import static com.xiaoqiang.online.util.SPUtil.Init;


/**
 * Ceate author: xiaoqiang on 2017/5/16 17:27
 * DriverFragment (TODO)
 * 主要功能：驾驶模式页面
 * 邮箱：sin2t@sina.com
 */
public class DriverFragment extends Fragment implements View.OnClickListener {

    private View view;
    private ImageView phone_iv;
    private ToggleButton sound_iv, sound_mute;
    private TextView text_driver;

    private ImageView mic_img;
    public static DriverFragment driverFragment;
    private ImageView sound_img;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_driver, container, false);
        findViews();
        return view;
    }

    private void findViews() {
        driverFragment = this;
        sound_iv = (ToggleButton) view.findViewById(R.id.sound_iv);
        phone_iv = (ImageView) view.findViewById(R.id.phone_iv);
        text_driver = (TextView) view.findViewById(R.id.text_driver);
        sound_mute = (ToggleButton) view.findViewById(R.id.sound_mute);
        sound_iv.setOnClickListener(this);
        phone_iv.setOnClickListener(this);
        sound_mute.setOnClickListener(this);
        // ---视频页面view-----
        mic_img = (ImageView) getActivity().findViewById(R.id.mic_img);
        sound_img = (ImageView) getActivity().findViewById(R.id.sound_img);
        // ---功能分割线---------
        Boolean mic = Init(getActivity()).getMicSP();// true=开
        if (mic) {
            text_driver.setText("麦克风已开启视频已暂停");
        } else {
            text_driver.setText("麦克风已关闭视频已暂停");
        }
        sound_mute.setChecked(SPUtil.Init(getActivity()).getMicSP());// true=开
        sound_iv.setChecked(Init(getActivity()).getSpeakerSP());// true=开
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sound_iv:
                boolean audioEnabled = CiscoApiInterface.app.notSendAudioStream();
                if (audioEnabled) {
                    SdpSsrcVariable.audiomuted = true;
                    SPUtil.Init(getActivity()).setSpeakerSP(true);
                    sound_iv.setChecked(true);
                    sound_img.setImageResource(R.mipmap.speaker);
                } else {
                    SdpSsrcVariable.audiomuted = false;
                    SPUtil.Init(getActivity()).setSpeakerSP(false);
                    sound_iv.setChecked(false);
                    sound_img.setImageResource(R.mipmap.speaker_off);
                }
                break;
            case R.id.phone_iv:
                //挂断
                InitComm.init().showDialog(LayoutInflater.from(getActivity()).inflate(R.layout.activity_main, null), getActivity(), 1);
                break;
            case R.id.sound_mute:
                boolean enabled = CiscoApiInterface.app.onToggleMic();
                ToggleMic(enabled);
                break;
        }
    }

    public void ToggleMic(boolean enabled) {
        // 切换麦克风
        if (enabled) { // true=关闭
            SdpSsrcVariable.audiomuted = true;
            mic_img.setImageResource(R.mipmap.mute_off);
            sound_mute.setChecked(false);
            text_driver.setText("麦克风已关闭视频已暂停");
        } else {
            //变为开启
            SdpSsrcVariable.audiomuted = false;
            mic_img.setImageResource(R.mipmap.mute);
            sound_mute.setChecked(true);
            text_driver.setText("麦克风已开启视频已暂停");
        }
        XmppConnection.getInstance().SendPresenceMessage();

    }
}
