package com.example.workshop;

import static java.lang.Integer.parseInt;
import static java.security.AccessController.getContext;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.workshop.databinding.ActivityAddRouteBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class AddRoute extends AppCompatActivity implements OnMapReadyCallback {

    private EditText startLocationEditText, destinationEditText, timeEditText, priceEditText;
    private Button showOnMapButton, addRouteButton;
    private GoogleMap mMap;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_route);

        // Иницијализација на Views
        startLocationEditText = findViewById(R.id.startLocationEditText);
        destinationEditText = findViewById(R.id.destinationEditText);
        timeEditText = findViewById(R.id.timeEditText);
        priceEditText = findViewById(R.id.pricePerPersonEditText);
        showOnMapButton = findViewById(R.id.showOnMapButton);
        addRouteButton = findViewById(R.id.AddRouteButton);

        // Иницијализација на базата
        db = new DatabaseHelper(this);
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        int driverId = db.getUserIdByUsername(username);

        // Иницијализирај ја мапата
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this); // Повикај ја мапата асинхроно
        }else {
            Toast.makeText(this, "Map Fragment is null!", Toast.LENGTH_SHORT).show();
        }

        // Клик на "Прикажи на мапа"
        showOnMapButton.setOnClickListener(v -> {
            String start = startLocationEditText.getText().toString();
            String end = destinationEditText.getText().toString();

            if (!start.isEmpty() && !end.isEmpty()) {
                showRouteOnMap(start, end);
            } else {
                Toast.makeText(this, "Внесете и почетна и дестинациска локација!", Toast.LENGTH_SHORT).show();
            }
        });

        // Клик на "Додај рута"
        addRouteButton.setOnClickListener(v -> {
            String start = startLocationEditText.getText().toString();
            String end = destinationEditText.getText().toString();
            String startTime = timeEditText.getText().toString();
            String clientList = "";
            int cena = Integer.parseInt(priceEditText.getText().toString());

            if (start.isEmpty() || end.isEmpty() || startTime.isEmpty()||cena==0) {
                Toast.makeText(this, "ПОПОЛНЕТЕ ГИ СИТЕ ПОЛИЊА!", Toast.LENGTH_LONG).show();
                return;
            }

            // Провери дали возачот има активно возило
            int vehicleId = db.getVehicleIdbyDriverId(driverId);
            if (vehicleId != -1) {

                boolean inserted = db.insertRide(vehicleId,driverId,start,end,startTime,clientList,cena);
                if (inserted) {
                    db.updateVehicleActiveStatus(driverId, 1);
                    Toast.makeText(this, "Успешно внесена рута", Toast.LENGTH_SHORT).show();

                    // Префрли се назад на UserDriver
                    Intent intent2 = new Intent(this, UserDriver.class);
                    intent2.putExtra("username",username);
                    startActivity(intent2);
                    finish();
                } else {
                    Toast.makeText(this, "Грешка при внесување на рутата!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Немате активно возило", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng macedoniaCenter = new LatLng(41.6086, 21.7453);
        float zoomLevel = 7.0f;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(macedoniaCenter, zoomLevel));
    }

    private void showRouteOnMap(String startLocation, String destination) {
        if (mMap == null) {
            Toast.makeText(this, "Мапата не е подготвена!", Toast.LENGTH_SHORT).show();
            return;
        }
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> startAddresses = geocoder.getFromLocationName(startLocation, 1);
            List<Address> endAddresses = geocoder.getFromLocationName(destination, 1);

            if (startAddresses != null && !startAddresses.isEmpty() &&
                    endAddresses != null && !endAddresses.isEmpty()) {

                Address startAddress = startAddresses.get(0);
                Address endAddress = endAddresses.get(0);

                LatLng startLatLng = new LatLng(startAddress.getLatitude(), startAddress.getLongitude());
                LatLng endLatLng = new LatLng(endAddress.getLatitude(), endAddress.getLongitude());

                // Јасно од мапата и додај маркери
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(startLatLng).title("Почетна локација: " + startLocation));
                mMap.addMarker(new MarkerOptions().position(endLatLng).title("Дестинација: " + destination));
                drawRoute(startLatLng, endLatLng);
                // Камерата да ги опфати двете локации
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(startLatLng);
                builder.include(endLatLng);
                LatLngBounds bounds = builder.build();
                int padding = 100;
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            } else {
                Toast.makeText(this, "Локациите не се пронајдени!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Грешка при пребарување на локациите!", Toast.LENGTH_SHORT).show();
        }
    }
    private void drawRoute(LatLng startLatLng, LatLng endLatLng) {
        String directionsUrl = "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + startLatLng.latitude + "," + startLatLng.longitude +
                "&destination=" + endLatLng.latitude + "," + endLatLng.longitude +
                "&mode=driving" +
                "&key="+getString(R.string.my_key);

        new Thread(() -> {
            try {
                URL url = new URL(directionsUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                inputStream.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray routes = jsonResponse.getJSONArray("routes");

                if (routes.length() > 0) {
                    JSONObject route = routes.getJSONObject(0);
                    JSONObject overviewPolyline = route.getJSONObject("overview_polyline");
                    String encodedPolyline = overviewPolyline.getString("points");

                    List<LatLng> decodedPath = decodePolyline(encodedPolyline);

                    runOnUiThread(() -> {
                        PolylineOptions polylineOptions = new PolylineOptions()
                                .addAll(decodedPath)
                                .color(Color.BLUE)
                                .width(10f);
                        mMap.addPolyline(polylineOptions);

                        // Додавање маркери за почетна и крајна точка
                        mMap.addMarker(new MarkerOptions().position(startLatLng).title("Почетна точка"));
                        mMap.addMarker(new MarkerOptions().position(endLatLng).title("Крајна точка"));

                        // Прилагодување на камерата за да се прикаже целата рута
                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                        for (LatLng latLng : decodedPath) {
                            boundsBuilder.include(latLng);
                        }
                        LatLngBounds bounds = boundsBuilder.build();
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    private List<LatLng> decodePolyline(String encodedPolyline) {
        List<LatLng> polyline = new ArrayList<>();
        int index = 0, len = encodedPolyline.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            polyline.add(point);
        }
        return polyline;
    }
}
