package com.tolgaozkaya.androdeprem;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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

public class EarthquakeArchiveActivity extends AppCompatActivity {

    private EarthquakeAdapter earthquakeAdapter;
    private EditText skipEditText;
    private EditText limitEditText;
    private EditText startDateEditText;
    private EditText endDateEditText;
    private TextView errorTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake_archive);

        ListView earthquakeListView = findViewById(R.id.depremList);
        skipEditText = findViewById(R.id.skipEditText);
        limitEditText = findViewById(R.id.limitEditText);
        startDateEditText = findViewById(R.id.startDateEditText);
        endDateEditText = findViewById(R.id.endDateEditText);
        Button searchButton = findViewById(R.id.searchButton);
        errorTextView = findViewById(R.id.errorTextView);

        List<Earthquake> earthquakes = new ArrayList<>();
        earthquakeAdapter = new EarthquakeAdapter(this, earthquakes);
        earthquakeListView.setAdapter(earthquakeAdapter);

        // Arama düğmesine tıklama olayını ayarla
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gerekli metin alanlarını al
                String skip = skipEditText.getText().toString();
                String limit = limitEditText.getText().toString();
                String startDate = startDateEditText.getText().toString();
                String endDate = endDateEditText.getText().toString();

                if (skip.isEmpty() || limit.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
                    errorTextView.setText("Tüm alanlar gereklidir.");
                } else {
                    errorTextView.setText("");
                    // Deprem arşivini ara
                    searchEarthquakeArchive(skip, limit, startDate, endDate);
                }
            }
        });
    }

    private void searchEarthquakeArchive(String skip, String limit, String startDate, String endDate) {
        // Deprem arşivini getiren AsyncTask'i başlat
        new EarthquakeArchiveAsyncTask().execute(skip, limit, startDate, endDate);
    }

    private class EarthquakeArchiveAsyncTask extends AsyncTask<String, Void, List<Earthquake>> {

        @Override
        protected List<Earthquake> doInBackground(String... params) {
            List<Earthquake> earthquakes = new ArrayList<>();

            try {
                String baseUrl = "https://api.orhanaydogdu.com.tr/deprem/kandilli/archive";
                String skipParam = params[0];
                String limitParam = params[1];
                String startDateParam = params[2];
                String endDateParam = params[3];
                String urlString = baseUrl + "?skip=" + skipParam + "&limit=" + limitParam + "&date=" + startDateParam + "&date_end=" + endDateParam;

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Yanıtı oku
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream, "UTF-8");
                String jsonResponse = scanner.useDelimiter("\\A").next();
                scanner.close();

                // JSON yanıtını analiz et
                JSONObject responseJson = new JSONObject(jsonResponse);
                JSONArray resultArray = responseJson.getJSONArray("result");

                for (int i = 0; i < resultArray.length(); i++) {
                    JSONObject earthquakeJson = resultArray.getJSONObject(i);

                    // Gerekli verileri çek
                    String title = earthquakeJson.getString("title");
                    String date = earthquakeJson.getString("date");
                    double magnitude = earthquakeJson.getDouble("mag");
                    int depth = earthquakeJson.getInt("depth");
                    double latitude = earthquakeJson.getJSONObject("geojson")
                            .getJSONArray("coordinates").getDouble(1);
                    double longitude = earthquakeJson.getJSONObject("geojson")
                            .getJSONArray("coordinates").getDouble(0);

                    // Deprem nesnesini oluştur ve listeye ekle
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

            earthquakeAdapter.clear();

            if (earthquakes.isEmpty()) {
                errorTextView.setText("Sonuç bulunamadı.");
            } else {
                errorTextView.setText("");
                // Deprem verilerini adaptöre ekle
                earthquakeAdapter.addAll(earthquakes);
            }
        }
    }
}
