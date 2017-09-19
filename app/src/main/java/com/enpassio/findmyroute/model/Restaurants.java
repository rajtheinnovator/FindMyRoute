package com.enpassio.findmyroute.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by ABHISHEK RAJ on 9/20/2017.
 */

public class Restaurants implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Restaurants> CREATOR = new Parcelable.Creator<Restaurants>() {
        @Override
        public Restaurants createFromParcel(Parcel in) {
            return new Restaurants(in);
        }

        @Override
        public Restaurants[] newArray(int size) {
            return new Restaurants[size];
        }
    };
    private String nameOfRestaurant;
    private HashMap<String, Double> locationOfRestaurant;

    public Restaurants(String name, HashMap<String, Double> location) {
        nameOfRestaurant = name;
        locationOfRestaurant = location;
    }

    protected Restaurants(Parcel in) {
        nameOfRestaurant = in.readString();
        locationOfRestaurant = (HashMap) in.readValue(HashMap.class.getClassLoader());
    }

    public String getNameOfRestaurant() {
        return nameOfRestaurant;
    }

    public void setNameOfRestaurant(String nameOfRestaurant) {
        this.nameOfRestaurant = nameOfRestaurant;
    }

    public HashMap<String, Double> getLocationOfRestaurant() {
        return locationOfRestaurant;
    }

    public void setLocationOfRestaurant(HashMap<String, Double> locationOfRestaurant) {
        this.locationOfRestaurant = locationOfRestaurant;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nameOfRestaurant);
        dest.writeValue(locationOfRestaurant);
    }
}
