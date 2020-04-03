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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;


public class HistoriesAdapter extends RecyclerView.Adapter<HistoriesAdapter.ViewHolder> {

    private Context context;
    private ArrayList<History> list;

    public HistoriesAdapter(Context context, ArrayList<History> list) {
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
        holder.tvTime.setText(getDateFormatTo("time",history.getUnlock_time()));
        holder.tvDate.setText(getDateFormatTo("day",history.getUnlock_time()));
        holder.iv_unlockType.setBackgroundResource(getBackgroundRes(history.getUnlock_type()));
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
        TextView tvCover_Name, tvTime, tvDate;
        ImageView iv_unlockType;
        LinearLayout item_history;
        public ViewHolder(View view) {
            super(view);
            this.tvDate = view.findViewById(R.id.tv_date);
            this.tvCover_Name = view.findViewById(R.id.tvCover_Name);
            this.item_history = view.findViewById(R.id.item_history);
            this.tvTime = view.findViewById(R.id.tvTime);
            this.iv_unlockType = view.findViewById(R.id.iv_unlockType);
        }
    }

    public int getBackgroundRes(String unlock_type){
        switch (unlock_type){
            case LyokoString.UNLOCK_TYPE_SMARTPHONE:
                return R.drawable.ic_phone_unlock ;
            case LyokoString.UNLOCK_TYPE_FINGERPRINT:
                return R.drawable.ic_fingerprint;
            case LyokoString.UNLOCK_TYPE_OTP:
                return R.drawable.ic_otp;
            default:
                throw new IllegalStateException("Unexpected value: " + unlock_type);
        }
    }

    public String getDateFormatTo(String formatTo,Long timestamp){
        String pattern;
        Date date =new Date(timestamp);
        Locale locale = new Locale("vi", "VN");
        switch (formatTo){
            case "day":
                pattern = "EEE, dd-MM-yyyy";
                break;
            case "time":
                pattern = "HH:mm";
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + formatTo);
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
        return simpleDateFormat.format(date);
    }

}
