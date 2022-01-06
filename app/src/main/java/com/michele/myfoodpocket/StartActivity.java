package com.michele.myfoodpocket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

import java.net.InetAddress;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); // In questo modo disabilito la modalità notte anche se il telefono ce l'ha attiva

        if(isNetworkConnected()) {
            if(FirebaseAuth.getInstance().getCurrentUser() != null) {
                Intent newIntent = new Intent(StartActivity.this, MainActivity.class);
                startActivity(newIntent);
                finish(); // Kill dell'activity così non può essere ripresa con il back button
            }
            else {
                Intent newIntent = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(newIntent);
                finish(); // Kill dell'activity così non può essere ripresa con il back button
            }
        }
        else {
            Intent newIntent = new Intent(StartActivity.this, NoInternetConnectionActivity.class);
            startActivity(newIntent);
            finish(); // Kill dell'activity così non può essere ripresa con il back button
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }
}