package com.enpassio.findmyroute.utils;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.enpassio.findmyroute.model.FuelStations;
import com.enpassio.findmyroute.model.Restaurants;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by ABHISHEK RAJ on 9/19/2017.
 */

public class RestaurantAndFuelStations {

    private static final String urlForRestaurantsAndFuelStations = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
    private static boolean mFuelCheckBoxStatus;
    private static boolean mRestaurantCheckBoxStatus;
    private static ArrayList<MarkerOptions> markerOptionsArrayList;
    private static MyArrayListOfMarker myArrayListOfMarker;


    public static void getRestaurantsAndFuelStationsAlongThePath(ArrayList<HashMap<String, Double>> selectedPolyLinePoints, boolean fuelCheckBoxStatus, boolean restaurantCheckBoxStatus, Activity activity) {
        mFuelCheckBoxStatus = fuelCheckBoxStatus;
        mRestaurantCheckBoxStatus = restaurantCheckBoxStatus;
        markerOptionsArrayList = new ArrayList<>();
        Uri baseUri = Uri.parse(urlForRestaurantsAndFuelStations);
        myArrayListOfMarker = (MyArrayListOfMarker) activity;

        ArrayList<HashMap<String, Double>> pointsAlongThePath = selectedPolyLinePoints;

        for (int i = 0; i < pointsAlongThePath.size(); i++) {
            HashMap<String, Double> hashMap = pointsAlongThePath.get(i);
            Double latitude = hashMap.get("lat");
            Double longitude = hashMap.get("lng");
            Uri.Builder uriBuilderGasStationAndRestaurants = baseUri.buildUpon();
            uriBuilderGasStationAndRestaurants.appendQueryParameter("location", "" + latitude + "," + longitude);
            uriBuilderGasStationAndRestaurants.appendQueryParameter("rankBy", "distance");
            uriBuilderGasStationAndRestaurants.appendQueryParameter("types", "restaurant|gas_station");
            uriBuilderGasStationAndRestaurants.appendQueryParameter("sensor", "false");
            uriBuilderGasStationAndRestaurants.appendQueryParameter("radius", "100");
            uriBuilderGasStationAndRestaurants.appendQueryParameter("key", "AIzaSyB-iknh4cmq7Rqtg-lZX1hN124bjxYQGeU");

            Request requestGasStationsAndRestaurants = new Request.Builder()
                    .url(uriBuilderGasStationAndRestaurants.toString())
                    .build();
            OkHttpClient clientGasStationsAndRestaurants = new OkHttpClient();
            clientGasStationsAndRestaurants.newCall(requestGasStationsAndRestaurants).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonData = response.body().string();
                    JSONObject jsonObject;
                    try {
                        jsonObject = new JSONObject(jsonData);
                        ArrayList<HashMap<String, Bundle>> hm_RestAndFuel = JSON_RestaurantsAndFuelStations.parseJson(jsonObject);

                        ArrayList<FuelStations> fuelStationsArrayList = new ArrayList<>();
                        ArrayList<Restaurants> restaurantsArrayList = new ArrayList<>();
                        for (int k = 0; k < hm_RestAndFuel.size(); k++) {
                            HashMap<String, Bundle> fuelStationsRestaurantsHashMap = hm_RestAndFuel.get(k);
                            Bundle bundle = fuelStationsRestaurantsHashMap.get("fuelStationsRestaurantsBundle");
                            fuelStationsArrayList = bundle.getParcelableArrayList("fuelStationsArrayList");
                            restaurantsArrayList = bundle.getParcelableArrayList("restaurantsArrayList");
                        }
                        if (mFuelCheckBoxStatus) {
                            for (int m = 0; m < fuelStationsArrayList.size(); m++) {
                                FuelStations fuelStation = fuelStationsArrayList.get(m);
                                HashMap<String, Double> hm = fuelStation.getLocation();
                                String name = fuelStation.getNameOfFuelStation();
                                Double lat = hm.get("lat");
                                Double lng = hm.get("lng");
                                MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lng)).title(name);
                                markerOptionsArrayList.add(marker);
                            }
                        }
                        if (mRestaurantCheckBoxStatus) {
                            for (int n = 0; n < restaurantsArrayList.size(); n++) {
                                Restaurants restaurant = restaurantsArrayList.get(n);
                                HashMap<String, Double> hm = restaurant.getLocationOfRestaurant();
                                String name = restaurant.getNameOfRestaurant();
                                Double lat = hm.get("lat");
                                Double lng = hm.get("lng");
                                MarkerOptions marker = new MarkerOptions().position(new LatLng(lat, lng)).title(name);
                                markerOptionsArrayList.add(marker);
                            }
                        }
                        Log.v("my_taggg", "try markerOptionsArrayList size is: " + markerOptionsArrayList.size());
                    } catch (Exception e) {
                        Log.d("Exception", e.toString());
                    }
                    myArrayListOfMarker.onEvent(markerOptionsArrayList);
                    Log.v("my_taggg", "onResponse markerOptionsArrayList size is: " + markerOptionsArrayList.size());

                }
                @Override
                public void onFailure(Call call, IOException e) {

                }
            });
            Log.v("my_taggg", "for markerOptionsArrayList size is: " + markerOptionsArrayList.size());
        }
        Log.v("my_taggg", "return markerOptionsArrayList size is: " + markerOptionsArrayList.size());
    }

    public interface MyArrayListOfMarker {
        void onEvent(ArrayList<MarkerOptions> markerOptionsArrayList);
    }
}
