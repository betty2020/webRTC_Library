/*
 *  Copyright 2014 The WebRTC Project Authors. All rights reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.cisco.core;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.cisco.core.util.AppRTCUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * AppRTCAudioManager manages all audio related parts of the AppRTC demo.
 */
public class AppRTCAudioManager {
    private static final String TAG = "AppRTCAudioManager";

    public enum AudioDevice {
        SPEAKER_PHONE,
        WIRED_HEADSET,
        EARPIECE,
    }

    private final Context apprtcContext;
    private final Runnable onStateChangeListener;
    private boolean initialized = false;
    private AudioManager audioManager;
    private int savedAudioMode = AudioManager.MODE_INVALID;//保存音频模式， 当前模式无效。 多种
    private boolean savedIsSpeakerPhoneOn = false;
    private boolean savedIsMicrophoneMute = false;
    private final AudioDevice defaultAudioDevice = AudioDevice.SPEAKER_PHONE;
    private AppRTCProximitySensor proximitySensor = null;
    private AudioDevice selectedAudioDevice;
    private final Set<AudioDevice> audioDevices = new HashSet<>();
    private BroadcastReceiver wiredHeadsetReceiver;

    //接近传感器改变状态
    private void onProximitySensorChangedState() {
        if (audioDevices.size() == 2
                && audioDevices.contains(AppRTCAudioManager.AudioDevice.EARPIECE)
                && audioDevices.contains(
                AppRTCAudioManager.AudioDevice.SPEAKER_PHONE)) {
            if (proximitySensor.sensorReportsNearState()) {
                setAudioDevice(AppRTCAudioManager.AudioDevice.EARPIECE);
            } else {
                setAudioDevice(AppRTCAudioManager.AudioDevice.SPEAKER_PHONE);
            }
        }
    }

    /**
     * Construction
     */
    public static AppRTCAudioManager create(Context context, Runnable deviceStateChangeListener) {
        return new AppRTCAudioManager(context, deviceStateChangeListener);
    }

