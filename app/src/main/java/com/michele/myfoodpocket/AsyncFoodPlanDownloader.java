package com.michele.myfoodpocket;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AsyncFoodPlanDownloader extends AsyncTask<Integer, Integer, String> { // <Params, Update progress, Results>

    private int num;
    private ArrayList<Meal> meals;
    private Context context;

    public AsyncFoodPlanDownloader(ArrayList<Meal> meals, Context context) {
        this.num = num;
        this.meals = meals;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Toast.makeText(context, context.getResources().getString(R.string.food_plan_download_started), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPostExecute(String filePath) {
        super.onPostExecute(filePath);
        Log.d("DEBUG_ASYNC_FILE_PATH", filePath);
        Toast.makeText(context, context.getResources().getString(R.string.food_plan_download_finished), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onProgressUpdate(Integer... prog) {
        super.onProgressUpdate(prog);
    }

    @Override
    protected String doInBackground(Integer... params) { // ... significa che 0 o n oggetti possono essere passati come parametri (vedi varargs)
        // Simulo un delay di 10 secondi per l'esecuzione del thread
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String filePath = saveData();
        return filePath;
    }

    private String saveData() {
        File root = null;
        Calendar calendar = Calendar.getInstance(Locale.getDefault()); // Serve per creare il nome del file
        String filename = "MyFoodPocket_" + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DAY_OF_MONTH) +
                calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) + calendar.get(Calendar.SECOND) + ".csv";

        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        root = new File(root, filename);

        try {
            FileOutputStream fout = new FileOutputStream(root);
            fout.write(getContent().getBytes());
            fout.flush();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root.getAbsolutePath();
    }

    private String getContent() {
        String content = "";
        content += "Data,Categoria,Descrizione,Numero calorie" + "\n"; // Intestazione del file CSV
        for(int i = 0; i < meals.size(); i++) {
            Meal meal = meals.get(i);
            // Con la split prendo soltanto la data dal campo "emailDate" e scarto l'email che non serve
            content += meal.getEmailDate().split(":")[1] + "," + meal.getCategory() + "," + meal.getDescription() + "," + meal.getCalories() + "\n";
        }

        return content;
    }
}