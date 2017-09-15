package com.enpassio.findmyroute;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    int FROM_PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    int TO_PLACE_AUTOCOMPLETE_REQUEST_CODE = 2;
    String fromLatLong;
    String toLatLong;

    TextView fromLocationTextView;
    TextView toLocationTextView;
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FROM_PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
                Log.i(TAG, "Place: " + place.getLatLng());
                fromLatLong = String.valueOf(place.getLatLng());
                fromLocationTextView.setText(String.valueOf(place.getLatLng()));
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
                Log.i(TAG, "Place: " + place.getName());
                Log.i(TAG, "Place: " + place.getLatLng());
                toLatLong = String.valueOf(place.getLatLng());
                toLocationTextView.setText(String.valueOf(place.getLatLng()));
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
                //TODO: handle routes using AsyncTask/Retrofit
                break;
        }
    }
}
