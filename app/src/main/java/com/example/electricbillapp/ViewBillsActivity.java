package com.example.electricbillapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ViewBillsActivity extends AppCompatActivity {

    private TableLayout tableLayout;
    private String selectedBillId;
    private EditText searchInput;
    private final Handler searchHandler = new Handler();
    private Runnable searchRunnable;

    private static final String BASE_URL = "http://192.168.20.100/electricbillapp/";
    private static final String DELETE_BILL_URL = BASE_URL + "delete_bill.php";
    private static final String VIEW_BILLS_URL = BASE_URL + "view_bills.php";
    private static final String SEARCH_BILLS_URL = BASE_URL + "search_bills.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bills);

        tableLayout = findViewById(R.id.tableLayout);
        searchInput = findViewById(R.id.searchInput);

        // Set up TextWatcher with debounce for search input
        searchInput.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable); // Cancel previous search
                }

                searchRunnable = () -> searchBills(s.toString().trim());
                searchHandler.postDelayed(searchRunnable, 500); // 500ms debounce
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        // Fetch and display all bills initially
        fetchAndDisplayBills();
    }

    private void fetchAndDisplayBills() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, VIEW_BILLS_URL,
                response -> {
                    try {
                        JSONArray billsArray = new JSONArray(response);
                        clearTableRows(); // Clear existing rows
                        if (billsArray.length() == 0) {
                            Toast.makeText(this, "No bills found.", Toast.LENGTH_SHORT).show();
                        }
                        for (int i = 0; i < billsArray.length(); i++) {
                            JSONObject bill = billsArray.getJSONObject(i);
                            String billId = bill.getString("bill_id");
                            String customerId = bill.getString("customer_id");
                            String consumption = bill.getString("consumption");
                            String totalBill = bill.getString("total_bill");
                            String billDate = bill.getString("bill_date");
                            String adminId = bill.getString("admin_id");
                            addRowToTable(billId, customerId, consumption, totalBill, billDate, adminId);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(ViewBillsActivity.this, "Error parsing bills: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ViewBillsActivity.this, "Error fetching bills: " + error.getMessage(), Toast.LENGTH_SHORT).show());

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void addRowToTable(String billId, String customerId, String consumption, String totalBill, String billDate, String adminId) {
        TableRow tableRow = new TableRow(this);
        tableRow.setBackgroundColor(getResources().getColor(android.R.color.black)); // Row border color

        // Create TextViews for each column with consistent styles
        TextView tvBillId = createTextView(billId);
        TextView tvCustomerId = createTextView(customerId);
        TextView tvConsumption = createTextView(consumption);
        TextView tvTotalBill = createTextView(totalBill);
        TextView tvBillDate = createTextView(billDate);
        TextView tvAdminId = createTextView(adminId);

        // Set a long press listener on the table row to show the context menu
        tableRow.setOnLongClickListener(view -> {
            selectedBillId = billId;
            registerForContextMenu(tableRow);
            openContextMenu(view);
            return true;
        });

        // Add TextViews to the row
        tableRow.addView(tvBillId);
        tableRow.addView(tvCustomerId);
        tableRow.addView(tvConsumption);
        tableRow.addView(tvTotalBill);
        tableRow.addView(tvBillDate);
        tableRow.addView(tvAdminId);

        // Add row to the table
        tableLayout.addView(tableRow);
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setPadding(10, 10, 10, 10);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setBackgroundColor(getResources().getColor(android.R.color.white)); // Cell background
        textView.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        ));
        return textView;
    }

    private void clearTableRows() {
        int childCount = tableLayout.getChildCount();
        if (childCount > 1) {
            tableLayout.removeViews(1, childCount - 1); // Keep header row
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, 1, 0, "Update Bill");
        menu.add(0, 2, 0, "Delete Bill");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                updateBill(selectedBillId);
                return true;
            case 2:
                showDeleteConfirmationDialog(selectedBillId);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void updateBill(String billId) {
        Intent intent = new Intent(ViewBillsActivity.this, UpdateBillActivity.class);
        intent.putExtra("BILL_ID", billId);
        startActivity(intent);
    }

    private void deleteBill(String billId) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DELETE_BILL_URL,
                response -> {
                    if (response.trim().equalsIgnoreCase("success")) {
                        Toast.makeText(ViewBillsActivity.this, "Bill deleted successfully", Toast.LENGTH_SHORT).show();
                        fetchAndDisplayBills();
                    } else {
                        Toast.makeText(ViewBillsActivity.this, "Error deleting bill", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(ViewBillsActivity.this, "Error deleting bill: " + error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("bill_id", billId);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(stringRequest);
    }

    private void showDeleteConfirmationDialog(String billId) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("Yes", (dialog, which) -> deleteBill(billId))
                .setNegativeButton("No", null)
                .show();
    }

    private void searchBills(String query) {
        if (!query.isEmpty()) {
            StringRequest stringRequest = new StringRequest(Request.Method.GET, SEARCH_BILLS_URL + "?bill_id=" + query,
                    response -> {
                        try {
                            JSONArray billsArray = new JSONArray(response);
                            clearTableRows(); // Clear existing rows
                            if (billsArray.length() == 0) {
                                Toast.makeText(this, "No bills found for the query.", Toast.LENGTH_SHORT).show();
                            }
                            for (int i = 0; i < billsArray.length(); i++) {
                                JSONObject bill = billsArray.getJSONObject(i);
                                // Convert each value to string explicitly
                                String billId = String.valueOf(bill.getString("bill_id"));
                                String customerId = String.valueOf(bill.getString("customer_id"));
                                String consumption = String.valueOf(bill.getString("consumption"));
                                String totalBill = String.valueOf(bill.getString("total_bill"));
                                String billDate = String.valueOf(bill.getString("bill_date"));
                                String adminId = String.valueOf(bill.getString("admin_id"));
                                addRowToTable(billId, customerId, consumption, totalBill, billDate, adminId);
                            }
                        } catch (JSONException e) {
                            Toast.makeText(ViewBillsActivity.this, "Error parsing search results: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(ViewBillsActivity.this, "Error performing search: " + error.getMessage(), Toast.LENGTH_SHORT).show());

            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(stringRequest);
        } else {
            fetchAndDisplayBills(); // Fetch all bills if the query is empty
        }
    }
}
