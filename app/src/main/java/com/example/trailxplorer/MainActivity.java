package com.example.trailxplorer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager;

    private Chronometer chronometer;

    private Button recordButton;
    private TextView recordText;
    private TextView latUp;
    private TextView longUp;

    private int MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION = 1;
    private int MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private int MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private int MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private double lat = 0;
    private double lon = 0;

    private long pauseOffset;
    private boolean running = false;

    Boolean permissionExternalStorage;

    private ArrayList<TrackPoint> trackPoints = new ArrayList<>(); //Shouldn't have to use it

    String directoryName = "GPStracks";
    String fileName;
    GPXHelper gpxHelper;
    File dir;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gpxHelper = new GPXHelper(this);
        fileName = Calendar.getInstance().getTime().toString();

        chronometer = findViewById(R.id.chronometer);
        recordText = findViewById(R.id.txtRecord);
        latUp = findViewById(R.id.latitudeUpdate);
        longUp = findViewById(R.id.longitudeUpdate);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        permissionExternalStorage();
        addLocationListener();

        recordButton = findViewById(R.id.recordButton);

        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!running)
                {
                    startChronometer();
                    dir = gpxHelper.newDirectory(directoryName);
                    file = gpxHelper.newFile(dir, fileName);
                }
                else
                {
                    gpxHelper.closeFile(file);
                    intoRecordActivity();
                }
            }
        });
    }

    public void startChronometer()
    {
        recordText.setText("Recording your journey!");
        chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
        chronometer.start();
        running = true;
    }

    public void intoRecordActivity()
    {
        recordText.setText("");
        chronometer.stop();
        pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
        running = false;
        String chrono = (String) chronometer.getText();
        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra("Latitude", lat);
        intent.putExtra("Longitude", lon);
        intent.putExtra("Chronometer", chrono);
        startActivity(intent);

    }

    private void addLocationListener() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_REQUEST_ACCESS_FINE_LOCATION);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSION_REQUEST_ACCESS_COARSE_LOCATION);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if(running)
                {
                    gpxHelper.writeInFile(file, location);
                    lat = location.getLatitude();
                    lon = location.getLongitude();
                    latUp.setText("Current Latitude: " + location.getLatitude());
                    longUp.setText("Current Longitude: " + location.getLongitude());
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                if (provider == LocationManager.GPS_PROVIDER) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(l != null)
                    {
                        if(running)
                        {
                            gpxHelper.writeInFile(file, l);
                            lat = l.getLatitude();
                            lon = l.getLongitude();
                            latUp.setText("Current Latitude: " + l.getLatitude());
                            longUp.setText("Current Longitude: " + l.getLongitude());
                        }
                    }
                }
            }

            @Override
            public void onProviderDisabled(String provider) {
                if(provider == LocationManager.GPS_PROVIDER)
                {
                    if(running)
                    {
                        lat = 0.0;
                        lon = 0.0;
                        latUp.setText("Current Latitude: N/A");
                        longUp.setText("Current Longitude: N/A");
                    }
                }
            }
        });
    }

    public void permissionExternalStorage()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            permissionExternalStorage = !(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED);
                return ;
        }
    }
}
