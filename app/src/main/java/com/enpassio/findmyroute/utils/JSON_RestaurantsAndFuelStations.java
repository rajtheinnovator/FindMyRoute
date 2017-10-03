package com.enpassio.findmyroute.utils;

import android.os.Bundle;

import com.enpassio.findmyroute.model.FuelStations;
import com.enpassio.findmyroute.model.Restaurants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ABHISHEK RAJ on 9/20/2017.
 */

public class JSON_RestaurantsAndFuelStations {
    static ArrayList<HashMap<String, Bundle>> hashMapArrayList = new ArrayList<>();
    private static JSONArray resultsArray;
    private static ArrayList<FuelStations> fuelStationsArrayList;
    private static ArrayList<Restaurants> restaurantsArrayList;
    private static JSONObject item;
    private static JSONObject geometry;
    private static JSONObject location;
    private static Double lati;
    private static Double longit;
    private static String name;
    private static JSONArray typeArray;

    public static ArrayList<HashMap<String, Bundle>> parseJson(JSONObject jsonObject) {
        try {
            if (jsonObject.has("results"))
                resultsArray = jsonObject.getJSONArray("results");
            fuelStationsArrayList = new ArrayList<>();
            restaurantsArrayList = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {
                if (resultsArray.length() > 0) {
                    item = resultsArray.getJSONObject(i);
                    if (item.has("geometry")) {
                        geometry = item.getJSONObject("geometry");

                        if (geometry.has("location")) {
                            location = geometry.getJSONObject("location");

                            if (location.has("lat")) {
                                lati = location.getDouble("lat");
                            }
                            if (location.has("lng")) {
                                longit = location.getDouble("lng");
                            }
                        }
                    }

                    HashMap<String, Double> latLong = new HashMap<>();
                    latLong.put("lat", lati);
                    latLong.put("lng", longit);
                    if (item.has("name")) {
                        name = item.getString("name");
                    }
                    if (item.has("types")) {
                        typeArray = item.getJSONArray("types");
                    }
                    int typeIsFuelStation = 0;
                    int typeIsRestaurant = 0;
                    //check if type restaurant and gas_station exist
                    for (int j = 0; j < typeArray.length(); j++) {
                        String type = typeArray.getString(j);
                        if (type.equals("restaurant")) {
                            typeIsRestaurant += 1;
                        } else if (type.equals("gas_station")) {
                            typeIsFuelStation += 1;
                        }
                    }
                    if (typeIsFuelStation > 0) {
                        //that means, the landmark is a gas_station
                        fuelStationsArrayList.add(new FuelStations(name, latLong));
                    } else if (typeIsRestaurant > 0) {
                        //that means, the landmark is a restaurant
                        restaurantsArrayList.add(new Restaurants(name, latLong));
                    }
                    Bundle fuelStationsRestaurantsBundle = new Bundle();
                    fuelStationsRestaurantsBundle.putParcelableArrayList("fuelStationsArrayList", fuelStationsArrayList);
                    fuelStationsRestaurantsBundle.putParcelableArrayList("restaurantsArrayList", restaurantsArrayList);
                    HashMap<String, Bundle> fuelStationsRestaurantsHashMap = new HashMap<String, Bundle>();
                    fuelStationsRestaurantsHashMap.put("fuelStationsRestaurantsBundle", fuelStationsRestaurantsBundle);
                    hashMapArrayList.add(fuelStationsRestaurantsHashMap);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hashMapArrayList;
    }
}
