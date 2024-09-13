package com.example.qrcodescanner;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class LocationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LocationsAdapter adapter;
    private List<LocationData> locationList;
    private Button backButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locations);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        locationList = new ArrayList<>();
        adapter = new LocationsAdapter(locationList, this::onLocationClicked);
        recyclerView.setAdapter(adapter);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String userId = mAuth.getCurrentUser().getUid();
        db.collection("users").document(userId).collection("locations")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(LocationsActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        locationList.clear();
                        for (QueryDocumentSnapshot document : value) {
                            LocationData locationData = document.toObject(LocationData.class);
                            locationData.setDocumentId(document.getId());
                            locationList.add(locationData);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void onLocationClicked(LocationData location) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("latitude", location.getLatitude());
        intent.putExtra("longitude", location.getLongitude());
        intent.putExtra("locationId", location.getDocumentId());
        startActivity(intent);
    }
}