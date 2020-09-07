package com.mysoft.mageon.way;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

public class CurrentSostoyanie {
    private static CurrentSostoyanie mInstance = new CurrentSostoyanie();
    private Location mMyLocation = null;
    private Location mNextLocation = null;
    private Location mSignalLocation = null;
    private LatLng mMyLatLng = null;
    private boolean mFixcamera = false;
    private boolean mService_Started = false;
    private Point mNextPoint = null;
    private Marker mYatut = null;
    private Marker mNewRadius = null;
    private List<Marker> mAr_Marker = new ArrayList<Marker>();;
    private List<Marker> mSearch_Marker = new ArrayList<Marker>();;
    private ArrayList<Point> mAr_Control = new ArrayList<Point>();;
    private ArrayList<Object> mAr_ControlID = new ArrayList<Object>();

    private CurrentSostoyanie() {
    }

    public static CurrentSostoyanie getInstance(){
        return mInstance;
    }

    public Location getNextLocation() {
        return mNextLocation;
    }

    public void setNextLocation(Location arg) {
        mInstance.mNextLocation = arg;
    }

    public Location getSignalLocation() {
        return mSignalLocation;
    }

    public void setSignalLocation(Location arg) {
        mInstance.mSignalLocation = arg;
    }

    public Location getMyLocation() {
        return mMyLocation;
    }

    public void setMyLocation(Location arg) {
        mInstance.mMyLocation = arg;
    }

    public LatLng getMyLatLng() {
        return mMyLatLng;
    }

    public void setNewRadius(Marker arg) {
        mInstance.mNewRadius = arg;
    }

    public Marker getNewRadius() {
        return mNewRadius;
    }

    public void setMyLatLng(LatLng arg) {
        mInstance.mMyLatLng = arg;
    }

    public boolean isFixcamera() {
        return mFixcamera;
    }

    public void setFixcamera(boolean arg) {
        mInstance.mFixcamera = arg;
    }

    public boolean isService_Started() {
        return mService_Started;
    }

    public void setService_Started(boolean arg) {
        mInstance.mService_Started = arg;
    }

    public Point getNextPoint() {
        return mNextPoint;
    }

    public void setNextPoint(Point mNextPoint) {
        mInstance.mNextPoint = mNextPoint;
    }

    public Marker getYatut() {
        return mYatut;
    }

    public void setYatut(Marker arg) {
        mInstance.mYatut = arg;
    }

    public ArrayList<Point> getAr_Control() {
        return mAr_Control;
    }

    public void setAr_Control(ArrayList<Point> arg) {
        mInstance.mAr_Control = arg;
    }

    public List<Marker> getAr_Marker() {
        return mAr_Marker;
    }

    public void setAr_Marker(ArrayList<Marker> arg) {
        mInstance.mAr_Marker = arg;
    }

    public void setSearch_Marker(ArrayList<Marker> arg) {
        mInstance.mSearch_Marker = arg;
    }

    public List<Marker> getSearch_Marker() {
        return mSearch_Marker;
    }

    public ArrayList<Object> getAr_ControlID() {
        return mAr_ControlID;
    }

    public void setAr_ControlID(ArrayList<Object> arg) {
        mInstance.mAr_ControlID = arg;
    }

    public void addAr_ControlID(Object arg) {
        mAr_ControlID.add(arg);
    }

    public void removeAr_ControlID(Object arg) {
        if (mAr_ControlID.contains(arg))
            mAr_ControlID.remove(arg);
    }
}
