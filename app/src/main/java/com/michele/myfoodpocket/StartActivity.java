package com.michele.myfoodpocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent new_intent = new Intent(StartActivity.this, MainActivity.class);
            startActivity(new_intent);
        }
        else {
            Intent new_intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(new_intent);
        }
    }
}