package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FoodPlanActivity extends AppCompatActivity {

    public ProgressBar pBar;
    private AsyncFoodPlanDownloader asyncFoodPlanDownloader;

    private ArrayList<Meal> meals;

    private boolean firstDownload = true; // Booleano che serve per far partire il thread solo la prima volta che si entra in questa activity (altrimenti il thread parte ad ogni cambiamento dei dati su Firebase)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_plan);
    }

    public void food_plan_button_download(View view) {
        pBar = findViewById(R.id.food_plan_progress_bar);

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
                        asyncFoodPlanDownloader = new AsyncFoodPlanDownloader(pBar, 0, meals);
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