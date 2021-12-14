package com.michele.myfoodpocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private User userProfile;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null) {
            // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
            database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
            databaseReference = database.getReference();

            databaseReference = database.getReference("User");

            databaseReference.orderByChild("email").equalTo(user.getEmail()).addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { //insieme di risposta
                        if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                            userKey = postSnapshot.getKey();
                            userProfile = postSnapshot.getValue(User.class); // <= reference al nostro oggetto
                        }
                    }

                    TextView tvEmail = (TextView)(findViewById(R.id.profile_email));
                    tvEmail.setText(userProfile.getEmail());
                    TextView tvSex = (TextView)(findViewById(R.id.profile_sex));
                    tvSex.setText(userProfile.getSex());
                    EditText etHeight = (EditText)(findViewById(R.id.profile_height));
                    etHeight.setText(userProfile.getHeight());
                    EditText etWeight = (EditText) (findViewById(R.id.profile_weight));
                    etWeight.setText(userProfile.getWeight());
                    TextView tvBirthdate = (TextView)(findViewById(R.id.profile_birthdate));
                    tvBirthdate.setText(userProfile.getBirthDate());
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
            Toast.makeText(this, "Impossibile recuperare l'utente.", Toast.LENGTH_SHORT).show();
        }


    }

    public void profile_onclick(View view) {
        databaseReference = database.getReference("User");

        String newHeight = ((EditText)(findViewById(R.id.profile_height))).getText().toString();
        String newWeight = ((EditText)(findViewById(R.id.profile_weight))).getText().toString();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(userKey + "/height", newHeight);
        childUpdates.put(userKey + "/weight", newWeight);

        databaseReference.updateChildren(childUpdates);

        Toast.makeText(this, "Modifica effettuata con successo", Toast.LENGTH_SHORT).show();

        Intent newIntent = new Intent(ProfileActivity.this, MainActivity.class);
        startActivity(newIntent);
        finish(); // Kill dell'activity così non può essere ripresa con il back button
    }
}