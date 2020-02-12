package com.lyoko.smartlock;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

public class SignUpDialog extends AppCompatDialogFragment {

    public TextView tvSDT;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.sign_dialog, null);

        tvSDT = view.findViewById(R.id.tvSDT);
        Bundle bundle = getArguments();
        String sdt_copy = bundle.getString("SDT","");
        tvSDT.setText(sdt_copy);



        builder.setView(view)
//                .setTitle("Thông báo")
//                .setMessage("")
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        Intent i = new Intent(DialogLogin.this, Login2Activity.class);
//                        startActivity(i);
                    }
                });


        return builder.create();
    }
}
