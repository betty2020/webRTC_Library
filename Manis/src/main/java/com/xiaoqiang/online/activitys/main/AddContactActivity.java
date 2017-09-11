package com.xiaoqiang.online.activitys.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.cisco.core.entity.Friend;
import com.cisco.core.httpcallback.FriendsCallback;
import com.cisco.core.interfaces.CiscoApiInterface;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.adapter.RecycleViewDivider;
import com.xiaoqiang.online.adapter.RecyclerAddContactAdapter;
import com.xiaoqiang.online.base.BaseActivity;
import com.xiaoqiang.online.commonUtils.InitComm;
import com.xiaoqiang.online.commonUtils.MLog;
import com.xiaoqiang.online.commonUtils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Ceate author: xiaoqiang on 2017/4/28 11:01
 * AddContactActivity (TODO)
 * 主要功能：通讯录->新建联系人页面
 * 邮箱：yugu88@163.com
 */
public class AddContactActivity extends BaseActivity {

    private FrameLayout frameLayout;
    private EditText mClearEditText;
    //private TextView serchView;
    private TextView tv_head;
    private ImageView btn_back;
    private RecyclerView serch_list;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Friend> list = new ArrayList<Friend>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void findViews() {
        setContentView(R.layout.activity_add_contact);
        tv_head = (TextView) this.findViewById(R.id.tv_head);
        frameLayout = (FrameLayout) findViewById(R.id.cloud_back3);
        serch_list = (RecyclerView) findViewById(R.id.serch_list);
        btn_back = (ImageView) this.findViewById(R.id.btn_back);
    }

    @Override
    public void init() {
        InitComm.init().startCloudMove(this, frameLayout);// 云动画
        tv_head.setText("新建联系人");
        btn_back.setOnClickListener(this);
        //serchView.setOnClickListener(this);
        initSearchEdtit();//搜索
        InitComm.init().showView(this, null, false);
    }

    /**
     * 搜索栏
     */
    private void initSearchEdtit() {
        mClearEditText = (EditText) findViewById(R.id.et_search);
        mClearEditText.setHint("请输入用户名");
        final TextView text_cancel = (TextView) findViewById(R.id.text_cancel);
        text_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClearEditText.setText(null);
                InitComm.init().hideInput(AddContactActivity.this, mClearEditText);
                serchContact(null);
            }
        });
        initListener();// 监听点击了搜索按钮事件
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
                    serchContact(null);
                }
            }
        });
    }

    private void initListener() {
        // 键盘编辑事件监听
        mClearEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    serchContact(mClearEditText.getText().toString().trim());
                    InitComm.init().hideInput(AddContactActivity.this, mClearEditText);
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
                    serchContact(mClearEditText.getText().toString().trim());
                    InitComm.init().hideInput(AddContactActivity.this, mClearEditText);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        serchContact(null);// 加载数据
        initRecyclerView();//初始化view
    }

    /**
     * 初始化列表
     */
    private void initRecyclerView() {
        if (list != null) {
            //            serch_list.setHasFixedSize(true);
            final RecyclerAddContactAdapter mAdapter = new RecyclerAddContactAdapter(this, list);
            mLayoutManager = new LinearLayoutManager(this);
            serch_list.setLayoutManager(mLayoutManager);
            serch_list.setItemAnimator(new DefaultItemAnimator());// 设置Item默认动画
            serch_list.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL, R.drawable.recycler_view_divider));// 添加分割线。
            serch_list.setAdapter(mAdapter);
            mAdapter.setOnItemClickLitener(new RecyclerAddContactAdapter.OnItemClickLitener() {

                @Override
                public void onItemClick(View view, final int position) {
                    InitComm.init().showView(AddContactActivity.this, null, false);
                    // 获取网络数据
                    String userid = InitComm.userInfo.getmUserId();
                    CiscoApiInterface.app.AddFriends(list.get(position).getmUserId(), userid, new FriendsCallback() {
                        @Override
                        public void onSucess(List<Friend> friendsList) {
                            ToastUtils.show(AddContactActivity.this, "添加成功");
                            InitComm.init().closeView();
                            list.remove(position);
                            mAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onFailed() {
                            ToastUtils.show(AddContactActivity.this, "添加失败");
                            InitComm.init().closeView();
                        }
                    });
                }

                @Override
                public void onItemLongClick(View view, int position) {

                }
            });
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && InitComm.isFirstClick()) {
            finishResult();
            //startActivity(new Intent(AddContactActivity.this,MainActivity.class));
        }
        return false;
    }

    public void finishResult() {
        Intent mIntent = new Intent();
        this.setResult(Activity.RESULT_OK, mIntent);
        this.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                if (InitComm.isFirstClick()) {
                    finishResult();
                    //overridePendingTransition(R.animator.zoom_enter, R.animator.zoom_exit);//缩小到左上角效果
                    finish();
                }
                break;
            case R.id.et_search:
                InitComm.init().showView(this, null, false);
                serchContact(null);// 加载数据
                break;
        }
    }

    /**
     * @param s 搜索条件
     */
    public void serchContact(String s) {

        //获取网络数据
        String userid = InitComm.userInfo.getmUserId();
        CiscoApiInterface.app.SelectFriendsList(userid, new FriendsCallback() {
            @Override
            public void onSucess(List<Friend> friendsList) {
                InitComm.init().closeView();
                list.clear();
                for (int i = 0; i < friendsList.size(); i++) {
                    MLog.e(i + "=fUserId=" + friendsList.get(i).getfUserId());
                    if (TextUtils.isEmpty(friendsList.get(i).getfUserId())) {
                        list.add(friendsList.get(i));
                    }
                }
                initRecyclerView();
            }

            @Override
            public void onFailed() {
                InitComm.init().closeView();
                ToastUtils.show(AddContactActivity.this, "未找到可添加的联系人");
            }
        }, s);
    }

    @Override
    protected void onDestroy() {
        InitComm.init().stopCloud();// 云动画 关闭
        super.onDestroy();
    }
}
