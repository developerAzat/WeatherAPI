package com.azat.firstproject;


import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final String OPEN_WEATHER_MAP_API_BY_CITY_NAME =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";
    private static  final String OPEN_WEATHER_MAP_API_BY_LAT_AND_LON =
            "http://api.openweathermap.org/data/2.5/weather?%s&units=metric";

    Handler handler;

    ArrayList<String> weathers = new ArrayList<>();

    ArrayAdapter<String> adapter;

    ArrayList<String> selectedWeathers = new ArrayList<>();
    ListView weatherList;

    private LocationManager locationManager;

    TextView currentCityView;


    @Override
    protected void onStart() {
        super.onStart();
        addWeatherInList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        handler = new Handler();


        weatherList = (ListView) findViewById(R.id.weatherList);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, weathers);
        weatherList.setAdapter(adapter);

        weatherList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapter.getItem(i);
                if (weatherList.isItemChecked(i) == true) {
                    selectedWeathers.add(item);
                } else {
                    selectedWeathers.remove(item);
                }
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        currentCityView = findViewById(R.id.currentCityView);

    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 1, locationListener);

        }
        catch (SecurityException e) {
            Toast.makeText(this,"Ошибка получения разрешения на доступ к GPS",Toast.LENGTH_LONG);
        }

        checkEnabled();
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }



    public void addWeatherInList() {
        FileInputStream fin = null;
        try {
            fin = openFileInput("content.txt");
            byte[] bytes = new byte[fin.available()];
            fin.read(bytes);
            String text = new String (bytes);
            final String[] cityNames = text.split(" ");

            for(int i=0;i<cityNames.length;i++) {

                final int finalI = i;
                new Thread() {
                    public void run() {
                            final JSONObject json = getJSON(cityNames[finalI]);
                            if(json != null){
                                handler.post(new Runnable(){
                                    public void run(){
                                        addWeather(json);
                                    }
                                });
                            }
                    }
                }.start();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally{

            try{
                if(fin!=null)
                    fin.close();
            }
            catch(IOException ex){

                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void addWeather(JSONObject json) {
        try {
            String item = "";
            item += json.getString("name");
            item += " " + String.format("%.2f", json.getJSONObject("main").getDouble("temp")) + " ℃ ";
            item += String.format("%.1f", json.getJSONObject("wind").getDouble("speed")) + " м/с";

            if(weathers.contains(item) == false) {
                weathers.add(item);
            }

            adapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void remove(View view) {
        for (int i = 0; i < selectedWeathers.size(); i++) {
            adapter.remove(selectedWeathers.get(i));
        }
        FileOutputStream fout = null;
        try {
            fout = openFileOutput("content.txt", MODE_PRIVATE);
            for (String item : weathers) {
                item = item.split(" ")[0];
                item += " ";
                fout.write(item.getBytes());
            }
        } catch (FileNotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        }
        weatherList.clearChoices();
        selectedWeathers.clear();
        adapter.notifyDataSetChanged();

    }


    public static JSONObject getJSON( String city){
        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API_BY_CITY_NAME, city));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key",
                    "99c2f3f29433a3ea1fc59ccc944382bb");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }

    public static JSONObject getJSON(double lat,double lon){
        String string = "lat=" + String.format("%.2f",lat) + "&lon=" +String.format("%.2f",lon);

        try {
            URL url = new URL(String.format(OPEN_WEATHER_MAP_API_BY_LAT_AND_LON, string));
            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key",
                    "99c2f3f29433a3ea1fc59ccc944382bb");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp="";
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());


            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }

    public void add(View view) {
        Intent intent = new Intent(this,AddCityActivity.class);

        startActivity(intent);
    }


    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }


        @Override
        public void onProviderEnabled(String s) {
            checkEnabled();
            try {
                showLocation(locationManager.getLastKnownLocation(s));
            }
            catch (SecurityException e) {
                Log.e("error","showLocationError");
            }

        }

        @Override
        public void onProviderDisabled(String s) {
            checkEnabled();
        }
    };



    private void showLocation(final Location location) {

        if (location == null) {
            return;
        }
        if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            new Thread() {
                public void run() {
                    final JSONObject json= getJSON(location.getLatitude(),location.getLongitude());
                    if(json == null){
                        handler.post(new Runnable(){
                            public void run(){
                                Log.e("error_lat_lon","erroLatlon");
                            }
                        });
                    } else {
                        handler.post(new Runnable(){
                            public void run(){
                                jsonCurrentCityViewShow(json);
                            }
                        });
                    }
                }
            }.start();

        }
    }

    private  void jsonCurrentCityViewShow(JSONObject json) {

        try {
            String string = json.getString("name") + " ";
            string += String.format("%.2f", json.getJSONObject("main").getDouble("temp")) + " ℃ ";
            string += String.format("%.1f", json.getJSONObject("wind").getDouble("speed")) + " м/с";
            currentCityView.setText(string);
        } catch (JSONException e) {
            Toast.makeText(this,e.getMessage(),Toast.LENGTH_LONG);
        }
    }

    private void checkEnabled() {
        TextView textView = findViewById(R.id.statusView);
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            textView.setText("GPS Включен");
        }
        else {
            textView.setText("Включите GPS для определения \n вашего текущего местоположения");
        }

    }
}
