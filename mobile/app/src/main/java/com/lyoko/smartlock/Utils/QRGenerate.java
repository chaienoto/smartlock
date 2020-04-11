package com.lyoko.smartlock.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lyoko.smartlock.R;

import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class QRGenerate {
    public static void showMyQR(Activity activity) {
        QRCodeWriter writer = new QRCodeWriter();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        final View view = inflater.inflate(R.layout.myqr_dialog,null);
        final ImageView img_myQR = view.findViewById(R.id.img_myQR);
        final ImageView img_close_myQR_dialog = view.findViewById(R.id.img_close_myQR_dialog);
        builder.setView(view);
        try {
            BitMatrix bitMatrix = writer.encode(phone_login, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            for (int x = 0; x<512; x++){
                for (int y=0; y<512; y++){
                    bitmap.setPixel(x,y,bitMatrix.get(x,y)? Color.BLACK : Color.WHITE);
                }
            }
            img_myQR.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final AlertDialog dialog = builder.create();
        img_close_myQR_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
