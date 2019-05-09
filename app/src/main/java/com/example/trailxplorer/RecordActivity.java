package com.example.trailxplorer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class RecordActivity extends AppCompatActivity {

    double alt;
    String chronometer;
    String distance;

    TextView altitudeTxt;
    TextView chronoTxt;
    TextView distanceTxt;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_main);

        altitudeTxt = findViewById(R.id.record_max_altitude);
        chronoTxt = findViewById(R.id.record_chronometer);
        distanceTxt = findViewById(R.id.distance);

        Intent i = getIntent();
        alt = i.getDoubleExtra("Altitude", 0);
        chronometer = i.getStringExtra("Chronometer");
        distance =  i.getStringExtra("Distance");

        altitudeTxt.setText("Max Altitude: " + String.valueOf(alt));
        chronoTxt.setText("Time taken: " + chronometer);
        distanceTxt.setText("Distance travelled: " + distance);
    }
}
