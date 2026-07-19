package org.seniorconnect.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shows live, user-requested location and nearby public places without retaining location data. */
public final class MapsActivity extends Activity {
    private static final int LOCATION_REQUEST = 100;
    private static final int SEARCH_RADIUS_METERS = 5_000;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView locationValue;
    private TextView[] placeNames;
    private TextView[] placeDistances;
    private View retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        locationValue = findViewById(R.id.current_location_value);
        placeNames = new TextView[] {
                findViewById(R.id.hospital_name),
                findViewById(R.id.bus_stop_name),
                findViewById(R.id.supermarket_name)
        };
        placeDistances = new TextView[] {
                findViewById(R.id.hospital_distance),
                findViewById(R.id.bus_stop_distance),
                findViewById(R.id.supermarket_distance)
        };
        retryButton = findViewById(R.id.retry_locations);
        findViewById(R.id.back_to_home).setOnClickListener(view -> finish());
        retryButton.setOnClickListener(view -> startLocationFlow());
        startLocationFlow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == LOCATION_REQUEST && hasLocationPermission()) {
            getCurrentLocation();
        } else if (requestCode == LOCATION_REQUEST) {
            showLocationProblem(R.string.location_permission_needed);
        }
    }

    @Override
    protected void onDestroy() {
        executor.shutdownNow();
        super.onDestroy();
    }

    private void startLocationFlow() {
        retryButton.setVisibility(View.GONE);
        setLoading();
        if (!hasLocationPermission()) {
            requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, LOCATION_REQUEST);
            return;
        }
        getCurrentLocation();
    }

    private boolean hasLocationPermission() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressWarnings("MissingPermission")
    private void getCurrentLocation() {
        LocationManager manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Location last = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (last == null) last = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (last != null) {
            loadNearbyPlaces(last);
            return;
        }
        try {
            manager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override public void onLocationChanged(Location location) { loadNearbyPlaces(location); }
                @Override public void onProviderDisabled(String provider) { showLocationProblem(R.string.location_unavailable); }
            }, Looper.getMainLooper());
        } catch (Exception error) {
            showLocationProblem(R.string.location_unavailable);
        }
    }

    private void loadNearbyPlaces(Location location) {
        executor.execute(() -> {
            try {
                String locality = findLocality(location);
                Place[] places = findPlaces(location);
                runOnUiThread(() -> showResults(locality, places));
            } catch (Exception error) {
                runOnUiThread(() -> showPlacesProblem());
            }
        });
    }

    private String findLocality(Location location) throws Exception {
        List<Address> addresses = new Geocoder(this, Locale.getDefault())
                .getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        if (addresses == null || addresses.isEmpty()) return getString(R.string.location_unavailable);
        Address address = addresses.get(0);
        List<String> parts = new ArrayList<>();
        if (address.getLocality() != null) parts.add(address.getLocality());
        if (address.getAdminArea() != null) parts.add(address.getAdminArea());
        if (address.getCountryName() != null) parts.add(address.getCountryName());
        return parts.isEmpty() ? getString(R.string.location_unavailable) : join(parts);
    }

    private Place[] findPlaces(Location origin) throws Exception {
        String query = "[out:json][timeout:15];("
                + "nwr(around:" + SEARCH_RADIUS_METERS + "," + origin.getLatitude() + "," + origin.getLongitude() + ")[amenity=hospital];"
                + "nwr(around:" + SEARCH_RADIUS_METERS + "," + origin.getLatitude() + "," + origin.getLongitude() + ")[highway=bus_stop];"
                + "nwr(around:" + SEARCH_RADIUS_METERS + "," + origin.getLatitude() + "," + origin.getLongitude() + ")[shop=supermarket];);out center tags;";
        String url = "https://overpass-api.de/api/interpreter?data="
                + URLEncoder.encode(query, StandardCharsets.UTF_8.name());
        HttpURLConnection connection = (HttpURLConnection) new java.net.URL(url).openConnection();
        connection.setConnectTimeout(12_000);
        connection.setReadTimeout(15_000);
        connection.setRequestProperty("User-Agent", "SeniorConnect/0.1");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
            JSONArray elements = new JSONObject(body.toString()).getJSONArray("elements");
            Place[] closest = new Place[3];
            for (int index = 0; index < elements.length(); index++) {
                JSONObject element = elements.getJSONObject(index);
                JSONObject tags = element.optJSONObject("tags");
                if (tags == null) continue;
                int category = categoryFor(tags);
                if (category < 0) continue;
                double latitude = element.has("lat") ? element.getDouble("lat") : element.getJSONObject("center").getDouble("lat");
                double longitude = element.has("lon") ? element.getDouble("lon") : element.getJSONObject("center").getDouble("lon");
                double distance = distanceKm(origin.getLatitude(), origin.getLongitude(), latitude, longitude);
                String name = tags.optString("name", getString(defaultPlaceNameFor(category)));
                if (closest[category] == null || distance < closest[category].distanceKm) {
                    closest[category] = new Place(name, distance);
                }
            }
            return closest;
        } finally {
            connection.disconnect();
        }
    }

    private int categoryFor(JSONObject tags) {
        if ("hospital".equals(tags.optString("amenity"))) return 0;
        if ("bus_stop".equals(tags.optString("highway"))) return 1;
        if ("supermarket".equals(tags.optString("shop"))) return 2;
        return -1;
    }

    private int defaultPlaceNameFor(int category) {
        if (category == 0) return R.string.nearby_hospital;
        if (category == 1) return R.string.nearby_bus_stop;
        return R.string.nearby_supermarket;
    }

    private void showResults(String locality, Place[] places) {
        locationValue.setText(locality);
        for (int index = 0; index < places.length; index++) {
            Place place = places[index];
            if (place == null) {
                placeNames[index].setText(R.string.no_place_found);
                placeDistances[index].setText(R.string.blank);
            } else {
                placeNames[index].setText(place.name);
                placeDistances[index].setText(getString(R.string.distance_away, place.distanceKm));
            }
        }
    }

    private void setLoading() {
        locationValue.setText(R.string.location_loading);
        for (int index = 0; index < placeNames.length; index++) {
            placeNames[index].setText(R.string.places_loading);
            placeDistances[index].setText(R.string.blank);
        }
    }

    private void showLocationProblem(int message) {
        locationValue.setText(message);
        for (int index = 0; index < placeNames.length; index++) {
            placeNames[index].setText(R.string.no_place_found);
            placeDistances[index].setText(R.string.blank);
        }
        retryButton.setVisibility(View.VISIBLE);
    }

    private void showPlacesProblem() {
        locationValue.setText(R.string.location_unavailable);
        for (int index = 0; index < placeNames.length; index++) {
            placeNames[index].setText(R.string.places_unavailable);
            placeDistances[index].setText(R.string.blank);
        }
        retryButton.setVisibility(View.VISIBLE);
    }

    private static String join(List<String> parts) { return android.text.TextUtils.join(", ", parts); }
    private static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1), dLon = Math.toRadians(lon2 - lon1);
        double value = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 6371.0 * 2 * Math.atan2(Math.sqrt(value), Math.sqrt(1 - value));
    }

    private static final class Place {
        final String name; final double distanceKm;
        Place(String name, double distanceKm) { this.name = name; this.distanceKm = distanceKm; }
    }
}
