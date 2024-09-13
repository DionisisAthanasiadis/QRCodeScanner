package com.example.qrcodescanner;

import android.Manifest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;

import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScanActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;

    private CompoundBarcodeView barcodeView;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        barcodeView = findViewById(R.id.barcode_scanner);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        barcodeView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                barcodeView.pause();
                handleResult(result.getResult());
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {
            }
        });
    }

    private void handleResult(Result rawResult) {
        String qrContent = rawResult.getText();

        if (qrContent != null) {
            String[] parts = qrContent.split(",");
            if (parts.length == 2) {
                try {
                    double latitude = Double.parseDouble(parts[0].trim());
                    double longitude = Double.parseDouble(parts[1].trim());

                    if (isValidLatitude(latitude) && isValidLongitude(longitude)) {
                        saveLocationToFirestore(qrContent, latitude, longitude);
                    } else {
                        Toast.makeText(this, "Invalid latitude or longitude range", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Invalid latitude or longitude format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid QR code content format", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "QR content is null", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveLocationToFirestore(String qrContent, double latitude, double longitude) {
        long timestamp = System.currentTimeMillis();

        Map<String, Object> qrData = new HashMap<>();
        qrData.put("qrContent", qrContent);
        qrData.put("latitude", latitude);
        qrData.put("longitude", longitude);
        qrData.put("timestamp", timestamp);

        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users").document(userId).collection("locations")
                .add(qrData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ScanActivity.this, "Location saved", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ScanActivity.this, MapActivity.class);
                    intent.putExtra("latitude", latitude);
                    intent.putExtra("longitude", longitude);
                    intent.putExtra("locationId", documentReference.getId());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ScanActivity.this, "Error saving location", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isValidLatitude(double latitude) {
        return latitude >= -90 && latitude <= 90;
    }

    private boolean isValidLongitude(double longitude) {
        return longitude >= -180 && longitude <= 180;
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}