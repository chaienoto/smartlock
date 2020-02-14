package com.lyoko.smartlock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {


    private Context context;
    private ArrayList<History> historyActivityArr;

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
        History history = historyActivityArr.get(position);
//chu y
        holder.ic.setImageResource(history.getIc());
        holder.tvTitle.setText(history.getTitle());
        holder.tvDescription.setText(history.getDescription());

    }

    @Override
    public int getItemCount() {
        return historyActivityArr.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView tvTitle, tvDescription;
        private ImageView ic;

        public ViewHolder(View view) {

            super(view);
            ic = itemView.findViewById(R.id.ic);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }

    public HistoryAdapter(Context context, ArrayList<History> historyActivityArr) {

        this.context = context;
        this.historyActivityArr = historyActivityArr;

    }
}
