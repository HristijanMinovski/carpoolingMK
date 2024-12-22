package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ReservationConfirmationActivity extends AppCompatActivity {
    private Button okButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_confirmation);
        okButton=findViewById(R.id.ok_button);

        TextView confirmationText = findViewById(R.id.reservation_text);

        Intent intent = getIntent();
        String startLocation = intent.getStringExtra("start_location");
        String driverUsername = intent.getStringExtra("driver_username");
        String startTime = intent.getStringExtra("start_time");

        String message = String.format("Резервиравте место на возењето од %s со возачот %s во %s часот",
                startLocation, driverUsername, startTime);

        confirmationText.setText(message);

        // Копче за затворање на активноста
        okButton.setOnClickListener(v -> finish());
    }
}
