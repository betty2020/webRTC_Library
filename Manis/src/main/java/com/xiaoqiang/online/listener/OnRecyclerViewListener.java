package com.xiaoqiang.online.listener;

/**
 * Ceate author: xiaoqiang on 2017/4/27 12:57
 * OnRecyclerViewListener (TODO)
 * 主要功能：通讯录页面的RecyclerView的ClickListener接口
 * 邮箱：yugu88@163.com
 */
public interface OnRecyclerViewListener {
    void onItemClick(int position);
    boolean onItemLongClick(int position);
}