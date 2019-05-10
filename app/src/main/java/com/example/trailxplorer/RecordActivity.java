package com.example.trailxplorer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.ArrayList;

public class RecordActivity extends AppCompatActivity {

    double alt;
    String chronometer;
    String distance;
    String averageSpeed;
    ArrayList<Integer> speed;

    TextView altitudeTxt;
    TextView chronoTxt;
    TextView distanceTxt;
    TextView averageSpeedTxt;
    TextView speedTxt;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.record_main);

        altitudeTxt = findViewById(R.id.record_max_altitude);
        chronoTxt = findViewById(R.id.record_chronometer);
        distanceTxt = findViewById(R.id.distance);
        averageSpeedTxt = findViewById(R.id.AverageSpeed);
        speedTxt = findViewById(R.id.speed);

        getSupportActionBar().setTitle("Trail Analytics");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        alt = i.getDoubleExtra("Altitude", 0);
        chronometer = i.getStringExtra("Chronometer");
        distance =  i.getStringExtra("Distance");
        averageSpeed = i.getStringExtra("Average Speed");
        speed = i.getIntegerArrayListExtra("Speed Through Time");

        altitudeTxt.setText("Max Altitude: " + String.valueOf(alt) + " in WGS 84 reference ellipsoid");
        chronoTxt.setText("Time taken: " + chronometer);
        distanceTxt.setText("Distance travelled: " + distance + " m");
        averageSpeedTxt.setText("Average Speed: " + averageSpeed + " k/h");

        if(speed.size() > 0)
        {
            String s = "";
            for(int j = 0; j < speed.size() - 1; j++)
            {
                s += "Speed " + j + ": " + speed.get(j) + " in k/h\n";
            }
            s += "Speed " + (speed.size() - 1) + ": " + speed.get(speed.size() - 1);
            speedTxt.setText(s);
        }
        else
        {
            speedTxt.setText("Go outside you fucking idiot");
        }
    }
}
