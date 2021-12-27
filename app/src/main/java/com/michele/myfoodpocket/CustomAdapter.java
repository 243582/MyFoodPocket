package com.michele.myfoodpocket;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
        return getViewOptimize(position, convertView, parent);
    }

    // Metodo ottimizzato per il reperimento degli oggetti della lista
    public View getViewOptimize(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) { // Se è la prima volta che leggo un oggetto della lista imposto il tag, in maniera da potermi basare su di esso le volte successive senza fare inflating
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.meal_row, null);
            viewHolder = new ViewHolder();
            viewHolder.category = (TextView)convertView.findViewById(R.id.meal_row_linear_grid_layout_text_view_category);
            viewHolder.description = (TextView)convertView.findViewById(R.id.meal_row_linear_grid_layout_text_view_description);
            viewHolder.calories = (TextView)convertView.findViewById(R.id.meal_row_linear_grid_layout_text_view_calories);
            viewHolder.picture = (ImageView)convertView.findViewById(R.id.meal_row_linear_image_view);
            convertView.setTag(viewHolder);
        } else { // Altrimenti utilizzo il tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Meal meal = getItem(position);

        ImageView imageView = (ImageView)convertView.findViewById(R.id.meal_row_linear_image_view);
        if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_breakfast)))
            viewHolder.picture.setImageResource(R.drawable.ic_breakfast);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_snack_morning)))
            viewHolder.picture.setImageResource(R.drawable.ic_snack_morning);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_lunch)))
            viewHolder.picture.setImageResource(R.drawable.ic_lunch);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_snack_afternoon)))
            viewHolder.picture.setImageResource(R.drawable.ic_snack_afternoon);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_dinner)))
            viewHolder.picture.setImageResource(R.drawable.ic_dinner);
        else if(meal.getCategory().equals(getContext().getResources().getString(R.string.main_category_other)))
            viewHolder.picture.setImageResource(R.drawable.ic_other);

        viewHolder.category.setText(getContext().getResources().getString(R.string.main_row_category) + ": " + meal.getCategory());
        viewHolder.description.setText(getContext().getResources().getString(R.string.main_row_description) + ": " + meal.getDescription());
        viewHolder.calories.setText(getContext().getResources().getString(R.string.main_row_calories) + ": " + meal.getCalories());

        return convertView;
    }

    private class ViewHolder {
        public TextView category;
        public TextView description;
        public TextView calories;
        public ImageView picture;
    }
}
