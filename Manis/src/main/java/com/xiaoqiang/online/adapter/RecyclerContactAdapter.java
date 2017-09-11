package com.xiaoqiang.online.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.javaBeen.ContactInfoBean;
import com.xiaoqiang.online.listener.OnRecyclerViewListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuAdapter;

import java.util.List;

/**
 * Ceate author: xiaoqiang on 2017/4/27 13:12
 * RecyclerContactAdapter (TODO)
 * 主要功能：通讯录页面RecyclerView适配器
 * 邮箱：yugu88@163.com
 */
public class RecyclerContactAdapter extends SwipeMenuAdapter<RecyclerContactAdapter.ViewHolder>
        implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {

    private List<ContactInfoBean> mDataList;

    private OnRecyclerViewListener onRecyclerViewListener;
    public void setOnRecyclerViewListener(OnRecyclerViewListener onRecyclerViewListener) {
        this.onRecyclerViewListener = onRecyclerViewListener;//封装点击事件给Activity
    }

    public RecyclerContactAdapter(List<ContactInfoBean> mDataList) {//构造函数
        this.mDataList = mDataList;
        MLog.d(mDataList.size());
    }
    ////////////////////----------此部分是RecyclerView的适配----------
    @Override
    public int getItemCount() {
        return mDataList.size();
    }
    @Override
    public View onCreateContentView(ViewGroup parent, int viewType) {
        return  LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item, parent, false);
    }

    @Override
    public ViewHolder onCompatCreateViewHolder(View realContentView, int viewType) {
        ViewHolder viewHolder = new ViewHolder(realContentView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerContactAdapter.ViewHolder holder, int position) {

        holder.vhPosition=holder.getAdapterPosition();
        holder.tvName.setText(mDataList.get(position).getName());
//        holder.tvPinyin.setText(mDataList.get(position).getPinyin());
        MLog.e(mDataList.get(position).getName());
    }
    public  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnLongClickListener{
        TextView tvName;
        TextView tvPinyin;
        int vhPosition;
        ImageView chat_img,chat_video,call_img;

        public ViewHolder(View v) {
            super(v);

            tvName=(TextView)v.findViewById(R.id.item_name);
            tvPinyin=(TextView)v.findViewById(R.id.item_pinyin);
            chat_img = (ImageView)v.findViewById(R.id.chat_img);
            chat_video = (ImageView)v.findViewById(R.id.chat_video);
            call_img = (ImageView)v.findViewById(R.id.call_img);
//            chat_img = (ImageView)v.findViewById(R.id.chat_img);
//            chat_img = (ImageView)v.findViewById(R.id.chat_img);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(onRecyclerViewListener!=null){
                onRecyclerViewListener.onItemClick(vhPosition);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(onRecyclerViewListener!=null){
                onRecyclerViewListener.onItemLongClick(vhPosition);
            }
            return false;
        }
    }

//////////////////////////////////-------------往下是Header部分的适配----------
    @Override
    public long getHeaderId(int position) {
        return getItemSortLetter(position).charAt(0);
    }
    public String getItemSortLetter(int position) {
        return mDataList.get(position).getPinyin().substring(0,1);
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        String showValue =getItemSortLetter(position);

        textView.setText(showValue);
    }

    public int getPositionForSection(char section) {
        for (int i = 0; i < getItemCount(); i++) {
            char firstChar = mDataList.get(i).getPinyin().charAt(0);
            if (firstChar == section) {
                return i;
            }
        }
        return -1;

    }
}
