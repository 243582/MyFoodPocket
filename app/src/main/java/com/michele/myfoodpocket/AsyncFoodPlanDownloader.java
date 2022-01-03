package com.michele.myfoodpocket;

import android.content.Intent;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AsyncFoodPlanDownloader extends AsyncTask<Integer, Integer, Integer> {

    private ProgressBar pBar;
    private int num;
    private ArrayList<Meal> meals;

    public AsyncFoodPlanDownloader(ProgressBar pBar, int num, ArrayList<Meal> meals) {
        this.pBar = pBar;
        this.num = num;
        this.meals = meals;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pBar.setProgress(0);
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        pBar.setProgress(100);
    }

    @Override
    protected void onProgressUpdate(Integer... prog) {
        super.onProgressUpdate(prog);
        float progress = (int)(prog[0])/(float)num;
        pBar.setProgress((int)(progress*100));
    }

    @Override
    protected Integer doInBackground(Integer... params) { // ... significa che 0 o n oggetti possono essere passati come parametri (vedi varargs)
        int i = 0;
        for(i = 0; i < meals.size(); i++)
            Log.d("DEBUGASYNC", "" + meals.get(i).toString());
            publishProgress(i);

        // Scrittura del file CSV
        try {
            Calendar calendar = Calendar.getInstance(Locale.getDefault());
            String filename = "MyFoodPocket_" + calendar.get(Calendar.YEAR) + calendar.get(Calendar.MONTH) + calendar.get(Calendar.DAY_OF_MONTH) +
                    calendar.get(Calendar.HOUR_OF_DAY) + calendar.get(Calendar.MINUTE) +  calendar.get(Calendar.SECOND);

            File file = new File(pBar.getContext().getExternalFilesDir(null) + File.separator + filename + ".csv");

            Log.d("DEBUGASYNCPLUS", file.getAbsolutePath());


            if(!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write(getContent());
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private String getContent() {
        String content = "";
        for(int i = 0; i < meals.size(); i++) {
            Meal meal = meals.get(i);
            // Con la split prendo soltanto la data dal campo "emailDate" e scarto l'email che non serve
            content += meal.getEmailDate().split(":")[1] + "," + meal.getCategory() + "," + meal.getDescription() + "," + meal.getCalories() + "\n";
        }

        return content;
    }
}
