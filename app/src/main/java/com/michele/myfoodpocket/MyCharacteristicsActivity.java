package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
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
        setContentView(R.layout.activity_my_characteristics);

        downloadButton = findViewById(R.id.my_characteristics_food_plan_download_button);
        downloadButton.setClickable(true);

        // Calcolo l'indice di massa corporea
        bodyMassIndex();

        verifyStoragePermissions(MyCharacteristicsActivity.this); // Verifico i permessi per la memorizzazione di file
    }

    // Storage Permissions: da Android 11 in poi è necessario chiedere i permessi oltre che a specificarli nel manifest per poter memorizzare file
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
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
                    textViewBasalMetabolism.setText("" + String.format("%.0f", MainActivity.getBasalMetabolicRate()) + " " + getResources().getString(R.string.unit_of_measure));
                    TextView textViewDailyCalories = findViewById(R.id.characteristics_text_view_daily_calories);
                    textViewDailyCalories.setText("" + String.format("%.0f", MainActivity.getDailyCaloriesNeed()) + " " + getResources().getString(R.string.unit_of_measure));
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

    public void action_button_on_click(View view) {
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

                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { //insieme di risposta
                        if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                            meals.add(postSnapshot.getValue(Meal.class)); // <= reference al nostro oggetto
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
                    // Failed to read value
                    Log.w("DEBUG", "Failed to read value.", error.toException());
                }
            });
        }
    }
}