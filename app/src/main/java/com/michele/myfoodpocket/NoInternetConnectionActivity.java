package com.michele.myfoodpocket;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.net.InetAddress;

public class NoInternetConnectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet_connection);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void noInternetConnectionRetry(View view) {
        if (isNetworkConnected()) {
            Intent newIntent = new Intent(NoInternetConnectionActivity.this, LoginActivity.class);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
            startActivity(newIntent);
            finish(); // Kill dell'activity così non può essere ripresa con il back button
        }
        else
            Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.no_internet_connection_still_not_available), Toast.LENGTH_SHORT).show();
    }
}