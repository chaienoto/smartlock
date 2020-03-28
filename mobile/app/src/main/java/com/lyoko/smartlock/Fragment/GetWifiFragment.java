package com.lyoko.smartlock.Fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lyoko.smartlock.Activities.AddDeviceActivity;
import com.lyoko.smartlock.Adapters.WifiScanAdapter;
import com.lyoko.smartlock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetWifiFragment extends Fragment implements WifiScanAdapter.OnItemClickedListener {
    EditText ed_wifi_password;
    RecyclerView wifi_list_recycleView;
    WifiManager wifiManager;
    TextView tv_ssid;
    WifiScanAdapter wifiScanAdapter;
    ArrayList<String> wifi_ssid_list = new ArrayList<String>();
    BroadcastReceiver wifiScanReceiver;

    public GetWifiFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_wifi, container, false);
        ed_wifi_password = view.findViewById(R.id.ed_wifi_password);
        tv_ssid = view.findViewById(R.id.tv_ssid);
        wifi_list_recycleView = view.findViewById(R.id.wifi_list_recycleView);
        // create new wifi scan
        wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) wifiManager.setWifiEnabled(true);
        // create new wifi receiver
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean getResultsSuccess = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (getResultsSuccess) scanSuccess();
                 else scanFailure();
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getContext().registerReceiver(wifiScanReceiver, intentFilter);

        if (!wifiManager.startScan()) {
            scanFailure();
        }

        AddDeviceActivity.btn_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddDeviceActivity.wifi_password = ed_wifi_password.getText().toString();
                AddDeviceActivity.gotoNextStep(3);
            }
        });
        return view;
    }

    private void scanFailure() {
        Toast.makeText(getContext(), "Không ổn rồi đại vương", Toast.LENGTH_SHORT).show();
    }

    private void scanSuccess() {
        List<ScanResult> results = wifiManager.getScanResults();
        for (ScanResult result : results){
            wifi_ssid_list.add(result.SSID);
        }
        wifiScanAdapter = new WifiScanAdapter(getContext(), wifi_ssid_list);
        wifiScanAdapter.setOnItemClickedListener(this);
        wifi_list_recycleView.setAdapter(wifiScanAdapter);
        wifi_list_recycleView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(wifiScanReceiver);
    }

    @Override
    public void onItemClick(String ssid) {
        tv_ssid.setText(ssid);
        AddDeviceActivity.wifi_ssid = ssid;
    }
}
