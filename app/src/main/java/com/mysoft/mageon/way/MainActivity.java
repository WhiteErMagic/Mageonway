package com.mysoft.mageon.way;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    DBHelper mDBHelper = null;
    PointsAdapter adapter_points;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        mDBHelper = DBHelper.getInctanceDBHelper(this);
        setContentView(com.mysoft.mageon.way.R.layout.activity_main_full_list);

        Toolbar toolbar = (Toolbar) findViewById(com.mysoft.mageon.way.R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(getResources().getDrawable(com.mysoft.mageon.way.R.drawable.app_mageon_logo));
        ImageButton btn_action_add = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_action_add);
        btn_action_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPoint();
            }
        });
        ImageButton btn_action_delete = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_action_delete);
        btn_action_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delPoints();
            }
        });
        ImageButton btn_action_map = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.btn_action_map);
        btn_action_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMap();
            }
        });

        updateLists(0);
    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(this, MyServiceMAGEON.class));
        super.onBackPressed();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            String mPoints = data.getStringExtra("points");
            if (mPoints != null) {
                JSONObject mTemp = null;
                try {
                    mTemp = new JSONObject(mPoints);
                    JSONArray arr = mTemp.names();
                    for (int ind = 0; ind < arr.length(); ind++) {
                        String mKey = arr.get(ind).toString();
                        Double lat = Double.parseDouble(mTemp.getJSONArray(mKey).get(0).toString());
                        Double lon = Double.parseDouble(mTemp.getJSONArray(mKey).get(1).toString());
                        DBHelper.getInctanceDBHelper(this).insertPoint(
                                new Point(0,
                                          arr.get(ind).toString(),
                                         false,
                                         "",
                                          lat,
                                          lon,
                                         10
                                )
                        );
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        updateLists(1);
    }

//**********
//Творчество
//**********

    protected void updateLists(int arg){
        if(arg == 0) {
            Cursor c_points = mDBHelper.getCursorPoints();
            adapter_points = new PointsAdapter(this, c_points, 0);
            ListView mLv = (ListView) findViewById(com.mysoft.mageon.way.R.id.list_points);
            mLv.setAdapter(adapter_points);

        }else{
            Cursor c_points = mDBHelper.getCursorPoints();
            ListView mLv = (ListView) findViewById(com.mysoft.mageon.way.R.id.list_points);
            adapter_points = (PointsAdapter) mLv.getAdapter();
            adapter_points.swapCursor(c_points);
            for(int ind = 0; ind < CurrentSostoyanie.getInstance().getAr_ControlID().size(); ind++)
                if (mDBHelper.getPoint((int) CurrentSostoyanie.getInstance().getAr_ControlID().get(ind)).id == 0) {
                    CurrentSostoyanie.getInstance().getAr_ControlID().remove(CurrentSostoyanie.getInstance().getAr_ControlID().get(ind));
                }
        }
    }

    private void addPoint() {
        Intent itn = new Intent(this, EditPointActivity.class);
        itn.putExtra("id", -1);
        startActivityForResult(itn, 1);
    }

    private void delPoints(){
        Intent itn = new Intent(this, DeleteActivity.class);
        startActivityForResult(itn, 1);
    }

    private void openMap(){
        Intent itn = new Intent(this, MapActivity.class);
        startActivity(itn);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLists(1);
    }
}