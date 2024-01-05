package com.mesbahi.crudapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;

public class NewConsumptionActivity extends AppCompatActivity {
    private ConsumptionAdapter consumptionAdapter;
    User loggedInUser = SessionManager.getInstance().getCurrentUser();

    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_consumption);

        dbHelper = new DBHelper(this);

        // Find the UI elements by their IDs
        //EditText etUserId = findViewById(R.id.etUserId);
        EditText etYear = findViewById(R.id.etYear);
        EditText etConsumptionValue = findViewById(R.id.etConsumptionValue);
        Spinner spinnerMonth = findViewById(R.id.spinnerMonth);
        Button btnAddConsumption = findViewById(R.id.btnAddConsumption);

        // Set up the spinner for months
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.months_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(adapter);

        // Set the year field to the current year and make it non-editable
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        etYear.setText(String.valueOf(currentYear));
        etYear.setEnabled(false); // Make it non-editable

        // Set default selected month
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH);
        spinnerMonth.setSelection(currentMonth);

        // Set an OnClickListener for the Add Consumption button
        btnAddConsumption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieve values from the input fields
                long userId = loggedInUser.getId();
                int year = currentYear; // Use the current year set above
                int month = spinnerMonth.getSelectedItemPosition() + 1; // Add 1 because months are 1-indexed
                double consumptionValue = Double.parseDouble(etConsumptionValue.getText().toString());

                // Check if the consumption already exists for the specified month and year
                if (dbHelper.isConsumptionExists(userId, month, year)) {
                    // Display an error message
                    Toast.makeText(NewConsumptionActivity.this, "Consumption already exists for the selected month and year", Toast.LENGTH_SHORT).show();
                } else {
                    // Add the electricity consumption to the database
                    long id = dbHelper.addElectricityConsumption(userId, month, year, consumptionValue);

                    if (id != -1) {
                        // Successful insertion
                        Toast.makeText(NewConsumptionActivity.this, "Electricity consumption added successfully", Toast.LENGTH_SHORT).show();
                        loadConsumptionsForUser(1,0);
                        finish(); // Close the activity after adding consumption
                    } else {
                        // Failed insertion
                        Toast.makeText(NewConsumptionActivity.this, "Failed to add electricity consumption", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

    }

    private void loadConsumptionsForUser(int userId, int selectedYear) {
        Cursor cursor;

        if (selectedYear == 0) {
            // If "All" is selected, load all consumptions for the user
            cursor = dbHelper.getAllElectricityConsumptionsForUser(userId);
        } else {
            // If a specific year is selected, load consumptions for that year
            cursor = dbHelper.getElectricityConsumptionsForUserAndYear(userId, selectedYear);
        }

        consumptionAdapter.swapCursor(cursor);
    }
}