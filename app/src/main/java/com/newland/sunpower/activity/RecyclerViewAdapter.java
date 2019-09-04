package com.newland.sunpower.activity;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.newland.sunpower.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHodler> {
    Context mContext;
    List<Map<String, Object>> mList = new ArrayList<>();

    public RecyclerViewAdapter(Context context, List<Map<String, Object>> list) {
        this.mContext = context;
        this.mList = list;
    }

    class MyViewHodler extends RecyclerView.ViewHolder {
        TextView deviceIdTextView;
        TextView sensorTextView;
        TextView timeTextView;
        TextView statusTextView;
        public MyViewHodler(View view) {
            super(view);
            deviceIdTextView = view.findViewById(R.id.deviceId_text);
            sensorTextView = view.findViewById(R.id.sensor_text);
            timeTextView = view.findViewById(R.id.time_text);
            statusTextView = view.findViewById(R.id.status_text);
        }
    }

    @Override
    public MyViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycleview, parent, false);
        MyViewHodler hodler = new MyViewHodler(view);
        return hodler;
    }

    @Override
    public void onBindViewHolder(MyViewHodler holder, int position) {
        holder.deviceIdTextView.setText(mList.get(position).get("DeviceId").toString());
        holder.sensorTextView.setText(mList.get(position).get("Sensor").toString());
        holder.timeTextView.setText(mList.get(position).get("RecordTime").toString());
        holder.statusTextView.setText(mList.get(position).get("Value").toString());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}
