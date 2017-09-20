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

    public static ArrayList<HashMap<String, Bundle>> parseJson(JSONObject jsonObject) {
        try {
            JSONArray resultsArray = jsonObject.getJSONArray("results");
            ArrayList<FuelStations> fuelStationsArrayList = new ArrayList<>();
            ArrayList<Restaurants> restaurantsArrayList = new ArrayList<>();
            for (int i = 0; i < resultsArray.length(); i++) {
                JSONObject item = resultsArray.getJSONObject(i);
                JSONObject geometry = item.getJSONObject("geometry");
                JSONObject location = geometry.getJSONObject("location");
                Double lati = location.getDouble("lat");
                Double longit = location.getDouble("lng");
                HashMap<String, Double> latLong = new HashMap<>();
                latLong.put("lat", lati);
                latLong.put("lng", longit);
                String name = item.getString("name");
                JSONArray typeArray = item.getJSONArray("types");
                int typeIsFuelStation = 0;
                int typeIsRestaurant = 0;
                for (int j = 0; j < typeArray.length(); j++) {
                    String type = typeArray.getString(j);
                    if (type.equals("restaurant")) {
                        typeIsRestaurant += 1;
                    } else if (type.equals("gas_station")) {
                        typeIsFuelStation += 1;
                    }
                }
                if (typeIsFuelStation > 0) {
                    fuelStationsArrayList.add(new FuelStations(name, latLong));
                } else if (typeIsRestaurant > 0) {
                    restaurantsArrayList.add(new Restaurants(name, latLong));
                }
                Bundle fuelStationsRestaurantsBundle = new Bundle();
                fuelStationsRestaurantsBundle.putParcelableArrayList("fuelStationsArrayList", fuelStationsArrayList);
                fuelStationsRestaurantsBundle.putParcelableArrayList("restaurantsArrayList", restaurantsArrayList);
                ;
                HashMap<String, Bundle> fuelStationsRestaurantsHashMap = new HashMap<String, Bundle>();
                fuelStationsRestaurantsHashMap.put("fuelStationsRestaurantsBundle", fuelStationsRestaurantsBundle);
                hashMapArrayList.add(fuelStationsRestaurantsHashMap);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return hashMapArrayList;
    }
}
