package com.example.qrcodescanner;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LocationsAdapter extends RecyclerView.Adapter<LocationsAdapter.LocationViewHolder> {

    private List<LocationData> locationList;
    private OnLocationClickListener onLocationClickListener;

    public LocationsAdapter(List<LocationData> locationList, OnLocationClickListener onLocationClickListener) {
        this.locationList = locationList;
        this.onLocationClickListener = onLocationClickListener;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        LocationData location = locationList.get(position);
        holder.qrContent.setText(location.getQrContent());
        holder.latitude.setText("Lat: " + location.getLatitude());
        holder.longitude.setText("Lng: " + location.getLongitude());
        holder.timestamp.setText("Timestamp: " + location.getTimestamp());

        holder.itemView.setOnClickListener(v -> onLocationClickListener.onLocationClick(location));
    }

    @Override
    public int getItemCount() {
        return locationList.size();
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView qrContent, latitude, longitude, timestamp;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            qrContent = itemView.findViewById(R.id.qrContent);
            latitude = itemView.findViewById(R.id.latitude);
            longitude = itemView.findViewById(R.id.longitude);
            timestamp = itemView.findViewById(R.id.timestamp);
        }
    }

    public interface OnLocationClickListener {
        void onLocationClick(LocationData location);
    }
}