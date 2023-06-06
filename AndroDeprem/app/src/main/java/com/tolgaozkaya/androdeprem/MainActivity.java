package com.tolgaozkaya.androdeprem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openLatestEarthquakesActivity(View view) {
        Intent intent = new Intent(this, LatestEarthquakesActivity.class);
        startActivity(intent);
    }

    public void openNearbyEarthquakesActivity(View view) {
        Intent intent = new Intent(this, NearbyEarthquakesActivity.class);
        startActivity(intent);
    }

    public void openSearchEarthquakesActivity(View view) {
        Intent intent = new Intent(MainActivity.this, EarthquakeArchiveActivity.class);
        startActivity(intent);
    }


}
