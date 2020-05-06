package com.lyoko.smartlock.Utils;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import androidx.appcompat.app.AlertDialog;

import com.lyoko.smartlock.R;

public class LoadingDialog {
    Activity activity;
    AlertDialog dialog;

    public LoadingDialog(Activity activity) {
        this.activity = activity;
    }

    public void startLoading(String message ){

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_loading,null);
        TextView loading_message = view.findViewById(R.id.loading_message);
        loading_message.setText(message);
        builder.setView(view);
        dialog = builder.create();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

     public void stopLoading(){
        dialog.dismiss();
    }
}
