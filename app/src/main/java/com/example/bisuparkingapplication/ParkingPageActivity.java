package com.example.bisuparkingapplication;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bisuparkingapplication.network.SupabaseClient;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ParkingPageActivity extends AppCompatActivity {
    private SupabaseClient supabaseClient;
    private Map<String, MaterialButton> parkingSpots;
    private Map<String, String> occupiedSpots;
    private String userPlateNumber;
    private String currentParkedSpot;
    private Timer updateTimer;
    private MaterialButton homeButton;
    private static final int UPDATE_INTERVAL = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parking_page);

        supabaseClient = SupabaseClient.getInstance(this);
        userPlateNumber = getSharedPreferences("BisuParking", MODE_PRIVATE)
                .getString("plate_number", "");

        initializeParkingSpots();
        homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        startPeriodicUpdates();
    }

    private void initializeParkingSpots() {
        parkingSpots = new HashMap<>();
        occupiedSpots = new HashMap<>();

        // Initialize left column spots
        String[] leftSpots = {"L1", "L2", "L3", "L4", "L5", "L6", "L7", "L8", "L9"};
        int[] leftButtonIds = {R.id.button5, R.id.button6, R.id.button8, R.id.button10,
                R.id.button12, R.id.button14, R.id.button16, R.id.button18,
                R.id.button19};

        // Initialize right column spots
        String[] rightSpots = {"R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9"};
        int[] rightButtonIds = {R.id.button4, R.id.button7, R.id.button9, R.id.button11,
                R.id.button13, R.id.button15, R.id.button17, R.id.button20,
                R.id.button21};

        // Set up left column
        for (int i = 0; i < leftSpots.length; i++) {
            MaterialButton spot = findViewById(leftButtonIds[i]);
            spot.setText(leftSpots[i]);
            final String spotId = leftSpots[i];
            spot.setOnClickListener(v -> handleSpotClick(spotId));
            parkingSpots.put(spotId, spot);
        }

        // Set up right column
        for (int i = 0; i < rightSpots.length; i++) {
            MaterialButton spot = findViewById(rightButtonIds[i]);
            spot.setText(rightSpots[i]);
            final String spotId = rightSpots[i];
            spot.setOnClickListener(v -> handleSpotClick(spotId));
            parkingSpots.put(spotId, spot);
        }

        // Initial load of parking status
        loadParkingStatus();
    }

    private void startPeriodicUpdates() {
        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> loadParkingStatus());
            }
        }, UPDATE_INTERVAL, UPDATE_INTERVAL);
    }

    private void loadParkingStatus() {
        supabaseClient.getParkingStatus(
                response -> {
                    try {
                        updateParkingDisplay(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(this, "Error updating parking status",
                        Toast.LENGTH_SHORT).show()
        );
    }

    private void updateParkingDisplay(JSONObject response) throws JSONException {
        // Clear previous status
        occupiedSpots.clear();
        for (MaterialButton button : parkingSpots.values()) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    Color.parseColor("#4CAF50"))); // Green
        }

        // Update with new status
        for (String spotId : parkingSpots.keySet()) {
            if (response.has(spotId)) {
                JSONObject spotData = response.getJSONObject(spotId);
                String plateNumber = spotData.getString("plate_number");
                occupiedSpots.put(spotId, plateNumber);

                MaterialButton spotButton = parkingSpots.get(spotId);
                spotButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        Color.parseColor("#E91E63"))); // Red

                if (plateNumber.equals(userPlateNumber)) {
                    currentParkedSpot = spotId;
                }
            }
        }
    }

    private void handleSpotClick(String spotId) {
        MaterialButton clickedButton = parkingSpots.get(spotId);

        if (occupiedSpots.containsKey(spotId)) {
            String plateNumber = occupiedSpots.get(spotId);
            if (plateNumber.equals(userPlateNumber)) {
                showExitDialog(spotId);
            } else {
                showOccupiedDialog(plateNumber);
            }
        } else {
            if (currentParkedSpot == null) {
                showParkingDialog(spotId);
            } else {
                Toast.makeText(this, "You are already parked in spot " + currentParkedSpot,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showParkingDialog(String spotId) {
        new AlertDialog.Builder(this)
                .setTitle("Park Here?")
                .setMessage("Would you like to park in spot " + spotId + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    parkInSpot(spotId);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void showExitDialog(String spotId) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_exit_parking, null);
        TextView plateText = dialogView.findViewById(R.id.plateNumberText);
        plateText.setText("Your vehicle (" + userPlateNumber + ")");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.exitButton).setOnClickListener(v -> {
            exitFromSpot(spotId);
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.cancelButton).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showOccupiedDialog(String plateNumber) {
        // Show only last 4 characters of plate number
        String maskedPlate = "****" + plateNumber.substring(Math.max(0, plateNumber.length() - 4));

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_occupied_spot, null);
        TextView occupiedText = dialogView.findViewById(R.id.occupiedText);
        occupiedText.setText("This spot is occupied by\n" + maskedPlate);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.okButton).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void parkInSpot(String spotId) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Processing parking request...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        supabaseClient.updateParkingStatus(userPlateNumber, spotId, true,
                response -> {
                    loadingDialog.dismiss();
                    if (response.optBoolean("success", false)) {
                        // Update local state
                        currentParkedSpot = spotId;
                        occupiedSpots.put(spotId, userPlateNumber);
                        MaterialButton spotButton = parkingSpots.get(spotId);
                        if (spotButton != null) {
                            runOnUiThread(() -> {
                                spotButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                        Color.parseColor("#E91E63"))); // Red
                                // Show success dialog and redirect
                                showParkingSuccessDialog(spotId);
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            new AlertDialog.Builder(ParkingPageActivity.this)
                                    .setTitle("Error")
                                    .setMessage("Failed to update parking status. Please try again.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
                    }
                },
                error -> {
                    loadingDialog.dismiss();
                    Log.e("ParkingPage", "Error in parkInSpot", error);

                    runOnUiThread(() -> {
                        String errorMessage = "Error parking in spot";
                        if (error.networkResponse != null) {
                            try {
                                String errorData = new String(error.networkResponse.data);
                                Log.e("ParkingPage", "Error response: " + errorData);
                                JSONObject errorJson = new JSONObject(errorData);
                                if (errorJson.has("message")) {
                                    errorMessage = errorJson.getString("message");
                                }
                            } catch (Exception e) {
                                Log.e("ParkingPage", "Error parsing error response", e);
                            }
                        }

                        new AlertDialog.Builder(ParkingPageActivity.this)
                                .setTitle("Error")
                                .setMessage(errorMessage + "\nPlease try again.")
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
        );
    }

    private void exitFromSpot(String spotId) {
        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(this)
                .setMessage("Processing exit...")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        supabaseClient.updateParkingStatus(userPlateNumber, spotId, false,
                response -> {
                    loadingDialog.dismiss();
                    if (response.optBoolean("success", false)) {
                        // Update local state
                        currentParkedSpot = null;
                        occupiedSpots.remove(spotId);
                        MaterialButton spotButton = parkingSpots.get(spotId);
                        if (spotButton != null) {
                            runOnUiThread(() -> {
                                spotButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                                        Color.parseColor("#4CAF50"))); // Green
                                // Show success dialog and redirect
                                showExitSuccessDialog(spotId);
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            new AlertDialog.Builder(ParkingPageActivity.this)
                                    .setTitle("Error")
                                    .setMessage("Failed to update parking status. Please try again.")
                                    .setPositiveButton("OK", null)
                                    .show();
                        });
                    }
                },
                error -> {
                    loadingDialog.dismiss();
                    Log.e("ParkingPage", "Error in exitFromSpot", error);

                    runOnUiThread(() -> {
                        String errorMessage = "Error exiting parking spot";
                        if (error.networkResponse != null) {
                            try {
                                String errorData = new String(error.networkResponse.data);
                                Log.e("ParkingPage", "Error response: " + errorData);
                                JSONObject errorJson = new JSONObject(errorData);
                                if (errorJson.has("message")) {
                                    errorMessage = errorJson.getString("message");
                                }
                            } catch (Exception e) {
                                Log.e("ParkingPage", "Error parsing error response", e);
                            }
                        }

                        new AlertDialog.Builder(ParkingPageActivity.this)
                                .setTitle("Error")
                                .setMessage(errorMessage + "\nPlease try again.")
                                .setPositiveButton("OK", null)
                                .show();
                    });
                }
        );
    }

    private void showParkingSuccessDialog(String spotId) {
        new AlertDialog.Builder(this)
                .setTitle("Successfully Parked!")
                .setMessage("Your vehicle has been parked in spot " + spotId)
                .setPositiveButton("Go to Home", (dialogInterface, i) -> {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(true)
                .create()
                .show();
    }

    private void showExitSuccessDialog(String spotId) {
        new AlertDialog.Builder(this)
                .setTitle("Successfully Exited!")
                .setMessage("Your vehicle has exited from spot " + spotId)
                .setPositiveButton("Go to Home", (dialogInterface, i) -> {
                    Intent intent = new Intent(this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(true)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (updateTimer != null) {
            updateTimer.cancel();
        }
    }
}