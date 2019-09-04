package com.newland.sunpower;

import android.app.Application;
import android.content.Context;

import com.newland.sunpower.utils.CrashHandlerUtil;

import org.litepal.LitePal;
import org.litepal.LitePalApplication;

/**
 * Created by jy on 2018/5/9.
 */

public class SunPower extends LitePalApplication {
    private static final String TAG = "SunPower";

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        CrashHandlerUtil.getmInstance().init(mContext);
    }

    public static Context getInstance() {
        return mContext;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
