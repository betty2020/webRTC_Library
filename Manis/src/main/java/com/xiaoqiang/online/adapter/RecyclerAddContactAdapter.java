package com.xiaoqiang.online.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cisco.core.entity.Friend;
import com.xiaoqiang.online.R;

import java.util.List;

/**
 * Created by xiaoqiang on 2017/6/11 18:42
 * 2017 to: 邮箱：sin2t@sina.com
 * androidApp
 */

public class RecyclerAddContactAdapter extends RecyclerView.Adapter<RecyclerAddContactAdapter.RecHoder> {

    Activity content;
    private List<Friend> mDataList;

    public RecyclerAddContactAdapter(Activity content, List mDataList) {
        this.content = content;
        this.mDataList = mDataList;
    }

    @Override
    public RecHoder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(content);
        View view = layoutInflater.inflate(R.layout.add_contact_item, parent, false);
        return new RecHoder(view);
    }

    @Override
    public void onBindViewHolder(final RecHoder holder, final int position) {
        // 如果设置了回调，则设置点击事件
        if (mOnItemClickLitener != null) {
            holder.add_friend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.add_friend, position);
                }
            });

            holder.add_friend.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.add_friend, position);
                    return false;
                }
            });
        }


        holder.add_friend.setText("添加");
        holder.item_name_add.setText(mDataList.get(position).getUserName());
        holder.item_email_add.setText(mDataList.get(position).getEmail());
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    public class RecHoder extends RecyclerView.ViewHolder {
        public TextView item_name_add, item_email_add, add_friend;
        public View view;

        public RecHoder(View itemView) {
            super(itemView);
            view = itemView;
            item_name_add = (TextView) itemView.findViewById(R.id.item_name_add);
            item_email_add = (TextView) itemView.findViewById(R.id.item_email_add);
            add_friend = (TextView) itemView.findViewById(R.id.add_friend);
        }
    }

}
