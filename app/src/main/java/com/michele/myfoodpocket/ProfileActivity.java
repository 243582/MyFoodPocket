package com.michele.myfoodpocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
                            Log.d("DEBUGPROFILE", postSnapshot.getValue().toString());
                        }
                    }

                    TextView tvEmail = (TextView)(findViewById(R.id.profile_email));
                    tvEmail.setText(getResources().getString(R.string.profile_textview_email) + " " + userProfile.getEmail());
                    TextView tvSex = (TextView)(findViewById(R.id.profile_sex));
                    String tvSexString = (userProfile.getSex() == 1 ? getResources().getString(R.string.sex_1) : getResources().getString(R.string.sex_2));
                    tvSex.setText(tvSexString);
                    EditText etHeight = (EditText)(findViewById(R.id.profile_height));
                    etHeight.setText("" + userProfile.getHeight());
                    EditText etWeight = (EditText) (findViewById(R.id.profile_weight));
                    etWeight.setText("" + userProfile.getWeight());
                    TextView tvBirthdate = (TextView)(findViewById(R.id.profile_birthdate));
                    tvBirthdate.setText(getResources().getString(R.string.profile_textview_birth_date) + " " + userProfile.getBirthDate());
                    Spinner spinnerSportFrequency = (Spinner)(findViewById(R.id.profile_spinner_sport_frequency));
                    spinnerSportFrequency.setSelection(userProfile.getSportFrequency()-1); // Sport: 1 leggero, 2 moderato, 3 pesante. Il -1 è per l'indice degli item dello spinner che partono da 0
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

    public void profileOnClick(View view) {
        databaseReference = database.getReference("User");

        int newHeight = Integer.parseInt(((EditText)(findViewById(R.id.profile_height))).getText().toString());
        float newWeight = Float.parseFloat(((EditText)(findViewById(R.id.profile_weight))).getText().toString());
        int newSportFrequency = ((Spinner)(findViewById(R.id.profile_spinner_sport_frequency))).getSelectedItemPosition() + 1; // Sport: 1 leggero, 2 moderato, 3 pesante. Il +1 è per l'indice degli item dello spinner che partono da 0


        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(userKey + "/height", newHeight);
        childUpdates.put(userKey + "/weight", newWeight);
        childUpdates.put(userKey + "/sportFrequency", newSportFrequency);

        databaseReference.updateChildren(childUpdates);

        Toast.makeText(this, getResources().getString(R.string.profile_toast_modify_success), Toast.LENGTH_SHORT).show();

        Intent newIntent = new Intent(ProfileActivity.this, MainActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Kill di tutte le activity nello stack tranne l'ultima
        startActivity(newIntent);
        finish(); // Kill dell'activity così non può essere ripresa con il back button
    }
}