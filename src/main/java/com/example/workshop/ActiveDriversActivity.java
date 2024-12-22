package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.workshop.databinding.ActivityActiveDriversBinding;

import java.util.ArrayList;
import java.util.List;

    public class ActiveDriversActivity extends AppCompatActivity {

        private RecyclerView recyclerView;
        private myAdapter adapter;
        private List<ScheduledRide> driverList;
        private DatabaseHelper db;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_active_drivers);

            recyclerView = findViewById(R.id.baranja);
            driverList = new ArrayList<>();
            db = new DatabaseHelper(this);
            Intent intent=getIntent();
            // Примени локациите
            double startLat = intent.getDoubleExtra("start_lat", 0);
            double startLng = intent.getDoubleExtra("start_lng", 0);
            double endLat = intent.getDoubleExtra("end_lat", 0);
            double endLng = intent.getDoubleExtra("end_lng", 0);
            String username=intent.getStringExtra("username");

            // Добиј ги возачите кои одговараат на локациите
            driverList = db.getFilteredRides(startLat, startLng, endLat, endLng, 0.05);

            // Подеси RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new myAdapter(driverList, this,username);
            recyclerView.setAdapter(adapter);
        }
    }
