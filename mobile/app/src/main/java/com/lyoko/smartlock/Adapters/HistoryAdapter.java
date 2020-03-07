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

import java.util.ArrayList;

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
        holder.tvTime.setText(String.valueOf(history.getTime()));
        holder.tvUnlock_Type.setText(history.getUnlock_Type());


    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    //1
    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tvCover_Name, tvState, tvTime, tvUnlock_Type;
        public ViewHolder(View view) {
            super(view);
            this.tvCover_Name = itemView.findViewById(R.id.tvCover_Name);
            this.tvTime = itemView.findViewById(R.id.tvTime);
            this.tvUnlock_Type = itemView.findViewById(R.id.tvUnlock_Type);
        }
    }





}
