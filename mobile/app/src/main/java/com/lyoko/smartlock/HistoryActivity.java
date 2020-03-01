package com.lyoko.smartlock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.lyoko.smartlock.Adapters.HistoryAdapter;
import com.lyoko.smartlock.Models.History;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ArrayList<History> historyActivityArr;
    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //Spinner
        Spinner spinner = findViewById(R.id.spinner);
        String compareValue = "Cover_Name";

        ArrayAdapter<CharSequence> arrayAdapter = ArrayAdapter.createFromResource(this, R.array.history_options, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);
        //lay theo gia tri
        if (compareValue != null) {
            int spinnerPos = arrayAdapter.getPosition(compareValue);
            spinner.setSelection(spinnerPos);
        }

        spinner.setOnItemSelectedListener(this);



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

//                Date date = model.getTime();
//                DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, Locale.CHINA);
//                String creationDate = dateFormat.format(date);
//
//                holder.tvTime.setText(creationDate);

                holder.tvUnlock_Type.setText(model.getUnlock_Type());

            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

    }

//sap xep theo loai
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

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
