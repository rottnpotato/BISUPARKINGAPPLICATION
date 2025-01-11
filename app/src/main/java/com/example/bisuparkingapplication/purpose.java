package com.example.bisuparkingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class purpose extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.purpose);

        Button buttonNext = findViewById(R.id.button);
        buttonNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(purpose.this, vehicle_type.class);
                startActivity(intent);
            }
        });

        
        Button buttonExit = findViewById(R.id.button3);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(purpose.this, exit.class);
                startActivity(intent);
            }
        });
    }
}
