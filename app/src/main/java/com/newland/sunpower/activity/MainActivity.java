package com.newland.sunpower.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.newland.sunpower.Constant;
import com.newland.sunpower.R;
import com.newland.sunpower.SunPower;
import com.newland.sunpower.base.BaseActivity;
import com.newland.sunpower.bean.DeviceInfo;
import com.newland.sunpower.utils.DataCache;
import com.newland.sunpower.utils.LogUtil;
import com.newland.sunpower.utils.SPHelper;
import com.newland.sunpower.utils.Utils;
import com.newland.sunpower.view.CircleSeekBarX;
import com.newland.sunpower.view.CircleSeekBarY;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cn.com.newland.nle_sdk.responseEntity.base.BaseResponseEntity;
import cn.com.newland.nle_sdk.util.NetWorkBusiness;
import cn.com.newland.nle_sdk.util.Tools;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity {

    private static String TAG = "MainActivity";

    private Context mContext;
    private TextView mAngleXText, mAngleYText, mPostResultTv;
    private CircleSeekBarX mCircleSeekBarX;
    private CircleSeekBarY mCircleSeekBarY;
    private TextView mLightValueText;
    private TextView mLampStateText;
    private ImageView mLampStateImageView, mLampControlImageView;
    private Button mQueryDatasBtn;

    private static final int GET_BOX_STATUS = 101;
    private static final int GET_BOX_STATUS_DELAY = 10000;
    private String mDeviceId;
    private SPHelper spHelper;
    private NetWorkBusiness mNetWorkBusiness;
    private double mAngleXMin = 0;
    private double mAngleXMax = 0;
    private double mAngleYMin = 0;
    private double mAngleYMax = 0;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GET_BOX_STATUS:
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spHelper = SPHelper.getInstant(getApplicationContext());
        mDeviceId = spHelper.getStringFromSP(getApplicationContext(), Constant.DEVICE_ID);
        mContext = this;
        mNetWorkBusiness = new NetWorkBusiness(DataCache.getAccessToken(getApplicationContext()), DataCache.getBaseUrl(getApplicationContext()));
        initView();
        initEvent();
        getAngleValueRegion();
        querySensorStatus();
    }

    private void initView() {
        initHeadView();
        setHeadVisable(true);
        initLeftTitleView("返回");
        initTitleView(this.getString(R.string.app_title));
        setRithtTitleViewVisable(false);

        mPostResultTv = (TextView) findViewById(R.id.postResult);
        mAngleXText = (TextView) findViewById(R.id.angleX_value);
        mAngleYText = (TextView) findViewById(R.id.angleY_value);
        mCircleSeekBarX = (CircleSeekBarX) findViewById(R.id.angleX_seekbar);
        mCircleSeekBarY = (CircleSeekBarY) findViewById(R.id.angleY_seekbar);
        mLightValueText = (TextView) findViewById(R.id.light_value_text);
        mLampStateText = (TextView) findViewById(R.id.lamp_state_text);
        mLampStateImageView = (ImageView) findViewById(R.id.lamp_state_imageView);
        mLampControlImageView = (ImageView) findViewById(R.id.lamp_control_imageView);

        mLampControlImageView.setOnClickListener(new ControlListener());

        mQueryDatasBtn = (Button) findViewById(R.id.query_datas_btn);
        mQueryDatasBtn.setOnClickListener(new QueryListener());

        mLampStateImageView.setBackgroundResource(R.mipmap.icon_lamp_off);
        mLampControlImageView.setBackgroundResource(R.drawable.btn_off);
        mLampControlImageView.setTag(false);
    }

    private void initEvent() {
        mCircleSeekBarX.setOnSeekBarChangeListener(new CircleSeekBarX.OnSeekBarChangeListener() {
            @Override
            public void onChanged(CircleSeekBarX seekbar, int curValue) {
                mAngleXText.setText(curValue + "°");
            }

            @Override
            public void onChangedFinish(CircleSeekBarX seekbar, final int curValue) {
                mAngleXText.setText(curValue + "°");
                LogUtil.d(TAG, "Control AngleX, curValue:" + curValue);
                final Gson gson = new Gson();
                mNetWorkBusiness.control(mDeviceId, DeviceInfo.apiTagAngleXCtrl, String.valueOf(curValue), new retrofit2.Callback<BaseResponseEntity>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull retrofit2.Response<BaseResponseEntity> response) {
                        BaseResponseEntity baseResponseEntity = response.body();
                        LogUtil.d(TAG, "open box, gson.toJson(baseResponseEntity):" + gson.toJson(baseResponseEntity));

                        if (baseResponseEntity != null) {
                            Tools.printJson(mPostResultTv, gson.toJson(baseResponseEntity));
                            try {
                                JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                                int status = (int) jsonObject.get("Status");
                                LogUtil.d(TAG, "Control AngleX, Status:" + status);
                                if (0 == status) {
                                    displayAngleXText(curValue);
                                } else {
                                    LogUtil.d(TAG, "return status value is error, open box fail");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            LogUtil.d(TAG, "请求出错 : 请求参数不合法或者服务出错");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull Throwable t) {
                        LogUtil.d(TAG, "请求出错 : \n" + t.getMessage());
                    }
                });
            }
        });

        mCircleSeekBarX.setCurProcess(0);

        mCircleSeekBarY.setOnSeekBarChangeListener(new CircleSeekBarY.OnSeekBarChangeListener() {
            @Override
            public void onChanged(CircleSeekBarY seekbar, int curValue) {
                mAngleYText.setText(curValue + "°");
            }

            @Override
            public void onChangedFinish(CircleSeekBarY seekbar, final int curValue) {
                mAngleYText.setText(curValue + "°");
                LogUtil.d(TAG, "Control AngleY, curValue:" + curValue);
                final Gson gson = new Gson();
                mNetWorkBusiness.control(mDeviceId, DeviceInfo.apiTagAngleYCtrl, String.valueOf(curValue), new retrofit2.Callback<BaseResponseEntity>() {
                    @Override
                    public void onResponse(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull retrofit2.Response<BaseResponseEntity> response) {
                        BaseResponseEntity baseResponseEntity = response.body();
                        LogUtil.d(TAG, "open box, gson.toJson(baseResponseEntity):" + gson.toJson(baseResponseEntity));

                        if (baseResponseEntity != null) {
                            Tools.printJson(mPostResultTv, gson.toJson(baseResponseEntity));
                            try {
                                JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                                int status = (int) jsonObject.get("Status");
                                LogUtil.d(TAG, "Control AngleY, Status:" + status);
                                if (0 == status) {
                                    displayAngleYText(curValue);
                                } else {
                                    LogUtil.d(TAG, "return status value is error, open box fail");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            LogUtil.d(TAG, "请求出错 : 请求参数不合法或者服务出错");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull Throwable t) {
                        LogUtil.d(TAG, "请求出错 : \n" + t.getMessage());
                    }
                });
            }
        });

        mCircleSeekBarY.setCurProcess(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    class ControlListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int controlValue = !((boolean) mLampControlImageView.getTag()) ? 1 : 0;
            LogUtil.d(TAG, "controlValue: " + controlValue);
            final Gson gson = new Gson();
            //调用命令控制接口
            /* *
             * param String deviceId：设备ID
             * param String apiTag：API标签
             * param Object data：命令值
             * param Callback<BaseResponseEntity> callback回调对象
             * */
            mNetWorkBusiness.control(mDeviceId, DeviceInfo.apiTagLampCtrl, controlValue, new retrofit2.Callback<BaseResponseEntity>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull retrofit2.Response<BaseResponseEntity> response) {
                    BaseResponseEntity baseResponseEntity = response.body();
                    LogUtil.d(TAG, "open box, gson.toJson(baseResponseEntity):" + gson.toJson(baseResponseEntity));

                    if (baseResponseEntity != null) {
                        Tools.printJson(mPostResultTv, gson.toJson(baseResponseEntity));
                        try {
                            JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                            int status = (int) jsonObject.get("Status");
                            LogUtil.d(TAG, "Status:" + status);
                            if (0 == status) {
                                displayLampStatus(controlValue);
                            } else {
                                LogUtil.d(TAG, "return status value is error, open box fail");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        LogUtil.d(TAG, "请求出错 : 请求参数不合法或者服务出错");
                    }
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull Throwable t) {
                    LogUtil.d(TAG, "请求出错 : \n" + t.getMessage());
                }
            });
        }
    }

    private void displayBrightness(int value) {
        if (null != mLightValueText) {
            mLightValueText.setText(value + "lx");
        }
    }

    private void displayLampStatus(int control) {
        if (control == Constant.BOX_STATUS_CLOSE) {
            displayLampStatusClose();
        } else if (control == Constant.BOX_STATUS_OPEN) {
            displayLampStatusOpen();
        }
    }

    private void displayLampStatusOpen() {
        mLampControlImageView.setBackgroundResource(R.drawable.btn_on);
        mLampStateImageView.setBackgroundResource(R.mipmap.icon_lamp_on);
        mLampControlImageView.setTag(true);
        mLampStateText.setText(R.string.lamp_open_state);
    }

    private void displayLampStatusClose() {
        mLampControlImageView.setBackgroundResource(R.drawable.btn_off);
        mLampStateImageView.setBackgroundResource(R.mipmap.icon_lamp_off);
        mLampControlImageView.setTag(false);
        mLampStateText.setText(R.string.lamp_close_state);
    }

    private void getAngleValueRegion() {
        mAngleXMin = Double.valueOf(spHelper.getStringFromSPDef(SunPower.getInstance(), Constant.ANGLEX_MIN, Constant.ANGLEX_MIN_DEFAULT_VALUE));
        mAngleXMax = Double.valueOf(spHelper.getStringFromSPDef(SunPower.getInstance(), Constant.ANGLEX_MAX, Constant.ANGLEX_MAX_DEFAULT_VALUE));
        mAngleYMin = Double.valueOf(spHelper.getStringFromSPDef(SunPower.getInstance(), Constant.ANGLEY_MIN, Constant.ANGLEY_MIN_DEFAULT_VALUE));
        mAngleYMax = Double.valueOf(spHelper.getStringFromSPDef(SunPower.getInstance(), Constant.ANGLEY_MAX, Constant.ANGLEY_MAX_DEFAULT_VALUE));
    }

    private void displayAngleXText(double value) {
        //控制舵机旋转的最小角度和最大角度
        if (value >= mAngleXMax) {
            value = mAngleXMax;
        } else if (value <= mAngleXMin) {
            value = mAngleXMin;
        }
        int angle = Integer.parseInt(new java.text.DecimalFormat("0").format(value));
        mCircleSeekBarX.setCurProcess(angle);
        mAngleXText.setText(angle + "°");
    }

    private void displayAngleYText(double value) {
        //控制舵机旋转的最小角度和最大角度
        if (value >= mAngleYMax) {
            value = mAngleYMax;
        } else if (value <= mAngleYMin) {
            value = mAngleYMin;
        }
        int angle = Integer.parseInt(new java.text.DecimalFormat("0").format(value));
        mCircleSeekBarY.setCurProcess(angle);
        mAngleYText.setText(angle + "°");
    }

    private void querySensorStatus() {
        LogUtil.d(TAG, "querySensorStatus");
        final Gson gson = new Gson();
        //查询单个传感器的最新状态接口
        /* *
         * param String deviceId：设备ID
         * param String apiTag：API标签
         * param Callback<BaseResponseEntity> callback回调对象
         * */
        mNetWorkBusiness.getSensor(mDeviceId, DeviceInfo.apiTagLight, new retrofit2.Callback<BaseResponseEntity>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull retrofit2.Response<BaseResponseEntity> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                try {
                    JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                    JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                    Object object = resultObj.get("Value");
                    if (null == object || object.equals("")) {
                        LogUtil.d(TAG, "Value is null, return");
                        return;
                    }
                    double value = (double) resultObj.get("Value");
                    LogUtil.d(TAG, "get Brightness value:" + value);
                    displayBrightness(Integer.parseInt(new java.text.DecimalFormat("0").format(value)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (baseResponseEntity != null) {
                    Tools.printJson(mPostResultTv, gson.toJson(baseResponseEntity));
                } else {
                    mPostResultTv.setText("请求出错 : 请求参数不合法或者服务出错");
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull Throwable t) {
                mPostResultTv.setText("请求出错 : \n" + t.getMessage());
            }
        });

        mNetWorkBusiness.getSensor(mDeviceId, DeviceInfo.apiTagAngleX, new retrofit2.Callback<BaseResponseEntity>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull retrofit2.Response<BaseResponseEntity> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                try {
                    JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                    JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                    Object object = resultObj.get("Value");
                    if (null == object || object.equals("")) {
                        LogUtil.d(TAG, "Value is null, return");
                        return;
                    }
                    double value = (double) resultObj.get("Value");
                    LogUtil.d(TAG, "get AngleX value:" + value);
                    displayAngleXText(value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (baseResponseEntity != null) {
                    Tools.printJson(mPostResultTv, gson.toJson(baseResponseEntity));
                } else {
                    mPostResultTv.setText("请求出错 : 请求参数不合法或者服务出错");
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull Throwable t) {
                mPostResultTv.setText("请求出错 : \n" + t.getMessage());
            }
        });

        mNetWorkBusiness.getSensor(mDeviceId, DeviceInfo.apiTagAngleY, new retrofit2.Callback<BaseResponseEntity>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull retrofit2.Response<BaseResponseEntity> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                try {
                    JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                    JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                    Object object = resultObj.get("Value");
                    if (null == object || object.equals("")) {
                        LogUtil.d(TAG, "Value is null, return");
                        return;
                    }
                    double value = (double) resultObj.get("Value");
                    LogUtil.d(TAG, "get AngleY value:" + value);
                    displayAngleYText(value);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (baseResponseEntity != null) {
                    Tools.printJson(mPostResultTv, gson.toJson(baseResponseEntity));
                } else {
                    mPostResultTv.setText("请求出错 : 请求参数不合法或者服务出错");
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull Throwable t) {
                mPostResultTv.setText("请求出错 : \n" + t.getMessage());
            }
        });

        mNetWorkBusiness.getSensor(mDeviceId, DeviceInfo.apiTagPowerState, new retrofit2.Callback<BaseResponseEntity>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull retrofit2.Response<BaseResponseEntity> response) {
                BaseResponseEntity baseResponseEntity = response.body();
                LogUtil.d(TAG, "get lamp state, response message: " + gson.toJson(baseResponseEntity));
                try {
                    JSONObject jsonObject = new JSONObject(gson.toJson(baseResponseEntity));
                    JSONObject resultObj = (JSONObject) jsonObject.get("ResultObj");
                    Object object = resultObj.get("Value");
                    if (null == object || object.equals("")) {
                        LogUtil.d(TAG, "Value is null, return");
                        return;
                    }
                    double value = (double) resultObj.get("Value");
                    LogUtil.d(TAG, "get lamp state value:" + value);
                    displayLampStatus(Integer.valueOf((int) value));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (baseResponseEntity != null) {
                    Tools.printJson(mPostResultTv, gson.toJson(baseResponseEntity));
                } else {
                    mPostResultTv.setText("请求出错 : 请求参数不合法或者服务出错");
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<BaseResponseEntity> call, @NonNull Throwable t) {
                mPostResultTv.setText("请求出错 : \n" + t.getMessage());
            }
        });

        mHandler.sendEmptyMessageDelayed(GET_BOX_STATUS, GET_BOX_STATUS_DELAY);
    }

    class QueryListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            LogUtil.d(TAG, "click query_datas_btn");
            if (Utils.isFastDoubleClick()) {
                return;
            }
            final Gson gson = new Gson();
            //查询传感器某个时间段的所有数据信息接口
            /* *
             * param String deviceId：设备ID
             * param String apiTag：API标签
             * param String Method：查询方式（1：XX分钟内 2：XX小时内 3：XX天内 4：XX周内 5：XX月内 6：按startDate与endDate指定日期查询）
             * param String TimeAgo：与Method配对使用表示"多少TimeAgo Method内"的数据，例：(Method=2,TimeAgo=30)表示30小时内的历史数据
             * param String StartDate：起始时间（可选，格式YYYY-MM-DD HH:mm:ss）
             * param String EndDate：结束时间（可选，格式YYYY-MM-DD HH:mm:ss）
             * param String Sort：时间排序方式，DESC:倒序，ASC升序
             * param String PageSize：指定每次要请求的数据条数，默认1000，最多3000
             * param String PageIndex：API标签
             * param Object data：指定页码
             * param Callback<BaseResponseEntity> callback回调对象
             * */
            mNetWorkBusiness.getSensorData(mDeviceId, DeviceInfo.apiTagPowerState, "6", "3", getStartTime(), getEndTime(), "DESC", "100", "1", new Callback<BaseResponseEntity>() {
                @Override
                public void onResponse(@NonNull Call<BaseResponseEntity> call, @NonNull Response<BaseResponseEntity> response) {
                    BaseResponseEntity baseResponseEntity = response.body();
                    LogUtil.d(TAG, "queryDatas, gson.toJson(baseResponseEntity):" + gson.toJson(baseResponseEntity));
                    if (baseResponseEntity != null) {
                        Tools.printJson(mPostResultTv, gson.toJson(baseResponseEntity));
                        Intent intent = new Intent(mContext, QueryDatasActivity.class);
                        intent.putExtra("jsonData", gson.toJson(baseResponseEntity));
                        startActivity(intent);
                    } else {
                        mPostResultTv.setText("请求出错 : 请求参数不合法或者服务出错");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BaseResponseEntity> call, @NonNull Throwable t) {
                    mPostResultTv.setText("请求出错 : \n" + t.getMessage());
                }
            });
        }
    }

    private String getStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 3);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startTime = df.format(calendar.getTime());
        LogUtil.d(TAG, "getStartTime: " + startTime);
        return startTime;
    }

    private String getEndTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String endTime = df.format(new Date());
        LogUtil.d(TAG, "getEndTime: " + endTime);
        return endTime;
    }
}
