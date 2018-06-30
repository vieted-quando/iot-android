package com.example.asus.smartdryingrack;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Typeface weatherFont;

    ConstraintLayout main_layout;

    TextView txtLocation;
    TextView txtWeatherIcon;
    TextView txtWeatherTemp;
    TextView txtWeatherDetail;

    TextView txtStatus;
    TextView txtHomeInfo;

    Button btnChangeLocation;
    Button btnRefresh;
    Button btnOnOff;

    ProgressDialog dialog;

    boolean isConnected;
    boolean isOn;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_layout = findViewById(R.id.main_layout);

        txtLocation = (TextView)findViewById(R.id.txtLocation);
        txtWeatherIcon = (TextView)findViewById(R.id.txtWeatherIcon);
        txtWeatherTemp = (TextView)findViewById(R.id.txtWeatherTemp);
        txtWeatherDetail = (TextView)findViewById(R.id.txtWeatherDetail);

        txtStatus = (TextView)findViewById(R.id.txtStatus);
        txtHomeInfo = (TextView)findViewById(R.id.txtHomeInfo);

        btnChangeLocation = (Button)findViewById(R.id.btnChangeLocation);
        btnRefresh = (Button)findViewById(R.id.btnRefresh);
        btnOnOff = (Button)findViewById(R.id.btnOnOff);

        btnChangeLocation.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);
        btnOnOff.setOnClickListener(this);

        isConnected = false;
        isOn = false;

        weatherFont = Typeface.createFromAsset(this.getAssets(), "fonts/weather.ttf");
        txtWeatherIcon.setTypeface(weatherFont);

        handler = new Handler();

        btnOnOff.setEnabled(false);
        connectToServer();
    }

    private void connectToServer() {
        final String location = new CityPreference(this).getCity();
        final Context context = this;
        updateWeatherData(context, location);
        updateHomeInfo(context);
    }

    private void updateWeatherData(final Context context, final String city) {
        new Thread(){
            public void run(){
                final JSONObject weatherInfo = FetchInfo.getWeather(city);
                if (weatherInfo == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(context, "Cannot get weather", Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            showWeatherInfo(weatherInfo);
                        }
                    });
                }
            }
        }.start();
    }

    private void updateHomeInfo(final Context context) {
        btnOnOff.setEnabled(false);

        new Thread(){
            public void run(){
                final JSONObject homeInfo = FetchInfo.getHomeInfo();
                if (homeInfo == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(context, "Cannot get home info", Toast.LENGTH_LONG).show();
                            btnOnOff.setEnabled(false);
                        }
                    });
                } else {
                    handler.post(new Runnable(){
                        public void run(){
                            showHomeInfo(homeInfo);
                            btnOnOff.setEnabled(true);
                        }
                    });
                }
            }
        }.start();
    }

    private void showWeatherInfo(JSONObject json){
        try {
            txtLocation.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("main");
            txtWeatherDetail.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Humidity: " + main.getString("humidity") + "%" +
                            "\n" + "Pressure: " + main.getString("pressure") + " hPa");

            txtWeatherTemp.setText(
                    String.format("%.2f", main.getDouble("temp"))+ " ℃");

            setWeatherIcon(details.getInt("id"),
                    json.getJSONObject("sys").getLong("sunrise") * 1000,
                    json.getJSONObject("sys").getLong("sunset") * 1000);

        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

    private void showHomeInfo(JSONObject json) {
        try {
            String status = json.getString("drying");
            if (status.trim().equalsIgnoreCase("Y")) {
                isOn = true;
                txtStatus.setText("Status: On");
            }
            else {
                isOn = false;
                txtStatus.setText("Status: Off");
            }

            double temp = json.getDouble("temperature");
            double humid = json.getDouble("humidity");
            String rain = json.getString("rainDrops");
            String info = "Temp: " + temp + " ℃";
            info += "\n" + "Humid: " + humid;
            info += "\n" + "Raindrop: " + rain;

            txtHomeInfo.setText(info);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (isOn) {
            btnOnOff.setText("Turn OFF");
        }
        else {
            btnOnOff.setText("Turn ON");
        }
    }

    private void setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = "";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = this.getString(R.string.weather_sunny);
            } else {
                icon = this.getString(R.string.weather_clear_night);
            }
        } else {
            switch(id) {
                case 2 : icon = this.getString(R.string.weather_thunder);
                    break;
                case 3 : icon = this.getString(R.string.weather_drizzle);
                    break;
                case 7 : icon = this.getString(R.string.weather_foggy);
                    break;
                case 8 : icon = this.getString(R.string.weather_cloudy);
                    break;
                case 6 : icon = this.getString(R.string.weather_snowy);
                    break;
                case 5 : icon = this.getString(R.string.weather_rainy);
                    break;
            }
        }
        txtWeatherIcon.setText(icon);
    }

    private void showLoadingDialog() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Connecting to server, please wait...");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();
    }

    private void hideLoadingDialog() {
        dialog.hide();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.btnChangeLocation:
                changeLocation();
                break;
            case R.id.btnRefresh:
                updateHomeInfo(this);
            case R.id.btnOnOff:
                turnOnOff();
                break;
        }
    }

    private void changeLocation() {
        showInputDialog();
    }

    private void showInputDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change city");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Go", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeCity(input.getText().toString());
            }
        });
        builder.show();
    }

    public void changeCity(String city){
        updateWeatherData(this, city);
        new CityPreference(this).setCity(city);
    }

    private void turnOnOff() {
        if (isOn) {
            turnOff();
        }
        else {
            turnOn();
        }
    }

    private void turnOn() {
        new Thread(){
            public void run(){
                FetchInfo.turnOn();
            }
        }.start();
        final Context context = this;
        CountDownTimer timer = new CountDownTimer(2000, 100) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                updateHomeInfo(context);
            }
        };
        timer.start();
    }

    private void turnOff() {
        new Thread(){
            public void run(){
                FetchInfo.turnOff();
            }
        }.start();
        final Context context = this;
        CountDownTimer timer = new CountDownTimer(2000, 100) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                updateHomeInfo(context);
            }
        };
        timer.start();
    }
}
