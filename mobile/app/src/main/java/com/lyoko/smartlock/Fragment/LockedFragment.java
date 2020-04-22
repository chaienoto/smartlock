package com.lyoko.smartlock.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lyoko.smartlock.Activities.MainActivity;
import com.lyoko.smartlock.R;

import static com.lyoko.smartlock.Activities.MainActivity.btn_door_lock;
import static com.lyoko.smartlock.Activities.MainActivity.clicked;
import static com.lyoko.smartlock.Activities.MainActivity.unlock;
import static com.lyoko.smartlock.Utils.LyokoString.phone_name;

/**
 * A simple {@link Fragment} subclass.
 */
public class LockedFragment extends Fragment {

    public LockedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_locked, container, false);
        btn_door_lock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!clicked){
                    unlock();
                    MainActivity.db.saveHistory(MainActivity.owner_phone_number, MainActivity.current_device_address,phone_name);
                }
                return true;
            }
        });
        return view;
    }
}
