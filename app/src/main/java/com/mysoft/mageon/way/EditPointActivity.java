package com.mysoft.mageon.way;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;

public class EditPointActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    double latitude;
    double longitude;
    boolean signal;
    String file_signal;
    int distance;
    int id;
    boolean control;
    EditText et;
    EditText et_latitude;
    EditText et_longitude;
    EditText et_distance;
    String curFileName;
    EditText et_file_signal;
    MediaPlayer mPlayer;
    Thread th = null;
    ImageButton btn_gps;
    ImageButton btn_map;
    GoogleApiClient mGoogleApiClient = null;
    //ShowDialogAsyncTask at= null;
    com.google.android.gms.location.LocationListener mLocationListener;
    ConnectProvider mConnectProvider = null;
    LocationRequest REQUEST = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mysoft.mageon.way.R.layout.activity_edit_point);
        mConnectProvider = ConnectProvider.getInstance(this);

        //Чтение переданных переменных
        Intent i = getIntent();
        id = i.getIntExtra("id", 0);
        final Point mPoint = DBHelper.getInctanceDBHelper(this).getPoint(id);

        //Поля формы
        et = (EditText) findViewById(com.mysoft.mageon.way.R.id.edit_name);
        et_latitude = (EditText) findViewById(com.mysoft.mageon.way.R.id.edit_latitude);
        et_longitude = (EditText) findViewById(com.mysoft.mageon.way.R.id.edit_longitude);
        et_distance = (EditText) findViewById(com.mysoft.mageon.way.R.id.edit_distance);
        et_file_signal = (EditText) findViewById(com.mysoft.mageon.way.R.id.edit_file_signal);
        Button btn_cancel = (Button) findViewById(com.mysoft.mageon.way.R.id.btn_cancel);
        btn_gps = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_gps);
        btn_map = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_map);

        //Заполнение полей формы
        if (id != -1) {
            et.setText(mPoint.name_point);
            if(mPoint.latitude == 0)
                et_latitude.setText("00.000000");
            else
                et_latitude.setText(Double.toString(mPoint.latitude));
            if(mPoint.longitude == 0)
                et_longitude.setText("00.000000");
            else
                et_longitude.setText(Double.toString(mPoint.longitude));
            et_distance.setText(Integer.toString(mPoint.distance));
            et_file_signal.setText(mPoint.file_signal);
        }else{
            String newLat = Double.toString(i.getDoubleExtra("latitude", 0));
            String newLong = Double.toString(i.getDoubleExtra("longitude", 0));

            if(newLat.length() > 9)
                et_latitude.setText(newLat.substring(0,9));
            else
                if(newLat.length() > 3)
                    et_latitude.setText(newLat);
            if(newLong.length() > 9)
                et_longitude.setText(newLong.substring(0,9));
            else
                if(newLong.length() > 3)
                    et_longitude.setText(newLong);
        }


        //Обработчики

        et.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et.setCursorVisible(true);
            }
        });

        //Запрос координат для точки
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(EditPointActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(EditPointActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(EditPointActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
                }else {
                    if (!mConnectProvider.checkGPS() && !mConnectProvider.checkNet()) {
                        //Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_no_available_providers), Toast.LENGTH_SHORT).show();
                        changeGPS();
                    } else {
                        getLocation();
                    }
                }
            }
        });
        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getCoordinatesFromMap();
            }
        });

        //Файлы сигнала
        ImageButton btn_file = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_file);
        btn_file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFile();
            }
        });

        ImageButton btn_bell = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_bell);
        btn_bell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBell();
            }
        });

        ImageButton btn_microphone = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_microphone);
        btn_microphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRecSound();
            }
        });
        ImageButton btn_play = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_play);
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beepSignal(et_file_signal.getText().toString());
            }
        });
        et_distance.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    et_distance.setCursorVisible(true);
            }
        });
        et_distance.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_ENTER) {
                    et_distance.clearFocus();
                    et_distance.setCursorVisible(false);
                }

                return false;
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button btn_save = (Button) findViewById(com.mysoft.mageon.way.R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!et.getText().toString().equals("")) {
                    mPoint.id = id;
                    mPoint.name_point = et.getText().toString();
                    mPoint.file_signal = et_file_signal.getText().toString();
                    if(et_latitude.getText().toString().equals(""))
                        mPoint.latitude = 0;
                    else
                        mPoint.latitude = Double.parseDouble(et_latitude.getText().toString());
                    if(et_longitude.getText().toString().equals(""))
                        mPoint.longitude = 0;
                    else
                        mPoint.longitude = Double.parseDouble(et_longitude.getText().toString());
                    if(et_distance.getText().toString().equals(""))
                        mPoint.distance = 0;
                    else
                        mPoint.distance = Integer.parseInt(et_distance.getText().toString());

                    if (mPoint.id == -1) {
                        if (addPoint(mPoint) != -1) {
                            Intent i_back = new Intent();
                            setResult(RESULT_OK, i_back);
                        }
                    } else {
                        if (updatePoint(mPoint) != -1) {
                            Intent i_back = new Intent();
                            setResult(RESULT_OK, i_back);
                        }
                    }

                    finish();
                } else
                    Toast.makeText(EditPointActivity.this, getResources().getString(com.mysoft.mageon.way.R.string.error_name), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void finish() {
        if (mPlayer != null)
            mPlayer.stop();
        if (th != null)
            th.interrupt();
        if(mGoogleApiClient != null)
            if(mGoogleApiClient.isConnected() && mLocationListener != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                mGoogleApiClient.disconnect();
        }
        super.finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            if (resultCode == RESULT_OK) {
                double[] mPoints = data.getDoubleArrayExtra("location");
                if (mPoints != null) {
                    if(Double.toString(mPoints[0]).length() > 9)
                        et_latitude.setText(Double.toString(mPoints[0]).substring(0, 9));
                    else
                        et_latitude.setText(Double.toString(mPoints[0]));

                    if(Double.toString(mPoints[1]).length() > 9)
                        et_longitude.setText(Double.toString(mPoints[1]).substring(0, 9));
                    else
                        et_longitude.setText(Double.toString(mPoints[1]));
                } else {
                    curFileName = data.getData().toString();
                    curFileName = curFileName.replace("/content:/", "content://");
                    et_file_signal.setText(curFileName);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null)
            mPlayer.stop();
        if (th != null)
            th.interrupt();
        if(mGoogleApiClient != null)
            if(mGoogleApiClient.isConnected() && mLocationListener != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
                mGoogleApiClient.disconnect();
            }
    }

    //**********
    //Творчество
    //**********

    private void getLocation() {

            if(mGoogleApiClient == null && mLocationListener == null && REQUEST == null) {
                REQUEST = LocationRequest.create()
                        .setInterval(1)         // 5 seconds
                        .setFastestInterval(1)    // 16ms = 60fps
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .addApi(LocationServices.API)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
                mGoogleApiClient.connect();

                mLocationListener = new com.google.android.gms.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        updateLocation(location);
                    }
                };
            }
            btn_gps.setEnabled(false);
    }

    private void updateLocation(Location location){
        if(location != null) {
            et_latitude.setText(Double.toString(location.getLatitude()).substring(0, 9));
            et_longitude.setText(Double.toString(location.getLongitude()).substring(0, 9));
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mLocationListener);
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
        REQUEST = null;
        mLocationListener = null;
        btn_gps.setEnabled(true);
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

    //Для сигнала выбор стандартного звонка
    private void getBell() {
        Intent itn = new Intent(this, BellActivity.class);
        itn.putExtra("puth_file", et_file_signal.getText().toString());
        startActivityForResult(itn, 1);
    }

    //Для сигнала выбор звукового файла
    private void getFile() {
        final int FILE_SELECT_CODE = 0;
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, getResources().getString(com.mysoft.mageon.way.R.string.point_t_getfile)),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {

        }

    }

    //Для сигнала записать собственный звуковой файл через диктофон
    private void getRecSound() {
        Intent itn = new Intent(this, DictofonActivity.class);
        startActivityForResult(itn, 1);
    }

    //Сигнал приближения точки маршрута
    private void beepSignal(String arg) {
        if (mPlayer == null)
            mPlayer = new MediaPlayer();

        try {

            mPlayer.reset();
            mPlayer.setDataSource(this, Uri.parse(arg));
            mPlayer.prepare();
            th = new Thread(new Runnable() {
                public void run() {
                    long time_start = System.currentTimeMillis();
                    mPlayer.start();
                    int long_song = mPlayer.getDuration();
                    long time_end = time_start + ((long) long_song);
                    while (time_end > System.currentTimeMillis()) {
                    }
                    mPlayer.stop();
                }
            });
            th.setDaemon(true);
            th.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Добавляет точку назначения
    private long addPoint(Point arg) {
        return DBHelper.getInctanceDBHelper(this).insertPoint(arg);
    }

    //Обновляет точку назначения
    private long updatePoint(Point arg) {
        return DBHelper.getInctanceDBHelper(this).updatePoint(arg);
    }

    private void getCoordinatesFromMap(){

        Intent itn = new Intent(this, MapActivityView.class);
        double lat = 0;
        double lon = 0;
        if(et_latitude.getText().toString().equals(""))
            lat = 0;
        else
            lat = Double.parseDouble(et_latitude.getText().toString());

        if(lat < -90 || lat > 90) {
            et_latitude.setText("00.000000");
        }

        if(et_longitude.getText().toString().equals(""))
            lon = 0;
        else
            lon = Double.parseDouble(et_longitude.getText().toString());

        if(lon < -180 || lon > 180) {
            et_longitude.setText("00.000000");
        }

        if((lat < -90 || lat > 90) || (lon < -180 || lon > 180)) {
            Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.error_coordinats), Toast.LENGTH_SHORT).show();
        }else {
            itn.putExtra("latitude", lat);
            itn.putExtra("longitude", lon);
            itn.putExtra("from", "point");
            startActivityForResult(itn, 1);
        }
    }
}
