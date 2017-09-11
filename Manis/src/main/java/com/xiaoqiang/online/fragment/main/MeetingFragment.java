package com.xiaoqiang.online.fragment.main;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
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

import com.cisco.core.entity.ConferenceRecord;
import com.cisco.core.httpcallback.ConferenceRecordCallback;
import com.cisco.core.httpcallback.MyCallback;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.adapter.RecycleViewDivider;
import com.xiaoqiang.online.adapter.RecyclerMeetAdapter;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;
import com.yanzhenjie.recyclerview.swipe.Closeable;
import com.yanzhenjie.recyclerview.swipe.OnSwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import java.util.ArrayList;
import java.util.List;

import static com.xiaoqiang.online.R.id.et_search;
import static com.xiaoqiang.online.commonUtils.InitComm.page;
import static com.xiaoqiang.online.commonUtils.InitComm.sum;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:06
 * MeetingFragment (TODO)
 * 主要功能：会议记录页面的Fragment
 * 邮箱：yugu88@163.com
 */
public class MeetingFragment extends Fragment implements View.OnClickListener {

    SwipeRefreshLayout mSwipeRefreshLayout;
    SwipeMenuRecyclerView mSwipeMenuRecyclerView;
    View view;
    RecyclerMeetAdapter mRecyclerMeetAdapter;
    private List<ConferenceRecord> mData = new ArrayList<ConferenceRecord>();
    private List<ConferenceRecord> tempExampleList = new ArrayList<>();
    //    private int sum = 0;//总数
    //    private int page = 2;//页数
    //搜索框
    private EditText mClearEditText;
    private TextView text_cancel;
    private String userid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_meeting, container, false);
        iniView();
        initSearchEdtit();// 初始化搜索栏
        return view;
    }

    @SuppressWarnings("ResourceAsColor")
    private void iniView() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(mOnRefreshListener);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.purple_normal,
                R.color.btn_pressed_green_solid, R.color.head_top);//设置 进度条的颜色变化，最多可以设置4种颜色
        //设置转圈所在圆形突起的背景色为默认的浅灰色
        mSwipeRefreshLayout.setProgressBackgroundColor(R.color.tab_bottom_font1);

        mSwipeMenuRecyclerView = (SwipeMenuRecyclerView) view.findViewById(R.id.recycler_view);
        mSwipeMenuRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));// 布局管理器。
        mSwipeMenuRecyclerView.setHasFixedSize(true);// 如果Item够简单，高度是确定的，打开FixSize将提高性能。
        mSwipeMenuRecyclerView.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画，加也行，不加也行。
        mSwipeMenuRecyclerView.addItemDecoration(new RecycleViewDivider(getActivity(), LinearLayoutManager.HORIZONTAL, R.drawable.recycler_view_divider));// 添加分割线。
        mSwipeMenuRecyclerView.addOnScrollListener(mOnScrollListener);// 添加滚动监听。
        // 为RecyclerView的Item创建菜单就两句话，不错就是这么简单：
        // 设置菜单创建器。
        mSwipeMenuRecyclerView.setSwipeMenuCreator(swipeMenuCreator);
        // 设置菜单Item点击监听。
        mSwipeMenuRecyclerView.setSwipeMenuItemClickListener(menuItemClickListener);
        //获取网络数据
        InitComm.init().showView(getActivity(), null, false);
        mSwipeRefreshLayout.setRefreshing(true);
        userid = InitComm.userInfo.getmUserId();
        CiscoApiInterface.app.GetConferencesList( 0, 1, userid, new ConferenceRecordCallback() {

            @Override
            public void onSucess(List<ConferenceRecord> conferenceRecordList, int count) {
                sum = count;
                mSwipeRefreshLayout.setRefreshing(false);
                mData.clear();
                tempExampleList.clear();
                mData.addAll(conferenceRecordList);
                tempExampleList.addAll(conferenceRecordList);
                InitComm.init().closeView();
                mRecyclerMeetAdapter = new RecyclerMeetAdapter(getActivity(), mData);
                mSwipeMenuRecyclerView.setAdapter(mRecyclerMeetAdapter);
            }

            @Override
            public void onFailed(String msg) {
                InitComm.init().closeView();
                ToastUtils.show(getActivity(), msg);
            }
        });
    }

    // 下拉刷新
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            mSwipeMenuRecyclerView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }, 2000);
            mClearEditText.setText(null);
            mSwipeRefreshLayout.setRefreshing(true);
            InitComm.init().hideInput(getActivity(), mClearEditText);
            InitComm.init().showView(getActivity(), null, false);
            text_cancel.setVisibility(View.GONE);
            //获取网络数据
            CiscoApiInterface.app.GetConferencesList(0, 1, userid, new ConferenceRecordCallback() {

                @Override
                public void onSucess(List<ConferenceRecord> conferenceRecordList, int count) {
                    InitComm.init().closeView();
                    mSwipeRefreshLayout.setRefreshing(false);
                    mData.clear();
                    tempExampleList.clear();
                    mData.addAll(conferenceRecordList);
                    tempExampleList.addAll(conferenceRecordList);
                    sum = count;
                    page = 2;
                    mRecyclerMeetAdapter.notifyDataSetChanged();
                }

                @Override
                public void onFailed(String msg) {
                    InitComm.init().closeView();
                    ToastUtils.show(getActivity(), msg);
                }
            });
        }
    };

    @Override
    public void onStop() {
        sum = 0;
        page = 2;
        super.onStop();
    }

    // 加载更多
    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //上滑
            if (!recyclerView.canScrollVertically(1) && dy > 20) {// 手指不能向上滑动了
                MLog.e("调用了加载。。。" + dy + " page=" + page + "sum=" + sum);
                mSwipeRefreshLayout.setRefreshing(true);
                mClearEditText.setText(null);
                InitComm.init().hideInput(getActivity(), mClearEditText);
                InitComm.init().showView(getActivity(), null, false);
                text_cancel.setVisibility(View.GONE);
                // 这里有个注意的地方，如果你刚进来时没有数据，但是设置了适配器，
                // 这个时候就会触发加载更多，需要开发者判断下是否有数据，如果有数据才去加载更多。
                CiscoApiInterface.app.GetConferencesList(sum, page, userid, new ConferenceRecordCallback() {

                    @Override
                    public void onSucess(List<ConferenceRecord> conferenceRecordList, int count) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        MLog.e(conferenceRecordList.size() + "count:" + count);
                        InitComm.init().closeView();
                        if (conferenceRecordList == null) {
                            ToastUtils.show(getActivity(), "已滑动到底部");
                            return;
                        }
                        ToastUtils.show(getActivity(), "已加载第" + page + "页");
                        sum = count;
                        page++;
                        mData.addAll(conferenceRecordList);
                        tempExampleList.addAll(conferenceRecordList);
                        //notifyItemInserted(position)与notifyItemRemoved(position)
                        mRecyclerMeetAdapter.notifyDataSetChanged();// 已淘汰刷新方式
                        mRecyclerMeetAdapter.notifyItemInserted(mData.size() - conferenceRecordList.size());
                    }

                    @Override
                    public void onFailed(String msg) {
                        InitComm.init().closeView();
                        ToastUtils.show(getActivity(), msg);
                    }
                });
            }
        }
    };
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
//            if (direction == SwipeMenuRecyclerView.RIGHT_DIRECTION) {
//                ToastUtils.show(getActivity(), "list第" + adapterPosition + "; 右侧菜单第" + menuPosition);
//            } else if (direction == SwipeMenuRecyclerView.LEFT_DIRECTION) {
//                ToastUtils.show(getActivity(), "list第" + adapterPosition + "; 左侧菜单第" + menuPosition);
//            }
            // TODO 推荐调用Adapter.notifyItemRemoved(position)，也可以Adapter.notifyDataSetChanged();
            if (menuPosition == 0) {// 删除按钮被点击。
                InitComm.init().showView(getActivity(),null,false);
                if (adapterPosition<mData.size()){
                    String conferenceId = mData.get(adapterPosition).getmConferenceId();
                    CiscoApiInterface.app.DeleteMeeting(conferenceId, new MyCallback() {
                        @Override
                        public void onSucess(String message) {
                            InitComm.init().closeView();
                            ToastUtils.show(getActivity(), message);
                            if ("删除成功".equals(message)) {
                                mData.remove(adapterPosition);
                                tempExampleList.remove(adapterPosition);
                                mRecyclerMeetAdapter.notifyItemRemoved(adapterPosition);
                                mRecyclerMeetAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onFailed(String message) {
                            InitComm.init().closeView();
                            ToastUtils.show(getActivity(), message.toString());
                        }
                    });
                }
            }
        }
    };

    @Override
    public void onClick(View v) {
    }

    /**
     * 搜索栏
     */
    private void initSearchEdtit() {
        mClearEditText = (EditText) view.findViewById(et_search);
        text_cancel = (TextView) view.findViewById(R.id.text_cancel);
        text_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClearEditText.setText(null);
                InitComm.init().hideInput(getActivity(), mClearEditText);
                filterData(null);
            }
        });
        mClearEditText.setHint("搜索会议");
        initListener();// 监听点击了搜索按钮事件
        // 根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //s:变化前的所有字符； start:字符开始的位置； count:变化前的总字节数；after:变化后的字节数
                MLog.d("变化前:" + s + ";" + start + ";" + count + ";" + after);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //S：变化后的所有字符；start：字符起始的位置；before: 变化之前的总字节数；count:变化后的字节数
                MLog.d("变化后:" + s + ";" + start + ";" + before + ";" + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                //s:变化后的所有字符
                MLog.d("变化:" + s);
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
        mData.clear();
        if (TextUtils.isEmpty(s)) {
            mData.addAll(tempExampleList);
        } else {
            ConferenceRecord tempContactInfoBean;
            for (int i = 0; i < tempExampleList.size(); i++) {
                tempContactInfoBean = tempExampleList.get(i);
                String number = tempContactInfoBean.getNumber();
                String title = tempContactInfoBean.getTitle();
                int a = 0;
                if (!TextUtils.isEmpty(title)) {
                    a = title.indexOf(s);
                } else if (!TextUtils.isEmpty(number)) {
                    a = number.indexOf(s);
                }
                if (a != -1) {
                    mData.add(tempContactInfoBean);
                }
            }
        }
        if (mData.size() < 1 || mData.isEmpty()) {
            ToastUtils.show(getActivity(), "没有查到相关会议");
        }
        mRecyclerMeetAdapter.notifyDataSetChanged();
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
}
