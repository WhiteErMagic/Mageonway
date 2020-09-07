package com.mysoft.mageon.way;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Boris on 22.03.2017.
 */

public class MyServiceMAGEON extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    com.google.android.gms.location.LocationListener mLocationListener;
    public static final int MSG_START = 1;
    public static final int MSG_STOP = 2;
    Messenger mMessenger;
    LocationManager mlocationManager = null;
    android.location.LocationListener mlocationListenerControl = null;
    ConnectProvider mConnectProvider;
    Intent intent = null;
    final static String MAGEONWAY = "MAGEONWAY";
    LocationRequest REQUEST;
    GoogleApiClient mGoogleApiClient;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mMessenger = new Messenger(new IncomingHandler());
        mConnectProvider = ConnectProvider.getInstance(this);
        return Service.START_STICKY;
    }

    private void startGPS() {
        mLocationListener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                sendMessageToApp(location);
            }
        };
        SharedPreferences sPref = getSharedPreferences("com.example.mageon.way", Context.MODE_PRIVATE);
        long mInterval = 1000;
        if(sPref.contains("interval"))
            mInterval = sPref.getLong("interval", 1)*1000;

        REQUEST = LocationRequest.create()
                .setInterval(mInterval)
                .setFastestInterval(mInterval)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mGoogleApiClient.connect();
        mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocationListenerControl=new android.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                //mlocationManager.removeUpdates(mlocationListenerControl);
                //sendMessageToApp(null);
            }
        };
        mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1500000,0,mlocationListenerControl);
        mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1500000,0,mlocationListenerControl);
        }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                REQUEST,
                mLocationListener);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START:
                    startGPS();
                    Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.start_service), Toast.LENGTH_SHORT).show();
                    break;
                case MSG_STOP:
                    if(mGoogleApiClient != null) {
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                        mlocationManager.removeUpdates(mlocationListenerControl);
                        Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.stop_service), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private void sendMessageToApp(Location location){
        if(intent == null) {
            intent = new Intent();
            intent.setAction(MAGEONWAY);
        }
        if(location != null)
            intent.putExtra("latlong", new double[]{location.getLatitude(), location.getLongitude()});
        else
            intent.removeExtra("latlong");
        sendBroadcast(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.bind_service), Toast.LENGTH_LONG).show();
         return mMessenger.getBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, getResources().getString(com.mysoft.mageon.way.R.string.destroy_service), Toast.LENGTH_SHORT).show();
        if(mLocationListener != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
            Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.stop_service), Toast.LENGTH_SHORT).show();
        }
        if(mlocationManager != null){
            mlocationManager.removeUpdates(mlocationListenerControl);
        }
    }
}
