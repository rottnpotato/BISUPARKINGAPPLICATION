package com.example.bisuparkingapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bisuparkingapplication.network.SupabaseClient;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;

public class PlateNumberActivity extends AppCompatActivity {
    private EditText plateNumberInput;
    private Button continueButton;
    private TextInputLayout plateInputLayout;
    private SupabaseClient supabaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plate_number);

        supabaseClient = SupabaseClient.getInstance(this);

        plateNumberInput = findViewById(R.id.plateNumberInput);
        continueButton = findViewById(R.id.continueButton);
        plateInputLayout = findViewById(R.id.plateInputLayout);

        plateNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                plateInputLayout.setError(null);
                continueButton.setEnabled(s.length() >= 6);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        continueButton.setOnClickListener(v -> {
            String plateNumber = plateNumberInput.getText().toString().trim();
            checkUserExistence(plateNumber);
        });
    }

    private void checkUserExistence(String plateNumber) {
        supabaseClient.getUserByPlate(plateNumber,
                response -> {
                    try {
                        if (response.length() > 0) {
                            // User exists, save plate and go to home
                            saveUserAndNavigate(plateNumber, true);
                        } else {
                            // New user, go to registration
                            saveUserAndNavigate(plateNumber, false);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        plateInputLayout.setError("An error occurred. Please try again.");
                    }
                },
                error -> plateInputLayout.setError("Connection error. Please try again.")
        );
    }

    private void saveUserAndNavigate(String plateNumber, boolean userExists) {
        SharedPreferences.Editor editor = getSharedPreferences("BisuParking", MODE_PRIVATE).edit();
        editor.putString("plate_number", plateNumber);
        editor.apply();

        Intent intent;
        if (userExists) {
            intent = new Intent(this, HomeActivity.class);
        } else {
            intent = new Intent(this, OwnerTypeActivity.class);
            intent.putExtra("plate_number", plateNumber);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }
}