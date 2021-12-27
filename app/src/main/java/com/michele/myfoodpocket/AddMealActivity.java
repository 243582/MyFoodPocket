package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddMealActivity extends AppCompatActivity {

    // Variabili per scattare foto e memorizzarla
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private String currentPhotoPath;
    private String photoPath = "none"; // Inizializzo il percorso della foto a "none": se verrà scattata verrà sostituito con il vero percorso, altrimenti rimarrà "none"
    private String stringDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_meal);

        // Recupero la data selezionata
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            stringDate = extras.getString("choiceDate");
        }

        // Rilevo la data e la stampo
        TextView tvDate = (TextView)(findViewById(R.id.add_meal_date_print));
        tvDate.setText(stringDate);
    }

    public void action_button_on_click(View view) {
        // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference databaseReference = database.getReference();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        TextView textViewDate = (TextView)(findViewById(R.id.add_meal_date_print));

        // Creo l'identificativo del pasto mediante email + timestamp
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        String id = user.getEmail() + ":" + textViewDate.getText().toString() + ":" + calendar.get(Calendar.HOUR) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);

        String emailDate = user.getEmail() + ":" + textViewDate.getText().toString();
        String category = ((Spinner)(findViewById(R.id.add_meal_spinner_category))).getSelectedItem().toString();
        EditText editTextDescription = (EditText)(findViewById(R.id.add_meal_edit_text_description));
        EditText editTextCalories = (EditText)(findViewById(R.id.add_meal_edit_text_calories));

        Meal newMeal = new Meal(emailDate, category, editTextDescription.getText().toString(), Integer.parseInt(editTextCalories.getText().toString()), photoPath, id);
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
        newIntent.putExtra("dateChoice", stringDate);
        startActivity(newIntent);
    }

    public void take_photo(View view) {
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
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            // [START upload_file]
            Uri file = Uri.fromFile(new File(currentPhotoPath));
            StorageReference riversRef = storageRef.child("Images/" + file.getLastPathSegment());
            photoPath = "Images/" + file.getLastPathSegment();
            UploadTask uploadTask;
            uploadTask = riversRef.putFile(file);

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