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
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_toast_welcome_back) + " " + user.getEmail().toString(),
                                    Toast.LENGTH_SHORT).show();

                            Intent newIntent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(newIntent);
                            finish(); // Kill dell'activity così non può essere ripresa con il back button

                        } else {
                            Toast.makeText(LoginActivity.this, getResources().getString(R.string.login_toast_autentication_failed),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    public void signUpOnClick(View view) {
        Intent newIntent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(newIntent);
        //finish(); // Kill dell'activity così non può essere ripresa con il back button
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        return false;
    }

    public void signInOnClick(View view) {
        String email = ((EditText)(findViewById(R.id.sign_in_email))).getText().toString();
        String password = ((EditText)(findViewById(R.id.sign_in_password))).getText().toString();

        signIn(email, password);
    }
}