package com.xiaoqiang.online.adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cisco.core.entity.Participant;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.fragment.VideoFragment;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FriendsListAdapter extends BaseAdapter {
    private final VideoFragment context;
    private final List<Participant> displayList;
    private final List<Participant> originalParticipantList;
    private boolean HostTag = false;
    private volatile String searchNickname;

    public FriendsListAdapter(VideoFragment context, List<Participant> participantList) {
        super();
        this.context = context;
        this.originalParticipantList = new CopyOnWriteArrayList<>(participantList);
        this.displayList = new CopyOnWriteArrayList<>(participantList);

    }

    public void refresh(List<Participant> list, boolean isHost) {
        synchronized (this.originalParticipantList) {
            this.originalParticipantList.clear();
            this.originalParticipantList.addAll(list);
        }
        HostTag = isHost;
        searchAndDisplay(this.searchNickname);
        notifyDataSetChanged();

    }


    @Override
    public int getCount() {
        return displayList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return displayList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int arg0, View convertView, ViewGroup arg2) {
        final ViewHolder viewHodler;
        if (convertView == null) {
            convertView = View.inflate(context.getActivity(),
                    R.layout.list_friends_text, null);
            viewHodler = new ViewHolder();
            viewHodler.displayName_txt = (TextView) convertView.findViewById(R.id.member_name);
            viewHodler.partcitation_yijiaozhuciren = (ImageView) convertView.findViewById(R.id.partcitation_yijiaozhuciren);
            viewHodler.partcitation_tiren = (ImageView) convertView.findViewById(R.id.partcitation_tiren);
            viewHodler.partcitation_shangzhupin = (ImageView) convertView.findViewById(R.id.partcitation_shangzhupin);
            viewHodler.partcitation_jingyin = (ImageView) convertView.findViewById(R.id.partcitation_jingyin);

            viewHodler.member_ishost = (TextView) convertView.findViewById(R.id.member_ishost);

            convertView.setTag(viewHodler);
        } else {
            viewHodler = (ViewHolder) convertView.getTag();
        }

        final Participant participant = displayList.get(arg0);
        String userid = participant.getUserid();
        viewHodler.displayName_txt.setText(participant.getNickname());

        Log.d("adapter=", "HostTag=" + HostTag);
        if (HostTag) {
            //主持人显示图标    自己是主持人
            Log.d("adapter=2=", "HostTag=host=" + participant.isHost() + ",name=" + participant.getNickname());
            if (participant.isHost()) {
                viewHodler.member_ishost.setText("(主持人,我)");
                viewHodler.partcitation_yijiaozhuciren.setBackgroundResource(R.mipmap.joinlist_host_high);
                viewHodler.partcitation_tiren.setBackgroundResource(R.mipmap.out);
                viewHodler.partcitation_shangzhupin.setBackgroundResource(R.mipmap.participant_screen);
                viewHodler.partcitation_yijiaozhuciren.setEnabled(false);
                viewHodler.partcitation_tiren.setEnabled(false);
                viewHodler.partcitation_shangzhupin.setEnabled(false);
            } else {
                Log.d("adapter=2=", "come on 群众");
                viewHodler.member_ishost.setText("(群众)");
                viewHodler.partcitation_yijiaozhuciren.setBackgroundResource(R.mipmap.joinlist_host_nor);
                viewHodler.partcitation_tiren.setBackgroundResource(R.mipmap.outcolor);
                viewHodler.partcitation_shangzhupin.setBackgroundResource(R.mipmap.participant_screen_color);
                viewHodler.partcitation_yijiaozhuciren.setEnabled(true);
                viewHodler.partcitation_tiren.setEnabled(true);
                viewHodler.partcitation_shangzhupin.setEnabled(true);
            }


        } else {
            //图标不可显示   自己不是主持人
            if (participant.isHost()) {
                viewHodler.member_ishost.setText("(主持人)");
            } else if (participant.getUserid().equals(InitComm.userInfo.getEndpoint())) {
                viewHodler.member_ishost.setText("(群众,我)");
            } else {
                viewHodler.member_ishost.setText("(群众)");
            }
            viewHodler.partcitation_yijiaozhuciren.setBackgroundResource(R.mipmap.joinlist_host_high);
            viewHodler.partcitation_tiren.setBackgroundResource(R.mipmap.out);
            viewHodler.partcitation_shangzhupin.setBackgroundResource(R.mipmap.participant_screen);
            viewHodler.partcitation_yijiaozhuciren.setEnabled(false);
            viewHodler.partcitation_tiren.setEnabled(false);
            viewHodler.partcitation_shangzhupin.setEnabled(false);
        }
        if (participant.isGetMuteMic()) {
            //true 代表关闭
            viewHodler.partcitation_jingyin.setBackgroundResource(R.mipmap.mute_off);
        } else {
            //true 代表打开
            viewHodler.partcitation_jingyin.setBackgroundResource(R.mipmap.mute);
        }

        viewHodler.partcitation_yijiaozhuciren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //移交主持人
                String jid = participant.getJid();
                Log.d("", "linpeng,移交主持人userid=" + userid + ",jid=" + jid);
                CiscoApiInterface.app.grantAdmin(jid, userid);
                HostTag = false;
                notifyDataSetChanged();

            }
        });

        viewHodler.partcitation_tiren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //踢人
                if (!participant.isHost()) {
                    CiscoApiInterface.app.hostTiren(userid);
                }

            }
        });
        viewHodler.partcitation_shangzhupin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //上主屏
            }
        });
        viewHodler.partcitation_jingyin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //静音
                boolean ismute = participant.isGetMuteMic();
                Log.d("", "linpeng,updatePartcipantList-friendsAdapter-点击静音userid=" + userid + ",ismute=" + ismute);
                if (userid.equals(InitComm.userInfo.getEndpoint())) {
                    boolean enabled = CiscoApiInterface.app.onToggleMic();//自己本地麦克风
                    context.ToggleMic(enabled);
                } else {
                    CiscoApiInterface.app.hostMute(userid, ismute);
                }
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    public void search(String sNickname) {
        this.searchNickname = sNickname;
        searchAndDisplay(this.searchNickname);
        notifyDataSetChanged();
    }

    private void searchAndDisplay(String sNickname) {
        synchronized (displayList) {
            displayList.clear();
            if (TextUtils.isEmpty(sNickname)) {
                displayList.addAll(originalParticipantList);
            } else {
                for (Participant tempContactInfoBean : originalParticipantList) {
                    String nickname = tempContactInfoBean.getNickname();
                    if (!TextUtils.isEmpty(nickname) && nickname.contains(sNickname)) {
                        displayList.add(tempContactInfoBean);
                    }
                }
            }
            if (displayList.isEmpty()) {
                ToastUtils.show(context.getActivity(), "没有查到相关人员");
            }
        }
    }

    private class ViewHolder {
        public TextView displayName_txt;
        public TextView member_ishost;
        public ImageView partcitation_yijiaozhuciren;
        public ImageView partcitation_tiren;
        public ImageView partcitation_shangzhupin;
        public ImageView partcitation_jingyin;

    }
}
