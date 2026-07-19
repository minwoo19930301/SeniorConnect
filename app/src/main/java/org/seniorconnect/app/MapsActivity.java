package org.seniorconnect.app;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Icon;
import org.maplibre.android.annotations.IconFactory;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
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
import java.util.regex.Pattern;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shows live, user-requested location and nearby public places without retaining location data. */
public final class MapsActivity extends Activity {
    private static final int LOCATION_REQUEST = 100;
    private static final int SEARCH_RADIUS_METERS = 5_000;
    private static final Pattern SUPERMARKET_NAME_CONFLICT = Pattern.compile(
            "\\b(jewel(?:l?er(?:y|s)?|ry)|goldsmith)\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern HOSPITAL_NAME_CONFLICT = Pattern.compile(
            "\\b(pharmacy|chemist|dentist|dental|laboratory|diagnostic|clinic)\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern BUS_STOP_NAME_CONFLICT = Pattern.compile(
            "\\b(railway|train|subway)\\b", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    // OpenFreeMap serves an OpenStreetMap-based style. The map keeps MapLibre attribution enabled.
    private static final String OPEN_STREET_MAP_STYLE = "https://tiles.openfreemap.org/styles/liberty";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private TextView locationValue;
    private View retryButton;
    private MapView mapView;
    private MapLibreMap map;
    private Location currentLocation;
    private Place[] currentPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapLibre.getInstance(getApplicationContext());
        setContentView(R.layout.activity_maps);
        locationValue = findViewById(R.id.current_location_value);
        mapView = findViewById(R.id.nearby_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(loadedMap -> {
            map = loadedMap;
            map.getUiSettings().setAttributionEnabled(true);
            map.getUiSettings().setLogoEnabled(false);
            map.setStyle(new Style.Builder().fromUri(OPEN_STREET_MAP_STYLE), style -> drawMap());
        });
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
        mapView.onDestroy();
        executor.shutdownNow();
        super.onDestroy();
    }

    @Override protected void onStart() { super.onStart(); mapView.onStart(); }
    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { mapView.onPause(); super.onPause(); }
    @Override protected void onStop() { mapView.onStop(); super.onStop(); }
    @Override public void onLowMemory() { super.onLowMemory(); mapView.onLowMemory(); }
    @Override protected void onSaveInstanceState(Bundle outState) {
        mapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
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
        currentLocation = new Location(location);
        drawMap();
        executor.execute(() -> {
            try {
                String locationLabel = findLocationLabel(location);
                Place[] places = findPlaces(location);
                runOnUiThread(() -> showResults(locationLabel, places));
            } catch (Exception error) {
                runOnUiThread(() -> showPlacesProblem());
            }
        });
    }

    /** Reverse-geocodes the current coordinate for display only; no address is persisted. */
    private String findLocationLabel(Location location) throws Exception {
        List<Address> addresses = new Geocoder(this, Locale.getDefault())
                .getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        if (addresses == null || addresses.isEmpty()) return getString(R.string.location_unavailable);
        Address address = addresses.get(0);
        String addressLine = address.getAddressLine(0);
        if (addressLine != null && !addressLine.trim().isEmpty()) return addressLine;
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
                if (!isConfidentCategoryMatch(category, tags, name)) continue;
                if (closest[category] == null || distance < closest[category].distanceKm) {
                    closest[category] = new Place(name, latitude, longitude, distance, category);
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

    private void showResults(String locationLabel, Place[] places) {
        locationValue.setText(locationLabel);
        currentPlaces = places;
        drawMap();
    }

    /**
     * Uses OSM's category tag plus conservative conflict checks before showing a nearby-place marker.
     * A rejected result is omitted rather than relabelled as another kind of place.
     */
    private boolean isConfidentCategoryMatch(int category, JSONObject tags, String name) {
        if (category == 0) return isConfidentHospital(tags, name);
        if (category == 1) return isConfidentBusStop(tags, name);
        return isConfidentSupermarket(tags, name);
    }

    private boolean isConfidentHospital(JSONObject tags, String name) {
        if (!"hospital".equals(tags.optString("amenity"))) return false;
        String healthcare = tags.optString("healthcare");
        if ("clinic".equals(healthcare) || "dentist".equals(healthcare)
                || "laboratory".equals(healthcare) || "pharmacy".equals(healthcare)) return false;
        return !HOSPITAL_NAME_CONFLICT.matcher(name).find();
    }

    private boolean isConfidentBusStop(JSONObject tags, String name) {
        if (!"bus_stop".equals(tags.optString("highway"))) return false;
        String railway = tags.optString("railway");
        if ("station".equals(railway) || "halt".equals(railway) || "tram_stop".equals(railway)
                || "subway_entrance".equals(railway)) return false;
        return !BUS_STOP_NAME_CONFLICT.matcher(name).find();
    }

    private boolean isConfidentSupermarket(JSONObject tags, String name) {
        if (!"supermarket".equals(tags.optString("shop"))) return false;
        String craft = tags.optString("craft");
        if ("jeweller".equals(craft) || "goldsmith".equals(craft)) return false;
        String amenity = tags.optString("amenity");
        if ("pharmacy".equals(amenity) || "bank".equals(amenity)) return false;
        return !SUPERMARKET_NAME_CONFLICT.matcher(name).find();
    }

    private void setLoading() {
        locationValue.setText(R.string.location_loading);
        currentPlaces = null;
        drawMap();
    }

    private void showLocationProblem(int message) {
        locationValue.setText(message);
        currentLocation = null;
        currentPlaces = null;
        drawMap();
        retryButton.setVisibility(View.VISIBLE);
    }

    private void showPlacesProblem() {
        locationValue.setText(R.string.location_unavailable);
        currentPlaces = null;
        drawMap();
        retryButton.setVisibility(View.VISIBLE);
    }

    private void drawMap() {
        if (map == null) return;
        map.clear();
        if (currentLocation == null) return;
        IconFactory icons = IconFactory.getInstance(this);
        LatLng userPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLngBounds.Builder bounds = new LatLngBounds.Builder().include(userPosition);
        boolean hasPlaceMarker = false;
        map.addMarker(new MarkerOptions()
                .position(userPosition)
                .title(getString(R.string.map_marker_you_are_here))
                .icon(bitmapIcon(R.drawable.ic_map_current_location, icons)));
        if (currentPlaces != null) {
            for (Place place : currentPlaces) {
                if (place == null) continue;
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(place.latitude, place.longitude))
                        .title(place.name)
                        .snippet(getString(R.string.distance_away, place.distanceKm))
                        .icon(iconFor(place.category, icons)));
                bounds.include(new LatLng(place.latitude, place.longitude));
                hasPlaceMarker = true;
            }
        }
        if (hasPlaceMarker) {
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 96));
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 14.0));
        }
    }

    private Icon iconFor(int category, IconFactory icons) {
        if (category == 0) return bitmapIcon(R.drawable.ic_map_hospital, icons);
        if (category == 1) return bitmapIcon(R.drawable.ic_map_bus_stop, icons);
        return bitmapIcon(R.drawable.ic_map_supermarket, icons);
    }

    /** MapLibre 11 accepts bitmaps for marker icons; app marker art is kept as scalable vector drawables. */
    private Icon bitmapIcon(int drawableId, IconFactory icons) {
        Drawable drawable = getDrawable(drawableId);
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return icons.fromBitmap(bitmap);
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
        final String name; final double latitude; final double longitude; final double distanceKm; final int category;
        Place(String name, double latitude, double longitude, double distanceKm, int category) {
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.distanceKm = distanceKm;
            this.category = category;
        }
    }
}
