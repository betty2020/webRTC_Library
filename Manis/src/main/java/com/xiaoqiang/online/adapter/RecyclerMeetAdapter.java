/*
 * Copyright 2016 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xiaoqiang.online.adapter;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cisco.core.entity.ConferenceRecord;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.activitys.main.MeetingActivity;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.listener.OnItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.text.SimpleDateFormat;
import java.util.List;


/**
 * Ceate author: xiaoqiang on 2017/4/28 11:03
 * RecyclerMeetAdapter (TODO)
 * 主要功能：会议记录页面RecyclerView适配器
 * 邮箱：yugu88@163.com
 */
public class RecyclerMeetAdapter extends SwipeMenuAdapter<RecyclerMeetAdapter.DefaultViewHolder> implements CiscoApiInterface.OnLoginLafterJoinRoomEvents {

    private List<ConferenceRecord> conferenceRecordList;

    private OnItemClickListener mOnItemClickListener;
    private View view;
    private Activity context;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 1;

    public RecyclerMeetAdapter(Activity context, List<ConferenceRecord> conferenceRecordList) {
        this.context = context;
        this.conferenceRecordList = conferenceRecordList;
        for (int i = 0; i < conferenceRecordList.size(); i++) {
            ConferenceRecord record = conferenceRecordList.get(i);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return conferenceRecordList == null ? 0 : conferenceRecordList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        // 最后一个item设置为footerView
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        MLog.e(viewType);
        if (conferenceRecordList.size() > 10) {
            if (viewType == TYPE_ITEM) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
            } else if (viewType == TYPE_FOOTER) {
                // type == TYPE_FOOTER 返回footerView
                view = LayoutInflater.from(context).inflate(R.layout.footerview, null);
            }
        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        }
        return view;
    }

    @Override
    public RecyclerMeetAdapter.DefaultViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        DefaultViewHolder viewHolder = new DefaultViewHolder(realContentView);
        viewHolder.mOnItemClickListener = (OnItemClickListener) mOnItemClickListener;
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerMeetAdapter.DefaultViewHolder holder, int position) {
        if (holder instanceof DefaultViewHolder && position < conferenceRecordList.size()) {
            holder.setIsRecyclable(false);
            ConferenceRecord record = conferenceRecordList.get(position);
            String startTime = record.getStartTime();
            String status = record.getStatus();
            String title = record.getTitle();
            final String number = record.getcNumber();
            final String meetPassword = record.getMeetPassword();
            SimpleDateFormat time = new SimpleDateFormat("yyyy/MM/dd");
            if (startTime != null) {
                holder.tv_number.setText("(" + time.format(Long.parseLong(startTime)) + ")");
            }
            if ("MSD0".equals(status)) {//switch--case 复用逻辑处理会延时
                holder.text_meeting.setText("会议进行中");
                holder.text_meeting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //参加会议
                        MLog.e("参加预约会议:" + number + "密码：" + meetPassword);
                        CiscoApiInterface.app.LoginAfterJoinRoom(number, meetPassword, RecyclerMeetAdapter.this);
                    }
                });
                holder.sign_sta.setVisibility(View.VISIBLE);
                holder.image_meeting.setImageResource(R.mipmap.meeting_on);
            } else if ("MSA0".equals(status)) {
                holder.text_meeting.setText("已预约 参加");
                holder.text_meeting.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //参加会议
                        MLog.e("参加预约会议:" + number + "密码：" + meetPassword);
                        CiscoApiInterface.app.LoginAfterJoinRoom(number, meetPassword, RecyclerMeetAdapter.this);
                    }
                });
                holder.sign_sta.setVisibility(View.GONE);
                holder.image_meeting.setImageResource(R.mipmap.meeting_normal);
            } else if ("MSE0".equals(status)) {
                holder.text_meeting.setText("会议已结束");
                holder.sign_sta.setVisibility(View.GONE);
                holder.image_meeting.setImageResource(R.mipmap.meeting_done);
            } else {
                holder.text_meeting.setVisibility(View.GONE);
                holder.sign_sta.setVisibility(View.GONE);
                holder.image_meeting.setVisibility(View.GONE);
            }

            if (title == null) {
                holder.tvTitle.setText(number);
            } else {
                holder.tvTitle.setText(title);
            }
        }
    }

    @Override
    public void LoginLafterJoinRoom(boolean result, String requestMessage) {
        MLog.e("进入预约会议的回调" + result);
        if (result) {
            context.startActivity(new Intent(context, MeetingActivity.class));
        } else {
            ToastUtils.show(context, requestMessage);
        }
    }

//    @Override
//    public void LoginLafterCreateRoom(boolean result, String hostpass, String roomid, String requestMessage) {
//
//    }

    ///////////////-------------mHolder----------->
    static class DefaultViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView tvTitle, text_meeting, tv_number, foot_text;
        OnItemClickListener mOnItemClickListener;
        ImageView sign_sta, image_meeting;

        public DefaultViewHolder(View itemView) {
            super(itemView);
            setIsRecyclable(false);
            itemView.setOnClickListener(this);
            tvTitle = (TextView) itemView.findViewById(R.id.tv_title);
            tv_number = (TextView) itemView.findViewById(R.id.tv_number);
            sign_sta = (ImageView) itemView.findViewById(R.id.sign_sta);
            image_meeting = (ImageView) itemView.findViewById(R.id.image_meeting);
            text_meeting = (TextView) itemView.findViewById(R.id.text_meeting);
            //            foot_text = (TextView) itemView.findViewById(R.id.foot_text);
        }

        @Override
        public void onClick(View v) {
            if (mOnItemClickListener != null) {
                mOnItemClickListener.onItemClick(getAdapterPosition());
            }
        }
    }

}
