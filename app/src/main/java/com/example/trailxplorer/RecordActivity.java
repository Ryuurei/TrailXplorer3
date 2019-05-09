package com.example.trailxplorer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class RecordActivity extends AppCompatActivity {

    double lat;
    double lon;
    String chronometer;

    TextView latitudeTxt;
    TextView longitudeTxt;
    TextView chronoTxt;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_main);

        latitudeTxt = findViewById(R.id.record_latitude);
        longitudeTxt = findViewById(R.id.record_longitude);
        chronoTxt = findViewById(R.id.record_chronometer);

        Intent i = getIntent();
        lat = i.getDoubleExtra("Latitude", 0);
        lon = i.getDoubleExtra("Longitude", 0);
        chronometer = i.getStringExtra("Chronometer");

        latitudeTxt.setText("Latitude: " + String.valueOf(lat));
        longitudeTxt.setText("Longitude: " + String.valueOf(lon));
        chronoTxt.setText("Timer: " + chronometer);

    }
}
