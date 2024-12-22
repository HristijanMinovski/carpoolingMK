package com.example.workshop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.workshop.databinding.ActivityVehicleChangeBinding;

public class VehicleChangeActivity extends AppCompatActivity {
    private EditText name,year,price;
    private Button input;
    private DatabaseHelper db=new DatabaseHelper(this);

    @SuppressLint("MissingInflatedId")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_change);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        int driverId = db.getUserIdByUsername(username);
        name = findViewById(R.id.VehicleName);
        year = findViewById(R.id.VehicleYear);
        input = findViewById(R.id.vehicle_button);

        input.setOnClickListener(v -> {
            String name_s = name.getText().toString();
            int year_s = Integer.parseInt(year.getText().toString());
            Log.d("InputDebug", "Vehicle Name: " + name_s + ", Year: " + year_s );
            if (name_s.isEmpty() || year_s == 0 ) {
                Toast.makeText(this, "Пополнете ги сите полиња!", Toast.LENGTH_SHORT).show();
            } else {
                boolean uslov = db.vehicleUnique(driverId);
                if (uslov==true) {
                    int vehicle_id_s = db.getVehicleIdbyDriverId(driverId);
                    Log.d("DatabaseDebug", "Updating vehicle with ID: " + vehicle_id_s);
                    db.updateVehicle(vehicle_id_s, driverId, name_s, year_s, 0);
                } else {
                    Log.d("DatabaseDebug", "Inserting new vehicle for driverId: " + driverId);
                    boolean success=db.insertVehicle(driverId, name_s, 0, year_s);
                    if(!success){
                        Log.e("DatabaseError", "Failed to insert new vehicle.");
                    }
                }
                // Премини на UserDriver активност откако ќе заврши внесувањето
                Intent intent2 = new Intent(this, UserDriver.class);
                intent2.putExtra("username", username);
                startActivity(intent2);
                finish();
            }
        });
    }}
