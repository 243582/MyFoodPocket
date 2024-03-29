package com.michele.myfoodpocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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
    private EditText editTextWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isNetworkConnected()) {
            Intent newIntentNoConnection = new Intent(ProfileActivity.this, NoInternetConnectionActivity.class);
            newIntentNoConnection.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
            startActivity(newIntentNoConnection);
            finish(); // Kill dell'activity così non può essere ripresa con il back button
        }

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
                    String tvSexString = (userProfile.getSex() == 1 ? getResources().getString(R.string.sex_1) : getResources().getString(R.string.sex_2));
                    tvSex.setText(tvSexString);
                    TextView tvHeight = (TextView)(findViewById(R.id.profile_height));
                    tvHeight.setText("" + userProfile.getHeight());
                    editTextWeight = (EditText) (findViewById(R.id.profile_weight));
                    editTextWeight.setText("" + userProfile.getWeight());
                    TextView tvBirthdate = (TextView)(findViewById(R.id.profile_birthdate));
                    tvBirthdate.setText(userProfile.getBirthDate());
                    Spinner spinnerWorkHeaviness = (Spinner)(findViewById(R.id.profile_spinner_work_heaviness));
                    spinnerWorkHeaviness.setSelection(userProfile.getWorkHeaviness()-1); // Lavoro: 1 leggero, 2 moderato, 3 pesante. Il -1 è per l'indice degli item dello spinner che partono da 0
                    Spinner spinnerSportPracticed = (Spinner)(findViewById(R.id.profile_spinner_sport_practiced));
                    int sportPracticedInt = (userProfile.getSportPracticed() == true ? 0 : 1);
                    spinnerSportPracticed.setSelection(sportPracticedInt);
                }

                @Override
                public void onCancelled(DatabaseError error)
                {
                    Log.w("DEBUG_PROFILE", "Fail nella lettura del profilo utente", error.toException());
                }
            });
        }
        else {
            Toast.makeText(this, getResources().getString(R.string.profile_toast_user_fail), Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void profileOnClick(View view) {
        if(isNetworkConnected()) {
            if(checkInputOk()) {
                databaseReference = database.getReference("User");

                String stringWeight = ((EditText)(findViewById(R.id.profile_weight))).getText().toString();
                String formattedWeight; // Stringa che userò per rimuovere l'eventuale virgola inserita dall'utente al posto del punto
                if(stringWeight.contains(","))
                    formattedWeight = stringWeight.replace(",", ".");
                else
                    formattedWeight = stringWeight;
                float newWeight = Float.parseFloat(formattedWeight);
                int newWorkheaviness = ((Spinner)(findViewById(R.id.profile_spinner_work_heaviness))).getSelectedItemPosition() + 1; // Lavoro: 1 leggero, 2 moderato, 3 pesante. Il +1 è per l'indice degli item dello spinner che partono da 0
                int newSportPracticedInt = ((Spinner)(findViewById(R.id.profile_spinner_sport_practiced))).getSelectedItemPosition();
                boolean newSportPracticedBool = (newSportPracticedInt == 0 ? true : false);

                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(userKey + "/weight", newWeight);
                childUpdates.put(userKey + "/workHeaviness", newWorkheaviness);
                childUpdates.put(userKey + "/sportPracticed", newSportPracticedBool);

                databaseReference.updateChildren(childUpdates);

                Toast.makeText(this, getResources().getString(R.string.profile_toast_modify_success), Toast.LENGTH_SHORT).show();

                Intent newIntent = new Intent(ProfileActivity.this, MainActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity pregresse
                startActivity(newIntent);
                finish(); // Kill dell'activity così non può essere ripresa con il back button
            }
            else
                Toast.makeText(getBaseContext(), getResources().getString(R.string.profile_check_input_not_ok), Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.no_internet_connection_still_not_available), Toast.LENGTH_SHORT).show();
    }

    private boolean checkInputOk() {
        if(!editTextWeight.getText().toString().isEmpty() && !editTextWeight.getText().toString().contains("-") &&
                !editTextWeight.getText().toString().contains(" ") && !editTextWeight.getText().toString().contains(",")) {

            // Controllo che il formato numerico del peso sia corretto
            try {
                Double.parseDouble(editTextWeight.getText().toString());
                return true;
            } catch(NumberFormatException e){
                return false;
            }
        }
        else
            return false;
    }

    public void backButtonOnClick(View view) {
        super.onBackPressed();
    }
}