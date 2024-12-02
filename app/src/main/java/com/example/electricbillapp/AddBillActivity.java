package com.example.electricbillapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddBillActivity extends AppCompatActivity {

    private EditText etCustomerId, etConsumption;
    private Button btnAddBill;

    private static final String ADD_BILL_URL = "http://192.168.20.100/electricbillapp/insert_bill.php";
    private int adminId; // Store admin ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        etCustomerId = findViewById(R.id.etCustomerId);
        etConsumption = findViewById(R.id.etConsumption);
        btnAddBill = findViewById(R.id.btnAddBill);

        // Get Admin ID from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        adminId = sharedPreferences.getInt("adminId", -1); // Default to -1 if not found

        if (adminId == -1) {
            Toast.makeText(this, "Admin not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnAddBill.setOnClickListener(view -> {
            String customerId = etCustomerId.getText().toString().trim();
            String consumption = etConsumption.getText().toString().trim();

            if (!isValidInput(customerId, consumption)) {
                return;
            }

            addBill(customerId, consumption);
        });
    }

    private boolean isValidInput(String customerId, String consumption) {
        // Customer ID validation
        if (customerId.isEmpty()) {
            etCustomerId.setError("Customer ID is required");
            etCustomerId.requestFocus();
            return false;
        }
        if (!customerId.matches("\\d+")) {
            etCustomerId.setError("Customer ID must be a valid number");
            etCustomerId.requestFocus();
            return false;
        }

        // Consumption validation
        if (consumption.isEmpty()) {
            etConsumption.setError("Consumption is required");
            etConsumption.requestFocus();
            return false;
        }
        if (!consumption.matches("\\d+(\\.\\d{1,2})?")) { // Allow only up to 2 decimal places
            etConsumption.setError("Consumption must be a valid number with up to 2 decimal places");
            etConsumption.requestFocus();
            return false;
        }

        return true;
    }

    private void addBill(String customerId, String consumption) {
        String billDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // First, check if the customer ID is valid
        checkCustomerId(customerId, valid -> {
            if (!valid) {
                Toast.makeText(AddBillActivity.this, "No customer found with the provided ID", Toast.LENGTH_SHORT).show();
                return; // Exit if customer ID is invalid
            }

            // Proceed to add the bill if customer ID is valid
            StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_BILL_URL,
                    response -> {
                        if (response.trim().equalsIgnoreCase("success")) {
                            Toast.makeText(AddBillActivity.this, "Bill added successfully", Toast.LENGTH_SHORT).show();
                            finish(); // Go back to the previous activity
                        } else {
                            Toast.makeText(AddBillActivity.this, "Error: " + response, Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("AddBillError", "Error adding bill: " + error.getMessage());
                        Toast.makeText(AddBillActivity.this, "Error adding bill: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("customer_id", customerId);
                    params.put("consumption", consumption);
                    params.put("admin_id", String.valueOf(adminId)); // Auto-filled admin_id
                    params.put("bill_date", billDate);
                    return params;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(stringRequest);
        });
    }

    // New method to check if customer ID is valid
    private void checkCustomerId(String customerId, CustomerIdCallback callback) {
        String url = "http://192.168.20.100/electricbillapp/check_customer.php"; // URL to check customer ID
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    // Assuming the server returns "exists" if the customer ID is valid
                    callback.onResult(response.trim().equalsIgnoreCase("exists"));
                },
                error -> {
                    Log.e("CheckCustomerError", "Error checking customer ID: " + error.getMessage());
                    callback.onResult(false); // Assume invalid if there's an error
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("customer_id", customerId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    // Callback interface for customer ID check
    private interface CustomerIdCallback {
        void onResult(boolean valid);
    }
}
