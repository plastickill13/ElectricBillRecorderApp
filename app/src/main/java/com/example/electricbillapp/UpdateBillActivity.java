package com.example.electricbillapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateBillActivity extends AppCompatActivity {

    private EditText etCustomerId, etConsumption, etTotalBill, etBillDate;
    private Button btnUpdateBill;

    private static final String UPDATE_BILL_URL = "http://192.168.20.100/electricbillapp/update_bill.php";
    private String billId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_bill);

        // Get billId from intent
        billId = getIntent().getStringExtra("BILL_ID");

        // Initialize views
        etCustomerId = findViewById(R.id.etCustomerId);
        etConsumption = findViewById(R.id.etConsumption);
        etTotalBill = findViewById(R.id.etTotalBill);
        etBillDate = findViewById(R.id.etBillDate);
        btnUpdateBill = findViewById(R.id.btnUpdateBill);

        // Set up a TextWatcher for consumption input
        etConsumption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateTotalBill();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnUpdateBill.setOnClickListener(view -> {
            String customerId = etCustomerId.getText().toString().trim();
            String consumption = etConsumption.getText().toString().trim();
            String totalBill = etTotalBill.getText().toString().trim();
            String billDate = etBillDate.getText().toString().trim();

            if (!isValidDate(billDate)) {
                Toast.makeText(UpdateBillActivity.this, "Invalid date format. Use YYYY-MM-DD.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isValidInput(customerId, consumption, totalBill, billDate)) {
                updateBill(billId, customerId, consumption, totalBill, billDate);
            }
        });
    }

    private boolean isValidInput(String customerId, String consumption, String totalBill, String billDate) {
        if (customerId.isEmpty() || consumption.isEmpty() || totalBill.isEmpty() || billDate.isEmpty()) {
            Toast.makeText(UpdateBillActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate customer ID (e.g., must be numeric)
        if (!customerId.matches("\\d+")) {
            Toast.makeText(UpdateBillActivity.this, "Customer ID must be numeric", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate consumption (e.g., must be a positive number)
        if (!consumption.matches("\\d+(\\.\\d+)?") || Double.parseDouble(consumption) <= 0) {
            Toast.makeText(UpdateBillActivity.this, "Consumption must be a positive number", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Further validation can be added here if needed

        return true;
    }

    private void updateBill(String billId, String customerId, String consumption, String totalBill, String billDate) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPDATE_BILL_URL,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(UpdateBillActivity.this, "Bill updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UpdateBillActivity.this, ViewBillsActivity.class);
                        startActivity(intent); // Start ViewBillActivity
                        finish(); // Close the UpdateBillActivity
                    } else {
                        Toast.makeText(UpdateBillActivity.this, "Error updating bill", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("UpdateBillError", "Error updating bill: " + error.getMessage());
                    Toast.makeText(UpdateBillActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("bill_id", billId);
                params.put("customer_id", customerId);
                params.put("consumption", consumption);
                params.put("total_bill", totalBill);
                params.put("bill_date", billDate);
                
                // Retrieve admin ID from SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                int adminId = sharedPreferences.getInt("adminId", 1); // Use retrieved admin ID
                params.put("admin_id", String.valueOf(adminId)); // Convert to String
                
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    // Method to calculate total bill based on consumption
    private void calculateTotalBill() {
        String consumptionStr = etConsumption.getText().toString().trim();
        if (!consumptionStr.isEmpty()) {
            double consumption = Double.parseDouble(consumptionStr);
            double ratePerKWh = 11.3687; //rate per kWh
            double totalBill = consumption * ratePerKWh;
            etTotalBill.setText(String.format("%.2f", totalBill)); // Format to 2 decimal places
        } else {
            etTotalBill.setText(""); // Clear total bill if consumption is empty
        }
    }

    private boolean isValidDate(String date) {
        // Simple regex to check if the date is in YYYY-MM-DD format
        return date.matches("\\d{4}-\\d{2}-\\d{2}");
    }
}
