package com.example.android.moso_hw3;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class Location_Activity extends AppCompatActivity {

    FusedLocationProviderClient mFusedLocationClient;
    LocationCallback callback;
    TextView longtext,latitext;

    final int myrequestcode = 500;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    myrequestcode);
        }



// Permission is not granted
        setContentView(R.layout.activity_location_);

        longtext = (TextView) findViewById(R.id.longtext);
        latitext = (TextView) findViewById(R.id.latitext);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    public void btnLastLocation(View view) {
        try{
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d("MainActivity",
                                    "location: " + location.getLatitude() + ", " + location.getLongitude());
                            longtext.setText(Double.toString(location.getLongitude()));
                            latitext.setText(Double.toString(location.getLatitude()));
                        }
                    });
        }catch (SecurityException e){
            Log.d("error", e.getMessage());
        }
    }


    public void btnLocationUpdate(View view) {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(500);
        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        try{
            mFusedLocationClient.requestLocationUpdates(request, callback = new LocationCallback() {

                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    for(Location location : locationResult.getLocations()) {
                        Log.d("MyLocation", "location: " + location.getLatitude() + ", " + location.getLongitude());
                        longtext.setText(Double.toString(location.getLongitude()));
                        latitext.setText(Double.toString(location.getLatitude()));
                    }
                }
            }, null);
        }catch (SecurityException e){
            Log.d("error", e.getMessage());
        }

    }

    public void btnstop(View view){
        mFusedLocationClient.removeLocationUpdates(callback);
        longtext.setText("STOP");
        latitext.setText("STOP");
    }

}
