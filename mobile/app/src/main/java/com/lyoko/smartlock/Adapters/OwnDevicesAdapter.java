package com.lyoko.smartlock.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Helper;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.CLOSE_LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.HOLD_LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.OPEN_LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;
import static com.lyoko.smartlock.Utils.LyokoString.phone_name;


public class OwnDevicesAdapter extends RecyclerView.Adapter<OwnDevicesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Device_info> list;
    private OnOwnDeviceClickedListener callback;

    public OwnDevicesAdapter(Context context, ArrayList<Device_info> list) {
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Device_info info = list.get(position);
        holder.img_device_state.setBackgroundResource(getBackground(info.getState()));
        holder.tv_device_name.setText(info.getDevice_name());
        holder.img_device_state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int _state, _bg;
                Database_Helper db_helper = new Database_Helper();
                switch (info.getState()){
                    case 0: _state = OPEN_LOCK; _bg = R.drawable.ic_power_on; break;
                    case 1: _state = HOLD_LOCK; _bg = R.drawable.ic_power_hold; break;
                    default: _state = CLOSE_LOCK; _bg = R.drawable.ic_power_off;
                }
                db_helper.changed_update_code(phone_login,info.getAddress(),_state);
                db_helper.saveHistory(phone_login,info.getAddress(),phone_name);
                holder.img_device_state.setBackgroundResource(_bg);
            }
        });
        holder.img_device_state.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if ( info.getState() == 1) {
                    new Database_Helper().changed_update_code(phone_login,info.getAddress(),HOLD_LOCK);
                    holder.img_device_state.setBackgroundResource(R.drawable.ic_power_hold);
                }
                return true;
            }
        });
        holder.item_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onOwnDeviceItemClick(info.getAddress(), info.getDevice_name());
            }
        });
    }
    private int getBackground(int state){
        switch (state){
            case 1: return R.drawable.ic_power_on;
            case 2: return R.drawable.ic_power_hold;
            default: return R.drawable.ic_power_off;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tv_device_name;
        ImageView img_device_type,img_device_state;
        RelativeLayout item_device;
        public ViewHolder(View view) {
            super(view);
            this.img_device_type = view.findViewById(R.id.img_device_type);
            this.img_device_state = view.findViewById(R.id.img_device_state);
            this.tv_device_name = view.findViewById(R.id.tv_device_name);
            this.item_device = view.findViewById(R.id.item_device);
        }
    }
    public void setOnOwnDeviceClickedListener(OnOwnDeviceClickedListener callback){
        this.callback = callback;
    }

    public interface OnOwnDeviceClickedListener {
        void onOwnDeviceItemClick(String address, String name);
    }




}
