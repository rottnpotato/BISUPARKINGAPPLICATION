package com.example.bisuparkingapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bisuparkingapplication.network.SupabaseClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private TextView greetingText, lastParkedText, plateNumberText;
    private SupabaseClient supabaseClient;
    private String plateNumber;
    private TextView parkingStatusText;
    private RecyclerView parkingHistoryRecyclerView;
    private ParkingHistoryAdapter parkingHistoryAdapter;
    private TextView emptyStateText;

    private static class ParkingRecord {
        final long timestamp;
        final String parkingSpot;
        final boolean isParked;

        ParkingRecord(long timestamp, String parkingSpot, boolean isParked) {
            this.timestamp = timestamp;
            this.parkingSpot = parkingSpot;
            this.isParked = isParked;
        }
    }

    private static class ParkingHistoryAdapter extends RecyclerView.Adapter<ParkingHistoryAdapter.ViewHolder> {
        private final List<ParkingRecord> parkingRecords = new ArrayList<>();

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView timeText;
            TextView spotText;
            TextView statusText;

            ViewHolder(View view) {
                super(view);
                timeText = view.findViewById(R.id.timeText);
                spotText = view.findViewById(R.id.spotText);
                statusText = view.findViewById(R.id.statusText);

            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.parking_history_item, parent, false);
            return new ViewHolder(view);
        }



        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ParkingRecord record = parkingRecords.get(position);
            holder.timeText.setText(getRelativeTimeSpanString(record.timestamp));
            holder.spotText.setText("Spot: " + record.parkingSpot);
            holder.statusText.setText(record.isParked ? "Parked" : "Exited");
            holder.statusText.setTextColor(record.isParked ?
                    Color.parseColor("#4CAF50") : Color.parseColor("#E91E63"));
        }

        @Override
        public int getItemCount() {
            return parkingRecords.size();
        }

        public void updateParkingRecords(List<ParkingRecord> newRecords) {
            parkingRecords.clear();
            parkingRecords.addAll(newRecords);
            notifyDataSetChanged();
        }

        private CharSequence getRelativeTimeSpanString(long timestamp) {
            return DateUtils.getRelativeTimeSpanString(
                    timestamp,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
            );
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        supabaseClient = SupabaseClient.getInstance(this);
        plateNumber = getSharedPreferences("BisuParking", MODE_PRIVATE)
                .getString("plate_number", "");

        initializeViews();
        setupGreeting();
        loadParkingHistory();
        checkCurrentParkingStatus();
        parkingHistoryRecyclerView = findViewById(R.id.parkingHistoryRecyclerView);
        parkingHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        parkingHistoryAdapter = new ParkingHistoryAdapter();
        parkingHistoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        parkingHistoryRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        parkingHistoryRecyclerView.setAdapter(parkingHistoryAdapter);

        loadParkingHistory();

        findViewById(R.id.parkNowButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, ParkingPageActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        findViewById(R.id.logOutButton).setOnClickListener(v -> {

            new AlertDialog.Builder(this)
                    .setTitle("Log Out")
                    .setMessage("Are you sure you want to LogOut?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        getSharedPreferences("BisuParking", MODE_PRIVATE)
                                .edit()
                                .remove("plate_number")
                                .apply();
                        Log.d("HomeActivity", "Before Log Out, Plate Number: " + getSharedPreferences("BisuParking", MODE_PRIVATE)
                                .getString("plate_number", ""));
                        Intent intent = new Intent(this, PlateNumberActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_out_left, R.anim.slide_in_right);
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();


        });
    }

    private void initializeViews() {
        greetingText = findViewById(R.id.greetingText);
       // lastParkedText = findViewById(R.id.lastParkedText);
        emptyStateText = findViewById(R.id.emptyStateText);
        plateNumberText = findViewById(R.id.plateNumberText);
        plateNumberText.setText("Plate Number: " + plateNumber);
        parkingStatusText = findViewById(R.id.parkingStatusText);
    }

    private void setupGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Good Morning!";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting = "Good Afternoon!";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            greeting = "Good Evening!";
        } else {
            greeting = "Good Night!";
        }

        greetingText.setText(greeting);
    }

    private void loadParkingHistory() {
        supabaseClient.getParkingHistory(plateNumber,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            String jsonData = response.getString("data");
                            JSONArray historyArray = new JSONArray(jsonData);
                            List<ParkingRecord> parkingRecords = new ArrayList<>();

                            for (int i = 0; i < historyArray.length(); i++) {
                                JSONObject record = historyArray.getJSONObject(i);
                                parkingRecords.add(new ParkingRecord(
                                        record.getLong("timestamp"),
                                        record.getString("parking_spot"),
                                        record.getBoolean("is_parked")
                                ));
                            }

                            runOnUiThread(() -> {
                                if (parkingRecords.isEmpty()) {
                                    // Show empty state
                                    parkingHistoryRecyclerView.setVisibility(View.GONE);
                                    // Assuming you have a TextView for empty state
                                    emptyStateText.setVisibility(View.VISIBLE);
                                    emptyStateText.setText("No parking history found");
                                } else {
                                    parkingHistoryRecyclerView.setVisibility(View.VISIBLE);
                                    emptyStateText.setVisibility(View.GONE);
                                    parkingHistoryAdapter.updateParkingRecords(parkingRecords);
                                }
                            });
                        } else {
                            throw new Exception("Failed to load parking history");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            parkingHistoryRecyclerView.setVisibility(View.GONE);
                            emptyStateText.setVisibility(View.VISIBLE);
                            emptyStateText.setText("Error loading parking history");
                        });
                    }
                },
                error -> runOnUiThread(() -> {
                    parkingHistoryRecyclerView.setVisibility(View.GONE);
                    emptyStateText.setVisibility(View.VISIBLE);
                    emptyStateText.setText("Error loading parking history");
                })
        );
    }

    private void checkCurrentParkingStatus() {
        supabaseClient.getParkingStatus(
                response -> {
                    try {
                        boolean isParked = false;
                        String parkedSpot = "";

                        // Iterate through parking spots to find user's vehicle
                        for (Iterator<String> it = response.keys(); it.hasNext(); ) {
                            String spotId = it.next();
                            JSONObject spotData = response.getJSONObject(spotId);
                            String plateNumber = spotData.getString("plate_number");
                            if (plateNumber.equals(this.plateNumber)) {
                                isParked = true;
                                parkedSpot = spotId;
                                break;
                            }
                        }

                        updateParkingStatus(isParked, parkedSpot);
                    } catch (Exception e) {
                        e.printStackTrace();
                        parkingStatusText.setText("Error checking parking status");
                    }
                },
                error -> parkingStatusText.setText("Error checking parking status")
        );
    }

    private void updateParkingStatus(boolean isParked, String spotId) {
        runOnUiThread(() -> {
            if (isParked) {
                parkingStatusText.setText("Currently parked in spot " + spotId);
                parkingStatusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else {
                parkingStatusText.setText("Not currently parked");
                parkingStatusText.setTextColor(getResources().getColor(android.R.color.darker_gray));
            }
        });
    }

    private void updateLastParkedInfo(long timestamp, String spot) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        String dateString = sdf.format(new Date(timestamp));
        lastParkedText.setText("Last parked at " + spot + " on " + dateString);
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkCurrentParkingStatus(); // Refresh status when returning to the activity
        loadParkingHistory();
    }


}
