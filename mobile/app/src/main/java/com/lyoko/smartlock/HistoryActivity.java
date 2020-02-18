package com.lyoko.smartlock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.lyoko.smartlock.Adapters.HistoryAdapter;
import com.lyoko.smartlock.Models.History;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<History> historyActivityArr;
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        historyActivityArr = new ArrayList<>();
        createHistoryList();

        historyAdapter = new HistoryAdapter(this, historyActivityArr);
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Query query = firebaseFirestore.collection("/door/history/files");
        FirestoreRecyclerOptions<History> options = new FirestoreRecyclerOptions.Builder<History>()
                .setQuery(query, History.class)
                .build();
        adapter = new FirestoreRecyclerAdapter<History, HistoryViewHolder>(options) {

            @NonNull
            @Override
            public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);

                return new HistoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull HistoryViewHolder holder, int position, @NonNull History model) {

                holder.tvCover_Name.setText(model.getCover_Name());
                holder.tvState.setText(model.getState() + "");
                holder.tvTime.setText(model.getTime()+ "");
                holder.tvUnlock_Type.setText(model.getUnlock_Type());

            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

    private void createHistoryList() {

        historyActivityArr.add(new History("Tieu de 1", true, null,"aaaaa", R.drawable.ic_password));
//        historyActivityArr.add(new History("Tieu de 1", "Mo ta 1", "Tieu de 1", "Mo ta 1", R.drawable.ic_username));
//        historyActivityArr.add(new History("Tieu de 1", "Mo ta 1", "Tieu de 1", "Mo ta 1", R.drawable.ic_username));
    }
    private class HistoryViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCover_Name, tvState, tvTime, tvUnlock_Type;


        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCover_Name = itemView.findViewById(R.id.tvCover_Name);
            tvState = itemView.findViewById(R.id.tvState);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvUnlock_Type = itemView.findViewById(R.id.tvUnlock_Type);
        }

    }

    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }


//    public void showData(){
//        HistoryService historyService = new HistoryService();
//    }

}
