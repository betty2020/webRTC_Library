package com.xiaoqiang.online.fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cisco.core.VideoImplement;
import com.cisco.core.entity.Participant;
import com.cisco.core.entity.Stats;
import com.cisco.core.entity.UserInfo;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.meet.util.SdpSsrcVariable;
import com.cisco.core.util.DensityUtil;
import com.cisco.core.util.SdkPublicKey;
import com.cisco.core.xmpp.Key;
import com.cisco.core.xmpp.XmppConnection;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.MainActivity;
import com.xiaoqiang.online.adapter.ChatMsgViewAdapter;
import com.xiaoqiang.online.adapter.RecyclerListAdapter;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.customview.DialogStyle;
import com.xiaoqiang.online.customview.WhiteBoardView;
import com.xiaoqiang.online.javaBeen.ChatingRecord;
import com.xiaoqiang.online.listener.DialogOnKeyDownListener;
import com.xiaoqiang.online.meeting.MeetingParticipants;
import com.xiaoqiang.online.util.SPUtil;
import com.xiaoqiang.online.util.TimeRender;

import org.webrtc.MediaStream;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoTrack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.xiaoqiang.online.util.SPUtil.Init;


/**
 * Ceate author: xiaoqiang on 2017/5/16 15:59
 * VideoFragment (TODO) 此类全部从老项目中拷贝出来的代码，抽时间按MVP模式拆分整理。
 * 主要功能：视频会议页
 * 邮箱：sin2t@sina.com
 */
