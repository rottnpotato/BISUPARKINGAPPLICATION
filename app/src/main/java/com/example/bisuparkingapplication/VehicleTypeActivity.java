package com.example.bisuparkingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;
import com.example.bisuparkingapplication.network.SupabaseClient;
import org.json.JSONObject;

public class VehicleTypeActivity extends AppCompatActivity {
    private MaterialButton motorcycleButton, carButton;
    private CardView motorcycleCard, carCard;
    private String plateNumber;
    private SupabaseClient supabaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vehicle_type);

        supabaseClient = SupabaseClient.getInstance(this);
        plateNumber = getIntent().getStringExtra("plate_number");

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        motorcycleButton = findViewById(R.id.motorcycleButton);
        carButton = findViewById(R.id.carButton);
        motorcycleCard = findViewById(R.id.motorcycleCard);
        carCard = findViewById(R.id.carCard);
    }

    private void setupClickListeners() {
        View.OnClickListener motorcycleListener = v -> updateVehicleTypeAndProceed("MOTORCYCLE");
        View.OnClickListener carListener = v -> updateVehicleTypeAndProceed("CAR");

        // Set click listeners for both buttons and cards
        motorcycleButton.setOnClickListener(motorcycleListener);
        motorcycleCard.setOnClickListener(motorcycleListener);
        carButton.setOnClickListener(carListener);
        carCard.setOnClickListener(carListener);
    }

    private void updateVehicleTypeAndProceed(String vehicleType) {
        try {
            JSONObject updateData = new JSONObject();
            updateData.put("vehicle_type", vehicleType);

            supabaseClient.updateUserVehicle(plateNumber, updateData,
                    response -> {
                        // Check if response indicates success
                        if (response.optBoolean("success", false)) {
                            // Save to shared preferences for quick access
                            getSharedPreferences("BisuParking", MODE_PRIVATE)
                                    .edit()
                                    .putString("vehicle_type", vehicleType)
                                    .apply();

                            Intent intent = new Intent(this, ParkingPageActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        } else {
                            Toast.makeText(this, "Error updating vehicle type. Please try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("VehicleTypeError", "Error updating vehicle type: " +
                                (error.networkResponse != null ? new String(error.networkResponse.data) : "No response data"));
                        Toast.makeText(this, "Error updating vehicle type. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}