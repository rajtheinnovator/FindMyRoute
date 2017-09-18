package com.enpassio.findmyroute;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, GoogleMap.OnPolylineClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String url = "https://maps.googleapis.com/maps/api/directions/json?";
    int FROM_PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int TO_PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    Double fromLat;
    Double fromLong;
    Double toLat;
    Double toLong;
    LatLng toLatLong;
    String fromLocation;
    String toLocation;
    TextView fromLocationTextView;
    TextView toLocationTextView;
    PolylineOptions lineOptions;
    ArrayList<PolylineOptions> polylineOptionsArrayList;

    List<LatLng> list;

    GoogleMap m_map;
    boolean mapReady = false;

    ArrayList<Integer> distanceList;
    PolylineOptions shortestPolylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        distanceList = new ArrayList<>();

        Button fromPlaceButton = (Button) findViewById(R.id.from_place);
        Button toPlaceButton = (Button) findViewById(R.id.to_place);
        Button routesAvailable = (Button) findViewById(R.id.routes_available);
        fromLocationTextView = (TextView) findViewById(R.id.from_location);
        toLocationTextView = (TextView) findViewById(R.id.to_location);

        fromPlaceButton.setOnClickListener(this);
        toPlaceButton.setOnClickListener(this);
        routesAvailable.setOnClickListener(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FROM_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                fromLat = place.getLatLng().latitude;
                fromLong = place.getLatLng().longitude;
                fromLocation = place.getName().toString();
                fromLocationTextView.setText(place.getName().toString());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }

        if (requestCode == TO_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                toLocation = place.getName().toString();
                toLocationTextView.setText(place.getName().toString());
                toLat = place.getLatLng().latitude;
                toLong = place.getLatLng().longitude;
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.from_place:
                try {
                    AutocompleteFilter india = new AutocompleteFilter.Builder()
                            .setCountry("IN")
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(india)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, FROM_PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {
                }
                break;
            case R.id.to_place:
                try {
                    AutocompleteFilter india = new AutocompleteFilter.Builder()
                            .setCountry("IN")
                            .build();
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setFilter(india)
                                    .build(MainActivity.this);
                    startActivityForResult(intent, TO_PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesRepairableException e) {

                } catch (GooglePlayServicesNotAvailableException e) {
                }
                break;
            case R.id.routes_available:

                m_map.clear();
                distanceList.clear();
                MarkerOptions fromLocationMarker = new MarkerOptions().position(new LatLng(fromLat, fromLong)).title(fromLocation);
                MarkerOptions toLocationMarker = new MarkerOptions().position(new LatLng(toLat, toLong)).title(toLocation);

                m_map.addMarker(fromLocationMarker);
                m_map.addMarker(toLocationMarker);

                CameraPosition target = CameraPosition.builder().target(new LatLng(fromLat, fromLong)).zoom(18).build();
                m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

                //TODO: handle routes using AsyncTask/Retrofit/OkHttp
                try {
                    run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    void run() throws IOException {

        OkHttpClient client = new OkHttpClient();

        Uri baseUri = Uri.parse(url);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("origin", "" + fromLat + "," + fromLong);
        uriBuilder.appendQueryParameter("destination", "" + toLat + "," + toLong);
        uriBuilder.appendQueryParameter("key", "AIzaSyB-iknh4cmq7Rqtg-lZX1hN124bjxYQGeU");
        uriBuilder.appendQueryParameter("alternatives", "true");

        Request request = new Request.Builder()
                .url(uriBuilder.toString())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String jsonData = response.body().string();
                JSONObject jsonObject;
                List<List<HashMap<String, String>>> routes = new ArrayList<>();
                JSONArray jRoutes;
                JSONArray jLegs;
                JSONArray jSteps;

                try {
                    jsonObject = new JSONObject(jsonData);

                    jRoutes = jsonObject.getJSONArray("routes");


                    /** Traversing all routes */
                    for (int i = 0; i < jRoutes.length(); i++) {
                        jLegs = ((JSONObject) jRoutes.get(i)).getJSONArray("legs");
                        List path = new ArrayList<>();


                        /** Traversing all legs */
                        for (int j = 0; j < jLegs.length(); j++) {
                            String legsDuration = ((JSONObject) jLegs.get(j)).getJSONObject("duration").getString("text");
                            String[] parts = legsDuration.split(" ");
                            String part1 = parts[0];
                            String part2 = parts[1];
                            int minutes = Integer.parseInt(part1);
                            int hours = 0;
                            if (part2.equals("hours") || part2.equals("hour")) {
                                String part3 = parts[2];
                                String part4 = parts[3];
                                hours = Integer.parseInt(part1);
                                minutes = Integer.parseInt(part3) + 60 * hours;
                            }
                            distanceList.add(minutes);

                            jSteps = ((JSONObject) jLegs.get(j)).getJSONArray("steps");

                            /** Traversing all steps */
                            for (int k = 0; k < jSteps.length(); k++) {
                                String polyline = "";
                                polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                                list = decodePoly(polyline);

                                /** Traversing all points */
                                for (int l = 0; l < list.size(); l++) {
                                    HashMap<String, String> hm = new HashMap<>();
                                    hm.put("lat", Double.toString((list.get(l)).latitude));
                                    hm.put("lng", Double.toString((list.get(l)).longitude));
                                    path.add(hm);
                                }
                            }
                            routes.add(path);
                        }
                        //choose only first three routes
                        if (i == 2)
                            break;
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                }
                drawPolyLine(routes);
                list.clear();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }
        });

    }

    private void drawPolyLine(List<List<HashMap<String, String>>> routes) {
        ArrayList<LatLng> points;

        polylineOptionsArrayList = new ArrayList<>();

        int fasterRoute = distanceList.get(0);
        // Traversing through all the routes
        for (int i = 0; i < routes.size(); i++) {
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = routes.get(i);

            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);

                points.add(position);
            }

            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            lineOptions.width(12);

            if (fasterRoute >= distanceList.get(i)) {
                fasterRoute = distanceList.get(i);
                shortestPolylineOptions = lineOptions;
            }


            lineOptions.color(Color.GRAY).width(12);

            polylineOptionsArrayList.add(lineOptions);

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                final PolylineOptions finalLineOptions = lineOptions;
                finalLineOptions.clickable(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_map.addPolyline(finalLineOptions).setGeodesic(true);
                    }
                });
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (shortestPolylineOptions != null) {
                    shortestPolylineOptions.color(Color.RED).width(16);
                    m_map.addPolyline(shortestPolylineOptions);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        m_map = googleMap;
        m_map.setOnPolylineClickListener(this);
    }

    /**
     * Method to decode polyline points
     */
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    @Override
    public void onPolylineClick(Polyline polyline) {
        m_map.clear();
        for (int i = 0; i < polylineOptionsArrayList.size(); i++) {
            PolylineOptions polylineOptions = polylineOptionsArrayList.get(i);
            List<LatLng> pointsOptions = polylineOptions.getPoints();
            List<LatLng> pointsLine = polyline.getPoints();

            if (pointsLine.equals(pointsOptions)) {
                polylineOptions.color(Color.RED);

            } else {
                polylineOptions.color(Color.GRAY);
            }
            m_map.addPolyline(polylineOptions);
        }
    }
}