@SuppressLint("ValidFragment")
public class VideoFragment extends Fragment implements CiscoApiInterface.OnLoginEvents,
        CiscoApiInterface.OnLoginLafterJoinRoomEvents, CiscoApiInterface.UpdateUIEvents, View.OnClickListener {

    private boolean hideMyself;// 隐藏自己的视频状态
    private int videoN;// 加入的远端视频数量
    private LinearLayout below_down_group, below_up_group;
    private ImageView mic_img, video_img, sound_img, bottom_hangup, pull, chats_img, share_img, sip_img, hide_img;
    private TextView roomNumber;
    public ImageView btn_participant, btn_switch_camera;
    private RelativeLayout rl_top;
    private LinearLayout tou_layout;
    private Boolean mic;
    private Boolean speakerSP;
    private EditText mEditTextContent;
    private ChatMsgViewAdapter mAdapter;
    private List<ChatingRecord> mDataArrays = new ArrayList<ChatingRecord>();
    ;
    private SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    ;
    private long timeStart;
    private RelativeLayout ll_videoview;
    private WhiteBoardView whiteBoardView;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private RelativeLayout big_frame;

    //linpeng
    private TextView unreadLabel;// 未读消息textview
    private boolean unRead = false;
    private int unReadNumber = 0;
    private List<Participant> listParticipant = new CopyOnWriteArrayList<>();
    private Participant myParticipant = new Participant();
    private ListView mListView;
    private RecyclerView recyclerList;
    private Map<String, Participant> personList = new ConcurrentHashMap<>();
    private List<String> jidList = new CopyOnWriteArrayList<>();
    private View view;
    private RecyclerListAdapter rlAdapter;
    private TextView textView;

    public VideoFragment() {//没有默认构造参数横屏部分机型系统会崩溃
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mic_img:
                //麦克风
                boolean enabled = CiscoApiInterface.app.onToggleMic();
                ToggleMic(enabled);
                break;
            case R.id.video_img:
                // 打开/关闭画面，对方看不到自己
                boolean viewenabled = CiscoApiInterface.app.notSendVideoStream();
                ToggleVideoView(viewenabled);
                break;
            case R.id.sound_img:
                ToggleButton sound_iv = (ToggleButton) getActivity().findViewById(R.id.sound_iv);
                boolean audioEnabled = CiscoApiInterface.app.notSendAudioStream();
                if (audioEnabled) {
                    SdpSsrcVariable.audiomuted = true;
                    sound_img.setImageResource(R.mipmap.speaker);
                    sound_iv.setChecked(true);
                } else {
                    SdpSsrcVariable.audiomuted = false;
                    sound_img.setImageResource(R.mipmap.speaker_off);
                    sound_iv.setChecked(false);
                }
                break;
            case R.id.bottom_hangup:
                //挂断
                InitComm.init().showDialog(view, getActivity(), 1);
                break;
            case R.id.pull:
                if (below_down_group.getVisibility() == View.GONE) {
                    below_up_group.setVisibility(View.GONE);
                    below_down_group.setVisibility(View.VISIBLE);
                } else if (below_down_group.getVisibility() == View.VISIBLE) {
                    below_down_group.setVisibility(View.GONE);
                    below_up_group.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.chats_img:
                //聊天
                initChatDialog();//聊天
                break;
            case R.id.share_img:
                //分享 邀请参会
                InitComm.init().showShare(getActivity(), null, false);
                break;
            case R.id.sip_img:
                //呼叫sip
                InitComm.init().showDialogBySip(view, getActivity());
                break;
            case R.id.hide_img:
                //隐藏自己 参会人员小于2人，或者当前账户为主持人
                MLog.e("__隐藏自己__" + myParticipant.isHost());
                if (myParticipant.isHost()) {
                    // TODO: 2017/7/21 需求变了，需要重新考虑数据逻辑
                    if (hideMyself) {
                        personList.put(InitComm.userInfo.getmJid(), InitComm.localParticipant);
                        jidList.add(InitComm.userInfo.getmJid());
                        rlAdapter.notifyDataSetChanged();
                        hideMyself = false;
                    } else {
                        personList.remove(InitComm.userInfo.getmJid());
                        jidList.remove(InitComm.userInfo.getmJid());
                        addBigStream(jidList.get(0));
                        rlAdapter.notifyDataSetChanged();
                        hideMyself = true;
                    }
                } else {
                    ToastUtils.show(getActivity(), "多人参会时" + "\r\n" + "仅主持人可以隐藏视频");
                }
                break;
            case R.id.btn_switch_camera:
                //切换摄像头
                CiscoApiInterface.app.onCameraSwitch();// 切换摄像头
                break;
            case R.id.btn_participant:
                new MeetingParticipants(VideoFragment.this, listParticipant).initParticipantDialog();
                //                initParticipantDialog();//"参会人员"
                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_meeting, container, false);
        acquireWakeLock();//唤醒锁
        videoN = Init(getActivity()).getVideoNumberSP() - 1;// 减去本地一个

        //----------------------menu------------------>
        rl_top = (RelativeLayout) view.findViewById(R.id.rl_top);//最外层
        below_down_group = (LinearLayout) view.findViewById(R.id.below_down_group);
        below_up_group = (LinearLayout) view.findViewById(R.id.below_up_group);
        mic_img = (ImageView) view.findViewById(R.id.mic_img);
        video_img = (ImageView) view.findViewById(R.id.video_img);
        sound_img = (ImageView) view.findViewById(R.id.sound_img);
        bottom_hangup = (ImageView) view.findViewById(R.id.bottom_hangup);
        pull = (ImageView) view.findViewById(R.id.pull);
        chats_img = (ImageView) view.findViewById(R.id.chats_img);
        share_img = (ImageView) view.findViewById(R.id.share_img);
        sip_img = (ImageView) view.findViewById(R.id.sip_img);
        hide_img = (ImageView) view.findViewById(R.id.hide_img);
        unreadLabel = (TextView) view.findViewById(R.id.unread_msg_number);

        //-------------------头部--------------------------->
        btn_participant = (ImageView) view.findViewById(R.id.btn_participant);
        btn_switch_camera = (ImageView) view.findViewById(R.id.btn_switch_camera);
        roomNumber = (TextView) view.findViewById(R.id.contact_name_call);
        if (!TextUtils.isEmpty(SPUtil.Init(getContext()).getConferencePassSP())) {
            roomNumber.setText("会议号:" + Key.roomnumber + " 密码:" + SPUtil.Init(getContext()).getConferencePassSP());
        } else {// 2017.7.19 备注：房间号密码不可以用这些变量
            roomNumber.setText("会议号:" + Key.roomnumber);
        }
        tou_layout = (LinearLayout) view.findViewById(R.id.tou_layout);

        //====================中部surfaceView=====================>
        recyclerList = (RecyclerView) view.findViewById(R.id.recycler_list);
        recyclerList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerList.setHasFixedSize(true);
        recyclerList.setItemAnimator(new DefaultItemAnimator());
        big_frame = (RelativeLayout) view.findViewById(R.id.big_frame);

        //---------------------初始白板控件--------------------->
        ll_videoview = (RelativeLayout) view.findViewById(R.id.ll_videoview);
        whiteBoardView = (WhiteBoardView) view.findViewById(R.id.draw);

        registerClickListener();//注册点击事件
        initData();// 连接会议
        return view;
    }

    public void registerClickListener() {
        /* 顶部*/
        btn_switch_camera.setOnClickListener(this);
        btn_participant.setOnClickListener(this);
        /* munu*/
        mic_img.setOnClickListener(this);
        video_img.setOnClickListener(this);
        sound_img.setOnClickListener(this);
        bottom_hangup.setOnClickListener(this);
        pull.setOnClickListener(this);
        chats_img.setOnClickListener(this);
        share_img.setOnClickListener(this);
        sip_img.setOnClickListener(this);
        hide_img.setOnClickListener(this);
        big_frame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tou_layout.getVisibility() == View.GONE) {
                    tou_layout.setVisibility(View.VISIBLE);
                    rl_top.setVisibility(View.VISIBLE);
                    if (textView != null)
                        textView.setPadding(5, DensityUtil.dip2px(getActivity(), 35), 5, 5);
                } else if (tou_layout.getVisibility() == View.VISIBLE) {
                    tou_layout.setVisibility(View.GONE);
                    rl_top.setVisibility(View.GONE);
                    if (textView != null)
                        textView.setPadding(5, DensityUtil.dip2px(getActivity(), 5), 5, 5);
                }
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    public void initChatDialog() {
        DialogStyle chatdialog = new DialogStyle(getActivity(), R.layout.activity_chat,
                R.style.Theme_dialog, 0);
        mEditTextContent = (EditText) chatdialog.findViewById(R.id.et_sendmessage);
        mListView = (ListView) chatdialog.findViewById(R.id.listview);

        chatdialog.setDialogOnKeyDownListener(new DialogOnKeyDownListener() {
            @Override
            public void onKeyDownListener(int keyCode, KeyEvent event) {
                unRead = false;
            }
        });
        TextView tv_head = (TextView) chatdialog.findViewById(R.id.tv_head);
        tv_head.setText("聊天");
        chatdialog.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
        chatdialog.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭聊天窗
                if (chatdialog.isShowing()) {
                    chatdialog.cancel();
                    unRead = false;
                }
            }
        });

        mAdapter = new ChatMsgViewAdapter(getContext(), mDataArrays);
        mListView.setAdapter(mAdapter);
        mListView.setSelection(mListView.getCount());
        try {
            timeStart = sd.parse("0000-00-00 00:00").getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //------------------响应点击操作-------------->>
        unRead = true;
        unReadNumber = 0;
        updateUnreadLabel(unReadNumber);
        chatdialog.show();
    }

    public void initSDK() {
        VideoImplement.videoImplement.audioManager.setSpeakerPhoneOn(mic);
    }

    public void initData() {
        //---------------------设置摄像头，分辨率-------------------->
        boolean cameraSP = Init(getActivity()).getCameraSP();//获取摄像头状态
        String videoQuality = Init(getActivity()).getVideoQualitySP();// 获取视频分辨率。
        if (TextUtils.isEmpty(videoQuality)) {
            Init(getActivity()).setVideoQualitySP(getResources().getString(R.string.setting_video_2));
            videoQuality = getResources().getString(R.string.setting_video_2);
        }
        CiscoApiInterface.app.setVidyoInfo(cameraSP, videoQuality);// 空指针
        CiscoApiInterface.app.connectToRoom(getActivity(), this);

        mic = Init(getActivity()).getMicSP();
        if (mic) {
            mic_img.setImageResource(R.mipmap.mute);
        } else {
            mic_img.setImageResource(R.mipmap.mute_off);
        }
        speakerSP = Init(getActivity()).getSpeakerSP();
        if (speakerSP) {
            sound_img.setImageResource(R.mipmap.speaker);
        } else {
            sound_img.setImageResource(R.mipmap.speaker_off);
        }
    }

    @Override
    public void onDestroy() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimerTask.cancel();
        }
        super.onDestroy();
    }

    @Override
    public void LoginResult(boolean result, String requestMessage, UserInfo userInfo) {
        MLog.e("userInfo=" + userInfo);
        InitComm.userInfo = userInfo;
        if (result) {
            ToastUtils.show(getActivity(), requestMessage);
            CiscoApiInterface.app.LoginAfterJoinRoomByNickname("202000002", "123456", "健康云", this);
        } else {
            ToastUtils.show(getActivity(), requestMessage);
        }
    }

    @Override
    public void LoginLafterJoinRoom(boolean result, String requestMessage) {
        if (result) {
            CiscoApiInterface.app.connectToRoom(getActivity(), this);
        } else {
            ToastUtils.show(getActivity(), requestMessage);
        }
    }

    @Override
    public void callConnected() {
        //会议连接成功后，（1）更新view，（2）向服务器发送上行下行宽带，丢包率等。
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initSDK();//设置布局和SurfaceView初始化
                CiscoApiInterface.app.enableStatsEvents();//向服务器发送上行下行宽带，丢包率等。
            }
        });
    }

    @Override
    public void onIceFailed() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.show(getActivity(), "会议连接失败");
                CallHangup();
            }
        });
    }

    @Override
    public void IMMessageRecever(final String Message, final String friendName) {
        //必须在ui线程中调用
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String time3 = TimeRender.getDate();
                long value = 0;
                try {
                    if (!TextUtils.isEmpty(time3)) {
                        value = sd.parse(time3).getTime();
                        MLog.w(friendName + ":" + Message);//web6944:web6944   linpeng:web6944
                        if (value - timeStart > 120000) {
                            //setAdapter(friendName, Message, time3, 1, friendName);// TODO: 2017/7/24 参数为空
                        } else {
                            //setAdapter(friendName, Message, "", 1, friendName);// // TODO: 2017/7/25 此时setAdapter内部会有空的全局对象
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (unRead == false) {
                    updateUnreadLabel(++unReadNumber);
                }
            }
        });
    }

    @Override
    public void onAddStream(final Participant participant) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                // TODO: 2017/7/24  participant偶尔测出来为null，难复现。还无法定位。
                MLog.e("远端流：分屏=" + participant.isScreen() + " ,stageJid=" + Key.stageJid);
                onMakeUpParticipant(participant);//SDK返回数据直接存储
            }
        });
    }

    public synchronized void onMakeUpParticipant(Participant p) {
        //所有变量和视频数据存储于本类内，关闭页面就自动销毁。其他类没有调用，所以不再存储全局变量。
        personList.put(p.getJid(), p);
        jidList.add(p.getJid());
        rlAdapter.notifyDataSetChanged();
        addBigStream(p.getJid());
        MLog.e("当前:Jid=" + p.getJid() + ", Nickname=" + p.getNickname());
    }

    @Override
    public void onRemoveStream(String jid) {
        MLog.e("离开会议：" + jid);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                if (InitComm.mainJid.equals(jid)) {//主屏为要移除的流时，本地先换上主屏。
                    addBigStream(InitComm.userInfo.getmJid());
                }
                personList.remove(jid);
                rlAdapter.notifyDataSetChanged();
                MLog.e("移除成功");
            }
        });
    }

    @Override
    public void onAddLocalStream(final Participant participant) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                MLog.e("大屏展示本地视频");
                participant.setNickname(InitComm.userInfo.getmUserName());// TODO: 2017/7/21 临时数据 目前SDK提供数据为空。
                rlAdapter = new RecyclerListAdapter(personList, getActivity(), jidList);
                onMakeUpParticipant(participant);//SDK返回数据直接存储
                recyclerList.setAdapter(rlAdapter);
                rlAdapter.setOnItemClickLitener(new RecyclerListAdapter.OnItemClickLitener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        addBigStream(jidList.get(position));
                    }
                });
                InitComm.localParticipant = participant;
            }
        });
    }

    public synchronized void addBigStream(String jid) {//上主屏
        big_frame.removeAllViews();
        //主屏如果写死布局，每次换流会有诸多潜在的问题。没有（踢出后 再添加）速度快。
        SurfaceViewRenderer surface = SdkPublicKey.createSurfaceView(getActivity());
        SdkPublicKey.initSurfaceView(surface);
        Participant participant = personList.get(jid);

        MediaStream stream = participant.getStream();
        VideoTrack track = stream.videoTracks.get(0);
        VideoRenderer vr = new VideoRenderer(surface);
        track.addRenderer(vr);

        //由于点击屏幕时，上下layout隐藏，name会跟随上下移动所以提取为全局变量，供点击事件使用。
        textView = new TextView(getActivity());
        textView.setText(participant.getNickname());
        textView.setTextColor(0xff30A070);
        textView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        textView.setGravity(Gravity.RIGHT);
        textView.setPadding(5, DensityUtil.dip2px(getActivity(), 35), 5, 5);

        big_frame.addView(surface);//remove时此对象就会失去所有引用并销毁
        big_frame.addView(textView);
        InitComm.mainJid = jid;//存储主屏的jid
    }

    @Override
    public void onOccupantLeftRoom(final String username) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MLog.e(username + " 离开room.1");
            }
        });
    }

    @Override
    public void beHostToOperate(final boolean opreate, final int type) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (type) {
                    case 1://被主持人静音
                        ToggleMic(!opreate);
                        CiscoApiInterface.app.setMic(opreate);//调用设置麦克风接口。
                        break;
                    case 2://代表被主持人禁视频
                        break;
                    case 3://被主持人剔除
                        CallHangup();
                        break;
                    case 4://web端移交主持人，给自己
                        myParticipant.setHost(true);
                        ToastUtils.show(getActivity(), "我是主持人");
                        break;
                }
            }
        });
    }

    @Override
    public void beMeetingSuperAdministratorControl(final boolean allaudio, final boolean allvideo, final boolean muted, final boolean videoClosed, final boolean hangup, final String userJid, final boolean lostModerator) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (hangup == true) {// 全员结束会议
                    CallHangup();
                }
                if (lostModerator) {
                    LostModeratorDialog();
                } else {
                    ToggleMic(allaudio);// 全员开启音频或全员静音
                    CiscoApiInterface.app.setMic(!allaudio);
                    ToggleVideoView(allvideo);// 全员开启视频或关闭视频
                    CiscoApiInterface.app.setVideo(!allvideo);
                }
                if (userJid != null) {
                    String jid = CiscoApiInterface.app.getUserJid();//自己的jid。2017.7.19 备注：此变量需要修改
                    if (userJid.equals(jid)) {
                        ToggleMic(!muted);// 会控 控制移动端 静音
                        CiscoApiInterface.app.setMic(muted);
                        ToggleVideoView(!videoClosed);// 会控 控制移动端 打开/关闭视频
                        CiscoApiInterface.app.setVideo(videoClosed);
                    }
                }
            }
        });
    }

    /***
     * 上主屏回调
     */
    @Override
    public void OnTheMainScreen(String jid) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Key.stageJid = jid;// 大屏ID被替换。
                addBigStream(jid);
            }
        });
    }

    /***
     * 会议中没主持人  提示五分钟关闭
     */
    private AlertDialog isHostDialog;

    private void LostModeratorDialog() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                CallHangup();
            }
        };
        mTimer.schedule(mTimerTask, 1000 * 60 * 5);
        isHostDialog = new AlertDialog.Builder(getActivity())
                .setCancelable(false)
                .setTitle("温馨提示")
                .setMessage("主持人已退出，此次会议5分钟内即将关闭!")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //关闭会议
                    }
                }).show();
    }

    /***
     * 结束会议
     */
    private void CallHangup() {
        CiscoApiInterface.app.onCallHangUp();// 结束会议
        getActivity().startActivity(new Intent(getActivity(), MainActivity.class));
        getActivity().finish();
    }

    @Override
    public void updatePartcipantList(final List<Participant> listParticipants) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MLog.d("人员列表回调完成---->");
                listParticipant = new CopyOnWriteArrayList<Participant>();
                listParticipant.addAll(listParticipants);
                //人员列表实时更新
                for (Participant participant : listParticipant) {
                    if (participant.getJid().equals(InitComm.userInfo.getmJid())) {
                        myParticipant = participant;
                        MLog.w("匹配当前用户信息成功,是否主持人=" + myParticipant.isHost());
                    }
                }
            }
        });
    }

    @Override
    public void sipDialBack() {
    }

    @Override
    public void onStatsInformation(Stats stat) {
    }

    @Override
    public void peerConnectionStatsReady(StatsReport[] reports) {
    }

    /**
     * 打开/关闭画面，对方看不到自己
     *
     * @param viewenabled
     */
    private void ToggleVideoView(boolean viewenabled) {
        if (viewenabled) {
            SdpSsrcVariable.videomuted = false;
            video_img.setImageResource(R.mipmap.video_chat);
        } else {
            SdpSsrcVariable.videomuted = true;
            video_img.setImageResource(R.mipmap.video_chat_off);
        }
        XmppConnection.getInstance().SendPresenceMessage();
    }

    /**
     * 切换麦克风
     *
     * @param enabled
     */
    public void ToggleMic(boolean enabled) {

        ToggleButton sound_mute = (ToggleButton) getActivity().findViewById(R.id.sound_mute);
        TextView text_driver = (TextView) getActivity().findViewById(R.id.text_driver);
        if (enabled) { // 切换麦克风 true=关闭
            SdpSsrcVariable.audiomuted = true;
            mic_img.setImageResource(R.mipmap.mute_off);
            sound_mute.setChecked(false);
            text_driver.setText("麦克风已关闭视频已暂停");
        } else {//变为开启
            SdpSsrcVariable.audiomuted = false;
            mic_img.setImageResource(R.mipmap.mute);
            sound_mute.setChecked(true);
            text_driver.setText("麦克风已开启视频已暂停");
        }
        XmppConnection.getInstance().SendPresenceMessage();
    }

    private void send() {
        String contString = mEditTextContent.getText().toString();
        if (contString.length() > 0) {
            String timeEnd = TimeRender.getDate();
            long time2 = 0;
            try {
                time2 = sd.parse(timeEnd).getTime();
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            long passTime = time2 - timeStart;
            if (passTime > 120000) {
                setAdapter(InitComm.userInfo.getmUserName(), contString, timeEnd, 0, InitComm.userInfo.getEndpoint());
                timeStart = time2;
            } else {
                setAdapter(InitComm.userInfo.getmUserName(), contString, "", 0, InitComm.userInfo.getEndpoint());
            }
            CiscoApiInterface.app.sendImMessage(contString);
            mEditTextContent.setText("");

        }
    }

    public void setAdapter(String userName, String infoText, String date, int type, String friendName) {
        ChatingRecord entity = new ChatingRecord();

        entity.setUserName(userName);
        entity.setChatingText(infoText);
        entity.setDate(date);
        entity.setType(type);
        entity.setFriendName(friendName);
        mDataArrays.add(entity);
        mAdapter = new ChatMsgViewAdapter(getContext(), mDataArrays);
        //        mAdapter.setColl(mDataArrays);
        //        mAdapter.notifyDataSetChanged();
        mListView.setAdapter(mAdapter);// TODO: 2017/7/25  回调时mListView还没有findviewbyid，逻辑需要再调整。
        mListView.setSelection(mListView.getCount());
    }

    @Override//共享白板
    public void onWhiteBoard(final String action, final String url) {
        Log.d("callactivity", "linpeng,<--ase--action=" + action + "=url=" + url);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (action.equals("create")) {
                    MLog.d("onWhiteBoard__>_拉起画布");
                    //拉起画布
                    ll_videoview.setVisibility(View.INVISIBLE);
                    whiteBoardView.setVisibility(View.VISIBLE);
                }
                if (action.equals("message")) {
                    //拉起画布
                    JSONObject rootNode = JSON.parseObject(url);
                    String type = rootNode.getString("type");
                    if (type.equals("Whiteboard Text")) {
                        String message = rootNode.getString("message");
                        JSONObject rn = JSON.parseObject(message);
                        String text = rn.getString("text");
                        String mouse = rn.getString("mouse");
                        JSONArray jsonArray = JSONArray.parseArray(mouse);
                        float x = Float.parseFloat(jsonArray.getString(0));
                        float y = Float.parseFloat(jsonArray.getString(1));
                        whiteBoardView.drawingText(x, y, text);
                    }

                    if (type.equals("Whiteboard")) {
                        String message = rootNode.getString("message");
                        JSONObject rootNode2 = JSON.parseObject(message);
                        String mouse = rootNode2.getString("mouse");
                        String penColor = rootNode2.getString("penColor");
                        String penSize = rootNode2.getString("penSize");
                        JSONArray ja = JSONArray.parseArray(mouse);
                        whiteBoardView.setPaintPen();//橡皮擦
                        if (penColor.equals("black")) {
                            whiteBoardView.setPaintColor(Color.BLACK);//设置颜色
                        } else if (penColor.equals("red")) {
                            whiteBoardView.setPaintColor(Color.RED);//设置颜色
                        } else if (penColor.equals("yellow")) {
                            whiteBoardView.setPaintColor(Color.YELLOW);//设置颜色
                        } else if (penColor.equals("blue")) {
                            whiteBoardView.setPaintColor(Color.BLUE);//设置颜色
                        } else if (penColor.equals("brown")) {
                            whiteBoardView.setPaintColor(Color.GREEN);//设置颜色
                        } else if (penColor.equals("rgba(0,0,0,1.0)")) {
                            whiteBoardView.setPaintRubber();//橡皮擦
                        }
                        whiteBoardView.setPaintWidth(Integer.parseInt(penSize));
                        //
                        float x = Float.parseFloat(ja.getJSONArray(0).getString(0));
                        float y = Float.parseFloat(ja.getJSONArray(0).getString(1));
                        float x1 = Float.parseFloat(ja.getJSONArray(1).getString(0));
                        float y1 = Float.parseFloat(ja.getJSONArray(1).getString(1));
                        whiteBoardView.startPaint(x, y, x1, y1);
                        Log.d("callactivity", "linpeng,<--ase--x=" + x + ",y=" + y + ",x1=" + x1 + ",y1=" + y1 + ",y1*2=" + y1 * 2);
                        Log.d("callactivity", "linpeng,<--ase----------------------action=" + action + ",type=" + type + ",mouse=" + mouse + ",pencolor=" + penColor + ",pensize=" + penSize);
                    }
                    if (type.equals("Clear Whiteboard")) {
                        whiteBoardView.clear();
                    }

                }
                if (action.equals("destroy")) {
                    Log.d("callactivity", "linpeng,<--ase--destroy= ");
                    //关闭画布
                    ll_videoview.setVisibility(View.VISIBLE);
                    whiteBoardView.clear();
                    whiteBoardView.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 主持人离开
     */
    @Override
    public void OnHostLeave() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LostModeratorDialog();
            }
        });

    }

    /**
     * 获取屏幕锁保持常亮
     */
    private PowerManager.WakeLock wakeLock;

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, this.getClass().getName());
        wakeLock.acquire();
    }

    /**
     * 刷新未读消息数
     */
    public void updateUnreadLabel(int count) {
        if (count > 0) {
            unreadLabel.setText(String.valueOf(count));
            unreadLabel.setVisibility(View.VISIBLE);
        } else {
            unreadLabel.setVisibility(View.INVISIBLE);
        }
    }
}
