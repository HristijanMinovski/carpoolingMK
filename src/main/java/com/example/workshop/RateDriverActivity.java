package com.example.workshop;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RateDriverActivity extends AppCompatActivity {

    private EditText ratingInput;
    private Button submitButton;
    private DatabaseHelper dbHelper;

    private int rideId;
    private String driverUsername,client_username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_driver);

        dbHelper = new DatabaseHelper(this);
        Intent intent=getIntent();

        rideId = intent.getIntExtra("ride_id", -1);
        driverUsername = intent.getStringExtra("driver_username");
        client_username=intent.getStringExtra("username");
        //            Intent intent = new Intent(UserClient.this, RateDriverActivity.class);
        //            intent.putExtra("ride_id", unratedRide.getRideId());
        //            intent.putExtra("driver_username", unratedRide.getDriverUsername());
        //            intent.putExtra("username",username);

        TextView driverNameTextView = findViewById(R.id.driver_name);
        ratingInput = findViewById(R.id.rating_input);
        submitButton = findViewById(R.id.submit_button);

        // Прикажување на името на возачот
        driverNameTextView.setText("Возач: " + driverUsername);

        submitButton.setOnClickListener(v -> {
            String ratingStr = ratingInput.getText().toString();
            try {
                float rating = Float.parseFloat(ratingStr);
                if (rating >= 1 && rating <= 5) {
                    boolean success = dbHelper.updateRating(driverUsername, rating,"driver");
                    if (success) {
                        int client_id=dbHelper.getUserIdByUsername(client_username);
                        int driverId=dbHelper.getUserIdByUsername(driverUsername);
                        dbHelper.updateDaliOceneto(client_id,driverId);
                        Toast.makeText(this, "Оценката е успешно зачувана!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Грешка при ажурирање на возењето!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Оцена мора да биде помеѓу 1 и 5!", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Невалиден внес!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
