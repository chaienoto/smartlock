package com.lyoko.smartlock.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lyoko.smartlock.Models.History;
import com.lyoko.smartlock.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;


public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private ArrayList<History> list;

    public HistoryAdapter(Context context, ArrayList<History> historyActivityArr) {
        this.context = context;
        this.list = historyActivityArr;

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
        holder.tvCover_Name.setText(history.getCover_Name());
        holder.tvTime.setText(getDateFormatTo("time",history.getTimestamp()));
        holder.tvDate.setText(getDateFormatTo("day",history.getTimestamp()));
        holder.iv_unlockType.setBackgroundResource(getBackgroundRes(history.getUnlock_Type()));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvCover_Name, tvTime, tvDate;
        ImageView iv_unlockType;
        public ViewHolder(View view) {
            super(view);
            this.tvDate = view.findViewById(R.id.tv_date);
            this.tvCover_Name = view.findViewById(R.id.tvCover_Name);
            this.tvTime = view.findViewById(R.id.tvTime);
            this.iv_unlockType = view.findViewById(R.id.iv_unlockType);
        }
    }

    public int getBackgroundRes(String unlock_type){
        switch (unlock_type){
            case "smartphone":
                return R.drawable.ic_phone_unlock;
            case "fingerprint":
                return R.drawable.ic_fingerprint;
            case "otp":
                return R.drawable.ic_otp;
                default: return R.drawable.ic_username;
        }
    }
    
    public String getDateFormatTo(String to,Date date){
        String pattern;
        Locale locale = new Locale("vi", "VN");
        switch (to){
            case "day":
                pattern = "E, dd MMMM/yyyy";
                break;
            case "time":
                pattern = "HH:mm";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + to);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
        return simpleDateFormat.format(date);
    }

}
