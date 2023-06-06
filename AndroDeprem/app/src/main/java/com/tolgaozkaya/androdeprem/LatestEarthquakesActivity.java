package com.tolgaozkaya.androdeprem;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

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

public class LatestEarthquakesActivity extends AppCompatActivity {

    private EarthquakeAdapter earthquakeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_latestearthquake);

        ListView earthquakeListView = findViewById(R.id.depremList);

        // Yeni bir EarthquakeAdapter oluştur
        List<Earthquake> earthquakes = new ArrayList<>();
        earthquakeAdapter = new EarthquakeAdapter(this, earthquakes);

        // Adapter'ı ListView'a ayarla
        earthquakeListView.setAdapter(earthquakeAdapter);

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

                // JSON dizisini dolaşarak deprem verilerini al
                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject earthquakeJson = resultArray.getJSONObject(i);

                    // Veri alanlarını çözümle
                    String title = earthquakeJson.getString("title");
                    String date = earthquakeJson.getString("date");
                    double magnitude = earthquakeJson.getDouble("mag");
                    int depth = earthquakeJson.getInt("depth");
                    double latitude = earthquakeJson.getJSONObject("geojson")
                            .getJSONArray("coordinates").getDouble(1);
                    double longitude = earthquakeJson.getJSONObject("geojson")
                            .getJSONArray("coordinates").getDouble(0);

                    // Yeni Earthquake nesnesi oluştur ve listeye ekle
                    Earthquake earthquake = new Earthquake(title, date, magnitude, depth, latitude, longitude);
                    earthquakes.add(earthquake);
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
}
