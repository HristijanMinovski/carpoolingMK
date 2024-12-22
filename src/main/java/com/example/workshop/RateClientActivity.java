package com.example.workshop;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RateClientActivity extends AppCompatActivity {

    private DatabaseHelper db; // Инстанца од DatabaseHelper
    private String username;
    private int ocenuvacId;
    private int ocenetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_client);
        // Иницијализација на базата
        db = new DatabaseHelper(this);

        // Превземање на податоци од Intent
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        ocenuvacId = intent.getIntExtra("ocenuvac_id", -1);
        ocenetId = intent.getIntExtra("ocenet_id", -1);
        // Наоѓање на UI елементите
        TextView rateMessage = findViewById(R.id.rate_message);
        EditText ratingInput = findViewById(R.id.rating_input);
        Button submitButton = findViewById(R.id.submit_rating_button);
        // Поставување на текст за оценување
        String message = "Оценете го патникот \"" + db.getUserName(ocenetId) + "\" од 1-5:";
        rateMessage.setText(message);
        // Сетирање на listener за копчето за внесување
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ratingStr = ratingInput.getText().toString();
                // Проверка дали е внесена валидна оцена
                if (ratingStr.isEmpty()) {
                    Toast.makeText(RateClientActivity.this, "Ве молиме внесете оцена!", Toast.LENGTH_SHORT).show();
                    return;
                }
                int rating = Integer.parseInt(ratingStr);
                if (rating < 1 || rating > 5) {
                    Toast.makeText(RateClientActivity.this, "Оцената мора да биде помеѓу 1 и 5!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Ажурирање на рејтингот
                boolean isUpdated = db.updateRating(db.getUserName(ocenetId), rating, "client");
                if (isUpdated) {
                    // Поставување на `dali_oceneto` на 1
                    db.updateDaliOceneto(ocenuvacId, ocenetId);
                    Toast.makeText(RateClientActivity.this, "Успешно внесена оцена!", Toast.LENGTH_SHORT).show();
                    finish(); // Затворање на активноста
                } else {
                    Toast.makeText(RateClientActivity.this, "Грешка при внесување на оцената.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
