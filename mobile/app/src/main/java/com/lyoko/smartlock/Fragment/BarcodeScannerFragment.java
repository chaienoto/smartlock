package com.lyoko.smartlock.Fragment;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Vibrator;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.lyoko.smartlock.Activities.AddLockActivity;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.IFindLock;
import com.lyoko.smartlock.Utils.LyokoString;

import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class BarcodeScannerFragment extends Fragment {
    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    OnGetDeviceAddress callback;



    public BarcodeScannerFragment() {
        // Required empty public constructor
    }
    public void setOnGetDeviceAddress(OnGetDeviceAddress callback) {
        this.callback = callback;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
        surfaceView = view.findViewById(R.id.surfaceView);
        barcodeDetector = new BarcodeDetector.Builder(getContext()).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(getContext(), barcodeDetector).setRequestedPreviewSize(640, 480).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCode = detections.getDetectedItems();

                if (qrCode.size() != 0) {
                    String address =(qrCode.valueAt(0).displayValue);

                    Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    vibrator.vibrate(500);
                    if (address.contains(LyokoString.MAC_DEFAULT)){
                        callback.onAddressDeviceScanned(address);
                        getFragmentManager().beginTransaction().remove(BarcodeScannerFragment.this).commit();
                    } else {
                        callback.onAddressScannedUnsuitable();
                        getFragmentManager().beginTransaction().remove(BarcodeScannerFragment.this).commit();
                    }
                }
            }
        });
        return  view;
    }
    public  interface  OnGetDeviceAddress{
        void onAddressDeviceScanned(String address);
        void onAddressScannedUnsuitable();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(800);
    }
}
