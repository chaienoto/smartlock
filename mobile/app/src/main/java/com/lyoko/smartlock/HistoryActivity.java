package com.lyoko.smartlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity {

    private ArrayList<History> historyActivityArr ;
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);


        recyclerView = findViewById(R.id.recyclerView);
        historyActivityArr = new ArrayList<>();
        createHistoryList();

        historyAdapter = new HistoryAdapter(this, historyActivityArr);
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    private void createHistoryList() {

        historyActivityArr.add(new History("Tieu de 1", "Mo ta 1",R.drawable.ic_password));
        historyActivityArr.add(new History("Tieu de 2", "Mo ta 2",R.drawable.ic_username));
        historyActivityArr.add(new History("Tieu de 2", "Mo ta 2",R.drawable.ic_username));
    }
}
