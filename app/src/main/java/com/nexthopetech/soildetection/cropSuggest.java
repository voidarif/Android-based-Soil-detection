package com.nexthopetech.soildetection;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class cropSuggest extends AppCompatActivity {

    TextView showSoilName, suggestedCrops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_suggest);

        showSoilName = findViewById(R.id.show_soil_name);
        suggestedCrops = findViewById(R.id.suggestedCrops);

        Bundle extras = getIntent().getExtras();
        String soilName = extras.getString("soil_Name");
        showSoilName.setText(soilName);

        if(soilName.equals("")){
            suggestedCrops.setText("NO soil detected.");
        }


        if(soilName.equals("Black Soil")){
            suggestedCrops.setText("cotton, wheat, jowar, linseed, Virginia tobacco, castor, sunflower and millets.");
        }
        if(soilName.equals("Cinder Soil")){
            suggestedCrops.setText("Strawberries, peas, pole beans, lettuce, spinach, eggplants, scallions, carrots, garlic, and beets.");
        }

        if(soilName.equals("Laterite Soil")){
            suggestedCrops.setText("tea, coffee, rubber, cinchona, coconut, areca nut etc.");
        }
        if(soilName.equals("Peat Soil")){
            suggestedCrops.setText("potatoes, sugar beet, celery, onions, carrots, lettuce and market garden crops.");
        }
        if(soilName.equals("Yellow Soil")){
            suggestedCrops.setText("maize, groundnut, rice, fruits like mango, orange, vegetables, potato, and pulses.");
        }



    }
}