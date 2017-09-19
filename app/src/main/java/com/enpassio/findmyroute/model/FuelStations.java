package com.enpassio.findmyroute.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * Created by ABHISHEK RAJ on 9/20/2017.
 */

public class FuelStations implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<FuelStations> CREATOR = new Parcelable.Creator<FuelStations>() {
        @Override
        public FuelStations createFromParcel(Parcel in) {
            return new FuelStations(in);
        }

        @Override
        public FuelStations[] newArray(int size) {
            return new FuelStations[size];
        }
    };
    private String nameOfFuelStation;
    private HashMap<String, Double> location;

    public FuelStations(String name, HashMap<String, Double> latAndLong) {
        nameOfFuelStation = name;
        location = latAndLong;
    }

    protected FuelStations(Parcel in) {
        nameOfFuelStation = in.readString();
        location = (HashMap) in.readValue(HashMap.class.getClassLoader());
    }

    public String getNameOfFuelStation() {
        return nameOfFuelStation;
    }

    public void setNameOfFuelStation(String nameOfFuelStation) {
        this.nameOfFuelStation = nameOfFuelStation;
    }

    public HashMap<String, Double> getLocation() {
        return location;
    }

    public void setLocation(HashMap<String, Double> location) {
        this.location = location;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nameOfFuelStation);
        dest.writeValue(location);
    }
}
