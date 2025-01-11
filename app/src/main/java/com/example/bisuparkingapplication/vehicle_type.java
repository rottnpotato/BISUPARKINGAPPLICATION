package com.example.bisuparkingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class vehicle_type extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_type);

        ImageButton buttonNext = null;//= findViewById(R.id.imageButton4);
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start SecondActivity
                Intent intent = new Intent(vehicle_type.this, owner_type.class);
                startActivity(intent);
            }

        });
    }


}
