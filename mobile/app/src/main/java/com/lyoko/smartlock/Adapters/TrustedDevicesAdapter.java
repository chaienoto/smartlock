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

import com.lyoko.smartlock.Models.History;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Utils.LyokoString;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;


public class TrustedDevicesAdapter extends RecyclerView.Adapter<TrustedDevicesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<History> list;
    private OnUnknownBLEClickedListener ble1ClickCallback;
    private OnUnknownBLELongClickedListener bleLongClickCallback;

    public TrustedDevicesAdapter(Context context, ArrayList<History> list) {
        this.context = context;
        this.list = list;

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View historyActivityView = inflater.inflate(R.layout.item_history, parent, false);
        ViewHolder viewHolder = new ViewHolder(historyActivityView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        History history = list.get(position);
        holder.tvCover_Name.setText(history.getUnlock_name());
        holder.tvDate.setText(history.getUnlock_time());

        if (position % 2 == 0){
            holder.item_history.setBackgroundColor(COLOR_EVEN_POSITION);
        } else {
            holder.item_history.setBackgroundColor(COLOR_ODD_POSITION);
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvCover_Name, tvDate;
        ImageView iv_unlockType;
        LinearLayout item_history;
        public ViewHolder(View view) {
            super(view);
            this.tvDate = view.findViewById(R.id.tv_date);
            this.tvCover_Name = view.findViewById(R.id.tvCover_Name);
            this.iv_unlockType = view.findViewById(R.id.iv_unlockType);
            this.item_history = view.findViewById(R.id.item_history);
        }
    }

    public interface OnUnknownBLEClickedListener {
        void onItemClick(String ssid);
    }
    public interface OnUnknownBLELongClickedListener {
        void onItemClick(String ssid);
    }



}
