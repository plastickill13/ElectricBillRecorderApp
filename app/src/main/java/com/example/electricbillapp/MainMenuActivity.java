package com.example.electricbillapp;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainMenuActivity extends AppCompatActivity {

    @Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main_menu);

        Button btnSaveBill = findViewById(R.id.btnSaveBill);
        Button btnViewAll = findViewById(R.id.btnViewAll);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnAddCustomer = findViewById(R.id.btnAddCustomer);

        btnSaveBill.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddBillActivity.class);
            startActivity(intent);
        });

        btnViewAll.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewBillsActivity.class);
            startActivity(intent);
        });


        btnLogout.setOnClickListener(view -> {
            // Clear shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            // Return to login screen
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        btnAddCustomer.setOnClickListener(view -> {
            Intent intent = new Intent(this, AddCustomerActivity.class);
            startActivity(intent);
        });
    }
} 