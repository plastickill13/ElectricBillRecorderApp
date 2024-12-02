package com.example.electricbillapp;

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

import java.util.HashMap;
import java.util.Map;

public class AddCustomerActivity extends AppCompatActivity {

    private EditText etCustomerName, etCustomerAddress, etCustomerContact;
    private Button btnAddCustomer;

    private static final String ADD_CUSTOMER_URL = "http://192.168.20.100/electricbillapp/insert_customer.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_customer);

        etCustomerName = findViewById(R.id.etCustomerName);
        etCustomerAddress = findViewById(R.id.etCustomerAddress);
        etCustomerContact = findViewById(R.id.etCustomerContact);
        btnAddCustomer = findViewById(R.id.btnAddCustomer);

        btnAddCustomer.setOnClickListener(view -> {
            String name = etCustomerName.getText().toString().trim();
            String address = etCustomerAddress.getText().toString().trim();
            String contact = etCustomerContact.getText().toString().trim();

            if (!isValidInput(name, address, contact)) {
                return;
            }

            addCustomer(name, address, contact);
        });
    }

    private boolean isValidInput(String name, String address, String contact) {
        // Name validation: Allow only letters, spaces, and apostrophes
        if (name.isEmpty()) {
            etCustomerName.setError("Name is required");
            etCustomerName.requestFocus();
            return false;
        }
        if (!name.matches("[a-zA-Z\\s\\']+")) {
            etCustomerName.setError("Name must contain only letters and spaces");
            etCustomerName.requestFocus();
            return false;
        }
        if (name.length() < 3) {
            etCustomerName.setError("Name must be at least 3 characters");
            etCustomerName.requestFocus();
            return false;
        }

        // Address validation
        if (address.isEmpty()) {
            etCustomerAddress.setError("Address is required");
            etCustomerAddress.requestFocus();
            return false;
        }
        if (address.length() < 5) {
            etCustomerAddress.setError("Address must be at least 5 characters");
            etCustomerAddress.requestFocus();
            return false;
        }

        // Contact validation
        if (contact.isEmpty()) {
            etCustomerContact.setError("Contact is required");
            etCustomerContact.requestFocus();
            return false;
        }
        if (!contact.matches("\\d{10,15}")) {
            etCustomerContact.setError("Contact must be 10-15 digits");
            etCustomerContact.requestFocus();
            return false;
        }

        return true;
    }

    private void addCustomer(String name, String address, String contact) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, ADD_CUSTOMER_URL,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(AddCustomerActivity.this, "Customer added successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Go back to the previous activity
                    } else {
                        Toast.makeText(AddCustomerActivity.this, "Error: " + response, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("AddCustomerError", "Error adding customer: " + error.getMessage());
                    Toast.makeText(AddCustomerActivity.this, "Error adding customer: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("customer_name", name);
                params.put("customer_address", address);
                params.put("customer_contact", contact);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }
}
