package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void signIn(String email, String password) {
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("APPSTATE", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            Toast.makeText(LoginActivity.this, "Login: " + user.getEmail().toString(),
                                    Toast.LENGTH_SHORT).show();


                            Intent new_intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(new_intent);
                            finish(); // Kill dell'activity così non può essere ripresa con il back button

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("APPSTATE", "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    public void sign_up_onclick(View view) {
        Intent new_intent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(new_intent);
        finish(); // Kill dell'activity così non può essere ripresa con il back button
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        return false;
    }

    public void sign_in_onclick(View view) {
        String email = ((EditText)(findViewById(R.id.sign_in_email))).getText().toString();
        String password = ((EditText)(findViewById(R.id.sign_in_password))).getText().toString();

        signIn(email, password);
    }
}