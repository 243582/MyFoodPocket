package com.michele.myfoodpocket;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.util.Calendar;

public class AddMealActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        // Rilevo la data e la stampo
        TextView tvDate = (TextView)(findViewById(R.id.add_meal_date_print));
        tvDate.setText(": " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "/" +
                (Calendar.getInstance().get(Calendar.MONTH) + 1) + "/" + Calendar.getInstance().get(Calendar.YEAR));

    }
}