package com.tolgaozkaya.androdeprem;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class EarthquakeAdapter extends ArrayAdapter<Earthquake> {

    public EarthquakeAdapter(Context context, List<Earthquake> earthquakes) {
        super(context, 0, earthquakes);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;
        if (listItemView == null) {
            // convertView null ise, row_layout.xml dosyasından bir görünüm oluşturulur
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.row_layout, parent, false);
        }

        // Mevcut Earthquake nesnesini al
        Earthquake currentEarthquake = getItem(position);

        // listItemView içinde ilgili TextView öğelerini bul
        TextView titleTextView = listItemView.findViewById(R.id.depremYeri);
        TextView dateTextView = listItemView.findViewById(R.id.depremTarih);
        TextView magTextView = listItemView.findViewById(R.id.depremGorseli);
        TextView depthTextView = listItemView.findViewById(R.id.depremDerinlik);

        // TextView öğelerine değerleri ata
        titleTextView.setText(currentEarthquake.getTitle());
        dateTextView.setText(currentEarthquake.getDate());
        magTextView.setText(String.valueOf(currentEarthquake.getMagnitude()));
        depthTextView.setText(String.valueOf(currentEarthquake.getDepth() + " KM"));

        // Büyüklüğe bağlı olarak öğenin arka plan rengini özelleştir
        double magnitude = currentEarthquake.getMagnitude();
        if (magnitude > 5) {
            magTextView.setBackgroundResource(R.drawable.circle_deprem_kirmizi);
        } else if (magnitude < 3) {
            magTextView.setBackgroundResource(R.drawable.circle_deprem_yesil);
        } else {
            magTextView.setBackgroundResource(R.drawable.circle_deprem_turuncu);
        }

        // listItemView üzerine tıklama dinleyicisini ayarla
        listItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Deprem konumunu içeren koordinatlarla Google Haritalar'ı aç
                double latitude = currentEarthquake.getLatitude();
                double longitude = currentEarthquake.getLongitude();

                String location = "geo:" + latitude + "," + longitude + "?q=" + latitude + "," + longitude;
                Uri gmmIntentUri = Uri.parse(location);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    getContext().startActivity(mapIntent);
                }
            }
        });

        return listItemView;
    }
}
