package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class MealDetailActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private Meal meal;
    private String stringDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isNetworkConnected()) {
            Intent newIntentNoConnection = new Intent(MealDetailActivity.this, NoInternetConnectionActivity.class);
            newIntentNoConnection.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
            startActivity(newIntentNoConnection);
            finish(); // Kill dell'activity così non può essere ripresa con il back button
        }

        setContentView(R.layout.activity_meal_detail);

        // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
        database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference();

        Bundle extras = getIntent().getExtras();

        if(extras != null) { // Se il passaggio del parametro tra intent è avvenuto correttamente
            meal = (Meal) extras.getSerializable("detailMeal");
            stringDate = extras.getString("stringDate");
        }

        TextView textViewCategory = (TextView)findViewById(R.id.detail_text_view_category);
        textViewCategory.setText(meal.getCategory());
        TextView textViewDescription = (TextView)findViewById(R.id.detail_text_view_description);
        textViewDescription.setText(meal.getDescription());
        TextView textViewCalories = (TextView)findViewById(R.id.detail_text_view_calories);
        textViewCalories.setText("" + meal.getCalories());

        ImageView imageViewMeal = (ImageView)findViewById(R.id.detail_image_view_picture);
        if(!meal.getPhotoPath().equals("none")) { // Se il pasto ha una foto la scarico e la imposto
            // Ottenimento della foto del pasto (se presente)
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference picReference = storageRef.child(meal.getPhotoPath());
            final long TEN_MEGABYTE = 1024 * 1024 * 10; // Foto al massimo di 10 megabyte
            picReference.getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageViewMeal.setImageBitmap(bm);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("DEBUG_DETAIL", "Errore foto");
                }
            });
        }
        else { // Se il pasto non ha una foto ne metto una di default
            if(meal.getCategory().equals(getResources().getString(R.string.main_category_breakfast)))
                imageViewMeal.setImageResource(R.drawable.ic_breakfast);
            else if(meal.getCategory().equals(getResources().getString(R.string.main_category_snack_morning)))
                imageViewMeal.setImageResource(R.drawable.ic_snack_morning);
            else if(meal.getCategory().equals(getResources().getString(R.string.main_category_lunch)))
                imageViewMeal.setImageResource(R.drawable.ic_lunch);
            else if(meal.getCategory().equals(getResources().getString(R.string.main_category_snack_afternoon)))
                imageViewMeal.setImageResource(R.drawable.ic_snack_afternoon);
            else if(meal.getCategory().equals(getResources().getString(R.string.main_category_dinner)))
                imageViewMeal.setImageResource(R.drawable.ic_dinner);
            else if(meal.getCategory().equals(getResources().getString(R.string.main_category_other)))
                imageViewMeal.setImageResource(R.drawable.ic_other);
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void deleteOnClick(View view) {
        databaseReference = database.getReference("Meal");

        databaseReference.orderByChild("id").equalTo(meal.getId()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { // Insieme di risposta
                    if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                        String key = postSnapshot.getKey();
                        dataSnapshot.getRef().child(key).removeValue();
                    }
                }

                // Cancello anche la foto dallo storage di Firebase
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReference();
                StorageReference picRef = storageReference.child(meal.getPhotoPath());
                picRef.delete();

                Toast.makeText(getBaseContext(), getResources().getString(R.string.meal_detail_delete_success), Toast.LENGTH_SHORT).show();
                Intent newIntent = new Intent(MealDetailActivity.this, MainActivity.class);
                newIntent.putExtra("dateChoice", stringDate);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
                startActivity(newIntent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("DEBUG_DETAIL", "Fail nel reperimento della foto");
            }
        });
    }

    public void editOnClick(View view) {
        Intent newIntent = new Intent(MealDetailActivity.this, EditMealActivity.class);
        newIntent.putExtra("detailMeal", meal);
        startActivity(newIntent);
        finish(); // Kill dell'activity così non può essere ripresa con il back button
    }
}