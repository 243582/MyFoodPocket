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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            Toast.makeText(getApplicationContext(), user.getEmail().toString(), Toast.LENGTH_SHORT).show();


                            Intent new_intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(new_intent);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("APPSTATE", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
        // [END create_user_with_email]
    }

    public void sign_up_onclick(View view) {
        String email = ((EditText)(findViewById(R.id.sign_up_email))).getText().toString();
        String password = ((EditText)(findViewById(R.id.sign_up_password))).getText().toString();
        String sex = ((EditText)(findViewById(R.id.sign_up_sex))).getText().toString();
        String height = ((EditText)(findViewById(R.id.sign_up_height))).getText().toString();
        String weight = ((EditText)(findViewById(R.id.sign_up_weight))).getText().toString();
        String birthdate = ((EditText)(findViewById(R.id.sign_up_birthdate))).getText().toString();

        createAccount(email, password, sex, height, weight, birthdate);
    }
}