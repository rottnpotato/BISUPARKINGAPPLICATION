package com.example.bisuparkingapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bisuparkingapplication.network.SupabaseClient;
import org.json.JSONObject;

import java.util.Date;

public class OwnerTypeActivity extends AppCompatActivity {
    private CheckBox studentCheckBox, teacherCheckBox, visitorCheckBox;
    private EditText plateNumberConfirm;
    private Button nextButton;
    private String plateNumber;
    private SupabaseClient supabaseClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.owner_type);

        supabaseClient = SupabaseClient.getInstance(this);
        plateNumber = getIntent().getStringExtra("plate_number");

        initializeViews();
        setupCheckBoxListeners();
        setupNextButton();
    }

    private void initializeViews() {
        studentCheckBox = findViewById(R.id.STUDENT);
        teacherCheckBox = findViewById(R.id.checkBox2);
        visitorCheckBox = findViewById(R.id.checkBox3);
        plateNumberConfirm = findViewById(R.id.editTextText);
        nextButton = findViewById(R.id.button2);

        plateNumberConfirm.setText(plateNumber);
        plateNumberConfirm.setEnabled(false);
    }

    private void setupCheckBoxListeners() {
        CheckBox[] checkBoxes = {studentCheckBox, teacherCheckBox, visitorCheckBox};

        for (CheckBox checkBox : checkBoxes) {
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    for (CheckBox other : checkBoxes) {
                        if (other != buttonView) {
                            other.setChecked(false);
                        }
                    }
                }
                nextButton.setEnabled(studentCheckBox.isChecked() ||
                        teacherCheckBox.isChecked() ||
                        visitorCheckBox.isChecked());
            });
        }
    }

    private void setupNextButton() {
        nextButton.setEnabled(false);
        nextButton.setOnClickListener(v -> {
            String ownerType = getSelectedOwnerType();
            if (ownerType != null) {
                saveUserData(ownerType);
            }
        });
    }

    private String getSelectedOwnerType() {
        if (studentCheckBox.isChecked()) return "STUDENT";
        if (teacherCheckBox.isChecked()) return "TEACHER";
        if (visitorCheckBox.isChecked()) return "VISITOR";
        return null;
    }

    private void saveUserData(String ownerType) {
        try {
            JSONObject userData = new JSONObject();
            userData.put("plate_number", plateNumber);
            userData.put("owner_type", ownerType);
            userData.put("created_at", DateFormat.format("yyyy-MM-dd'T'HH:mm:ss'Z'", new Date()));

            supabaseClient.createUser(userData,
                    response -> {
                        // Check if response indicates success
                        if (response.optBoolean("success", false)) {
                            Intent intent = new Intent(this, VehicleTypeActivity.class);
                            intent.putExtra("plate_number", plateNumber);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                            finish();
                        } else {
                            Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Error saving data", Toast.LENGTH_SHORT).show();
                        Log.e("OwnerError", "Error data: " +
                                (error.networkResponse != null ? new String(error.networkResponse.data) : "No response data"));
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "An error occurred. Please try again.",
                    Toast.LENGTH_SHORT).show();
        }
    }
}