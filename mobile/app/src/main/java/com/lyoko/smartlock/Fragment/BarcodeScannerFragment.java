package com.lyoko.smartlock.Fragment;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Vibrator;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.lyoko.smartlock.Activities.AddDeviceActivity;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Utils.MACSuitable;
import java.io.IOException;

/**
 * A simple {@link Fragment} subclass.
 */
public class BarcodeScannerFragment extends Fragment {
    SurfaceView surfaceView;
    CameraSource cameraSource;
    BarcodeDetector barcodeDetector;
    OnGetDeviceAddress callback;
    View scannerLayout,scannerBar ;
    ObjectAnimator animator = null ;

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
        scannerLayout = view.findViewById(R.id.scannerLayout);
        scannerBar = view.findViewById(R.id.scannerBar);
        scannerBar.setVisibility(View.INVISIBLE);

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
                    String s = (qrCode.valueAt(0).displayValue);

                    if (MACSuitable.check(s)){
                        callback.onDeviceAddressSuitable(s);
                        getFragmentManager().beginTransaction().remove(BarcodeScannerFragment.this).commit();
                    } else {
                        callback.onDeviceAddressUnSuitable();
                        getFragmentManager().beginTransaction().remove(BarcodeScannerFragment.this).commit();
                    }
                }
            }
        });
        return  view;
    }
    public  interface  OnGetDeviceAddress{
        void onDeviceAddressSuitable(String address);
        void onDeviceAddressUnSuitable();
    }
    public void ani(){
        animator = null;
        ViewTreeObserver vto = scannerLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                scannerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    scannerLayout.getViewTreeObserver().
                            removeGlobalOnLayoutListener(this);

                } else {
                    scannerLayout.getViewTreeObserver().
                            removeOnGlobalLayoutListener(this);
                }

                float destination = (float)(scannerLayout.getY() +
                        scannerLayout.getHeight());

                animator = ObjectAnimator.ofFloat(scannerBar, "translationY",
                        scannerLayout.getY(),
                        destination);

                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.setDuration(3000);
                animator.start();

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(500);
    }
}
