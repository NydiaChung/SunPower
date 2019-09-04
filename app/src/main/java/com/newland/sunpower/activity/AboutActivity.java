package com.newland.sunpower.activity;

import android.os.Bundle;

import com.newland.sunpower.R;
import com.newland.sunpower.base.BaseActivity;

public class AboutActivity extends BaseActivity {
    private static String TAG = "AboutActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initView();
    }

    private void initView() {
        initHeadView();
        setHeadVisable(true);
        initLeftTitleView("关于我们");
        setLeftTitleView(true);
        setTitleViewVisable(false);
        setRithtTitleViewVisable(false);
    }
}
