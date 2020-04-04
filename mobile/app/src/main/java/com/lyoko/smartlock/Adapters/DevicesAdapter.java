package com.lyoko.smartlock.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyoko.smartlock.Models.BLE_Device;
import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.Models.History;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Utils.LyokoString;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;


public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Device_info> list;
    private OnDeviceClickedListener callback;

    public DevicesAdapter(Context context, ArrayList<Device_info> list) {
        this.context = context;
        this.list = list;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View historyActivityView = inflater.inflate(R.layout.item_device, parent, false);
        ViewHolder viewHolder = new ViewHolder(historyActivityView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position % 2 == 0){
            holder.item_device.setBackgroundColor(COLOR_EVEN_POSITION);
        } else {
            holder.item_device.setBackgroundColor(COLOR_ODD_POSITION);
        }
        final Device_info info = list.get(position);
        holder.tv_device_name.setText(info.getDevice_name());
        holder.item_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onItemClick(info.getAddress(), info.getDevice_name());
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_device_name, tv_device_rssi;
        LinearLayout item_device;
        public ViewHolder(View view) {
            super(view);
            this.tv_device_rssi = view.findViewById(R.id.tv_device_rssi);
            this.tv_device_name = view.findViewById(R.id.tv_device_name);
            this.item_device = view.findViewById(R.id.item_device);
        }
    }
    public void setOnDeviceClickedListener(OnDeviceClickedListener callback){
        this.callback = callback;
    }

    public interface OnDeviceClickedListener {
        void onItemClick(String address, String name);
    }




}
