package com.xiaoqiang.online.activitys.main.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.cisco.core.util.Constants;
import com.xiaoqiang.online.R;
import com.xiaoqiang.online.base.BaseActivity;

/**
 * @author linpeng
 */
public class SettingUrlActivity extends BaseActivity{
    private TextView tv_head;
    private ImageView btn_back;
    private Button btn_confirm;
    private EditText  et_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 管理activity
//        ActivityUtil.getInstance().popOtherActivity(SettingUrlActivity.class);
//        ActivityUtil.getInstance().addActivity(this);
    }

    @Override
    public void findViews() {
        setContentView(R.layout.activity_settingurl);

        tv_head = (TextView) findViewById(R.id.tv_head);
        btn_back = (ImageView) findViewById(R.id.btn_back);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        et_address = (EditText) findViewById(R.id.et_address);
    }

    @Override
    public void init() {
        tv_head.setText("服务器参数配置");
        btn_back.setImageResource(R.mipmap.head_back);
        btn_back.setVisibility(View.VISIBLE);
        btn_back.setOnClickListener(this);
        btn_confirm.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.btn_confirm:
                //确认
                String  address=et_address.getText().toString();
                if(!TextUtils.isEmpty(address)){
                    if(address.contains("192.168")){
                        Constants.SERVER="http://"+address+":8080/";
                    }else{
                        Constants.SERVER="https://"+address+"/";//https://dev.fdclouds.com/
                    }
                    //Constants.SERVER = address;
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
