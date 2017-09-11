package com.xiaoqiang.online.fragment;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.cisco.core.entity.Participant;
import com.cisco.core.entity.Stats;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.adapter.VideoListAdapter;
import com.xiaoqiang.online.commonUtils.ToastUtils;

import org.webrtc.StatsReport;

import java.util.ArrayList;
import java.util.List;


/**
 * Ceate author
 */
@SuppressLint("ValidFragment")
public class VideoFragment2 extends Fragment implements View.OnClickListener{

    private ListView video_listview;
    private  List<Participant> listParticipant;
    private VideoListAdapter videoListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_meeting2, container, false);
        initView(view);
        initVideoSdk();
        return view;
    }



    private void initView(View view) {
        video_listview = (ListView) view.findViewById(R.id.video_list);

        listParticipant=new ArrayList<>();
        videoListAdapter=new VideoListAdapter(this,listParticipant);
        video_listview.setAdapter(videoListAdapter);

    }

    private void initVideoSdk() {
//        CiscoApiInterface.app.setVidyoInfo(cameraSP, videoQuality);// 空指针
        CiscoApiInterface.app.connectToRoom(getActivity(), new CiscoApiInterface.UpdateUIEvents() {
            @Override
            public void callConnected() {
//                initSDK();//设置布局和SurfaceView初始化
                CiscoApiInterface.app.enableStatsEvents();//向服务器发送上行下行宽带，丢包率等。
            }

            @Override
            public void onIceFailed() {
                ToastUtils.show(getActivity(), "会议连接失败");
                CallHangup();
            }

            @Override
            public void IMMessageRecever(String Message, String name) {

            }

            @Override
            public void onAddStream(Participant participant) {
                listParticipant.add(participant);
                videoListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onRemoveStream(String jid) {

            }

            @Override
            public void onAddLocalStream(Participant participant) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listParticipant.add(participant);
                        videoListAdapter.notifyDataSetChanged();
                    }
                });

            }

            @Override
            public void onOccupantLeftRoom(String username) {

            }

            @Override
            public void beHostToOperate(boolean opreate, int type) {

            }

            @Override
            public void beMeetingSuperAdministratorControl(boolean allaudio, boolean allvideo, boolean muted, boolean videoClosed, boolean hangup, String userJid, boolean lostModerator) {

            }

            @Override
            public void OnTheMainScreen(String jid) {

            }

            @Override
            public void updatePartcipantList(List<Participant> listParticipant) {

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

            @Override
            public void onWhiteBoard(String action, String url) {

            }

            @Override
            public void OnHostLeave() {

            }
        });
    }
    /***
     * 结束会议
     */
    private void CallHangup() {
        CiscoApiInterface.app.onCallHangUp();// 结束会议
        getActivity().finish();
    }
    @Override
    public void onClick(View v) {

    }
}
