package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
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

public class LoginActivity extends AppCompatActivity  {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inizializzo Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    private void signIn(String email, String password) {
        if(checkInputOk(email, password)) {
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
                            }
                        }
                    });
        }
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.login_input_not_ok), Toast.LENGTH_SHORT).show();

    }

    public void signUpOnClick(View view) {
        Intent newIntent = new Intent(LoginActivity.this, SignUpActivity.class);
        startActivity(newIntent);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void signInOnClick(View view) {
        if(isNetworkConnected()) {
            String email = ((EditText)(findViewById(R.id.sign_in_email))).getText().toString();
            String password = ((EditText)(findViewById(R.id.sign_in_password))).getText().toString();

            signIn(email, password);
        }
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.no_internet_connection_still_not_available), Toast.LENGTH_SHORT).show();
    }

    private boolean checkInputOk(String email, String password) {
        if(!email.isEmpty() && !password.isEmpty())
            return true;
        else
            return false;
    }
}