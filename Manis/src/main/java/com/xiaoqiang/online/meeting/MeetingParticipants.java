package com.xiaoqiang.online.meeting;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cisco.core.entity.Friend;
import com.cisco.core.entity.Participant;
import com.cisco.core.httpcallback.FriendsCallback;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.cisco.core.xmpp.Key;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.adapter.FriendsListAdapter;
import com.xiaoqiang.online.adapter.InviteListAdapter;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.customview.DialogStyle;
import com.xiaoqiang.online.fragment.VideoFragment;
import com.xiaoqiang.online.util.SPUtil;

import java.util.List;

/**
 * Created by xiaoqiang on 2017/7/14 18:30
 * 2017 to: 邮箱：sin2t@sina.com
 * androidApp
 */

public class MeetingParticipants {
    List<Participant> listParticipant;
    ImageView add_img, btn_back, iv_pull;
    VideoFragment context;
    private FriendsListAdapter friendsAdapter;

    public MeetingParticipants(final VideoFragment context, final List<Participant> listParticipant) {
        this.listParticipant = listParticipant;
        this.context = context;
    }

    public void initParticipantDialog() {
        if (listParticipant.size() > 0) {
            DialogStyle participantdialog = new DialogStyle(context.getActivity(), R.layout.activity_participant,
                    R.style.Theme_dialog, 0);
            TextView tv_head = (TextView) participantdialog.findViewById(R.id.tv_head);
            add_img = (ImageView) participantdialog.findViewById(R.id.add_img);
            btn_back = (ImageView) participantdialog.findViewById(R.id.btn_back);
            btn_back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (participantdialog.isShowing()) {
                        participantdialog.dismiss();
                    }
                }
            });
            if (InitComm.Guest) {
                add_img.setVisibility(View.GONE);
            } else {
                add_img.setVisibility(View.VISIBLE);
                add_img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //邀请参会

                        //----------------------邀请参会页初始化--------->>
                        if (!InitComm.Guest) {
                            initInviteDialog();//"邀请参会"
                        }
                    }
                });
            }
            tv_head.setText("参会人员");
            iv_pull = (ImageView) participantdialog.findViewById(R.id.iv_pull);
            LinearLayout ll_conferenceinfo = (LinearLayout) participantdialog.findViewById(R.id.ll_conferenceinfo);
            View fenggexian = (View) participantdialog.findViewById(R.id.fenggexian);
            iv_pull.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ll_conferenceinfo.getVisibility() == View.GONE) {
                        ll_conferenceinfo.setVisibility(View.VISIBLE);
                        fenggexian.setVisibility(View.VISIBLE);
                    } else {
                        ll_conferenceinfo.setVisibility(View.GONE);
                        fenggexian.setVisibility(View.GONE);
                    }
                }
            });

            TextView number = (TextView) participantdialog.findViewById(R.id.participant_nonumber);
            initSearchEdtit(participantdialog);//初始化搜索

            TextView conferencepass = (TextView) participantdialog.findViewById(R.id.participant_pass);
            TextView type = (TextView) participantdialog.findViewById(R.id.participant_type);
            TextView hostpass = (TextView) participantdialog.findViewById(R.id.participant_hostpass);

            number.setText("会议号：" + Key.roomnumber);
            if (!TextUtils.isEmpty(SPUtil.Init(context.getActivity()).getConferencePassSP())) {
                conferencepass.setText("会议密码：" + SPUtil.Init(context.getActivity()).getConferencePassSP());
            } else {
                conferencepass.setText("会议密码：无密码");
            }

            type.setText("会议类型：临时会议");
            String hp = "";
            if (!TextUtils.isEmpty(Key.op)) {
                hp = Key.op;
            } else {
                hp = "无密码";
            }
            hostpass.setText("主持人密码：" + hp);
            ListView friendsListView = (ListView) participantdialog.findViewById(R.id.participant_listview);
            friendsAdapter = new FriendsListAdapter(this.context, listParticipant);
            friendsListView.setAdapter(friendsAdapter);
            participantdialog.show();
        }
    }
    /***
     * 邀请参会
     */
    public void initInviteDialog() {
        DialogStyle invitedialog = new DialogStyle(context.getActivity(), R.layout.activity_invitejoin,
                R.style.Theme_dialog, 0);
        TextView tv_head = (TextView) invitedialog.findViewById(R.id.tv_head);
        tv_head.setText("邀请参会");
        EditText et_search = (EditText) invitedialog.findViewById(R.id.et_search);
        et_search.setHint("搜索联系人");
        invitedialog.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (invitedialog.isShowing()) {
                    invitedialog.cancel();
                }
            }
        });
        initInviteData(invitedialog);
        invitedialog.show();
    }

    private void initInviteData(DialogStyle invitedialog ) {
        //获取网络数据
        String userid = InitComm.userInfo.getmUserId();
        CiscoApiInterface.app.GetFriendsList(userid, new FriendsCallback() {
            @Override
            public void onSucess(List<Friend> friendsList) {
                ListView inviteListView = (ListView) invitedialog.findViewById(R.id.invite_listview);
                InviteListAdapter inviteAdapter = new InviteListAdapter(context.getActivity(), friendsList);
                inviteListView.setAdapter(inviteAdapter);
                if (inviteAdapter != null) {
                    inviteAdapter.refresh(friendsList);
                }
            }

            @Override
            public void onFailed() {
                InitComm.init().closeView();
                ToastUtils.show(context.getActivity(), "请求数据失败");
            }
        });
    }

    /**
     * 搜索栏
     */
    private void initSearchEdtit(DialogStyle participantdialog) {
        EditText mClearEditText = (EditText) participantdialog.findViewById(R.id.et_search);
        final TextView text_cancel = (TextView) participantdialog.findViewById(R.id.text_cancel);
        text_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClearEditText.setText(null);
                InitComm.init().hideInput(context.getActivity(), mClearEditText);
                text_cancel.setVisibility(View.GONE);
                filterData(null);
            }
        });
        mClearEditText.setHint("搜索联系人");
        // 根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //s:变化前的所有字符； start:字符开始的位置； count:变化前的总字节数；after:变化后的字节数
                MLog.e("变化前:" + s + ";" + start + ";" + count + ";" + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //S：变化后的所有字符；start：字符起始的位置；before: 变化之前的总字节数；count:变化后的字节数
                MLog.e("变化后:" + s + ";" + start + ";" + before + ";" + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //s:变化后的所有字符
                MLog.e("变化:" + s);
                if (s.length() > 0) {
                    text_cancel.setVisibility(View.VISIBLE);
                } else {
                    text_cancel.setVisibility(View.GONE);
                    filterData(null);
                }
            }
        });
        // 键盘编辑事件监听
        mClearEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    InitComm.init().hideInput(context.getActivity(), mClearEditText);
                    String s = mClearEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(s)) {
                        filterData(s);
                    }
                    return true;
                }
                return false;
            }
        });

        // 键盘回车键监听
        mClearEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    InitComm.init().hideInput(context.getActivity(), mClearEditText);
                    String s = mClearEditText.getText().toString().trim();
                    if (!TextUtils.isEmpty(s)) {
                        filterData(s);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * 获取每个字符或者直接用 toCharArray 方法
     * 通过检索输入的字符，是不是顺序匹配pinyin项实现筛选
     *
     * @param nickName 搜索条件
     */
    private void filterData(String nickName) {
        friendsAdapter.search(nickName);
    }

}
