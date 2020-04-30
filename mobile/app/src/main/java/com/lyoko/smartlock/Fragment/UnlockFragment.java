package com.lyoko.smartlock.Fragment;

import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lyoko.smartlock.Activities.DeviceControllerActivity;
import com.lyoko.smartlock.R;

import static com.lyoko.smartlock.Utils.LyokoString.HOLD_LOCK;

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
        if (DeviceControllerActivity.hold){
            hold_bg.setBackgroundResource(R.drawable.background_lock_hold);
            tv_state_lock_info.setText("HOLDING");
        }

        img_unlocked.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                DeviceControllerActivity.db.changed_update_code(DeviceControllerActivity.owner_phone_number, DeviceControllerActivity.current_device_address,HOLD_LOCK);
                return true;
            }
        });
        DeviceControllerActivity.btn_door_lock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (DeviceControllerActivity.clicked){
                    if (DeviceControllerActivity.hold){
                        DeviceControllerActivity.lock();
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
