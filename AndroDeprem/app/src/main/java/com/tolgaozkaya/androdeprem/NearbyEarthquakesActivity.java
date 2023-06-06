package com.tolgaozkaya.androdeprem;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NearbyEarthquakesActivity extends AppCompatActivity {

    private EarthquakeAdapter earthquakeAdapter;
    private LocationManager locationManager;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearbyearthquake);

        ListView earthquakeListView = findViewById(R.id.depremList);

        // Yeni bir EarthquakeAdapter oluştur
        List<Earthquake> earthquakes = new ArrayList<>();
        earthquakeAdapter = new EarthquakeAdapter(this, earthquakes);

        // Adapter'ı ListView'a ayarla
        earthquakeListView.setAdapter(earthquakeAdapter);

        // Konum yöneticisini başlat
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Konum izni verilmiş mi kontrol et
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Konum güncellemelerini iste
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            // Son bilinen konumu al
            currentLocation = getCurrentLocation();
        } else {
            // Konum izinlerini iste
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        // Deprem verilerini getir
        new EarthquakeAsyncTask().execute();
    }


    @SuppressLint("StaticFieldLeak")
    private class EarthquakeAsyncTask extends AsyncTask<Void, Void, List<Earthquake>> {

        @Override
        protected List<Earthquake> doInBackground(Void... voids) {
            List<Earthquake> earthquakes = new ArrayList<>();

            try {
                URL url = new URL("https://api.orhanaydogdu.com.tr/deprem/kandilli/live");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Yanıtı oku
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream, "UTF-8");
                String jsonResponse = scanner.useDelimiter("\\A").next();
                scanner.close();

                // JSON yanıtını çözümle
                JSONObject responseJson = new JSONObject(jsonResponse);
                JSONArray resultArray = responseJson.getJSONArray("result");

                if (currentLocation != null) {
                    for (int i = 0; i < resultArray.length(); i++) {
                        JSONObject earthquakeJson = resultArray.getJSONObject(i);

                        String title = earthquakeJson.getString("title");
                        String date = earthquakeJson.getString("date");
                        double magnitude = earthquakeJson.getDouble("mag");
                        int depth = earthquakeJson.getInt("depth");
                        double latitude = earthquakeJson.getJSONObject("geojson")
                                .getJSONArray("coordinates").getDouble(1);
                        double longitude = earthquakeJson.getJSONObject("geojson")
                                .getJSONArray("coordinates").getDouble(0);

                        // Mevcut konum ile deprem konumu arasındaki mesafeyi hesapla
                        float distance = calculateDistance(currentLocation.getLatitude(),
                                currentLocation.getLongitude(), latitude, longitude);

                        // Mesafe 150 km'den az ise depremi listeye ekle
                        if (distance < 150) {
                            Earthquake earthquake = new Earthquake(title, date, magnitude, depth, latitude, longitude);
                            earthquakes.add(earthquake);
                        }
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return earthquakes;
        }

        @Override
        protected void onPostExecute(List<Earthquake> earthquakes) {
            super.onPostExecute(earthquakes);

            // Önceki verileri temizle
            earthquakeAdapter.clear();

            // Yeni deprem verilerini ekle
            earthquakeAdapter.addAll(earthquakes);
        }
    }

    private Location getCurrentLocation() {
        // Konum izni verilmiş mi kontrol et
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Ağ sağlayıcısından son bilinen konumu al
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return null;
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            // Konum güncellendiğinde deprem verilerini getir
            currentLocation = location;
            new EarthquakeAsyncTask().execute();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    private float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0] / 1000; // Kilometreye çevir
    }
}
