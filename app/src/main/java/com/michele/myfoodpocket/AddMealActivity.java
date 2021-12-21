package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddMealActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        // Rilevo la data e la stampo
        TextView tvDate = (TextView)(findViewById(R.id.add_meal_date_print));
        tvDate.setText(Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "/" +
                (Calendar.getInstance().get(Calendar.MONTH) + 1) + "/" + Calendar.getInstance().get(Calendar.YEAR));
    }

    public void action_button_on_click(View view) {
        // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference databaseReference = database.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView textViewDate = (TextView)(findViewById(R.id.add_meal_date_print));
        String emailDate = user.getEmail() + ":" + textViewDate.getText().toString();
        String category = ((Spinner)(findViewById(R.id.add_meal_spinner_category))).getSelectedItem().toString();
        EditText editTextDescription = (EditText)(findViewById(R.id.add_meal_edit_text_description));
        EditText editTextCalories = (EditText)(findViewById(R.id.add_meal_edit_text_calories));

        Meal newMeal = new Meal(emailDate, category, editTextDescription.getText().toString(), Integer.parseInt(editTextCalories.getText().toString()));
        databaseReference.child("Meal").push().setValue(newMeal).addOnSuccessListener(new OnSuccessListener<Void>()
        {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.add_meal_success), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception e)
            {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.add_meal_failed), Toast.LENGTH_SHORT).show();
            }
        });

        Intent newIntent = new Intent(AddMealActivity.this, MainActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
        startActivity(newIntent);
    }

    public void take_photo(View view) {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, 1);
        } catch (ActivityNotFoundException e) {
            // display error state to the user
        }
    }
}