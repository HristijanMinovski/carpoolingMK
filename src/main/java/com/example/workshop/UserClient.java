package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop.databinding.ActivityUserClientBinding;

import java.util.List;

public class UserClient extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng startLatLng;
    private LatLng endLatLng;
    private Marker startMarker;
    private Marker endMarker;
    private Button searchButton;
    private Button rateButton;
    private DatabaseHelper db=new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_client);
        Intent pomIntent=getIntent();
        String username=pomIntent.getStringExtra("username");

        searchButton = findViewById(R.id.search_button);
        rateButton = findViewById(R.id.oceni_vozenje);

        // Инстанцирање на SupportMapFragment и повикување onMapReady кога мапата е подготвена
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        searchButton.setOnClickListener(v -> {
            if (startLatLng != null && endLatLng != null) {
                // Пренеси ги локациите на вториот екран (ActiveDrivers)
                Intent intent = new Intent(UserClient.this, ActiveDriversActivity.class);
                intent.putExtra("start_lat", startLatLng.latitude);
                intent.putExtra("start_lng", startLatLng.longitude);
                intent.putExtra("end_lat", endLatLng.latitude);
                intent.putExtra("end_lng", endLatLng.longitude);
                intent.putExtra("username",username);
                startActivity(intent);
            } else {
                Toast.makeText(UserClient.this, "Изберете локации на мапата!", Toast.LENGTH_SHORT).show();
            }
        });
        rateButton.setOnClickListener(v->{
            int clientId=db.getUserIdByUsername(username);
            Ride unratedRide = db.getUnratedRideForClient(clientId);
            if (unratedRide != null){
            Intent intent = new Intent(UserClient.this, RateDriverActivity.class);
            intent.putExtra("ride_id", unratedRide.getRideId());
            intent.putExtra("driver_username", unratedRide.getDriverUsername());
            intent.putExtra("username",username);
            startActivity(intent);}
            else{
                Toast.makeText(this, "Нема неоценети возења!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    // Кога мапата ќе биде подготвена
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng macedoniaCenter = new LatLng(41.6086, 21.7453);

        // Зумирај на ниво на Македонија (Zoom level: 7 е соодветно)
        float zoomLevel = 7.0f;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(macedoniaCenter, zoomLevel));

        // Поставување на маркери за почетна и крајна локација
        mMap.setOnMapClickListener(latLng -> {
            // Ако нема поставено почетен маркер, постави го
            if (startMarker == null) {
                startLatLng = latLng;
                startMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Почетна Локација"));
            }
            // Ако има почетен маркер, постави краен маркер
            else if (endMarker == null) {
                endLatLng = latLng;
                endMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Крајна Локација"));
            }
            // Ако се веќе поставени двата маркери, обнови ја крајната локација
            else {
                endMarker.setPosition(latLng);
                endLatLng = latLng;
            }
        });
    }
}
