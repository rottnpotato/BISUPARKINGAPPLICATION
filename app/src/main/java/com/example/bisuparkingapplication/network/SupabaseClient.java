package com.example.bisuparkingapplication.network;

import android.content.Context;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import com.android.volley.toolbox.StringRequest;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SupabaseClient {
    // Replace these with your actual Supabase credentials
    private static final String SUPABASE_URL = "https://bllqqwpqiezjliwkjkvx.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImJsbHFxd3BxaWV6amxpd2tqa3Z4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzMxMDQzMDcsImV4cCI6MjA0ODY4MDMwN30.qaU65bikMUFM1RV2X2ECqr2gtRa5uHUUbPts3FEivDo";
    private static SupabaseClient instance;
    private RequestQueue requestQueue;

    private SupabaseClient(Context context) {
        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context);
        }
        return instance;
    }

    public void createUser(JSONObject userData, Response.Listener<JSONObject> successListener,
                           Response.ErrorListener errorListener) {
        String url = SUPABASE_URL + "/rest/v1/users";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject result = new JSONObject();
                        result.put("success", true);
                        result.put("data", response);
                        successListener.onResponse(result);
                    } catch (JSONException e) {
                        Log.e("SupabaseClient", "Error creating success response", e);
                        errorListener.onErrorResponse(new VolleyError("Error processing response"));
                    }
                },
                error -> {
                    Log.e("SupabaseClient", "Network error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("SupabaseClient", "Error code: " + error.networkResponse.statusCode);
                        Log.e("SupabaseClient", "Error data: " + new String(error.networkResponse.data));
                    }
                    errorListener.onErrorResponse(error);
                }) {
            @Override
            public byte[] getBody() {
                return userData.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds timeout
                0,     // no retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    public void getUserByPlate(String plateNumber, Response.Listener<JSONObject> successListener,
                               Response.ErrorListener errorListener) {
        String url = SUPABASE_URL + "/rest/v1/users?select=*&plate_number=eq." + plateNumber;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Convert array response to object
                        JSONObject result = new JSONObject();
                        if (response.length() > 0) {
                            result = response.getJSONObject(0);
                        }
                        successListener.onResponse(result);
                    } catch (JSONException e) {
                        Log.e("SupabaseClient", "Error parsing response: " + e.getMessage());
                        errorListener.onErrorResponse(new VolleyError("Error parsing response"));
                    }
                },
                error -> {
                    Log.e("SupabaseClient", "Network error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("SupabaseClient", "Error code: " + error.networkResponse.statusCode);
                        Log.e("SupabaseClient", "Error data: " + new String(error.networkResponse.data));
                    }
                    errorListener.onErrorResponse(error);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_KEY);

                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                10000, // Timeout in MS
                1,     // Max retries
                1.0f   // Backoff multiplier
        ));

        requestQueue.add(request);
    }

    // Method to update user's vehicle type
    public void updateUserVehicle(String plateNumber, JSONObject updateData,
                                  Response.Listener<JSONObject> successListener,
                                  Response.ErrorListener errorListener) {
        String url = SUPABASE_URL + "/rest/v1/users?plate_number=eq." + plateNumber;

        StringRequest request = new StringRequest(Request.Method.PATCH, url,
                response -> {
                    try {
                        JSONObject result = new JSONObject();
                        result.put("success", true);
                        result.put("data", response);
                        successListener.onResponse(result);
                    } catch (JSONException e) {
                        Log.e("SupabaseClient", "Error creating success response", e);
                        errorListener.onErrorResponse(new VolleyError("Error processing response"));
                    }
                },
                error -> {
                    Log.e("SupabaseClient", "Network error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("SupabaseClient", "Error code: " + error.networkResponse.statusCode);
                        Log.e("SupabaseClient", "Error data: " + new String(error.networkResponse.data));
                    }
                    errorListener.onErrorResponse(error);
                }) {
            @Override
            public byte[] getBody() {
                return updateData.toString().getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_KEY);
                headers.put("Content-Type", "application/json");
                headers.put("Prefer", "return=minimal");
                return headers;
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000, // 30 seconds timeout
                0,     // no retries
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }
    // Method to get current parking status of all spots
    public void getParkingStatus(Response.Listener<JSONObject> successListener,
                                 Response.ErrorListener errorListener) {
        String url = SUPABASE_URL + "/rest/v1/parking_status?select=*&is_parked=eq.true";
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Convert JSONArray to JSONObject with parking spot as key
                        JSONObject formattedResponse = new JSONObject();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject parking = response.getJSONObject(i);
                            formattedResponse.put(parking.getString("parking_spot"), parking);
                        }
                        successListener.onResponse(formattedResponse);
                    } catch (JSONException e) {
                        errorListener.onErrorResponse(null);
                    }
                },
                errorListener) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_KEY);
                return headers;
            }
        };
        requestQueue.add(request);
    }

    // Method to get user's last parking information
    public void getParkingHistory(String plateNumber, Response.Listener<JSONObject> successListener,
                                  Response.ErrorListener errorListener) {
        // Remove limit=1 to get all history, keep order by timestamp descending
        String url = SUPABASE_URL + "/rest/v1/parking_status?plate_number=eq." + plateNumber +
                "&order=timestamp.desc";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject result = new JSONObject();
                        result.put("success", true);
                        result.put("data", response);
                        successListener.onResponse(result);
                    } catch (JSONException e) {
                        Log.e("SupabaseClient", "Error creating success response", e);
                        errorListener.onErrorResponse(new VolleyError("Error processing response"));
                    }
                },
                error -> {
                    Log.e("SupabaseClient", "Network error: " + error.toString());
                    if (error.networkResponse != null) {
                        Log.e("SupabaseClient", "Error code: " + error.networkResponse.statusCode);
                        Log.e("SupabaseClient", "Error data: " + new String(error.networkResponse.data));
                    }
                    errorListener.onErrorResponse(error);
                }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("apikey", SUPABASE_KEY);
                headers.put("Authorization", "Bearer " + SUPABASE_KEY);
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                30000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    public void updateParkingStatus(String plateNumber, String parkingSpot, boolean isParked,
                                    Response.Listener<JSONObject> successListener,
                                    Response.ErrorListener errorListener) {
        try {
            String url = SUPABASE_URL + "/rest/v1/parking_status";
            JSONObject parkingData = new JSONObject();

            if (isParked) {
                // For parking: Create new record
                parkingData.put("plate_number", plateNumber);
                parkingData.put("parking_spot", parkingSpot);
                parkingData.put("is_parked", true);
                parkingData.put("timestamp", System.currentTimeMillis());
            } else {
                // For exiting: Update existing record
                url = url + "?plate_number=eq." + plateNumber +
                        "&parking_spot=eq." + parkingSpot +
                        "&is_parked=eq.true";
                parkingData.put("is_parked", false);
            }

            StringRequest request = new StringRequest(
                    isParked ? Request.Method.POST : Request.Method.PATCH,
                    url,
                    response -> {
                        try {
                            // Create a success response object
                            JSONObject result = new JSONObject();
                            result.put("success", true);
                            result.put("data", response);
                            successListener.onResponse(result);
                        } catch (JSONException e) {
                            Log.e("SupabaseClient", "Error creating success response", e);
                            errorListener.onErrorResponse(new VolleyError("Error processing response"));
                        }
                    },
                    errorListener) {
                @Override
                public byte[] getBody() {
                    return parkingData.toString().getBytes(StandardCharsets.UTF_8);
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("apikey", SUPABASE_KEY);
                    headers.put("Authorization", "Bearer " + SUPABASE_KEY);
                    headers.put("Content-Type", "application/json");
                    headers.put("Prefer", "return=minimal");
                    return headers;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000, // 30 seconds timeout
                    0,     // no retries
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e("SupabaseClient", "Error creating request", e);
            errorListener.onErrorResponse(new VolleyError("Error creating request data"));
        }
    }

}