package com.mysoft.mageon.way;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.android.SphericalUtil;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_GREEN;
import static com.google.android.gms.maps.model.BitmapDescriptorFactory.HUE_YELLOW;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    CurrentSostoyanie mCurrentSostoyanie = null;
    ConnectProvider mConnectProvider = null;
    GoogleMap map_go;
    ServiceConnection sConn;
    private Messenger messenger;
    TextView distance;
    TextView next_point;
    TextView text_distamce;
    TextView text_m;
    com.google.android.gms.location.LocationListener mLocationListener = null;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    String searchQuery;
    RecyclerView mRV_Items = null;
    int mOldState;

    int camsize = 2;
    GoogleApiClient mGoogleApiClient;
    LocationRequest REQUEST;
    ArrayList<String> searchList;
    ArrayList<String> filterTypes = new ArrayList<String>();
    AlertDialog dialog;
    private List<String> items;
    SearchView searchView;
    ArrayList<PlaceDetails> mSearchList = new ArrayList<PlaceDetails>();
    SearchRecyclerView mAdapter = null;
    private SearchHistoryRecyclerAdapter mSearchHistoryAdapter = null;
    BottomSheetBehavior bottomSheetBehavior;
    LinearLayout llBottomSheet;
    boolean startSearch;
    int mButtonPosition;
    int mSearch_radius;
    LatLng newPoint;
    final int mMaxSearchRadius = 50000;
    RefreshTask tempAS = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mysoft.mageon.way.R.layout.activity_map);
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        com.google.android.gms.analytics.Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("ImageMapActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        SupportMapFragment mp_go = (SupportMapFragment) getSupportFragmentManager().findFragmentById(com.mysoft.mageon.way.R.id.map_go);
        mp_go.getMapAsync(this);
        mSearch_radius = 0;
        mCurrentSostoyanie = CurrentSostoyanie.getInstance();
        mConnectProvider = ConnectProvider.getInstance(this);
        LinearLayout mSearch_Layout = (LinearLayout) findViewById(com.mysoft.mageon.way.R.id.search_layout);
        final ImageView b_list_search = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_list_search);
        llBottomSheet = (LinearLayout) findViewById(com.mysoft.mageon.way.R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        searchView = (SearchView) findViewById(com.mysoft.mageon.way.R.id.sw_search);
        KeyboardVisibilityEvent.setEventListener(
                this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        if(isOpen) {
                            visibilityOf(View.GONE);
                            b_list_search.setVisibility(View.GONE);
                            hideListSearch();
                        }else {
                            searchView.clearFocus();
                            searchView.refreshDrawableState();
                            if (mSearchList.size() > 0 && !startSearch)
                                showListSearch();
                            visibilityOf(View.VISIBLE);
                        }
                    }
                });

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus) {
                    hideSearchHistory();
                    if(CurrentSostoyanie.getInstance().getSearch_Marker().size() > 0 && searchView.getQuery().length() == 0) {
                        for (int ind = 0; ind < CurrentSostoyanie.getInstance().getSearch_Marker().size(); ind++)
                            CurrentSostoyanie.getInstance().getSearch_Marker().get(ind).remove();
                        CurrentSostoyanie.getInstance().getSearch_Marker().clear();
                        if(mSearchList.size() > 0)
                            for(int indM = 0; indM < mSearchList.size(); indM++)
                                mSearchList.get(indM).mMerker.remove();

                        mSearchList.clear();
                        b_list_search.setVisibility(View.GONE);
                    }else
                        if(mSearchList.size() > 0 && !startSearch)
                            b_list_search.setVisibility(View.VISIBLE);
                        else
                            visibilityOf(View.VISIBLE);
                }else {
                    if(mOldState < 3) {
                        b_list_search.setVisibility(View.GONE);
                        hideListSearch();
                    }
                }
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibilityOf(View.GONE);
                loadHistory("");
                if(mSearchList.size() > 0)
                    hideListSearch();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearch = true;
                ConnectivityManager cm =
                        (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    if (mCurrentSostoyanie != null) {
                        if (mCurrentSostoyanie.getMyLocation() != null) {
                            if (mCurrentSostoyanie.getNewRadius() != null)
                                makeSearch(query, mCurrentSostoyanie.getNewRadius().getPosition().latitude, mCurrentSostoyanie.getNewRadius().getPosition().longitude, mSearch_radius);
                            else if (mSearch_radius < 0.1)
                                Toast.makeText(MapActivity.this, getResources().getString(com.mysoft.mageon.way.R.string.error_no_available_providers), Toast.LENGTH_LONG).show();
                            else
                                makeSearch(query, mCurrentSostoyanie.getMyLocation().getLatitude(), mCurrentSostoyanie.getMyLocation().getLongitude(), mSearch_radius);

                            hideSearchHistory();
                            hideListSearch();
                            searchView.clearFocus();
                        }else
                            Toast.makeText(MapActivity.this, getResources().getString(com.mysoft.mageon.way.R.string.error_getlocatrion), Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(MapActivity.this, getResources().getString(com.mysoft.mageon.way.R.string.error_internet), Toast.LENGTH_SHORT).show();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                loadHistory(query);
                if(mSearchList.size() > 0) {
                    if (query.length() == 0)
                        for (int ind = 0; ind < mSearchList.size(); ind++)
                            mSearchList.get(ind).mMerker.remove();
                    mSearchList.clear();
                }
                    //hideListSearch();
                return false;
            }
        });




            if (!mConnectProvider.checkGPS() && !mConnectProvider.checkNet())
                Toast.makeText(MapActivity.this, getResources().getString(com.mysoft.mageon.way.R.string.error_no_available_providers), Toast.LENGTH_LONG).show();
             else
                getLocation();
        //Текстовые поля
        distance = (TextView) findViewById(com.mysoft.mageon.way.R.id.distance);
        next_point = (TextView) findViewById(com.mysoft.mageon.way.R.id.text_next_point);
        text_distamce = (TextView) findViewById(com.mysoft.mageon.way.R.id.text_distamce);
        text_m = (TextView) findViewById(com.mysoft.mageon.way.R.id.text_m);

        //Текущая локация/*
        final ImageView im_getlocation = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_getlocation);
        im_getlocation.setSelected(false);
        im_getlocation.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
                }else {

                        if (!mCurrentSostoyanie.isFixcamera()) {
                            if (mCurrentSostoyanie.getMyLocation() == null) {
                                if (!mConnectProvider.checkGPS() && !mConnectProvider.checkNet()) {
                                    Toast.makeText(MapActivity.this, getResources().getString(com.mysoft.mageon.way.R.string.error_no_available_providers), Toast.LENGTH_LONG).show();
                                    changeGPS();
                                } else {
                                    if (mCurrentSostoyanie.getMyLocation() != null) {
                                        if (camsize <= 2)
                                            camsize = 14;
                                        updateCamera(mCurrentSostoyanie.getMyLatLng(), camsize);
                                        setPictFixcamera();
                                    }else
                                        getLocationHand();
                                }
                            } else {
                                if (camsize <= 2)
                                    camsize = 14;
                                mCurrentSostoyanie.setFixcamera(true);
                                updateCamera(mCurrentSostoyanie.getMyLatLng(), camsize);
                                setPictFixcamera();
                            }
                        } else {
                            mCurrentSostoyanie.setFixcamera(false);
                            setPictFixcamera();
                        }
                    }
                }
        });

        //Карта плюс
        ImageButton b_map_plus = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.b_map_plus);
        b_map_plus.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                camsize = camsize + 1;
                map_go.animateCamera(CameraUpdateFactory.zoomTo(camsize));
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
                map_go.animateCamera(CameraUpdateFactory.zoomTo(camsize));
            }
        });

        //Старт-стоп сервис
        ImageView im_start = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_start);
        im_start.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
                } else {
                    if (!mCurrentSostoyanie.isService_Started()) {
                        if (!mConnectProvider.checkGPS() && !mConnectProvider.checkNet()) {
                            Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.error_no_available_providers), Toast.LENGTH_LONG).show();
                            changeGPS();
                        } else {
                            mCurrentSostoyanie.setNextPoint(null);
                            mCurrentSostoyanie.setService_Started(true);
                            v.setBackgroundResource(com.mysoft.mageon.way.R.drawable.navigation_off);
                            mCurrentSostoyanie.setFixcamera(true);
                            setPictFixcamera();
                            if(mCurrentSostoyanie.getAr_Control().size() > 0) {
                                distance.setText("0");
                                next_point.setText(getResources().getString(com.mysoft.mageon.way.R.string.point_getcoordinates));
                                distance.setVisibility(View.VISIBLE);
                                text_distamce.setVisibility(View.VISIBLE);
                                next_point.setVisibility(View.VISIBLE);
                                text_m.setVisibility(View.VISIBLE);
                            }
                            if (mCurrentSostoyanie.getMyLocation() != null) {
                                updateCamera(mCurrentSostoyanie.getMyLatLng(), camsize);
                            }
                            Message msg = Message
                                    .obtain(null, MyServiceMAGEON.MSG_START);
                            try {
                                messenger.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        stopService();
                    }
                }
            }
        });

            Intent intent = new Intent(this, MyServiceMAGEON.class);
            MyReceiver myReceiver = new MyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MyServiceMAGEON.MAGEONWAY);
            registerReceiver(myReceiver, intentFilter);
            startService(intent);

            sConn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    messenger = new Messenger(service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    messenger = null;
                }
            };
            bindService(new Intent(this, MyServiceMAGEON.class), sConn, Context.BIND_AUTO_CREATE);

        //Настройка поиска
        SharedPreferences sPref = getSharedPreferences("com.example.mageon.way", Context.MODE_PRIVATE);
        final SharedPreferences.Editor ed = sPref.edit();
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(com.mysoft.mageon.way.R.string.t_search_perameters));
        View  dialogView;
        if(Resources.getSystem().getConfiguration().locale.getLanguage().toUpperCase().equals("RU"))
            dialogView = this.getLayoutInflater().inflate(com.mysoft.mageon.way.R.layout.activity_filter_search, null);
        else
            dialogView = this.getLayoutInflater().inflate(com.mysoft.mageon.way.R.layout.activity_filter_search_en, null);
        final EditText search_radius = (EditText) dialogView.findViewById(com.mysoft.mageon.way.R.id.search_radius);
        final EditText gps_interval = (EditText) dialogView.findViewById(com.mysoft.mageon.way.R.id.gps_interval);
        final EditText repetbell = (EditText) dialogView.findViewById(com.mysoft.mageon.way.R.id.repetbell);
        final CheckBox cb_search_bank = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_bank);
        final CheckBox cb_vibrosignal = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_vibrosignal);
        final CheckBox cb_search_entertainment = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_entertainment);
        final CheckBox cb_search_establishments = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_establishments);
        final CheckBox cb_search_food = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_food);
        final CheckBox cb_search_medicine = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_medicine);
        final CheckBox cb_search_service = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_service);
        final CheckBox cb_search_shops = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_shops);
        final CheckBox cb_search_sport = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_sport);
        final CheckBox cb_search_transport = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_transport);
        final CheckBox cb_search_for_pets = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_for_pets);
        final CheckBox cb_search_now_open = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_now_open);
        final CheckBox cb_search_hotels = (CheckBox) dialogView.findViewById(com.mysoft.mageon.way.R.id.cb_search_hotels);
        alert.setView(dialogView);
        alert.setCancelable(false);
        alert.setNegativeButton(com.mysoft.mageon.way.R.string.b_cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alert.setPositiveButton(getResources().getString(com.mysoft.mageon.way.R.string.b_save), new DialogInterface.OnClickListener() {

            String[] arr = new String[]{};
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ed.putString("search_radius", search_radius.getText().toString());
                ed.putLong("interval", Long.parseLong(gps_interval.getText().toString()));
                ed.putString("repetbell", repetbell.getText().toString());
                mSearch_radius = Integer.parseInt(search_radius.getText().toString());
                if(cb_vibrosignal.isChecked()){
                    ed.putString("vibrosignal", "true");
                }else {
                    ed.remove("vibrosignal");
                }
                if(cb_search_bank.isChecked()){
                    searchList.add("bank");
                    ed.putBoolean("bank", true);
                }else {
                    searchList.remove("bank");
                    ed.remove("bank");
                }
                if(cb_search_entertainment.isChecked()){
                    searchList.add("entertainment");
                    ed.putBoolean("entertainment", true);
                    arr = new String[]{"amusement_park","aquarium","art_gallery","atm","bar","bowling_alley","casino","movie_theater",
                            "movie_rental","museum","night_club","painter","zoo","park","rv_park","campground"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("entertainment");
                    ed.remove("entertainment");
                    arr = new String[]{"amusement_park","aquarium","art_gallery","atm","bar","bowling_alley","casino","movie_theater",
                            "movie_rental","museum","night_club","painter","zoo","park","rv_park","campground"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.remove(arr[ind]);
                }
                if(cb_search_establishments.isChecked()){
                    searchList.add("establishments");
                    ed.putBoolean("establishments", true);
                    arr = new String[]{"city_hall","courthouse","embassy","local_government_office","post_office","school","university","police",
                            "fire_station","insurance_agency","real_estate_agency"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("establishments");
                    ed.remove("establishments");
                    arr = new String[]{"city_hall","courthouse","embassy","local_government_office","post_office","school","university","police",
                            "fire_station","insurance_agency","real_estate_agency"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.remove(arr[ind]);
                }
                 if(cb_search_food.isChecked()){
                    searchList.add("food");
                    ed.putBoolean("food", true);
                     arr = new String[]{"bakery","cafe","meal_delivery","meal_takeaway","restaurant","shopping_mall","store"};
                     for(int ind=1;ind<arr.length;ind++)
                         filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("food");
                    ed.remove("food");
                     arr = new String[]{"bakery","cafe","meal_delivery","meal_takeaway","restaurant","shopping_mall","store"};
                     for(int ind=1;ind<arr.length;ind++)
                         filterTypes.remove(arr[ind]);
                }
                if(cb_search_medicine.isChecked()){
                    searchList.add("medicine");
                    ed.putBoolean("medicine", true);
                    arr = new String[]{"dentist","doctor","doctor","pharmacy","physiotherapist"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("medicine");
                    ed.remove("medicine");
                    arr = new String[]{"dentist","doctor","doctor","pharmacy","physiotherapist"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.remove(arr[ind]);
                }
                if(cb_search_service.isChecked()){
                    searchList.add("service");
                    ed.putBoolean("service", true);
                    arr = new String[]{"electrician","laundry","lawyer","locksmith","plumber","roofing_contractor","storage"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("service");
                    ed.remove("service");
                    arr = new String[]{"electrician","laundry","lawyer","locksmith","plumber","roofing_contractor","storage"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.remove(arr[ind]);
                }
                if(cb_search_shops.isChecked()){
                    searchList.add("shops");
                    ed.putBoolean("shops", true);
                    arr = new String[]{"book_store","clothing_store","convenience_store","department_store","electronics_store","florist","furniture_store",
                                                "home_goods_store","jewelry_store","liquor_store","shoe_store"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("shops");
                    ed.remove("shops");
                    arr = new String[]{"book_store","clothing_store","convenience_store","department_store","electronics_store","florist","furniture_store",
                            "home_goods_store","jewelry_store","liquor_store","shoe_store"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.remove(arr[ind]);
                }
                if(cb_search_sport.isChecked()){
                    searchList.add("sport");
                    ed.putBoolean("sport", true);
                    arr = new String[]{"beauty_salon","hair_care","stadium","spa"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("sport");
                    ed.remove("sport");
                    arr = new String[]{"beauty_salon","hair_care","stadium","spa"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.remove(arr[ind]);
                }
                if(cb_search_transport.isChecked()){
                    searchList.add("transport");
                    ed.putBoolean("transport", true);
                    arr = new String[]{"bicycle_store","parking","airport","bus_stationcar_dealer","car_rental","car_repair","car_wash",
                            "gas_station","moving_company","subway_station","taxi_stand","train_station","transit_station","travel_agency"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("transport");
                    ed.remove("transport");
                    arr = new String[]{"book_store","clothing_store","convenience_store","department_store","electronics_store","florist","furniture_store",
                            "home_goods_store","jewelry_store","liquor_store","shoe_store"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.remove(arr[ind]);
                }
                if(cb_search_for_pets.isChecked()){
                    searchList.add("for_pets");
                    ed.putBoolean("for_pets", true);
                    arr = new String[]{"pet_store","veterinary_care"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.add(arr[ind]);
                }else {
                    searchList.remove("for_pets");
                    ed.remove("for_pets");
                    arr = new String[]{"pet_store","veterinary_care"};
                    for(int ind=1;ind<arr.length;ind++)
                        filterTypes.remove(arr[ind]);
                }
                if(cb_search_hotels.isChecked()){
                    searchList.add("hotels");
                    ed.putBoolean("hotels", true);
                    filterTypes.add("lodging");
                }else {
                    searchList.remove("hotels");
                    ed.remove("hotels");
                    filterTypes.remove("lodging");
                }
                if(cb_search_now_open.isChecked()){
                    searchList.add("opennow");
                    ed.putBoolean("opennow", true);
                }else {
                    searchList.remove("opennow");
                    ed.remove("opennow");
                }

                ed.commit();
            }
        });
        dialog = alert.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                search_radius.setCursorVisible(false);
                gps_interval.setCursorVisible(false);
                repetbell.setCursorVisible(false);
            }
        });

        search_radius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                search_radius.setCursorVisible(true);
            }
        });

        search_radius.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    if (search_radius.getText().toString().length() != 0) {
                        if (Integer.parseInt(search_radius.getText().toString()) > mMaxSearchRadius) {
                            search_radius.setText(Integer.toString(mMaxSearchRadius));
                            Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.error_maxradius)+Integer.toString(mMaxSearchRadius)+" meters" , Toast.LENGTH_SHORT).show();
                        }
                    }
                    search_radius.clearFocus();
                    search_radius.setCursorVisible(false);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search_radius.getWindowToken(), 0);
                }
                return false;
            }
        });

        gps_interval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gps_interval.setCursorVisible(true);
            }
        });
        gps_interval.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    gps_interval.setCursorVisible(true);
            }
        });

        repetbell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                repetbell.setCursorVisible(true);
            }
        });
        repetbell.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    repetbell.setCursorVisible(true);
            }
        });

        repetbell.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    repetbell.clearFocus();
                    repetbell.setCursorVisible(false);
                    repetbell.setSelection(0, 0);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(repetbell.getWindowToken(), 0);
                }
                return false;
            }
        });
        ImageView btn_settings = (ImageView) findViewById(com.mysoft.mageon.way.R.id.btn_settings);
        btn_settings.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.show();
                                            }
                                        });
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        final ViewGroup.LayoutParams params=llBottomSheet.getLayoutParams();
        params.height=displaymetrics.heightPixels/2-20;
        llBottomSheet.setLayoutParams(params);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        b_list_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    b_list_search.setVisibility(View.GONE);
                    mOldState = 1;
                    if(mButtonPosition == 0)
                        changeButtonPosition(1);
                }
            }
        });

        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback(){
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState == BottomSheetBehavior.STATE_HIDDEN){
                    params.height = displaymetrics.heightPixels / 2 - 20;
                    llBottomSheet.setLayoutParams(params);
                    mOldState = 1;
                    if(mSearchList.size() > 0 && !KeyboardVisibilityEvent.isKeyboardVisible(MapActivity.this)) {
                        ImageView b_list_search = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_list_search);
                        b_list_search.setVisibility(View.VISIBLE);
                    }
                    if(mButtonPosition == 1)
                        changeButtonPosition(0);

                    searchView.setVisibility(View.VISIBLE);
                }else if (newState == BottomSheetBehavior.STATE_EXPANDED && mOldState == 2){
                    params.height = displaymetrics.heightPixels;
                    llBottomSheet.setLayoutParams(params);
                    searchView.clearFocus();
                    searchView.setVisibility(View.GONE);
                    mOldState = 3;
                    b_list_search.setVisibility(View.GONE);
                }else if (newState == BottomSheetBehavior.STATE_EXPANDED && mOldState == 1){
                    mOldState = 2;
                    b_list_search.setVisibility(View.GONE);
                }else if (newState == BottomSheetBehavior.STATE_EXPANDED && mOldState == 3){
                    mOldState = 2;
                    params.height = displaymetrics.heightPixels / 2 - 20;
                    llBottomSheet.setLayoutParams(params);
                    searchView.setVisibility(View.VISIBLE);
                    b_list_search.setVisibility(View.GONE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
        final ImageView button_list_search = (ImageView) findViewById(com.mysoft.mageon.way.R.id.button_list_search);
        button_list_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOldState == 2) {
                    params.height = displaymetrics.heightPixels;
                    llBottomSheet.setLayoutParams(params);
                    mOldState = 3;
                    searchView.clearFocus();
                    searchView.setVisibility(View.GONE);
                }else if(mOldState == 3) {
                    params.height = displaymetrics.heightPixels / 2 - 20;
                    llBottomSheet.setLayoutParams(params);
                    mOldState = 2;
                    searchView.setVisibility(View.VISIBLE);
                }
            }
        });

        searchList = new ArrayList<String>();
        if(sPref.contains("bank"))
            searchList.add("bank");
        if(sPref.contains("entertainment"))
            searchList.add("entertainment");
        if(sPref.contains("establishments"))
            searchList.add("establishments");
        if(sPref.contains("food"))
            searchList.add("food");
        if(sPref.contains("medicine"))
            searchList.add("medicine");
        if(sPref.contains("service"))
            searchList.add("service");
        if(sPref.contains("shops"))
            searchList.add("shops");
        if(sPref.contains("sport"))
            searchList.add("sport");
        if(sPref.contains("transport"))
            searchList.add("transport");
        if(sPref.contains("for_pets"))
            searchList.add("for_pets");
        if(sPref.contains("hotels"))
            searchList.add("hotels");
        if(sPref.contains("opennow"))
            searchList.add("opennow");
        search_radius.setText(sPref.getString("search_radius", "0"));
        gps_interval.setText(Long.toString(sPref.getLong("interval", 0)));
        repetbell.setText(sPref.getString("repetbell", "1"));
        if(sPref.contains("vibrosignal"))
            cb_vibrosignal.setChecked(true);
        mSearch_radius = Integer.parseInt(search_radius.getText().toString());
        cb_search_bank.setChecked(searchList.contains("bank"));
        cb_search_entertainment.setChecked(searchList.contains("entertainment"));
        cb_search_establishments.setChecked(searchList.contains("establishments"));
        cb_search_food.setChecked(searchList.contains("food"));
        cb_search_medicine.setChecked(searchList.contains("medicine"));
        cb_search_service.setChecked(searchList.contains("service"));
        cb_search_shops.setChecked(searchList.contains("shops"));
        cb_search_sport.setChecked(searchList.contains("sport"));
        cb_search_transport.setChecked(searchList.contains("transport"));
        cb_search_for_pets.setChecked(searchList.contains("for_pets"));
        cb_search_hotels.setChecked(searchList.contains("hotels"));
        cb_search_now_open.setChecked(searchList.contains("opennow"));
    }

    private void visibilityOf(int arg){
        ImageButton b_map_plus = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.b_map_plus);
        b_map_plus.setVisibility(arg);
        ImageButton b_map_getlocation = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.b_map_getlocation);
        b_map_getlocation.setVisibility(arg);
        ImageButton b_map_minus = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.b_map_minus);
        b_map_minus.setVisibility(arg);
        ImageButton b_map_start = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.b_map_start);
        b_map_start.setVisibility(arg);
        if(searchView.getQuery().toString().equals("") && arg==View.VISIBLE) {
            ImageView btn_settings = (ImageView) findViewById(com.mysoft.mageon.way.R.id.btn_settings);
            btn_settings.setVisibility(View.VISIBLE);
        }else{
            ImageView btn_settings = (ImageView) findViewById(com.mysoft.mageon.way.R.id.btn_settings);
            btn_settings.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getOrder() == 0)
            clearMarkers();
        else
            openFilterSearch();
        return false;
    }

    private void showListSearch(){
        ViewGroup.LayoutParams params=llBottomSheet.getLayoutParams();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        params.height=displaymetrics.heightPixels/2-20;
        if(mOldState == 0 )
            mOldState = 2;
        if(mButtonPosition == 0)
            changeButtonPosition(1);
        llBottomSheet.setLayoutParams(params);
        llBottomSheet.setVisibility(View.VISIBLE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private void changeButtonPosition(int arg){
        mButtonPosition = arg;
        if(arg == 1) {
            final ImageView im_getlocation = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_getlocation);
            im_getlocation.animate().translationYBy(getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT * -120).setDuration(500);
            final ImageView b_map_plus = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_plus);
            b_map_plus.animate().translationYBy(getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT * -140).setDuration(500);
            final ImageView b_map_minus = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_minus);
            b_map_minus.animate().translationYBy(getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT * -140).setDuration(500);
        }else{
            final ImageView im_getlocation = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_getlocation);
            im_getlocation.animate().translationYBy(getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT * 120).setDuration(500);
            final ImageView b_map_plus = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_plus);
            b_map_plus.animate().translationYBy(getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT * 140).setDuration(500);
            final ImageView b_map_minus = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_minus);
            b_map_minus.animate().translationYBy(getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT * 140).setDuration(500);
        }
    }

    private void hideListSearch(){
        //if(mSearchList.size() > 0)
            if( bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN){
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                mOldState = 1;
            }
    }

    private void hideSearchHistory(){
        CardView cw_search = (CardView) findViewById(com.mysoft.mageon.way.R.id.cw_search);
        cw_search.setVisibility(View.GONE);
    }

    private void showSearchHistory(){
        CardView cw_search = (CardView) findViewById(com.mysoft.mageon.way.R.id.cw_search);
        cw_search.setVisibility(View.VISIBLE);
    }

    private void loadHistory(String query) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            items = DBHelper.getInctanceDBHelper(this).getListHistorySearch(query);
            if(items.size() > 0) {
                mRV_Items = (RecyclerView) findViewById(com.mysoft.mageon.way.R.id.rv_search_history);
                mSearchHistoryAdapter = new SearchHistoryRecyclerAdapter(this, items);
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                mRV_Items.setLayoutManager(mLayoutManager);
                mRV_Items.setAdapter(mSearchHistoryAdapter);
                showSearchHistory();
            }
        }
    }

    private void clearMarkers(){
                map_go.clear();
                if(mCurrentSostoyanie.getMyLatLng() != null)
                    map_go.addMarker(new MarkerOptions()
                            .position(mCurrentSostoyanie.getMyLatLng())
                            .anchor(0.5f, 0.5f)
                            .icon(BitmapDescriptorFactory.fromResource(com.mysoft.mageon.way.R.drawable.yatut)));
    }

    private void openFilterSearch(){
        dialog.show();

    }

    @Override
    public void onBackPressed() {
        if( mCurrentSostoyanie != null)
            if(mCurrentSostoyanie.isService_Started()) {
                stopService(new Intent(this, MyServiceMAGEON.class));
                mCurrentSostoyanie.setMyLocation(null);
                mCurrentSostoyanie.setMyLatLng(null);
            }
        finish();
    }

    @Override
    public void finish() {

        if( mCurrentSostoyanie != null) {
            if (mCurrentSostoyanie.getYatut() != null) {
                mCurrentSostoyanie.getYatut().remove();
                mCurrentSostoyanie.setYatut(null);
                List<Marker> mMarkers = mCurrentSostoyanie.getAr_Marker();
                for (int ind = 0; ind < mMarkers.size(); ind++) {
                    mMarkers.get(ind).remove();
                }
                mMarkers.clear();
            }
            mCurrentSostoyanie.setMyLatLng(null);
            mCurrentSostoyanie.setMyLocation(null);
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected() && mLocationListener != null) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                    mGoogleApiClient.disconnect();
                }
            }
        }
        if(tempAS != null)
            if(!tempAS.isCancelled())
                tempAS.cancel(true);
        super.finish();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location myLoc = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(myLoc == null)
            myLoc = mCurrentSostoyanie.getMyLocation();
        if(myLoc != null){
            camsize = 14;
            mCurrentSostoyanie.setFixcamera(true);
            updateCamera(new LatLng(myLoc.getLatitude(), myLoc.getLongitude()), camsize);
            setPictFixcamera();
        }else
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
        Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.destroy_service) , Toast.LENGTH_SHORT).show();
    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            double[] latlong = arg1.getDoubleArrayExtra("latlong");
            if (latlong != null) {
                if(camsize <= 2)
                    camsize = 14;
                updateCamera(new LatLng(latlong[0], latlong[1]), camsize);
                if (mCurrentSostoyanie.getAr_Control().size() > 0)
                    if (mCurrentSostoyanie.getNextPoint() == null){
                        getNearestPoint(mCurrentSostoyanie.getMyLocation());
                        getDistance();
                    }else
                      getDistance();
            }else{
                ImageView im_start = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_start);
                im_start.setBackgroundResource(com.mysoft.mageon.way.R.drawable.navigation);
                if(mCurrentSostoyanie.isService_Started()) {
                    Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.error_no_available_providers), Toast.LENGTH_SHORT).show();
                    Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.stop_service), Toast.LENGTH_SHORT).show();
                }
                mCurrentSostoyanie.setService_Started(false);
            }
        }
    }

    public void onMapReady(GoogleMap googleMap) {
        map_go = googleMap;

        UiSettings settings = map_go.getUiSettings();
        settings.setMapToolbarEnabled(false);
        settings.setCompassEnabled(true);
        settings.setZoomGesturesEnabled(false);


            updateMarkers();

        final AlertDialog.Builder alertmenu = new AlertDialog.Builder(this);
        alertmenu.setItems(new String[]{getResources().getString(com.mysoft.mageon.way.R.string.action_add), getResources().getString(com.mysoft.mageon.way.R.string.action_set_point_search)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                switch (item){
                    case 0:
                        addNewPoint();
                        break;
                    case 1:
                        setNewPointOfSearch();
                        break;
                    default:
                        break;

                }
            }
        });
        map_go.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                newPoint = latLng;
                alertmenu.show();
            }
        });
        map_go.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.equals(mCurrentSostoyanie.getNewRadius())) {
                    mCurrentSostoyanie.getNewRadius().remove();
                }
                return false;
            }
        });
    }

    private void setNewPointOfSearch(){
        if (mCurrentSostoyanie.getNewRadius() != null)
            mCurrentSostoyanie.getNewRadius().remove();

        mCurrentSostoyanie.setNewRadius(map_go.addMarker(new MarkerOptions()
                .position(newPoint)
                .title(getResources().getString(com.mysoft.mageon.way.R.string.action_set_point_search))
                .icon(BitmapDescriptorFactory.defaultMarker(HUE_YELLOW))));

        mCurrentSostoyanie.getNewRadius().showInfoWindow();
    }

    private void addNewPoint(){
        Intent itnPoint = new Intent(this, EditPointActivity.class);
        itnPoint.putExtra("latitude", newPoint.latitude);
        itnPoint.putExtra("longitude", newPoint.longitude);
        itnPoint.putExtra("id", -1);
        startActivity(itnPoint);
    }

    //Установить камеру
    protected void setPictFixcamera() {
        ImageView im_getlocation = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_getlocation);
        if (mCurrentSostoyanie.isFixcamera())
            im_getlocation.setSelected(true);
        else
            im_getlocation.setSelected(false);
    }

    protected void deleteRowHistory(String arg){
        DBHelper.getInctanceDBHelper(this).deleteRowHistory(arg);
        items.remove(arg);
        mSearchHistoryAdapter.notifyDataSetChanged();
    }

    protected void getRowHistory(String arg){
        searchView.setQuery(arg, false);
        hideSearchHistory();
    }

    //Остановить сервис
    private void stopService(){
        mCurrentSostoyanie.setService_Started(false);
        ImageView im_start = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_map_start);
        im_start.setBackgroundResource(com.mysoft.mageon.way.R.drawable.navigation);
        distance.setVisibility(View.INVISIBLE);
        text_distamce.setVisibility(View.INVISIBLE);
        next_point.setVisibility(View.INVISIBLE);
        text_m.setVisibility(View.INVISIBLE);
        Message msg = Message
                .obtain(null, MyServiceMAGEON.MSG_STOP);
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void updateCamera(LatLng latlng, int arg){
        if(arg > 0)
            camsize = arg;
            if (mCurrentSostoyanie.getMyLocation() == null)
                mCurrentSostoyanie.setMyLocation(new Location("myloc"));

            mCurrentSostoyanie.getMyLocation().setLatitude(latlng.latitude);
            mCurrentSostoyanie.getMyLocation().setLongitude(latlng.longitude);
            mCurrentSostoyanie.setMyLatLng(latlng);

            if (mCurrentSostoyanie.getYatut() == null) {
                mCurrentSostoyanie.setYatut(map_go.addMarker(new MarkerOptions()
                        .position(latlng)
                        .anchor(0.5f, 0.5f)
                        .icon(BitmapDescriptorFactory.fromResource(com.mysoft.mageon.way.R.drawable.yatut))));
            } else
                mCurrentSostoyanie.getYatut().setPosition(latlng);
            if(mCurrentSostoyanie != null) {
                if (mCurrentSostoyanie.isFixcamera())
                    map_go.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, camsize),1000, null);
            }else{
                camsize = 14;
                map_go.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, camsize),1000, null);
            }
    }

    private void getLocation() {
        if(mLocationListener == null)
            mLocationListener = new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(location != null) {
                        camsize = 14;
                        mCurrentSostoyanie.setMyLocation(location);
                        mCurrentSostoyanie.setMyLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                        mCurrentSostoyanie.setFixcamera(true);
                        setPictFixcamera();
                        updateCamera(new LatLng(location.getLatitude(), location.getLongitude()), camsize);
                    }
            }
        };
        if(REQUEST == null)
            REQUEST = LocationRequest.create()
                .setInterval(1)         // 5 seconds
                .setFastestInterval(1)    // 16ms = 60fps
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();
    }

    private void getLocationHand() {
        mCurrentSostoyanie.setFixcamera(true);
        setPictFixcamera();
        if(mLocationListener == null)
            mLocationListener = new com.google.android.gms.location.LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(location != null) {
                        camsize = 14;
                        mCurrentSostoyanie.setMyLocation(location);
                        mCurrentSostoyanie.setMyLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                        mCurrentSostoyanie.setFixcamera(true);
                        updateCamera(new LatLng(location.getLatitude(), location.getLongitude()), camsize);
                    }
                }
            };
        if(REQUEST == null)
            REQUEST = LocationRequest.create()
                    .setInterval(1)         // 5 seconds
                    .setFastestInterval(1)    // 16ms = 60fps
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(mGoogleApiClient == null)
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

        mGoogleApiClient.connect();
        if(tempAS == null)
            tempAS = new RefreshTask();
        else
            if(!tempAS.isCancelled())
                tempAS.cancel(true);

        tempAS.execute();
    }

    protected void getControlList() {
        ArrayList<Object> temp = CurrentSostoyanie.getInstance().getAr_ControlID();
        ArrayList<Point> ar_control = mCurrentSostoyanie.getAr_Control();
        if(mCurrentSostoyanie.getAr_Control() == null) {
            ar_control = new ArrayList<Point>();
        }else {
            ar_control.clear();
        }
        DBHelper mDBHelper = DBHelper.getInctanceDBHelper(this);
        if (temp.size() > 0) {
            for (int ind = 0; ind < temp.size(); ind++) {
                Point tekPoint = mDBHelper.getPoint((int) temp.get(ind));
                ar_control.add((Point) tekPoint);
            }
        }
    }

    private void updateMarkers(){
        getControlList();
        LatLng latlng = null;
        ArrayList<Point> ar_control = mCurrentSostoyanie.getAr_Control();
        List<Marker> mMarkers = mCurrentSostoyanie.getAr_Marker();
        for(int ind = 0; ind < mMarkers.size(); ind++) {
            mMarkers.get(ind).remove();
        }
        mMarkers.clear();

        if(ar_control.size() > 0) {
            camsize = 14;
            for (int ind = 0; ind < ar_control.size(); ind++) {
                Point tekPoint = (Point) ar_control.get(ind);
                latlng = new LatLng(tekPoint.latitude, tekPoint.longitude);
                Marker marker = map_go.addMarker(new MarkerOptions()
                        .title(tekPoint.name_point)
                        .position(latlng)
                        .icon(BitmapDescriptorFactory.defaultMarker(HUE_GREEN)));
                marker.showInfoWindow();
                mMarkers.add(marker);
            }
            if (latlng != null) {
                map_go.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, camsize), 1000, null);
            }
        }
    }

    private void getNearestPoint(Location loc){
        float dist = 0;
        mCurrentSostoyanie.setMyLocation(loc);
        ArrayList<Point> ar_control = mCurrentSostoyanie.getAr_Control();
        for(int ind = 0; ind<ar_control.size(); ind++){

            if(ind == 0) {
                mCurrentSostoyanie.setNextPoint(ar_control.get(ind));

                if( mCurrentSostoyanie.getNextLocation() == null){
                    Location mNewloc = new Location("loc");
                    mCurrentSostoyanie.setNextLocation(mNewloc);
                }
                mCurrentSostoyanie.getNextLocation().setLatitude(mCurrentSostoyanie.getNextPoint().latitude);
                mCurrentSostoyanie.getNextLocation().setLongitude(mCurrentSostoyanie.getNextPoint().longitude);
                dist = loc.distanceTo(mCurrentSostoyanie.getNextLocation());
            }else {
                mCurrentSostoyanie.getNextLocation().setLatitude(ar_control.get(ind).latitude);
                mCurrentSostoyanie.getNextLocation().setLongitude(ar_control.get(ind).longitude);
                if (dist > loc.distanceTo(mCurrentSostoyanie.getNextLocation())) {
                    dist = loc.distanceTo(mCurrentSostoyanie.getNextLocation());
                    mCurrentSostoyanie.setNextPoint(ar_control.get(ind));
                }
            }
        }
        next_point.setText(mCurrentSostoyanie.getNextPoint().name_point);
        mCurrentSostoyanie.getNextLocation().setLatitude(mCurrentSostoyanie.getNextPoint().latitude);
        mCurrentSostoyanie.getNextLocation().setLongitude(mCurrentSostoyanie.getNextPoint().longitude);
    }

    private void getDistance() {
        double distTemp = SphericalUtil.computeDistanceBetween(mCurrentSostoyanie.getMyLatLng(), new LatLng(mCurrentSostoyanie.getNextLocation().getLatitude(), mCurrentSostoyanie.getNextLocation().getLongitude()));
        int dist = Math.round((int) distTemp);
        distance.setText(Integer.toString(dist));
        if (dist <= mCurrentSostoyanie.getNextPoint().distance) {
            Toast.makeText(this, mCurrentSostoyanie.getNextPoint().name_point, Toast.LENGTH_SHORT).show();
            beepSignal(mCurrentSostoyanie.getNextPoint().file_signal);
        }
    }

    private void beepSignal(String arg) {
        BellAsyncTask at = new BellAsyncTask();
        mCurrentSostoyanie.getAr_Control().remove(mCurrentSostoyanie.getNextPoint());
        CurrentSostoyanie.getInstance().removeAr_ControlID((Object) mCurrentSostoyanie.getNextPoint().id);
        mCurrentSostoyanie.setNextPoint(null);
        mCurrentSostoyanie.setNextLocation(null);
        SharedPreferences sPref = getSharedPreferences("com.example.mageon.way", Context.MODE_PRIVATE);
        String cyclring = "1";
        if(sPref.contains("repetbell")) {
            cyclring = sPref.getString("repetbell", "1");
        }
        String vibro = "false";
        if(sPref.contains("vibrosignal")) {
            vibro = sPref.getString("vibrosignal", "true");
        }
        at.execute(arg, cyclring, vibro);
    }

    private class BellAsyncTask extends AsyncTask<String, Void, Void>{
        @Override
        protected Void doInBackground(String... voids) {
            final MediaPlayer mPlayer = new MediaPlayer();
            try {
                mPlayer.setDataSource(MapActivity.this, Uri.parse(voids[0]));
                mPlayer.prepare();
                long time_start = System.currentTimeMillis();
                mPlayer.start();
                int long_song = mPlayer.getDuration();
                /*Vibrator v = null;
                if(voids[2].equals("true")){
                    v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    long[] mVibratePattern = new long[]{0, 200, 200, 0};
                    //v.vibrate(mVibratePattern, 3);
                }*/
                long time_end = time_start + ((long) long_song*Integer.parseInt(voids[1]));
                while (time_end > System.currentTimeMillis()) {
                }
                mPlayer.stop();
                mPlayer.release();
                //if(v != null)
                  //  v.cancel();
            } catch (IOException e) {
                e.printStackTrace();
            }            return null;
        }
    }

    private void makeSearch(String argSearch, double argLat, double argLong, int argRadius) {
        searchQuery = argSearch;
        SearchTask mSearch = new SearchTask();
        GenericUrl startUrl = new GenericUrl("https://maps.googleapis.com/maps/api/place/search/json?");
        HttpTransport httpTransport = new NetHttpTransport();
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
        HttpRequest request = null;
        try {
            request = requestFactory.buildGetRequest(startUrl);
            request.getUrl().put("language", Locale.getDefault().getLanguage());
            request.getUrl().put("key", getResources().getString(com.mysoft.mageon.way.R.string.google_webservice_key));
            request.getUrl().put("location", argLat + "," + argLong);
            if (argRadius > 0)
                request.getUrl().put("radius", argRadius);
            if (!argSearch.equals(""))
                request.getUrl().put("keyword", argSearch);

            request.getUrl().put("sensor", "false");
            CheckBox search_now_open = (CheckBox) findViewById(com.mysoft.mageon.way.R.id.cb_search_now_open);
            if (searchList.contains("nowopen"))
                request.getUrl().put("opennow", true);
            String types = "";
            if (searchList.contains("bank"))
                types = "bank|";
            if (searchList.contains("entertainment"))
                types = types + "amusement_park|aquarium|art_gallery|atm|bar|bowling_alley|casino|movie_theater|movie_rental|museum|night_club|painter|zoo|park|rv_park|campground|";
            if (searchList.contains("establishments"))
                types = types + "city_hall|courthouse|embassy|local_government_office|post_office|school|university|police|fire_station|insurance_agency|real_estate_agency|";
            if (searchList.contains("food"))
                types = types + "bakery|cafe|meal_delivery|meal_takeaway|restaurant|shopping_mall|store|";
            if (searchList.contains("medicine"))
                types = types + "dentist|doctor|doctor|pharmacy|physiotherapist|";
            if (searchList.contains("service"))
                types = types + "electrician|laundry|lawyer|locksmith|plumber|roofing_contractor|storage|";
            if (searchList.contains("shops"))
                types = types + "book_store|clothing_store|convenience_store|department_store|electronics_store|florist|furniture_store|store|";
            if (searchList.contains("sport"))
                types = types + "beauty_salon|hair_care|stadium|spa|";
            if (searchList.contains("transport"))
                types = types + "bicycle_store|parking|airport|bus_stationcar_dealer|car_rental|car_repair|car_wash|gas_station|moving_company|subway_station|taxi_stand|train_station" +
                        "|transit_station|travel_agency|";
            if (searchList.contains("for_pets"))
                types = types + "pet_store|veterinary_care|";
            if (searchList.contains("hotels"))
                types = types + "lodging|";

            if(!types.equals(""))
                request.getUrl().put("types", types);

            mSearch.execute(request.getUrl());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void makeSearchDetiles(String arg_id, String open) {
        SearchDetiles mSearch = new SearchDetiles();
        mSearch.execute(arg_id, open);
    }

    private class SearchTask extends AsyncTask<GenericUrl, Integer, String> {
        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MapActivity.this);
            progressDialog.setMessage(getResources().getString(com.mysoft.mageon.way.R.string.t_wait));
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.setProgress(1);
            progressDialog.show();
        }

        protected String doInBackground(GenericUrl... urls) {
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            //GenericUrl url = new GenericUrl("https://maps.googleapis.com/maps/api/place/search/json?location=55.0081463,73.3359871&radius=400&type=bus_station&key=AIzaSyB7lhvWy4_DpUQeuyN7GEmA1L01nEvmzmQ");
            String respond = "";
            JSONObject JSONrespond = null;
            HttpRequest request = null;
            boolean needrequest = true;
            JSONArray JSONresults = new JSONArray();
            JSONArray results = null;
            try {
                do {
                    request = requestFactory.buildGetRequest(urls[0]);
                    respond = request.execute().parseAsString();
                    try {
                        JSONrespond = new JSONObject(respond);
                        if (JSONrespond.get("status").equals("OK")){
                            if(!JSONrespond.has("next_page_token")){
                                needrequest = false;
                            }else {
                                urls[0] = new GenericUrl("https://maps.googleapis.com/maps/api/place/search/json?");
                                urls[0].put("pagetoken", JSONrespond.get("next_page_token"));
                                urls[0].put("key", getResources().getString(com.mysoft.mageon.way.R.string.google_webservice_key));
                            }
                            results = JSONrespond.getJSONArray("results");
                            for (int ind = 0; ind < results.length(); ind++)
                                JSONresults.put(results.getJSONObject(ind));
                        }else {
                            if (JSONrespond.get("status").equals("INVALID_REQUEST ")) {
                                try {
                                    wait(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }else
                                needrequest = false;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }while(needrequest);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(JSONresults.equals(""))
                return "";
            else
                return JSONresults.toString();
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            parseRespond(result);
        }
    }

    private void parseRespond(String arg) {
        mSearchList.clear();
        try {
                JSONArray results = new JSONArray(arg);
                List<Marker> ml_Search_Markers = CurrentSostoyanie.getInstance().getSearch_Marker();
                for (int ind = 0; ind < results.length(); ind++) {
                    PlaceDetails mPlace = new PlaceDetails();
                    JSONObject mResult = results.getJSONObject(ind);
                    JSONObject mResult_Geometry = mResult.getJSONObject("geometry");
                    JSONObject mResult_Location = mResult_Geometry.getJSONObject("location");
                    mPlace.mLocation = new LatLng(mResult_Location.optDouble("lat"), mResult_Location.optDouble("lng"));
                    mPlace.mTypes = mResult.getString("types");

                    if (!mResult.isNull("rating"))
                        mPlace.mMerker = map_go.addMarker(new MarkerOptions().title(mResult.getString("name"))
                                .position(new LatLng(mResult_Location.optDouble("lat"), mResult_Location.optDouble("lng")))
                                .icon(BitmapDescriptorFactory.defaultMarker())
                                .snippet(getResources().getString(com.mysoft.mageon.way.R.string.t_rating) + mResult.getString("rating")));
                    else
                        mPlace.mMerker = map_go.addMarker(new MarkerOptions().title(mResult.getString("name"))
                                .position(new LatLng(mResult_Location.optDouble("lat"), mResult_Location.optDouble("lng")))
                                .icon(BitmapDescriptorFactory.defaultMarker()));
                    mPlace.mName = mResult.getString("name");
                    if (!mResult.isNull("rating"))
                        mPlace.mRating = Float.parseFloat(mResult.getString("rating"));
                    mPlace.mAddress = mResult.getString("vicinity");
                    mPlace.mID = mResult.getString("place_id");
                    if(!mResult.isNull("opening_hours"))
                        mPlace.mOpen = mResult.getJSONObject("opening_hours").getBoolean("open_now");
                    mSearchList.add(mPlace);
                }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mSearchList.size() > 0) {
            Collections.sort(mSearchList);
            if(mAdapter == null) {
                RecyclerView mRv = (RecyclerView) findViewById(com.mysoft.mageon.way.R.id.rv);
                mAdapter = new SearchRecyclerView(mSearchList, this);
                LinearLayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                mRv.setLayoutManager(mLayoutManager);
                //mRv.setHasFixedSize(true);
                //mRv.setItemAnimator(new DefaultItemAnimator());
                mRv.setAdapter(mAdapter);
            }else {
                mAdapter.notifyDataSetChanged();
            }
            showListSearch();
            DBHelper.getInctanceDBHelper(this).addListHistorySearch(searchQuery);
            startSearch = false;
        }else{
            if(mAdapter != null) {
                ImageView b_list_search = (ImageView) findViewById(com.mysoft.mageon.way.R.id.b_list_search);
                b_list_search.setVisibility(View.GONE);
                mAdapter.notifyDataSetChanged();
            }
            Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.error_notsearch), Toast.LENGTH_SHORT).show();
        }
    }

    protected void getMarkerDetiles(String arg){
        if(mSearchList.size() > 0){
            for(int ind = 0; ind < mSearchList.size(); ind++){
                if(mSearchList.get(ind).mID == arg){
                    mSearchList.get(ind).mMerker.showInfoWindow();
                    if(mCurrentSostoyanie.isFixcamera()) {
                        mCurrentSostoyanie.setFixcamera(false);
                        setPictFixcamera();
                    }
                    map_go.setPadding(0,0,0,100);
                    map_go.animateCamera(CameraUpdateFactory.newLatLngZoom(mSearchList.get(ind).mMerker.getPosition(), camsize), 1000, null);
                    map_go.setPadding(0,0,0,0);
                }
            }

        }
    }

    private class SearchDetiles extends AsyncTask<String, Integer, String[]> {
        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MapActivity.this);
            progressDialog.setMessage(getResources().getString(com.mysoft.mageon.way.R.string.t_wait));
            progressDialog.setIndeterminate(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setCancelable(true);
            progressDialog.setProgress(1);
            progressDialog.show();
        }

        protected String[] doInBackground(String... urls) {
            HttpTransport httpTransport = new NetHttpTransport();
            HttpRequestFactory requestFactory = httpTransport.createRequestFactory();
            //GenericUrl url = new GenericUrl("https://maps.googleapis.com/maps/api/place/search/json?location=55.0081463,73.3359871&radius=400&type=bus_station&key=AIzaSyB7lhvWy4_DpUQeuyN7GEmA1L01nEvmzmQ");
            GenericUrl startUrl = new GenericUrl("https://maps.googleapis.com/maps/api/place/details/json?");
            startUrl.put("language", Locale.getDefault().getLanguage());
            //request.getUrl().put("key", getResources().getString(R.string.google_maps_key));
            startUrl.put("key", getResources().getString(com.mysoft.mageon.way.R.string.google_webservice_key));
            startUrl.put("placeid", urls[0]);
            HttpRequest request = null;
            String[] respond = new String[]{"",urls[1]};
            try {
                request = requestFactory.buildGetRequest(startUrl);
                respond[0] = request.execute().parseAsString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return respond;
        }

        protected void onPostExecute(String... result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
            parseRespondDetiles(result[0], result[1]);
        }
    }

    private void parseRespondDetiles(String arg1, String arg2) {
        JSONObject respond = null;
        try {
            respond = new JSONObject(arg1);
            if (respond.get("status").equals("OK")) {
                Intent itn = new Intent(this, ShowDetailes.class);
                itn.putExtra( "result", respond.getString("result").toString());
                itn.putExtra( "open", arg2);
                startActivity(itn);
            }else{
                Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.error_nodate), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void changeGPS() {
        if (!ConnectProvider.getInstance(this).checkforstart()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(com.mysoft.mageon.way.R.string.error_gps_notenabled).setPositiveButton(getResources().getString(com.mysoft.mageon.way.R.string.b_OK),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface d, int id) {
                            Intent itn = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(itn, RESULT_OK);
                            d.dismiss();
                        }
                    }).setNegativeButton(getResources().getString(com.mysoft.mageon.way.R.string.b_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog ald = builder.create();
            ald.show();
        }
    }

    class RefreshTask extends AsyncTask<Object, Object, String[]> {

        @Override
        protected String[] doInBackground(Object... params) {
            boolean someCondition = true;
            //while (someCondition) {
                try {
                    //sleep for 1s in background...
                    Thread.sleep(5000);
                    //and update textview in ui thread
                    someCondition = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            //}
            return null;
        }
        protected void onPostExecute(String... result) {
            super.onPostExecute(result);
            if(mCurrentSostoyanie.getMyLocation() == null) {
                Toast.makeText(getApplicationContext(),  getResources().getString(com.mysoft.mageon.way.R.string.error_gps_coordinats), Toast.LENGTH_SHORT).show();
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                mCurrentSostoyanie.setFixcamera(false);
                mGoogleApiClient.disconnect();
                setPictFixcamera();
            }
        }
    }
}
