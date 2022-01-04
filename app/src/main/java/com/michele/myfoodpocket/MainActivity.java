package com.michele.myfoodpocket;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.navigation.ui.AppBarConfiguration;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private TextView textViewDate;
    private Button dateButton;
    private Calendar myCalendar;

    private static double basalMetabolicRate = 0;
    private static double dailyCaloriesNeed = 0;

    static final int MIN_AGE = 18;
    static final int MEDIUM_AGE_1 = 30;
    static final int MEDIUM_AGE_2 = 60;
    static final int MAX_AGE = 75;

    private ArrayList <Meal> meals;
    private int caloriesOfTheDay;

    private String stringDate = "";

    private String mealKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Se vengo da AddMealActivity o DetailActivity recupero la data alla quale ho appena aggiunto un pasto e imposto la visualizzazione dei pasti su tale data
        Bundle extras = getIntent().getExtras();
        if (extras != null)
            stringDate = extras.getString("dateChoice");
        else
            stringDate = "none"; // Altrimenti imposto tale stringa al valore "none"

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
        navigationView.getMenu().findItem(R.id.nav_food_plan).setOnMenuItemClickListener(this);
        navigationView.getMenu().findItem(R.id.nav_exit).setOnMenuItemClickListener(this);
        navigationView.getMenu().findItem(R.id.nav_characteristics).setOnMenuItemClickListener(this);

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

        // Impostazione della data
        textViewDate = (TextView) findViewById(R.id.main_text_view_date);
        dateButton = findViewById(R.id.main_date_button);
        myCalendar = Calendar.getInstance();
        if(!stringDate.equals("none")) { // Se mi è stata passata una data da AddMealActivity imposto tale data
            textViewDate.setText(stringDate);
        }
        else { // Altrimenti prendo la data odierna
            String myFormat = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
            textViewDate.setText(sdf.format(myCalendar.getTime()));
            stringDate = sdf.format(myCalendar.getTime()).toString();
        }
        dateSetup(); // In ogni caso chiamo la dateSetup()
        
        // Reperimento pasto
        caloriesOfTheDay = 0;
        getMeal();
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
            case R.id.nav_characteristics:
                newIntent = new Intent(MainActivity.this, MyCharacteristicsActivity.class);
                startActivity(newIntent);
                break;
            case R.id.nav_results:
                newIntent = new Intent(MainActivity.this, ResultsActivity.class);
                startActivity(newIntent);
                break;
            case R.id.nav_food_plan:
                newIntent = new Intent(MainActivity.this, FoodPlanActivity.class);
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

    private void dailyCalories()
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
                    for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { //insieme di risposta
                        if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                            userKey = postSnapshot.getKey();
                            userInfo = postSnapshot.getValue(User.class); // <= reference al nostro oggetto
                        }
                    }

                    int birthdayDay = Integer.parseInt(userInfo.getBirthDate().split("/")[0]);
                    int birthdayMonth = Integer.parseInt(userInfo.getBirthDate().split("/")[1]);
                    int birthdayYear = Integer.parseInt(userInfo.getBirthDate().split("/")[2]);
                    int age = getAge(birthdayDay, birthdayMonth, birthdayYear);
                    basalMetabolicRate = 0;
                    dailyCaloriesNeed = 0;

                    // Se l'utente è maschio
                    if(userInfo.getSex() == 1) {
                        if(age >= MIN_AGE && age <= MEDIUM_AGE_2 - 1) {
                            if(age <= MEDIUM_AGE_1 - 1)
                                basalMetabolicRate = 15.3 * userInfo.getWeight() + 679;
                            else
                                basalMetabolicRate = 11.6 * userInfo.getWeight() + 879;
                            if(userInfo.getWorkHeaviness() == 1) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.55;
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.41;
                                }
                            }
                            else if(userInfo.getWorkHeaviness() == 2) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.78;
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.70;
                                }
                            }
                            else if(userInfo.getWorkHeaviness() == 3) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 2.10;
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 2.01;
                                }
                            }
                        }
                        else if(age >= MEDIUM_AGE_2 && age <= MAX_AGE-1) {
                            basalMetabolicRate = 11.9 * userInfo.getWeight() + 700;
                            if(userInfo.getSportPracticed() == true) {
                                dailyCaloriesNeed = basalMetabolicRate * 1.51;
                            }
                            else {
                                dailyCaloriesNeed = basalMetabolicRate * 1.40;
                            }
                        }
                        else if(age >= MAX_AGE) {
                            basalMetabolicRate = 8.4 * userInfo.getWeight() + 819;
                            if(userInfo.getSportPracticed() == true) {
                                dailyCaloriesNeed = basalMetabolicRate * 1.51;
                            }
                            else {
                                dailyCaloriesNeed = basalMetabolicRate * 1.33;
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
                            if(userInfo.getWorkHeaviness() == 1) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.56;
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.42;
                                }
                            }
                            else if(userInfo.getWorkHeaviness() == 2) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.64;
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.56;
                                }
                            }
                            else if(userInfo.getWorkHeaviness() == 3) {
                                if(userInfo.getSportPracticed() == true) {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.82;
                                }
                                else {
                                    dailyCaloriesNeed = basalMetabolicRate * 1.73;
                                }
                            }
                        }
                        else if(age >= MEDIUM_AGE_2 && age <= MAX_AGE-1) {
                            basalMetabolicRate = 9.2 * userInfo.getWeight() + 688;
                            if(userInfo.getSportPracticed() == true) {
                                dailyCaloriesNeed = basalMetabolicRate * 1.56;
                            }
                            else {
                                dailyCaloriesNeed = basalMetabolicRate * 1.44;
                            }
                        }
                        else if(age >= MAX_AGE) {
                            basalMetabolicRate = 9.8 * userInfo.getWeight() + 624;
                            if(userInfo.getSportPracticed() == true) {
                                dailyCaloriesNeed = basalMetabolicRate * 1.56;
                            }
                            else {
                                dailyCaloriesNeed = basalMetabolicRate * 1.37;
                            }
                        }
                        else {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.main_toast_error_age), Toast.LENGTH_SHORT).show();
                        }
                    }
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
        newIntent.putExtra("choiceDate", stringDate);
        startActivity(newIntent);
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        textViewDate.setText(sdf.format(myCalendar.getTime()));
        stringDate = sdf.format(myCalendar.getTime()).toString(); // Aggiorno la stringa da passare all'intent dell'aggiunta di un nuovo pasto
        getMeal(); // Chiamo un refresh sui dati per visualizzare i nuovi pasti giornalieri
    }

    private void dateSetup() {
        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }

        };
        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
    
    private void getMeal() {
        if(user != null) {
            // Azzero le calorie giornaliere in quanto vengono ricalcolate
            caloriesOfTheDay = 0;

            // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
            database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
            databaseReference = database.getReference();
            databaseReference = database.getReference("Meal");

            databaseReference.orderByChild("emailDate").equalTo(user.getEmail() + ":" + textViewDate.getText()).addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    ListView newListView = findViewById(R.id.main_list_view);
                    newListView.setAdapter(null); // Pulizia delle righe prima di inserire quelle nuove
                    TextView newNullMealsTextView = findViewById(R.id.main_null_meals);

                    if(dataSnapshot.hasChildren() == false) { // Se non c'è nessun pasto nella tal data
                        newNullMealsTextView.setText(getResources().getString(R.string.main_null_meals)); // Indico che non ci sono pasti e di aggiungerne uno nuovo
                        newNullMealsTextView.setVisibility(View.VISIBLE); // Imposto che sia visibile
                        newListView.setVisibility(View.GONE); // Rimuovo la visibilità della listview

                        // Anche nel caso in cui non ci siano pasti bisogna aggiornare calorie assunte in tal giorno e progress bar
                        ProgressBar pb = findViewById(R.id.main_progress_bar);
                        TextView tvCaloriesTaken = findViewById(R.id.main_text_view_calories_taken);
                        tvCaloriesTaken.setText(getResources().getString(R.string.main_calories_taken) + ": " + caloriesOfTheDay + "/" + String.format("%.0f", dailyCaloriesNeed));
                        pb.setProgress((int)(((caloriesOfTheDay * 100) / dailyCaloriesNeed)));
                    }
                    else { // Se c'è almeno un pasto nella tal data
                        newNullMealsTextView.setText(""); // Lascio vuota la text view che suggerisce di aggiungere un nuovo pasto
                        newNullMealsTextView.setVisibility(View.GONE); // Rimuovo la visibilità di tale text view in modo che non lasci dello spazio vuoto in mezzo allo schermo
                        newListView.setVisibility(View.VISIBLE); // Reimposto la visibilità della listview

                        meals = new ArrayList<Meal>();
                        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { //insieme di risposta
                            if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                                mealKey = postSnapshot.getKey();
                                meals.add(postSnapshot.getValue(Meal.class)); // <= reference al nostro oggetto
                            }
                        }

                        CustomAdapter adapter = new CustomAdapter(getBaseContext(), R.layout.meal_row, meals.toArray(new Meal[0])); // Passo un array di Meal come richiesto dall'adapter, new Meal[0] serve per il tipo su cui viene costruito l'array della funzione toArray
                        newListView.setAdapter(adapter);

                        for(int i = 0; i < meals.size(); i++) {
                            caloriesOfTheDay += meals.get(i).getCalories();
                        }

                        // Una volta calcolato metabolismo basale e apporto calorico giornaliero, vengono segnalati i progressi giornalieri dell'utente
                        ProgressBar pb = findViewById(R.id.main_progress_bar);
                        TextView tvCaloriesTaken = findViewById(R.id.main_text_view_calories_taken);
                        tvCaloriesTaken.setText(getResources().getString(R.string.main_calories_taken) + ": " + caloriesOfTheDay + "/" + String.format("%.0f", dailyCaloriesNeed));
                        pb.setProgress((int)(((caloriesOfTheDay * 100) / dailyCaloriesNeed)));

                        // Imposto i listener degli elementi della listview
                        AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {

                            @Override
                            public void onItemClick(AdapterView<?> adapter, View view,
                                                    int position, long id) {
                                Intent newIntent = new Intent(MainActivity.this, MealDetailActivity.class);
                                Meal m = (Meal)adapter.getItemAtPosition(position);
                                newIntent.putExtra("detailMeal", m);
                                newIntent.putExtra("stringDate", stringDate);
                                startActivity(newIntent);
                            }
                        };
                        newListView.setOnItemClickListener(clickListener);
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

    public static double getDailyCaloriesNeed() {
        return dailyCaloriesNeed;
    }

    public static double getBasalMetabolicRate() {
        return basalMetabolicRate;
    }
}