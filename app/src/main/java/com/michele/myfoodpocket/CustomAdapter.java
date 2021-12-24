package com.michele.myfoodpocket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CustomAdapter extends ArrayAdapter <Meal> {

    public CustomAdapter(Context context, int textViewResourceId, Meal [] meals) {
        super(context, textViewResourceId, meals);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.meal_row, null);

        Meal meal = getItem(position);

        ImageView imageView = (ImageView)convertView.findViewById(R.id.meal_row_linear_image_view);
        if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_breakfast)))
            imageView.setImageResource(R.drawable.ic_breakfast);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_snack_morning)))
            imageView.setImageResource(R.drawable.ic_snack_morning);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_lunch)))
            imageView.setImageResource(R.drawable.ic_lunch);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_snack_afternoon)))
            imageView.setImageResource(R.drawable.ic_snack_afternoon);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_dinner)))
            imageView.setImageResource(R.drawable.ic_dinner);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_other)))
            imageView.setImageResource(R.drawable.ic_other);

        TextView textViewCategory = (TextView)convertView.findViewById(R.id.meal_row_linear_grid_layout_text_view_category);
        TextView textViewDescription = (TextView)convertView.findViewById(R.id.meal_row_linear_grid_layout_text_view_description);
        TextView textViewCalories = (TextView)convertView.findViewById(R.id.meal_row_linear_grid_layout_text_view_calories);

        textViewCategory.setText(getContext().getResources().getString(R.string.main_row_category) + ": " + meal.getCategory());
        textViewDescription.setText(getContext().getResources().getString(R.string.main_row_description) + ": " + meal.getDescription());
        textViewCalories.setText(getContext().getResources().getString(R.string.main_row_calories) + ": " + meal.getCalories());

        return convertView;
    }
}
