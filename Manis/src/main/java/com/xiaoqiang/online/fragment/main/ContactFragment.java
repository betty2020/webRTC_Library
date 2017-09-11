package com.xiaoqiang.online.fragment.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.cisco.core.entity.Friend;
import com.cisco.core.httpcallback.FriendsCallback;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.adapter.RecycleViewDivider;
import com.xiaoqiang.online.adapter.RecyclerContactAdapter;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.xiaoqiang.online.customview.SideBar;
import com.xiaoqiang.online.javaBeen.ContactInfoBean;
import com.xiaoqiang.online.listener.OnRecyclerViewListener;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Ceate author: xiaoqiang on 2017/4/27 10:57
 * ContactFragment (TODO)
 * 主要功能：通讯录页面的Fragment
 * 邮箱：yugu88@163.com
 */

public class ContactFragment extends Fragment implements View.OnClickListener {
    private View view;
    //数据源
    private List<ContactInfoBean> exampleList;
    private List<ContactInfoBean> tempExampleList;//实际操作数据
    //搜索框
    private EditText mClearEditText;
    private TextView tvNoResult;//如果结果为空，则显示
    //侧边栏
    private SideBar sideBar;
    private TextView anno;
    //列表
    private SwipeMenuRecyclerView recyclerView;
    private RecyclerContactAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_contact, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initData();
        initViews();
    }

    private void initViews() {
        tvNoResult = (TextView) view.findViewById(R.id.noResult);
        //此处老是提示空指针错误，有人知道为什么吗
        //是在一次AS升级之后出现的，知道的话发我邮箱指导一下，谢谢！sin2t@sina.com
        tvNoResult.setVisibility(View.GONE);
        initRecyclerView();//初始化列表
        initSideBar();//初始化侧边音序索引
        initSearchEdtit();// 初始化搜索栏
    }

    /**
     * 搜索栏
     */
    private void initSearchEdtit() {
        mClearEditText = (EditText) view.findViewById(R.id.et_search);
        final TextView text_cancel = (TextView) view.findViewById(R.id.text_cancel);
        text_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClearEditText.setText(null);
                InitComm.init().hideInput(getActivity(), mClearEditText);
                filterData(null);
            }
        });
        mClearEditText.setHint("搜索联系人");
        initListener();//初始化搜索按钮事件
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
                //filterData(s.toString());
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
    }

    /**
     * 获取每个字符或者直接用 toCharArray 方法
     * 通过检索输入的字符，是不是顺序匹配pinyin项实现筛选
     *
     * @param s 搜索条件
     */
    private void filterData(String s) {
        tempExampleList.clear();
        if (TextUtils.isEmpty(s)) {
            tempExampleList.addAll(exampleList);
        } else {
            ContactInfoBean tempContactInfoBean;
            int listSize = exampleList.size();
            for (int i = 0; i < listSize; i++) {
                tempContactInfoBean = exampleList.get(i);
                String[] str = tempContactInfoBean.getPinyin().split(" ");
                MLog.e(tempContactInfoBean.getPinyin());
                int strLength = str.length;
                for (int j = 0; j < strLength; j++) {
                    boolean booleen = str[j].toLowerCase().startsWith(s.toLowerCase());
                    boolean has = str[j].toLowerCase().contains(s.toLowerCase());
                    MLog.e(str[j] + str[j].toLowerCase() + " _ " + s.toLowerCase());
                    if (booleen) {
                        tempExampleList.add(tempContactInfoBean);
                    }
                }
            }
        }
        //放置没有搜索结果的图片
        if (tempExampleList.size() == 0) {
            tvNoResult.setVisibility(View.VISIBLE);
        } else {
            tvNoResult.setVisibility(View.GONE);
        }
        mAdapter.notifyDataSetChanged();
    }

    /**
     * 侧边音序索引
     */
    private void initSideBar() {
        sideBar = (SideBar) view.findViewById(R.id.sideBar);
        anno = (TextView) view.findViewById(R.id.anno);
        anno.setVisibility(View.GONE);
        sideBar.setTextView(anno);
        // 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    ((LinearLayoutManager) mLayoutManager).scrollToPositionWithOffset(position, 0);
                }
            }
        });
    }

    /**
     * 初始化列表
     */
    private void initRecyclerView() {
        recyclerView = (SwipeMenuRecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setHasFixedSize(true);
        mAdapter = new RecyclerContactAdapter(tempExampleList);
        mAdapter.setOnRecyclerViewListener(new OnRecyclerViewListener() {
            @Override
            public void onItemClick(int position) {
                //ToastUtils.show(getActivity(), tempExampleList.get(position).getName());
            }

            @Override
            public boolean onItemLongClick(int position) {
                return false;
            }
        });
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(mAdapter));//头布局
        recyclerView.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        recyclerView.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        recyclerView.addItemDecoration(new RecycleViewDivider(getActivity(), LinearLayoutManager.HORIZONTAL, R.drawable.recycler_view_divider));// 添加分割线。
        // 设置侧滑菜单创建器。
        recyclerView.setSwipeMenuCreator(swipeMenuCreator);
        // 设置菜单Item点击监听。
        recyclerView.setSwipeMenuItemClickListener(menuItemClickListener);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * 如有需要，可以去掉相应的拼音内容，以查看是否出现问题
     * 注：加空格是因为SQLite的order by 在没加空格时会出现非预期排序
     */
    private void initData() {
        exampleList = new ArrayList<>();
        tempExampleList = new ArrayList<>();
        InitComm.init().showView(getActivity(), null, false);
        //获取网络数据
        String userid = InitComm.userInfo.getmUserId();
        CiscoApiInterface.app.GetFriendsList(userid, new FriendsCallback() {

            @Override
            public void onSucess(List<Friend> friendsList) {
                InitComm.init().closeView();
                for (int i = 0; i < friendsList.size(); i++) {
                    String name = friendsList.get(i).getUserName();
                    MLog.e(name);
                    char words[] = name.toCharArray();
                    HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
                    format.setCaseType(HanyuPinyinCaseType.LOWERCASE);//小写 UPPERCASE 大写
                    format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
                    format.setVCharType(HanyuPinyinVCharType.WITH_V);
                    try {
                        tempExampleList.clear();
                        StringBuilder pinyin = new StringBuilder();
                        for (int j = 0; j < words.length; j++) {// words格式 ：[y, o, n, g, m, e, i] [邓, 蔷] [t, e, s, t, 9]
                            MLog.e(words[j]);
                            String[] zi = PinyinHelper.toHanyuPinyinStringArray(words[j], format);//所转化的非汉字即为null
                            if (zi != null) {
                                pinyin.append(zi[0]);
                            } else {
                                pinyin.append(words[j]);//如非汉字即为拼音，可直接使用
                            }
                        }
                        MLog.e(pinyin.toString());
                        ContactInfoBean info = new ContactInfoBean(friendsList.get(i).getUserName(), pinyin.toString() + " 齊 " + name, friendsList.get(i));//齊 ：一个不常用字作为分隔符
                        exampleList.add(info);
                        sortList();
                        tempExampleList.addAll(exampleList);
                        mAdapter.notifyDataSetChanged();
                    } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
                        MLog.e("非汉字转化异常");
                        badHanyuPinyinOutputFormatCombination.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailed() {
                InitComm.init().closeView();
                ToastUtils.show(getActivity(), "请求数据失败");
            }
        });
    }

    private void sortList() {
        Collections.sort(exampleList);
    }

    private void initListener() {
        // 键盘编辑事件监听
        mClearEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    filterData(mClearEditText.getText().toString().trim());
                    InitComm.init().hideInput(getActivity(), mClearEditText);
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
                    filterData(mClearEditText.getText().toString().trim());
                    InitComm.init().hideInput(getActivity(), mClearEditText);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
    }

    // 菜单创建器。在Item要创建菜单的时候调用。
    private SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
        @Override
        public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
            int width = getResources().getDimensionPixelSize(R.dimen.item_height);
            // MATCH_PARENT 自适应高度，保持和内容一样高；也可以指定菜单具体高度，也可以用WRAP_CONTENT。
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            // 添加右侧的，如果不添加，则右侧不会出现菜单。
            {
                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity())
                        .setBackgroundDrawable(R.mipmap.main_tab_backgroud)
                        .setImage(R.mipmap.trash)
                        .setWidth(width)
                        .setHeight(height);
                swipeRightMenu.addMenuItem(deleteItem);// 添加一个按钮到右侧侧菜单。
            }
        }
    };
    // 菜单点击监听。
    private OnSwipeMenuItemClickListener menuItemClickListener = new OnSwipeMenuItemClickListener() {
        /**
         * Item的菜单被点击的时候调用。
         * @param closeable       closeable. 用来关闭菜单。
         * @param adapterPosition adapterPosition. 这个菜单所在的item在Adapter中position。
         * @param menuPosition    menuPosition. 这个菜单的position。比如你为某个Item创建了2个MenuItem，那么这个position可能是是 0、1，
         * @param direction       如果是左侧菜单，值是：SwipeMenuRecyclerView#LEFT_DIRECTION，如果是右侧菜单，值是：SwipeMenuRecyclerView
         *                        #RIGHT_DIRECTION.
         */
        @Override
        public void onItemClick(Closeable closeable, final int adapterPosition, int menuPosition, int direction) {
            closeable.smoothCloseMenu();// 关闭被点击的菜单。

            //            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {//右侧菜单
            //                ToastUtils.show(getActivity(), "list第" + adapterPosition + "; 右侧菜单第" + menuPosition);
            //            } else if (direction == SwipeMenuRecyclerView.LEFT_DIRECTION) {// 左侧菜单
            //                ToastUtils.show(getActivity(), "list第" + adapterPosition + "; 左侧菜单第" + menuPosition);
            //            }
            // TODO 推荐调用Adapter.notifyItemRemoved(position)，也可以Adapter.notifyDataSetChanged();
            if (menuPosition == 0) {// 删除按钮被点击。
                InitComm.init().showView(getActivity(), "加载中", false);
                //获取网络数据
                String userid = InitComm.userInfo.getmUserId();
                CiscoApiInterface.app.DeleteFriends(tempExampleList.get(adapterPosition).getFriend().getmUserId(), userid, new FriendsCallback() {
                    @Override
                    public void onSucess(List<Friend> friendsList) {
                        InitComm.init().closeView();
                        tempExampleList.remove(adapterPosition);
                        exampleList.remove(adapterPosition);
                        mAdapter.notifyItemRemoved(adapterPosition);
                        ToastUtils.show(getActivity(), "删除成功");
                    }

                    @Override
                    public void onFailed() {
                        InitComm.init().closeView();
                        ToastUtils.show(getActivity(), "删除失败");
                    }
                });
            }
        }
    };
}
