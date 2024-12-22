package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.workshop.databinding.ActivityUserDriverBinding;

public class UserDriver extends AppCompatActivity {
    private TextView userNameTextView;
    private TextView vehicleNameTextView;
    private TextView vehicleYearTextView;

    private TextView driverRatingTextView;
    private DatabaseHelper dbHelper;
    private Button routeButton,rateButton;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_user_driver);

        // Инстанцирање на текстуални полиња
        userNameTextView = findViewById(R.id.userNameTextView);
        vehicleNameTextView = findViewById(R.id.vehicleNameTextView);
        vehicleYearTextView = findViewById(R.id.vehicleYearTextView);
        driverRatingTextView = findViewById(R.id.driverRatingTextView);
        routeButton=findViewById(R.id.route_button);
        rateButton=findViewById(R.id.rate_button);
        DatabaseHelper db=new DatabaseHelper(this);

        if (getIntent() != null && getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        }

        // Toolbar и копче
        Toolbar toolbar = findViewById(R.id.appbar_driver);
        setSupportActionBar(toolbar);
        Button appbarButton = toolbar.findViewById(R.id.appbar_button);

        appbarButton.setOnClickListener(view -> {
            Intent intent2 = new Intent(UserDriver.this, VehicleChangeActivity.class);
            intent2.putExtra("username", username);
            startActivity(intent2);
        });

        routeButton.setOnClickListener(v->{
            Intent intent3 = new Intent(UserDriver.this, AddRoute.class);
            intent3.putExtra("username",username);
            startActivity(intent3);
        });
        rateButton.setOnClickListener(v->{
            Intent intent4=new Intent(UserDriver.this,RateClientActivity.class);
            intent4.putExtra("username",username);
            int ocenuvac_id=db.getUserIdByUsername(username);
            int ocenet_id=db.getFirstUnratedOcenet(ocenuvac_id);
            intent4.putExtra("ocenuvac_id",ocenuvac_id);
            intent4.putExtra("ocenet_id",ocenet_id);
            startActivity(intent4);
        });
        // Пополнување на податоците
        dbHelper = new DatabaseHelper(this);
        loadDriverData(username);
    }

    // Метод за полнење на податоците
    private void loadDriverData(String username) {

        int driverId = dbHelper.getUserIdByUsername(username);
        String vehicleName = dbHelper.getVehicleName(driverId);
        int vehicleYear = dbHelper.getVehicleYear(driverId);
        float driverRating = dbHelper.getDriverRating(driverId);

        userNameTextView.setText("Корисничко име: " + username);
        vehicleNameTextView.setText("Возило: " + vehicleName);
        vehicleYearTextView.setText("Година: " + vehicleYear);
        driverRatingTextView.setText("Рејтинг: " + driverRating);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (getIntent() != null && getIntent().hasExtra("username")) {
            username = getIntent().getStringExtra("username");
        }
        loadDriverData(username); // Осигурај се дека секогаш се обновуваат податоците
    }
}
