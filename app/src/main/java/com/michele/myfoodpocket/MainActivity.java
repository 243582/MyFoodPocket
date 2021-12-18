package com.michele.myfoodpocket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.michele.myfoodpocket.databinding.ActivityMainBinding;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private String userKey;
    private User userInfo;

    private double basalMetabolicRate = 0;
    private double dailyCaloriesNeed = 0;

    static final int MIN_AGE = 18;
    static final int MEDIUM_AGE_1 = 30;
    static final int MEDIUM_AGE_2 = 60;
    static final int MAX_AGE = 75;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Barra di supporto
        setSupportActionBar(binding.appBarMain.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View navHeader = navigationView.inflateHeaderView(R.layout.nav_header_main) ;

        navigationView.getMenu().findItem(R.id.nav_profile).setOnMenuItemClickListener(this);
        navigationView.getMenu().findItem(R.id.nav_results).setOnMenuItemClickListener(this);
        navigationView.getMenu().findItem(R.id.nav_exit).setOnMenuItemClickListener(this);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_profile, R.id.nav_results, R.id.nav_exit)
                .setOpenableLayout(drawer)
                .build();

        user = FirebaseAuth.getInstance().getCurrentUser();
        TextView tv = navHeader.findViewById(R.id.nav_header_main_email);
        tv.setText(user.getEmail());

        // Calcolo apporto calorico giornaliero
        dailyCalories();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        binding.drawerLayout.openDrawer(GravityCompat.START);
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        Intent newIntent;
        switch(menuItem.getItemId()) {
            case R.id.nav_profile:
                newIntent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(newIntent);
                break;
            case R.id.nav_results:
                newIntent = new Intent(MainActivity.this, ResultsActivity.class);
                startActivity(newIntent);
                break;
            case R.id.nav_exit:
                mAuth.signOut();
                newIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(newIntent);
                finish(); // Kill dell'activity così non può essere ripresa con il back button
                break;
        }
        return false;
    }

    private int getAge(int day, int month, int year){
        Calendar dob = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dob.set(year, month, day);

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
            age--;
        }

        Integer ageInt = new Integer(age);
        String ageS = ageInt.toString();

        return Integer.parseInt(ageS);
    }

    public void dailyCalories()
    {
        /* Per calcolare le calorie giornaliere viene applicata la formula del metabolismo basale
           moltiplicata per il LAF (Livello Attività Fisica).
           Riferimento: https://www.my-personaltrainer.it/calcolo-calorie.html
         */

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
                    Log.d("DEBUGMAIN", "DENTRO3");
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { //insieme di risposta
                        if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                            userKey = postSnapshot.getKey();
                            userInfo = postSnapshot.getValue(User.class); // <= reference al nostro oggetto
                            Log.d("DEBUGMAIN", userInfo.toString());
                        }
                    }

                    int birthdayDay = Integer.parseInt(userInfo.getBirthDate().split("/")[0]);
                    int birthdayMonth = Integer.parseInt(userInfo.getBirthDate().split("/")[1]);
                    int birthdayYear = Integer.parseInt(userInfo.getBirthDate().split("/")[2]);
                    int age = getAge(birthdayDay, birthdayMonth, birthdayYear);
                    basalMetabolicRate = 0;
                    dailyCaloriesNeed = 0;
                    TextView tvBasalMetabolicRate = findViewById(R.id.main_text_view_basal_metabolic_rate);
                    TextView tvCalories = findViewById(R.id.main_text_view_daily_calories);

                    // Se l'utente è maschio
                    if(userInfo.getSex() == 1) {
                        if(age >= MIN_AGE && age <= MEDIUM_AGE_2 - 1) {
                            if(age <= MEDIUM_AGE_1 - 1)
                                basalMetabolicRate = 15.3 * userInfo.getWeight() + 679;
                            else
                                basalMetabolicRate = 11.6 * userInfo.getWeight() + 879;
                            String basalMetabolicRatePrint = String.format("%.0f", basalMetabolicRate);
                            tvBasalMetabolicRate.setText(getResources().getString(R.string.basal_metabolic_rate) + ": " + basalMetabolicRatePrint + " " + getResources().getString(R.string.unit_of_measure));
                            if(userInfo.getWorkHeaviness() == 1) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.55;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.41;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                            }
                            else if(userInfo.getWorkHeaviness() == 2) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.78;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.70;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                            }
                            else if(userInfo.getWorkHeaviness() == 3) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 2.10;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 2.01;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                            }
                        }
                        else if(age >= MEDIUM_AGE_2 && age <= MAX_AGE-1) {
                            basalMetabolicRate = 11.9 * userInfo.getWeight() + 700;
                            String basalMetabolicRatePrint = String.format("%.0f", basalMetabolicRate);
                            tvBasalMetabolicRate.setText(getResources().getString(R.string.basal_metabolic_rate) + ": " + basalMetabolicRatePrint + " " + getResources().getString(R.string.unit_of_measure));
                            if(userInfo.getSportPracticed() == true) {
                                dailyCaloriesNeed = basalMetabolicRate * 1.51;
                                String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                            }
                            else {
                                dailyCaloriesNeed = basalMetabolicRate * 1.40;
                                String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                            }
                        }
                        else if(age >= MAX_AGE) {
                            basalMetabolicRate = 8.4 * userInfo.getWeight() + 819;
                            String basalMetabolicRatePrint = String.format("%.0f", basalMetabolicRate);
                            tvBasalMetabolicRate.setText(getResources().getString(R.string.basal_metabolic_rate) + ": " + basalMetabolicRatePrint + " " + getResources().getString(R.string.unit_of_measure));
                            if(userInfo.getSportPracticed() == true) {
                                dailyCaloriesNeed = basalMetabolicRate * 1.51;
                                String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                            }
                            else {
                                dailyCaloriesNeed = basalMetabolicRate * 1.33;
                                String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                            }
                        }
                        else {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.main_toast_error_age), Toast.LENGTH_SHORT).show();
                        }
                    }
                    // Se l'utente è femmina
                    else {
                        if(age >= MIN_AGE && age <= MEDIUM_AGE_2 - 1) {
                            if(age <= MEDIUM_AGE_1 - 1)
                                basalMetabolicRate = 14.7 * userInfo.getWeight() + 496;
                            else
                                basalMetabolicRate = 8.7 * userInfo.getWeight() + 829;
                            String basalMetabolicRatePrint = String.format("%.0f", basalMetabolicRate);
                            tvBasalMetabolicRate.setText(getResources().getString(R.string.basal_metabolic_rate) + ": " + basalMetabolicRatePrint + " " + getResources().getString(R.string.unit_of_measure));
                            if(userInfo.getWorkHeaviness() == 1) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.56;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.42;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                            }
                            else if(userInfo.getWorkHeaviness() == 2) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.64;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.56;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                            }
                            else if(userInfo.getWorkHeaviness() == 3) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.82;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.73;
                                    String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                    tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                                }
                            }
                        }
                        else if(age >= MEDIUM_AGE_2 && age <= MAX_AGE-1) {
                            basalMetabolicRate = 9.2 * userInfo.getWeight() + 688;
                            String basalMetabolicRatePrint = String.format("%.0f", basalMetabolicRate);
                            tvBasalMetabolicRate.setText(getResources().getString(R.string.basal_metabolic_rate) + ": " + basalMetabolicRatePrint + " " + getResources().getString(R.string.unit_of_measure));
                            if(userInfo.getSportPracticed() == true) {
                                dailyCaloriesNeed = basalMetabolicRate * 1.56;
                                String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                            }
                            else {
                                dailyCaloriesNeed = basalMetabolicRate * 1.44;
                                String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                            }
                        }
                        else if(age >= MAX_AGE) {
                            basalMetabolicRate = 9.8 * userInfo.getWeight() + 624;
                            String basalMetabolicRatePrint = String.format("%.0f", basalMetabolicRate);
                            tvBasalMetabolicRate.setText(getResources().getString(R.string.basal_metabolic_rate) + ": " + basalMetabolicRatePrint + " " + getResources().getString(R.string.unit_of_measure));
                            if(userInfo.getSportPracticed() == true) {
                                dailyCaloriesNeed = basalMetabolicRate * 1.56;
                                String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                            }
                            else {
                                dailyCaloriesNeed = basalMetabolicRate * 1.37;
                                String dailyCaloriesPrint = String.format("%.0f", dailyCaloriesNeed);
                                tvCalories.setText(getResources().getString(R.string.daily_calories_need) + ": " + dailyCaloriesPrint + " " + getResources().getString(R.string.unit_of_measure));
                            }
                        }
                        else {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.main_toast_error_age), Toast.LENGTH_SHORT).show();
                        }
                    }

                    // Una volta calcolato metabolismo basale e apporto calorico giornaliero, vengono segnalati i progressi giornalieri dell'utente
                    ProgressBar pb = findViewById(R.id.main_progress_bar);
                    TextView tvCaloriesTaken = findViewById(R.id.main_text_view_calories_taken);
                    tvCaloriesTaken.setText(tvCaloriesTaken.getText() + ": " + 50); // 50 provvisorio, da sostituire poi
                    pb.setProgress((int)(((2000 * 100) / dailyCaloriesNeed)));

                }

                @Override
                public void onCancelled(DatabaseError error)
                {
                    // Failed to read value
                    Log.w("DEBUG", "Failed to read value...", error.toException());
                }
            });
        }
        else {
            Toast.makeText(this, getResources().getString(R.string.profile_toast_user_fail), Toast.LENGTH_SHORT).show();
        }
    }

    // Action button: aggiungere un pasto
    public void action_button_on_click(View view) {
        Intent newIntent = new Intent(this, AddMealActivity.class);
        startActivity(newIntent);
    }
}