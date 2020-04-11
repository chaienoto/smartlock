package com.lyoko.smartlock.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.lyoko.smartlock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class GetDeviceNameFragment extends Fragment {
    public  static EditText add_device_name;
    public GetDeviceNameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_get_device_name, container, false);
        add_device_name = view.findViewById(R.id.add_device_name);
        return view;
    }

}
