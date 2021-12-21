package com.michele.myfoodpocket;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;

public class MyCharacteristicsActivity extends AppCompatActivity {

    private double bodyMassIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_characteristics);

        // Calcolo l'indice di massa corporea
        bodyMassIndex();
    }

    private void bodyMassIndex() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference databaseReference = database.getReference();
            databaseReference = database.getReference("User");

            databaseReference.orderByChild("email").equalTo(user.getEmail()).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    User userCharacteristics = null;

                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { //insieme di risposta
                        if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                            userCharacteristics = postSnapshot.getValue(User.class); // <= reference al nostro oggetto
                            Log.d("DEBUGPROFILE", postSnapshot.getValue().toString());
                        }
                    }

                    double heightMeter = (double)(userCharacteristics.getHeight()) / 100;
                    double heightSquareMeter = heightMeter * heightMeter;
                    bodyMassIndex = (double)(userCharacteristics.getWeight()) / (heightSquareMeter);

                    NumberFormat nf= NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(2);

                    TextView textViewBasalMetabolism = findViewById(R.id.characteristics_text_view_basal_metabolism);
                    textViewBasalMetabolism.setText("" + String.format("%.0f", MainActivity.getBasalMetabolicRate()) + getResources().getString(R.string.unit_of_measure));
                    TextView textViewDailyCalories = findViewById(R.id.characteristics_text_view_daily_calories);
                    textViewDailyCalories.setText("" + String.format("%.0f", MainActivity.getDailyCaloriesNeed()) + getResources().getString(R.string.unit_of_measure));
                    TextView textViewBodyMassIndex = findViewById(R.id.characteristics_text_view_body_mass_index);
                    textViewBodyMassIndex.setText("" + nf.format(bodyMassIndex));
                }

                @Override
                public void onCancelled(DatabaseError error)
                {
                    // Failed to read value
                    Log.w("DEBUG", "Failed to read value.", error.toException());
                }
            });
        }
        else {
            Toast.makeText(this, getResources().getString(R.string.profile_toast_user_fail), Toast.LENGTH_SHORT).show();
        }
    }
}