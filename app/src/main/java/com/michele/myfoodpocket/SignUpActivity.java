package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void createAccount(String email, String password, String sex, String height, String weight, String birthdate) {
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.sign_up_toast_welcome) + " " + user.getEmail().toString(), Toast.LENGTH_SHORT).show();

                        // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
                        FirebaseDatabase database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
                        DatabaseReference myRef = database.getReference();
                        User newUser = new User(email, sex, height, weight, birthdate);
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
    // [END create_user_with_email]
    }

    public void signUpOnClick(View view) {
        String email = ((EditText)(findViewById(R.id.sign_up_email))).getText().toString();
        String password = ((EditText)(findViewById(R.id.sign_up_password))).getText().toString();
        String sex = ((EditText)(findViewById(R.id.sign_up_sex))).getText().toString();
        String height = ((EditText)(findViewById(R.id.sign_up_height))).getText().toString();
        String weight = ((EditText)(findViewById(R.id.sign_up_weight))).getText().toString();
        String birthdate = ((EditText)(findViewById(R.id.sign_up_birthdate))).getText().toString();

        createAccount(email, password, sex, height, weight, birthdate);
    }
}