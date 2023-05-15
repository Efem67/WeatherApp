package com.example.weatherappv2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private TextView locationNameTv;
    private TextView descriptionTv;
    private TextView temperatureTv;
    private TextView humidityTv;
    private TextView maxTempTv;
    private TextView minTempTv;
    private TextView pressureTv;
    private TextView windSpeedTv;
    private ImageView mainWeatherIv;
    private Button changeLocationBtn;
    private Button getCurrentLocationBtn;
    private EditText enterLocationInput;
    LocationManager locationManager;
    double lat;
    double lon;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationNameTv = findViewById(R.id.locationName);
        descriptionTv = findViewById(R.id.description);
        temperatureTv = findViewById(R.id.temperature);
        humidityTv = findViewById(R.id.humidity);
        maxTempTv = findViewById(R.id.maxTemp);
        minTempTv = findViewById(R.id.minTemp);
        pressureTv = findViewById(R.id.pressure);
        windSpeedTv = findViewById(R.id.windSpeed);
        mainWeatherIv = findViewById(R.id.mainWeather);
        changeLocationBtn = findViewById(R.id.changeLocation);
        getCurrentLocationBtn = findViewById(R.id.refreshLocation);
        enterLocationInput = findViewById(R.id.enterLocationEt);

        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        } else {
            getLocation();
        }

        changeLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newLocation = String.valueOf(enterLocationInput.getText());

                if (newLocation.equals("")) return;
                try {
                    getWeatherInfo(newLocation, null, null);
                }
                catch (Exception e){

                }

            }
        });

        getCurrentLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });

    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", new  DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            Location myLocation = getLastKnownLocation();
//            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (myLocation != null) {
                lat = myLocation.getLatitude();
                lon = myLocation.getLongitude();

                Log.d("XXX", "LAT: " + lat + " LON: " + lon);
                getWeatherInfo(null, lat, lon);
            }
            else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    LocationManager mLocationManager;


    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public void getWeatherInfo(String cityName, Double latitude, Double longitude) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherAPI weatherApi = retrofit.create(WeatherAPI.class);
        Call<OpenWeatherMap> call;
        if (cityName == null) {
            call = weatherApi.getWeatherWithLocation(latitude, longitude);
        } else {
            call = weatherApi.getWeatherWithName(cityName);
        }


        call.enqueue(new Callback<OpenWeatherMap>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call<OpenWeatherMap> call, Response<OpenWeatherMap> response) {
                if (!response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, response.code(), Toast.LENGTH_SHORT).show();
                    return;
                }
                OpenWeatherMap list = response.body();
                String locationName = String.valueOf(list.getName());
                String locationShortcut = String.valueOf(list.getSys().getCountry());

                String temperature = String.valueOf(list.getMain().getTemp());
                String humidity = String.valueOf(list.getMain().getHumidity());
                String maxTemp = String.valueOf(list.getMain().getTempMax());
                String minTemp = String.valueOf(list.getMain().getTempMin());
                String pressure = String.valueOf(list.getMain().getPressure());
                String windSpeed = String.valueOf(list.getWind().getSpeed());

//                Log.d("retrofit", "Temperature: " + temperature);
//                Log.d("retrofit", "Humidity: " + humidity);
//                Log.d("retrofit", "Max Temp: " + maxTemp);
//                Log.d("retrofit", "Min Temp: " + minTemp);
//                Log.d("retrofit", "Pressure: " + pressure);
//                Log.d("retrofit", "WindSpeed: " + windSpeed);


                locationNameTv.setText(locationName + ", " + locationShortcut);

                if (Integer.parseInt(humidity) <= 20) {
                    descriptionTv.setText("Full sunlight");
                    mainWeatherIv.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_wb_sunny_24));
                } else if (Integer.parseInt(humidity) > 20 && Integer.parseInt(humidity) < 60) {
                    descriptionTv.setText("Few clouds");
                    mainWeatherIv.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.clouded_weather_svgrepo_com));
                } else {
                    descriptionTv.setText("A lot of clouds");
                    mainWeatherIv.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_baseline_cloud_24));
                }

                temperatureTv.setText(temperature + " °C");
                humidityTv.setText(humidity + "%");
                maxTempTv.setText(maxTemp + " °C");
                minTempTv.setText(minTemp + " °C");
                pressureTv.setText(pressure + " hpA");
                windSpeedTv.setText(windSpeed + " km/h");


//                Toast.makeText(MainActivity.this, "ezzz", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<OpenWeatherMap> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Problem z pobraniem danych", Toast.LENGTH_SHORT).show();
            }
        });
    }







}