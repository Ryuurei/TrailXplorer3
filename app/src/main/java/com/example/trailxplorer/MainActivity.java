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
import android.widget.ArrayAdapter;
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

    private ArrayList<Location> trackPoints = new ArrayList<>();

    String directoryName = "GPStracks";
    String fileName;
    GPXHelper gpxHelper;
    File dir;
    File file;
    double totatDistance;
    double altitude;

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
                    totatDistance = getDistance(trackPoints);
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
        String dst = String.valueOf(totatDistance);
        String alt = String.valueOf(altitude);
        String chrono = (String) chronometer.getText();
        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra("Altitude", alt);
        intent.putExtra("Chronometer", chrono);
        intent.putExtra("Distance", dst);
        startActivity(intent);
    }

    public double getDistance(ArrayList<Location> trackpoints)
    {
        double total = 0;
        for(int i = 0; i < trackpoints.size() - 1; i++)
        {
            Location loc1 = trackpoints.get(i);
            Location loc2 = trackpoints.get(i + 1);
            total += distanceTwoPoints(loc1.getLatitude(), loc2.getLatitude(), loc1.getLongitude(), loc2.getLongitude());
            altitude = Math.max(loc1.getAltitude(), loc2.getAltitude());
        }
        return total;
    }

    public double distanceTwoPoints(double lat1, double lat2, double lon1, double lon2)
    {
        double earthRadius = 3958.75;
        double distanceLat = Math.toRadians(lat2 - lat1);
        double distanceLon = Math.toRadians(lon2 - lon1);
        double a = (Math.sin(distanceLat / 2) * Math.sin(distanceLat / 2))
                + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(distanceLon / 2) * Math.sin(distanceLon / 2));
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;
        double meterConversion = 1609;
        return (int) (distance * meterConversion);
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
                    trackPoints.add(location);
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
                            trackPoints.add(l);
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
