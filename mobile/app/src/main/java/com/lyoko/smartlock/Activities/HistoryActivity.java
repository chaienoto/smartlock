package com.lyoko.smartlock.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lyoko.smartlock.Adapters.HistoryAdapter;
import com.lyoko.smartlock.Models.History;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Services.IHistory;

import java.util.ArrayList;

public class HistoryActivity extends AppCompatActivity implements IHistory {
    Database_Service db_service = new Database_Service();

    RecyclerView recyclerView;
    HistoryAdapter historyAdapter;
//    private Spinner spinner;
//    private FirestoreRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        recyclerView = findViewById(R.id.recyclerView);

//        setSupportActionBar(toolbar);
//        spinner = findViewById(R.id.spinner);

        db_service.getHistories(this);


    }



    @Override
    public void show_history(ArrayList<History> list) {
        historyAdapter = new HistoryAdapter(this,list);
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

}





