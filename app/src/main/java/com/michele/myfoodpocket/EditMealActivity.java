package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import android.view.View;

public class EditMealActivity extends AppCompatActivity {

    // Variabili per scattare foto e memorizzarla
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;
    private String photoPath; // Inizializzo il percorso della foto a "none": se verrà scattata verrà sostituito con il vero percorso, altrimenti rimarrà "none"
    private String stringDate;

    private Meal meal;
    private String mealKey;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private boolean isFirstPhoto; // Booleano che mi serve per capire se l'utente sta scattando la foto al pasto per la prima volta (e sostituendo)
                                  // la foto di default, oppure se sta facendo una nuova foto al pasto e sta sostituendo una precedente foto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
        databaseReference = database.getReference("Meal");

        if(!isNetworkConnected()) {
            Intent newIntentNoConnection = new Intent(EditMealActivity.this, NoInternetConnectionActivity.class);
            newIntentNoConnection.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
            startActivity(newIntentNoConnection);
            finish(); // Kill dell'activity così non può essere ripresa con il back button
        }

        setContentView(R.layout.activity_edit_meal);

        // Recupero il pasto da modificare
        Bundle extras = getIntent().getExtras();
        if(extras != null) { // Se il passaggio del parametro tra intent è avvenuto correttamente
            meal = (Meal) extras.getSerializable("detailMeal");
            stringDate = extras.getString("stringDate");
        }

        // Prelevo la data e la stampo
        TextView tvDate = (TextView) (findViewById(R.id.edit_meal_date_print));
        tvDate.setText(meal.getEmailDate().split(":")[1]); // Splitto per ":" e prendo il secondo oggetto, cioè la data
        TextView tvDescription = (TextView) (findViewById(R.id.edit_meal_edit_text_description));
        tvDescription.setText(meal.getDescription());
        TextView tvCalories = (TextView) (findViewById(R.id.edit_meal_edit_text_calories));
        tvCalories.setText("" + meal.getCalories());

        // Recupero la key firebase del pasto
        databaseReference.orderByChild("id").equalTo(meal.getId()).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) { //insieme di risposta
                    if(postSnapshot!= null && postSnapshot.getValue()!= null) {
                        mealKey = postSnapshot.getKey();
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError error)
            {
                // Failed to read value
                Log.w("DEBUG", "Failed to read value.", error.toException());
            }
        });

        // Imposto lo spinner sulla categoria del pasto da modificare
        Spinner spinner = (Spinner)(findViewById(R.id.edit_meal_spinner_category));
        if(meal.getCategory().equals(getResources().getString(R.string.edit_category_name_breakfast)))
            spinner.setSelection(0);
        else if(meal.getCategory().equals(getResources().getString(R.string.edit_category_name_snack_morning)))
            spinner.setSelection(1);
        else if(meal.getCategory().equals(getResources().getString(R.string.edit_category_name_lunch)))
            spinner.setSelection(2);
        else if(meal.getCategory().equals(getResources().getString(R.string.edit_category_name_snack_afternoon)))
            spinner.setSelection(3);
        else if(meal.getCategory().equals(getResources().getString(R.string.edit_category_name_dinner)))
            spinner.setSelection(4);
        else if(meal.getCategory().equals(getResources().getString(R.string.edit_category_name_other)))
            spinner.setSelection(5);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void action_button_on_click(View view) {
        if(isNetworkConnected()) {
            // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference databaseReference = database.getReference("Meal");

            String newCategory = ((Spinner)(findViewById(R.id.edit_meal_spinner_category))).getSelectedItem().toString();
            EditText editTextDescription = (EditText)(findViewById(R.id.edit_meal_edit_text_description));
            String newDescription = editTextDescription.getText().toString();
            EditText editTextCalories = (EditText)(findViewById(R.id.edit_meal_edit_text_calories));
            int newCalories = Integer.parseInt(editTextCalories.getText().toString());

            if(inputControlOk(editTextDescription, editTextCalories)) {
                Map<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(mealKey + "/category", newCategory);
                childUpdates.put(mealKey + "/description", newDescription);
                childUpdates.put(mealKey + "/calories", newCalories);
                childUpdates.put(mealKey + "/photoPath", photoPath);

                databaseReference.updateChildren(childUpdates);

                Toast.makeText(this, getResources().getString(R.string.profile_toast_modify_success), Toast.LENGTH_SHORT).show();

                Intent newIntent = new Intent(EditMealActivity.this, MainActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity pregresse
                newIntent.putExtra("dateChoice", meal.getEmailDate().split(":")[1]); // Split per ":" e prendo il secondo oggetto, cioè la data
                startActivity(newIntent);
                finish(); // Kill dell'activity così non può essere ripresa con il back button
            }
            else {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.edit_meal_input_control_not_ok), Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.no_internet_connection_not_available), Toast.LENGTH_SHORT);
    }

    public boolean inputControlOk(EditText editTextDescripton, EditText editTextCalories) {
        if(!editTextDescripton.getText().toString().isEmpty() && !editTextCalories.getText().toString().isEmpty())
            return true;
        else
            return false;
    }

    public void takePhoto(View view) {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create the File where the photo should go
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.michele.myfoodpocket.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath(); // Questa è la variabile in cui l'immagine è salvata sul telefono prima di essere caricata in cloud

        if(meal.getPhotoPath().equals("none")) {
            // Aggiorno il path della foto
            Uri uriPhotoPath = Uri.fromFile(image);
            photoPath = "Images/" + uriPhotoPath.getLastPathSegment();

            isFirstPhoto = true; // è la prima foto scattata al pasto, quindi non devo sovrascrivere un'immagine già presente nello storage
            return image;
        }
        else {
            // Aggiorno il path della foto
            photoPath = meal.getPhotoPath();

            isFirstPhoto = false; // NON è la prima foto scattata al posto, quindi devo sovrascrivere un'immagine già presente nello storage
            return image;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            Uri file;
            StorageReference riversRef;

            if(isFirstPhoto) {
                file = Uri.fromFile(new File(currentPhotoPath));
                riversRef = storageRef.child("Images/" + file.getLastPathSegment());
            }
            else {
                file = Uri.fromFile(new File(currentPhotoPath));
                riversRef = storageRef.child(photoPath);
            }

            UploadTask uploadTask;
            uploadTask = riversRef.putFile(file);

            TextView picTakenTextView = (TextView)findViewById(R.id.edit_meal_pic_taken);
            picTakenTextView.setText(getResources().getString(R.string.edit_meal_pic_taken_string));

            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    // ...
                }
            });
            // [END upload_file]
        }
    }
}