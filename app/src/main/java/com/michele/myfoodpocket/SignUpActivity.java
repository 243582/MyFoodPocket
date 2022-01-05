package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Calendar;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Calendar myCalendar;
    private TextView textViewBirthDateString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        myCalendar = Calendar.getInstance();
        textViewBirthDateString = (TextView)findViewById(R.id.sign_up_hint_birth_date_string);
        dateSetup();
    }

    public void signUpOnClick(View view) {
        String email = ((EditText)(findViewById(R.id.sign_up_email))).getText().toString();
        String password = ((EditText)(findViewById(R.id.sign_up_password))).getText().toString();
        String passwordRepeat = ((EditText)(findViewById(R.id.sign_up_password_repeat))).getText().toString();
        int sex = ((Spinner)(findViewById(R.id.sign_up_sex))).getSelectedItemPosition();
        String height = ((EditText)(findViewById(R.id.sign_up_height))).getText().toString();
        String weight = ((EditText)(findViewById(R.id.sign_up_weight))).getText().toString();
        String birthdate = ((TextView)(findViewById(R.id.sign_up_hint_birth_date_string))).getText().toString();
        int workHeaviness = ((Spinner)(findViewById(R.id.spinner_work_heaviness))).getSelectedItemPosition();
        int sportPracticed = ((Spinner)(findViewById(R.id.spinner_sport_practiced))).getSelectedItemPosition();

        if(checkInputOk(email, password, passwordRepeat, height, weight, birthdate))
            createAccount(email, password, passwordRepeat, sex, Integer.parseInt(height), Float.parseFloat(weight), birthdate, workHeaviness, sportPracticed);
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.sign_up_input_not_ok), Toast.LENGTH_SHORT).show();
    }

    private boolean checkInputOk(String email, String password, String passwordRepeat, String height, String weight, String birthdate) {
        if(!email.isEmpty() && !password.isEmpty() && !passwordRepeat.isEmpty() && !height.isEmpty() && !weight.isEmpty() && !birthdate.isEmpty() && password.equals(passwordRepeat)) {
            int birthdayDay = Integer.parseInt(birthdate.split("/")[0]);
            int birthdayMonth = Integer.parseInt(birthdate.split("/")[1]);
            int birthdayYear = Integer.parseInt(birthdate.split("/")[2]);
            int age = getAge(birthdayDay, birthdayMonth, birthdayYear);
            if(age >= 18) // Se l'utente è maggiorenne ok
                return true;
            else
                Toast.makeText(getBaseContext(), getResources().getString(R.string.sign_up_not_adult), Toast.LENGTH_SHORT).show();
                return false;
        }
        else
            return false;
    }

    private int getAge(int day, int month, int year){
        LocalDate birthDate = LocalDate.of(year, month, day);
        LocalDate today = LocalDate.now();
        return Period.between(birthDate, today).getYears();
    }

    private void createAccount(String email, String password, String passwordRepeat, int sex, int height, float weight, String birthdate, int workHeaviness, int sportPracticed) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.sign_up_toast_welcome) + " " + user.getEmail().toString(), Toast.LENGTH_SHORT).show();

                    // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
                    FirebaseDatabase database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
                    DatabaseReference myRef = database.getReference();

                    // Stabilisco il sesso in base all'input selezionato
                    int sexChoice = sex + 1; // Sesso: maschio 1, femmina 2. Il +1 è per l'indice degli item dello Spinner che partono da 0

                    // Stabilisco il carico di lavoro dell'utente
                    int workHeavinessChoice = workHeaviness + 1; // Lavoro: 1 leggero, 2 moderato, 3 pesante. Il +1 è per l'indice degli item dello spinner che partono da 0

                    // Stabilisco se l'utente fa sport oppure no
                    boolean sportPracticeChoice = (sportPracticed == 0 ? true : false); // Sport: 0 praticato, 1 non praticato

                    User newUser = new User(email, sexChoice, height, weight, birthdate, workHeavinessChoice, sportPracticeChoice);
                    myRef.child("User").push().setValue(newUser).addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.sign_up_toast_register_success), Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.sign_up_toast_register_failed), Toast.LENGTH_SHORT).show();
                        }
                    });
                    Intent newIntent = new Intent(SignUpActivity.this, MainActivity.class);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack, così il back button non mi ritorna al login ma esce dall'app dopola registrazione
                    startActivity(newIntent);
                    finish(); // Kill dell'activity così non può essere ripresa con il back button
                } else {
                    Log.d("APPSTATE", "createUserWithEmail:failure", task.getException());
                    //Toast.makeText(SignUpActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(SignUpActivity.this, getResources().getString(R.string.sign_up_toast_register_failed),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void dateSetup() {
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
        textViewBirthDateString.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SignUpActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        textViewBirthDateString.setText(sdf.format(myCalendar.getTime()));
    }
}