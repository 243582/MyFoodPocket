package com.michele.myfoodpocket;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.michele.myfoodpocket.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        View navHeader = navigationView.inflateHeaderView(R.layout.nav_header_main) ;

        navigationView.getMenu().findItem(R.id.nav_home).setOnMenuItemClickListener(this);
        navigationView.getMenu().findItem(R.id.nav_profile).setOnMenuItemClickListener(this);
        navigationView.getMenu().findItem(R.id.nav_results).setOnMenuItemClickListener(this);
        navigationView.getMenu().findItem(R.id.nav_exit).setOnMenuItemClickListener(this);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_results, R.id.nav_exit)
                .setOpenableLayout(drawer)
                .build();
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        //NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        //NavigationUI.setupWithNavController(navigationView, navController);
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
        Intent new_intent;
        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                break;
            case R.id.nav_profile:
                new_intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(new_intent);
                break;
            case R.id.nav_results:
                new_intent = new Intent(MainActivity.this, ResultsActivity.class);
                startActivity(new_intent);
                break;
            case R.id.nav_exit:
                mAuth.signOut();
                new_intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(new_intent);
                break;
        }
        return false;
    }
}