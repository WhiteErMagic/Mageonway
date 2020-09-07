package com.mysoft.mageon.way;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Boris on 12.01.2017.
 */
public class ConnectProvider {

    private static ConnectProvider ourInstance = new ConnectProvider();
    LocationManager locationManager;
    Context ctx;
    GoogleApiClient mGoogleApiClient;
    LatLng myLatLng = null;

    public static ConnectProvider getInstance(Context ctx) {
        ourInstance.locationManager = (LocationManager) ctx.getSystemService(LOCATION_SERVICE);
        ourInstance.ctx = ctx;

        return ourInstance;
    }

    private ConnectProvider() {
    }

    protected boolean checkforstart() {
        if (!ourInstance.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !ourInstance.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(ctx, ctx.getResources().getString(com.mysoft.mageon.way.R.string.error_no_available_providers), Toast.LENGTH_SHORT).show();
            return false;
        } else
            return true;
    }

    protected boolean checkNet() {
        return ourInstance.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    protected boolean checkGPS() {
        return ourInstance.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

   /* protected Location getLocation() {
        int ind = 1000;
        int n = 0;
        float minotcl = -1;
        Location minloc = null;
        Location mlocation = null;
        while ((n < ind)) {
            mlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            n = n + 1;

            if (mlocation != null) {
                if ((minotcl > mlocation.getAccuracy() | (minotcl == -1))) {
                    minotcl = mlocation.getAccuracy();
                    minloc = mlocation;
                }
            }
        }
        mlocation = minloc;

        return mlocation;
    }*/

    protected void getMyLocation(GoogleApiClient mGoogleApiClient) {
        //ShowDialogAsyncTask at = new ConnectProvider.ShowDialogAsyncTask();
        //at.execute();
        //GoogleApiClient mGoogleApiClient = null;
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location != null) {
            //Getting longitude and latitude
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();
        }
    }

    protected class ShowDialogAsyncTask extends AsyncTask<Void, Integer, Void> {

        Location location = null;
        ProgressDialog progressDialog;
        //LocationManager locationManager;
        android.location.LocationListener locationListener;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //locationManager = ConnectProvider.getInstance(ctx).locationManager;

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                super.onPreExecute();
                progressDialog = new ProgressDialog(ctx);
                progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    public void onCancel(DialogInterface dialog) {
                    }
                });

                locationListener = new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        //alert("Статус");
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle bundle) {
                        //alert("Статус");
                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        //alert("Доступ");
                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                progressDialog.setCancelable(true);
                progressDialog.setMessage(ctx.getResources().getString(com.mysoft.mageon.way.R.string.point_getcoordinates));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setProgress(1);
                progressDialog.show();

            } else {
                Toast.makeText(ctx, ctx.getResources().getString(com.mysoft.mageon.way.R.string.error_gps_notenabled), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected Void doInBackground(Void... arg) {

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

                int ind = 1000;
                int n = 0;
                float minotcl = -1;
                Location minloc = null;
                while (n < ind) {

                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    n = n + 1;
                    if (location != null) {
                        if ((minotcl > location.getAccuracy() | (minotcl == -1))) {
                            minotcl = location.getAccuracy();
                            minloc = location;
                        }
                    }
                }
                location = minloc;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (location != null) {
                //if (myLatLng == null) {
                //  myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                //map.addMarker(new MarkerOptions().position(myLatLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.yatut))).setTitle(ctx.getString(R.string.t_map_mylocation));
                CurrentSostoyanie.getInstance().setMyLocation(location);
                CurrentSostoyanie.getInstance().setMyLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                //CurrentSostoyanie.getInstance(ctx).setFixcamera(true);
                ((MapActivity) ctx).setPictFixcamera();
                ((MapActivity) ctx).updateCamera(CurrentSostoyanie.getInstance().getMyLatLng(), 0);
                //map.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 12));
            } else {
                Toast.makeText(ctx, ctx.getString(com.mysoft.mageon.way.R.string.error_gps_coordinats), Toast.LENGTH_SHORT).show();
            }
            progressDialog.cancel();
            locationManager.removeUpdates(locationListener);
        }
    }
}
