package com.mysoft.mageon.way;


import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.io.Serializable;

public class PlaceDetails implements Serializable, Comparable<PlaceDetails> {

    public String mName;
    public String mAddress;
    public float mRating;
    public LatLng mLocation;
    public String mID;
    public String mTypes;
    public boolean mOpen;
    public Marker mMerker;

    public void PlaceDetails(){

    }

    String getName(){
        return this.mName;
    }

    @Override
    public int compareTo(PlaceDetails o) {
        return mName.compareTo(o.getName());
    }
}