    private AppRTCAudioManager(Context context, Runnable deviceStateChangeListener) {
        apprtcContext = context;
        onStateChangeListener = deviceStateChangeListener;
        audioManager = ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE));

        proximitySensor = AppRTCProximitySensor.create(context, new Runnable() {
            public void run() {
                onProximitySensorChangedState();
            }
        });
        AppRTCUtils.logDeviceInfo(TAG);
    }

    public void init() {
        Log.d(TAG, "init");
        if (initialized) {
            return;
        }
        // 获取系统各项参数
        savedAudioMode = audioManager.getMode();// audioManager为 framwork层管理音频类
        savedIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();//打开扬声器
        savedIsMicrophoneMute = audioManager.isMicrophoneMute();//MIC静音
        audioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);

        setMicrophoneMute(false);// 设置麦克风静音状态。
        updateAudioDeviceState(hasWiredHeadset());//更新的列表可能的音频设备和制造新设备的选择。
        registerForWiredHeadsetIntentBroadcast();// 寄存器接收的广播意图当有线耳机插入或拔出。收到意图将有一个额外的“状态”值,0意味着不插入,和1意味着插入。
        initialized = true;// 标记是否完成了初始化
    }

    public void close() {
        Log.d(TAG, "close");
        if (!initialized) {
            return;
        }
        unregisterForWiredHeadsetIntentBroadcast();
        setSpeakerphoneOn(savedIsSpeakerPhoneOn);
        setMicrophoneMute(savedIsMicrophoneMute);
        audioManager.setMode(savedAudioMode);
        audioManager.abandonAudioFocus(null);

        if (proximitySensor != null) {
            proximitySensor.stop();
            proximitySensor = null;
        }

        initialized = false;
    }

    /**
     * Changes selection of the currently active audio device.
     * 当前活动音频设备的更改选择。
     */
    public void setAudioDevice(AudioDevice device) {
        Log.d(TAG, "setAudioDevice(device=" + device + ")");
        AppRTCUtils.assertIsTrue(audioDevices.contains(device));

        switch (device) {
            case SPEAKER_PHONE:
                setSpeakerphoneOn(true);
                selectedAudioDevice = AudioDevice.SPEAKER_PHONE;
                break;
            case EARPIECE:
                setSpeakerphoneOn(false);
                selectedAudioDevice = AudioDevice.EARPIECE;
                break;
            case WIRED_HEADSET:
                setSpeakerphoneOn(false);
                selectedAudioDevice = AudioDevice.WIRED_HEADSET;
                break;
            default:
                Log.e(TAG, "Invalid audio device selection");
                break;
        }
        onAudioManagerChangedState();
    }

    /**
     * Returns current set of available/selectable audio devices.
     */
    public Set<AudioDevice> getAudioDevices() {
        return Collections.unmodifiableSet(new HashSet<AudioDevice>(audioDevices));
    }

    /**
     * Returns the currently selected audio device.
     */
    public AudioDevice getSelectedAudioDevice() {
        return selectedAudioDevice;
    }

    /**
     * 寄存器接收的广播意图当有线耳机插入或拔出。收到意图将有一个额外的“状态”值,0意味着不插入,和1意味着插入。
     */
    private void registerForWiredHeadsetIntentBroadcast() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);

        /** Receiver which handles changes in wired headset availability. */
        wiredHeadsetReceiver = new BroadcastReceiver() {
            private static final int STATE_UNPLUGGED = 0;
            private static final int STATE_PLUGGED = 1;
            private static final int HAS_NO_MIC = 0;
            private static final int HAS_MIC = 1;

            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra("state", STATE_UNPLUGGED);
                int microphone = intent.getIntExtra("microphone", HAS_NO_MIC);
                String name = intent.getStringExtra("name");
                Log.d(TAG, "BroadcastReceiver.onReceive" + AppRTCUtils.getThreadInfo()
                        + ": "
                        + "a=" + intent.getAction()
                        + ", s=" + (state == STATE_UNPLUGGED ? "unplugged" : "plugged")
                        + ", m=" + (microphone == HAS_MIC ? "mic" : "no mic")
                        + ", n=" + name
                        + ", sb=" + isInitialStickyBroadcast());

                boolean hasWiredHeadset = (state == STATE_PLUGGED);
                switch (state) {
                    case STATE_UNPLUGGED:
                        updateAudioDeviceState(hasWiredHeadset);
                        break;
                    case STATE_PLUGGED:
                        if (selectedAudioDevice != AudioDevice.WIRED_HEADSET) {
                            updateAudioDeviceState(hasWiredHeadset);
                        }
                        break;
                    default:
                        Log.e(TAG, "Invalid state");
                        break;
                }
            }
        };

        apprtcContext.registerReceiver(wiredHeadsetReceiver, filter);
    }

    /**
     * Unregister receiver for broadcasted ACTION_HEADSET_PLUG intent.
     */
    private void unregisterForWiredHeadsetIntentBroadcast() {
        apprtcContext.unregisterReceiver(wiredHeadsetReceiver);
        wiredHeadsetReceiver = null;
    }

    /**
     * Sets the speaker phone mode.
     */
    public void setSpeakerphoneOn(boolean on) {
        boolean wasOn = audioManager.isSpeakerphoneOn();
        if (wasOn == on) {
            return;
        }
        audioManager.setSpeakerphoneOn(on);
    }

    /**
     * 设置麦克风静音状态。
     */
    public void setMicrophoneMute(boolean on) {
        boolean wasMuted = audioManager.isMicrophoneMute();
        if (wasMuted == on) {
            return;
        }
        audioManager.setMicrophoneMute(on);
    }

    /**
     * 获取当前耳机状态。
     */
    private boolean hasEarpiece() {
        return apprtcContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_TELEPHONY);
    }

    /**
     * Checks whether a wired headset is connected or not.
     * This is not a valid indication that audio playback is actually over
     * the wired headset as audio routing depends on other conditions. We
     * only use it as an early indicator (during initialization) of an attached
     * wired headset.
     */
    @Deprecated
    private boolean hasWiredHeadset() {
        return audioManager.isWiredHeadsetOn();
    }

    /**
     * 更新的列表可能的音频设备和制造新设备的选择。
     */
    private void updateAudioDeviceState(boolean hasWiredHeadset) {
        // Update the list of available audio devices.
        audioDevices.clear();
        if (hasWiredHeadset) {
            // If a wired headset is connected, then it is the only possible option.
            audioDevices.add(AudioDevice.WIRED_HEADSET);
        } else {
            // No wired headset, hence the audio-device list can contain speaker
            // phone (on a tablet), or speaker phone and earpiece (on mobile phone).
            audioDevices.add(AudioDevice.SPEAKER_PHONE);
            if (hasEarpiece()) {
                audioDevices.add(AudioDevice.EARPIECE);
            }
        }
        Log.d(TAG, "audioDevices: " + audioDevices);

        // Switch to correct audio device given the list of available audio devices.
        if (hasWiredHeadset) {
            setAudioDevice(AudioDevice.WIRED_HEADSET);
        } else {
            setAudioDevice(defaultAudioDevice);
        }
    }

    /**
     * Called each time a new audio device has been added or removed.
     */
    private void onAudioManagerChangedState() {
        Log.d(TAG, "onAudioManagerChangedState: devices=" + audioDevices
                + ", selected=" + selectedAudioDevice);

        // Enable the proximity sensor if there are two available audio devices
        // in the list. Given the current implementation, we know that the choice
        // will then be between EARPIECE and SPEAKER_PHONE.
        if (audioDevices.size() == 2) {
            AppRTCUtils.assertIsTrue(audioDevices.contains(AudioDevice.EARPIECE)
                    && audioDevices.contains(AudioDevice.SPEAKER_PHONE));
            // Start the proximity sensor.
            proximitySensor.start();
        } else if (audioDevices.size() == 1) {
            // Stop the proximity sensor since it is no longer needed.
            proximitySensor.stop();
        } else {
            Log.e(TAG, "Invalid device list");
        }

        if (onStateChangeListener != null) {
            // Run callback to notify a listening client. The client can then
            // use public getters to query the new state.
            onStateChangeListener.run();
        }
    }


    /**
     * 听筒、扬声器切换
     * <p>
     * 注释： 敬那些年踩过的坑和那些网上各种千奇百怪坑比方案！！
     * <p>
     * AudioManager设置声音类型有以下几种类型（调节音量用的是这个）:
     * <p>
     * STREAM_ALARM 警报
     * STREAM_MUSIC 音乐回放即媒体音量
     * STREAM_NOTIFICATION 窗口顶部状态栏Notification,
     * STREAM_RING 铃声
     * STREAM_SYSTEM 系统
     * STREAM_VOICE_CALL 通话
     * STREAM_DTMF 双音多频,不是很明白什么东西
     * <p>
     * ------------------------------------------
     * <p>
     * AudioManager设置声音模式有以下几个模式（切换听筒和扬声器时setMode用的是这个）
     * <p>
     * MODE_NORMAL 正常模式，即在没有铃音与电话的情况
     * MODE_RINGTONE 铃响模式
     * MODE_IN_CALL 接通电话模式 5.0以下
     * MODE_IN_COMMUNICATION 通话模式 5.0及其以上
     *
     * @param on
     */
    public void setSpeakerPhoneOn(boolean on) {
        Log.d(TAG, "---audioEnabled=setSpeakerPhoneOn=" + on);
        if (on) {
            //打开扬声器
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC), AudioManager.FX_KEY_CLICK);

        } else {
            audioManager.setSpeakerphoneOn(false);

            //5.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);

            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
            }
        }

    }

    /**
     * linpeng add
     *
     * @param on
     */
    public void setSpeakerByLin(boolean on) {
        if (on) {
            setSpeakerphoneOn(true);
        } else {
            setSpeakerphoneOn(false);//关闭扬声器
            audioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
            //把声音设定成Earpiece（听筒）出来，设定为正在通话中
            audioManager.setMode(AudioManager.MODE_IN_CALL);
        }
    }

}
