package com.lyoko.smartlock.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.Models.Remote_device;
import com.lyoko.smartlock.R;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;


public class RemoteDevicesAdapter extends RecyclerView.Adapter<RemoteDevicesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Remote_device> list;
    private OnRemoteDeviceClickedListener callback;

    public RemoteDevicesAdapter(Context context, ArrayList<Remote_device> list) {
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
        final Remote_device device = list.get(position);
        holder.tv_device_name.setText(device.getOwnerPhoneNumber()+"\t"+device.getDevice_name());
        holder.item_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onRemoteDeviceItemClick(device.getOwnerPhoneNumber(), device.getAddress(), device.getDevice_name());
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
    public void setOnRemoteDeviceClickedListener(OnRemoteDeviceClickedListener callback){
        this.callback = callback;
    }

    public interface OnRemoteDeviceClickedListener {
        void onRemoteDeviceItemClick(String owner, String address, String name);
    }

}
