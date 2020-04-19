package com.lyoko.smartlock.Fragment;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lyoko.smartlock.Activities.MainActivity;
import com.lyoko.smartlock.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class UnlockFragment extends Fragment {
    ImageView img_unlocked;
    public static TextView tv_state_lock_info;
    public static ConstraintLayout hold_bg;

    public UnlockFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_unlock, container, false);
        img_unlocked = view.findViewById(R.id.img_unlocked);
        tv_state_lock_info = view.findViewById(R.id.tv_state_lock_info);
        hold_bg = view.findViewById(R.id.hold_bg);
        if (MainActivity.hold){
            hold_bg.setBackgroundResource(R.drawable.background_lock_hold);
            tv_state_lock_info.setText("HOLDING");
        }

        img_unlocked.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                MainActivity.db.changeLockState(MainActivity.owner_phone_number,MainActivity.current_device_address,2);
                return true;
            }
        });
        MainActivity.btn_door_lock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (MainActivity.clicked){
                    if (MainActivity.hold){
                        MainActivity.lock();
                        tv_state_lock_info.setText("UNLOCKED");
                        hold_bg.setBackgroundResource(R.drawable.background_lock);
                    }
                }
                return true;
            }
        });
        return view;
    }
}
