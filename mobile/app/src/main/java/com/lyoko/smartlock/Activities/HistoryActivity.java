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
import java.util.Collections;
import java.util.Comparator;

public class HistoryActivity extends AppCompatActivity implements IHistory {
    Database_Service db_service = new Database_Service();

    RecyclerView recyclerView;
    HistoryAdapter historyAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        recyclerView = findViewById(R.id.recyclerView);

//        setSupportActionBar(toolbar);

        db_service.getHistories(this);

    }




    @Override
    public void show_history(ArrayList<History> list) {

            Collections.sort(list, new Comparator<History>() {
                @Override
                public int compare(History o1, History o2) {
                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                }
            });

        historyAdapter = new HistoryAdapter(this, list);
        recyclerView.setAdapter(historyAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


}





