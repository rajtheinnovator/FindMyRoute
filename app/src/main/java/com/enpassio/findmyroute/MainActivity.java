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
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

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
    List<Place> placeLists;
    JSONArray jsonArray;
    ArrayList<Place> placeArrayLists;

    GoogleMap m_map;
    boolean mapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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


                MarkerOptions fromLocationMarker = new MarkerOptions().position(new LatLng(fromLat, fromLong)).title(fromLocation);
                MarkerOptions toLocationMarker = new MarkerOptions().position(new LatLng(toLat, toLong)).title(toLocation);

                m_map.addMarker(fromLocationMarker);
                m_map.addMarker(toLocationMarker);

                CameraPosition target = CameraPosition.builder().target(new LatLng(fromLat, fromLong)).zoom(18).bearing(90).tilt(60).build();
                m_map.moveCamera(CameraUpdateFactory.newCameraPosition(target));

                //TODO: handle routes using AsyncTask/Retrofit
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
        Log.v("my_tag", "urls is: " + uriBuilder.toString());


        Request request = new Request.Builder()
                .url(uriBuilder.toString())
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                /*
                Instruct GSON to parse as a Post array (which we convert into a list)
                */

                /** code below referenced from: https://stackoverflow.com/a/29680883/5770629
                 */

                String jsonData = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(jsonData);

                    JSONArray routeArray = jsonObject.getJSONArray("routes");
                    JSONObject routes = routeArray.getJSONObject(0);
                    JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
                    String encodedString = overviewPolylines.getString("points");
                    final List<LatLng> list = decodePoly(encodedString);

                    //referenced from the @link: https://stackoverflow.com/a/14978267/5770629
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_map.addPolyline(new PolylineOptions()
                                    .addAll(list)
                                    .width(12)
                                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                                    .geodesic(true)
                            );
                        }
                    });


                    for (int z = 0; z < list.size() - 1; z++) {
                        final LatLng src = list.get(z);
                        final LatLng dest = list.get(z + 1);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_map.addPolyline(new PolylineOptions()
                                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude, dest.longitude))
                                        .width(2)
                                        .color(Color.BLUE).geodesic(true));
                            }
                        });
                    }


                } catch (JSONException j) {

                }

            }

            @Override
            public void onFailure(Call call, IOException e) {
                call.cancel();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mapReady = true;
        m_map = googleMap;
    }

    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
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
}
