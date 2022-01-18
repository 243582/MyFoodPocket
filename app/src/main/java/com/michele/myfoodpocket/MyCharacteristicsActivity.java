package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import java.util.ArrayList;

public class MyCharacteristicsActivity extends AppCompatActivity {

    private AsyncFoodPlanDownloader asyncFoodPlanDownloader;
    private double bodyMassIndex;
    private boolean firstDownload = true; // Booleano che serve per far partire il thread solo la prima volta che si entra in questa activity (altrimenti il thread parte ad ogni cambiamento dei dati su Firebase)
    private Button downloadButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isNetworkConnected()) {
            Intent newIntentNoConnection = new Intent(MyCharacteristicsActivity.this, NoInternetConnectionActivity.class);
            newIntentNoConnection.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
            startActivity(newIntentNoConnection);
            finish(); // Kill dell'activity così non può essere ripresa con il back button
        }

        setContentView(R.layout.activity_my_characteristics);

        downloadButton = findViewById(R.id.my_characteristics_food_plan_download_button);
        downloadButton.setClickable(true);

        // Calcolo l'indice di massa corporea
        bodyMassIndex();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
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
                        }
                    }

                    double heightMeter = (double)(userCharacteristics.getHeight()) / 100;
                    double heightSquareMeter = heightMeter * heightMeter;
                    bodyMassIndex = (double)(userCharacteristics.getWeight()) / (heightSquareMeter);

                    NumberFormat nf= NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(2);

                    TextView textViewBasalMetabolism = findViewById(R.id.characteristics_text_view_basal_metabolism);
                    textViewBasalMetabolism.setText("" + String.format("%.0f", MainActivity.getBasalMetabolicRate()) + " " + getResources().getString(R.string.unit_of_measure));
                    TextView textViewDailyCalories = findViewById(R.id.characteristics_text_view_daily_calories);
                    textViewDailyCalories.setText("" + String.format("%.0f", MainActivity.getDailyCaloriesNeed()) + " " + getResources().getString(R.string.unit_of_measure));
                    TextView textViewBodyMassIndex = findViewById(R.id.characteristics_text_view_body_mass_index);
                    textViewBodyMassIndex.setText("" + nf.format(bodyMassIndex));
                }

                @Override
                public void onCancelled(DatabaseError error)
                {
                    // Fail nella lettura del profilo
                    Log.w("DEBUG_CHARACTERISTICS", "Fail nella lettura del profilo utente nelle caratteristiche", error.toException());
                }
            });
        }
        else {
            Toast.makeText(this, getResources().getString(R.string.profile_toast_user_fail), Toast.LENGTH_SHORT).show();
        }
    }

    public void actionButtonOnClick(View view) {
        downloadButton.setClickable(false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference databaseReference = database.getReference();
            databaseReference = database.getReference("Meal");

            ArrayList<Meal> meals = new ArrayList<Meal>();

            databaseReference.orderByChild("email").equalTo(user.getEmail()).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    meals.clear();

                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { // Insieme di risposta
                        if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                            meals.add(postSnapshot.getValue(Meal.class)); // <= Reference all'oggetto
                        }
                    }
                    if(firstDownload == true) {
                        asyncFoodPlanDownloader = new AsyncFoodPlanDownloader(meals, getBaseContext());
                        asyncFoodPlanDownloader.execute();
                        firstDownload = false;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("DEBUG_CHARACTERISTICS", "Fail nella lettura dei pasti nelle caratteristiche", error.toException());
                }
            });
        }
    }
}