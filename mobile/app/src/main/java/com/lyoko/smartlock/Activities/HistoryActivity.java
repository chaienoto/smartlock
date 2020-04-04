package com.lyoko.smartlock.Activities;

import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.lyoko.smartlock.Adapters.HistoriesAdapter;
import com.lyoko.smartlock.Models.History;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Interface.IHistory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_BLUE;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;

public class HistoryActivity extends AppCompatActivity implements IHistory {
    Database_Service db_service = new Database_Service();
    RecyclerView histories_recyclerView;
    HistoriesAdapter historyAdapter;
    RelativeLayout history_list_layout;
    Toolbar toolbar;
    private String current_device_address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        histories_recyclerView = findViewById(R.id.history_recyclerView);
        toolbar = findViewById(R.id.history_toolbar);
        history_list_layout = findViewById(R.id.history_list_layout);

        toolbar.setTitle("History");
        getWindow().setStatusBarColor(COLOR_BLUE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle bundle = getIntent().getExtras();
        current_device_address = bundle.getString("address");
        db_service.getHistories(current_device_address,this);
    }

    @Override
    public void show_history(ArrayList<History> list) {
        Collections.sort(list, new Comparator<History>() {
            @Override
            public int compare(History o1, History o2) {
                return o2.getUnlock_time().compareTo(o1.getUnlock_time());
            }
        });
        if (list.size() % 2 != 0){
            history_list_layout.setBackgroundColor(COLOR_ODD_POSITION);
        } else history_list_layout.setBackgroundColor(COLOR_EVEN_POSITION);

        historyAdapter = new HistoriesAdapter(this, list);
        histories_recyclerView.setAdapter(historyAdapter);
        histories_recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }


}





