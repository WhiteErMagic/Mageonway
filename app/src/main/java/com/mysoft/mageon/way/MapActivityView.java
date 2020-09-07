package com.mysoft.mageon.way;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivityView extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LatLng mTempLatLng;
    Marker mMarker = null;
    Intent itn;
    int camsize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mysoft.mageon.way.R.layout.activity_map_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(com.mysoft.mageon.way.R.id.map);
        mapFragment.getMapAsync(this);
        camsize = 14;
        itn = getIntent();
        Button btn_ok = (Button) findViewById(com.mysoft.mageon.way.R.id.b_map_ok);
        Button btn_cancel = (Button) findViewById(com.mysoft.mageon.way.R.id.b_map_cansel);
        btn_ok.setVisibility(View.VISIBLE);
        btn_cancel.setVisibility(View.VISIBLE);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Аitn = new Intent();
                if(mTempLatLng != null) {
                    itn.putExtra("location", new double[]{mTempLatLng.latitude, mTempLatLng.longitude});
                    setResult(RESULT_OK, itn);
                }else
                    setResult(RESULT_CANCELED, itn);
                finish();
            }
        });
        //Карта плюс
        ImageButton b_map_plus = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.b_map_plus);
        b_map_plus.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                camsize = camsize + 1;
                mMap.animateCamera(CameraUpdateFactory.zoomTo(camsize));
            }
        });

        //Карта минус
        ImageButton b_map_minus = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.b_map_minus);
        b_map_minus.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (camsize > 2)
                    camsize = camsize - 1;
                mMap.animateCamera(CameraUpdateFactory.zoomTo(camsize));
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        UiSettings settings = mMap.getUiSettings();
        settings.setMapToolbarEnabled(false);
        settings.setCompassEnabled(true);
        settings.setZoomGesturesEnabled(false);
            if(itn.getDoubleExtra("latitude", 0) != 0){
                mTempLatLng = new LatLng(itn.getDoubleExtra("latitude", 0), itn.getDoubleExtra("longitude", 0));
                mMarker = mMap.addMarker(new MarkerOptions()
                        .position(mTempLatLng)
                        .icon(BitmapDescriptorFactory.defaultMarker()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mTempLatLng, camsize), 1000, null);
            }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    mTempLatLng = latLng;
                    if(mMarker != null)
                        mMarker.remove();
                    mMarker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.defaultMarker()));
                }
            });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    marker.remove();
                    if(mMarker != null)
                        mMarker.remove();
                    return false;
                }
            });
        }
}
