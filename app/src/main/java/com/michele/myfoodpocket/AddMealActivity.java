package com.michele.myfoodpocket;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
    private StorageReference riversRef;
    private Uri file;
    private boolean isPhotoTaken = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!isNetworkConnected()) {
            Intent newIntentNoConnection = new Intent(AddMealActivity.this, NoInternetConnectionActivity.class);
            newIntentNoConnection.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
            startActivity(newIntentNoConnection);
            finish(); // Kill dell'activity così non può essere ripresa con il back button
        }

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

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void actionButtonOnClick(View view) {
        if(isNetworkConnected()) {
            // Ho dovuto specificare l'URL perché quello ottenuto automaticamente non corrispondeva a quello effettivo del database Firebase
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://myfoodpocket-bf82e-default-rtdb.europe-west1.firebasedatabase.app/");
            DatabaseReference databaseReference = database.getReference();

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            TextView textViewDate = (TextView)(findViewById(R.id.add_meal_date_print));

            // Creo l'identificativo del pasto mediante email + timestamp
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            String id = user.getEmail() + ":" + textViewDate.getText().toString() + ":" + calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":" + calendar.get(Calendar.SECOND);

            String emailDate = user.getEmail() + ":" + textViewDate.getText().toString();
            String category = ((Spinner)(findViewById(R.id.add_meal_spinner_category))).getSelectedItem().toString();
            EditText editTextDescription = (EditText)(findViewById(R.id.add_meal_edit_text_description));
            EditText editTextCalories = (EditText)(findViewById(R.id.add_meal_edit_text_calories));

            if(inputControlOk(editTextDescription, editTextCalories)) {
                Meal newMeal = new Meal(emailDate, category, editTextDescription.getText().toString().replace("\n", ""), Integer.parseInt(editTextCalories.getText().toString()), photoPath, id, user.getEmail());

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

                if(isPhotoTaken) { // Se la foto è stata scattata la memorizzo nello storage Firebase
                    UploadTask uploadTask;
                    uploadTask = riversRef.putFile(file);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.d("DEBUG_ADD", "Fail nel caricamento della foto");
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Ora che ho caricato la foto, posso cancellarla dalla memoria del telefono
                            File fdelete = new File(file.getPath());
                            if (fdelete.exists()) {
                                fdelete.delete();
                            }
                        }
                    });
                }

                /* Aggiungo un delay di 1000 secondi per permettere il corretto caricamento dell'immagine sullo storage di Firebase */
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent newIntent = new Intent(AddMealActivity.this, MainActivity.class);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK); // Kill di tutte le activity nello stack
                newIntent.putExtra("dateChoice", stringDate);
                startActivity(newIntent);
            }
            else {
                Toast.makeText(getBaseContext(), getResources().getString(R.string.add_meal_input_control_not_ok), Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(getBaseContext(), getResources().getString(R.string.no_internet_connection_not_available), Toast.LENGTH_SHORT).show();
    }

    public void takePhoto(View view) {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Creo il File nel quale verrà memorizzata la foto
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.d("DEBUG_ADD", "Fail nella creazione del file");
        }
        // Se il file è stato creato correttamente continuo
        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.michele.myfoodpocket.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        // Creazione del nome dell'immagine
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefisso */
                ".jpg",         /* suffisso */
                storageDir      /* directory */
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            isPhotoTaken = true; // La foto è stata scattata, allora la memorizzerò

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            file = Uri.fromFile(new File(currentPhotoPath)); // Qui l'immagine è memorizzata nel telefono

            // Compressione della foto
            File fileToCompress = new File (file.getPath());
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(fileToCompress.getPath ());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 15, new FileOutputStream (fileToCompress));
            }
            catch (Throwable t) {
                Log.d("DEBUG_ADD", "Errore nella compressione della foto" + t.toString ());
                t.printStackTrace ();
            }

            riversRef = storageRef.child("Images/" + file.getLastPathSegment()); // Preparo il riferimento per l'upload su Firebase Storage (che avviene nella actionButtonOnClick)
            photoPath = "Images/" + file.getLastPathSegment(); // Memorizzo il path della foto sul telefono perché poi la cancellerò dalla memoria

            TextView picTakenTextView = (TextView)findViewById(R.id.add_meal_pic_taken);
            picTakenTextView.setText(getResources().getString(R.string.add_meal_pic_taken_string));
        }
    }

    public boolean inputControlOk(EditText editTextDescripton, EditText editTextCalories) {
        if(!editTextDescripton.getText().toString().isEmpty() && !editTextCalories.getText().toString().isEmpty()
                && !editTextCalories.getText().toString().contains(".") && !editTextCalories.getText().toString().contains(",")
                && !editTextCalories.getText().toString().contains("-") && !editTextCalories.getText().toString().contains(" ")) {
            // Controllo che il formato numerico delle calorie sia corretto
            try
            {
                Integer.parseInt(editTextCalories.getText().toString());

                return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
        else
            return false;
    }
}