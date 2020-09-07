package com.mysoft.mageon.way;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShowDetailes extends AppCompatActivity {
    DisplayMetrics displaymetrics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mysoft.mageon.way.R.layout.row_search_detailes);

        Intent itn = getIntent();
        displaymetrics = new DisplayMetrics();

        try {
            JSONObject result = new JSONObject(itn.getStringExtra("result"));
            if (!result.isNull("formatted_address")) {
                String str_address = result.getString("formatted_address");
            }
            if (!result.isNull("formatted_phone_number")) {
                final String str_phone_loc = result.getString("formatted_phone_number");
                TextView t_phone1 = (TextView) findViewById(com.mysoft.mageon.way.R.id.detiles_t_phone);
                t_phone1.setText(str_phone_loc);
                t_phone1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:"+((TextView) v).getText().toString()));
                        if (callIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(callIntent);
                        }
                    }
                });
            }
            if (!result.isNull("international_phone_number")) {
                final String str_phone_inter = result.getString("international_phone_number");
                TextView t_phone2 = (TextView) findViewById(com.mysoft.mageon.way.R.id.detiles_t_phone_inter);
                t_phone2.setText(str_phone_inter);
                t_phone2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                                callIntent.setData(Uri.parse("tel:"+((TextView) v).getText().toString()));
                                if (callIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(callIntent);
                                }
                            }
                        });
            }
            if (!result.isNull("name")) {
                String str_name = result.getString("name");
                TextView t_name = (TextView) findViewById(com.mysoft.mageon.way.R.id.detiles_name);
                t_name.setText(str_name);
            }
            if (!result.isNull("opening_hours")) {
                JSONObject str_hours = result.getJSONObject("opening_hours");
                boolean open = str_hours.getBoolean("open_now");
                final ExpandableListView list_week_open = (ExpandableListView) findViewById(com.mysoft.mageon.way.R.id.week_open);
                list_week_open.setGroupIndicator(null);
                ArrayList<ArrayList<String>> groups = new ArrayList<>();
                ArrayList<String> children1 = new ArrayList<String>();
                if (!str_hours.isNull("weekday_text")) {
                    JSONArray arr_week = str_hours.getJSONArray("weekday_text");
                    for (int ind = 0; ind < arr_week.length(); ind++)
                        children1.add(arr_week.getString(ind));
                }
                groups.add(children1);
                ExpListAdapter adapter = new ExpListAdapter(getApplicationContext(), groups, str_hours.getBoolean("open_now"));
                list_week_open.setAdapter(adapter);
            }else{
                final ExpandableListView list_week_open = (ExpandableListView) findViewById(com.mysoft.mageon.way.R.id.week_open);
                list_week_open.setGroupIndicator(null);
                ArrayList<ArrayList<String>> groups = new ArrayList<>();
                ArrayList<String> children1 = new ArrayList<String>();
                if (!itn.getBooleanExtra("open", false)) {
                        children1.add("");
                }
                groups.add(children1);
                ExpListAdapter adapter = new ExpListAdapter(getApplicationContext(), groups, itn.getBooleanExtra("open", false));
                list_week_open.setAdapter(adapter);
            }
            if (!result.isNull("reviews")) {
                JSONArray arr_reviews = result.getJSONArray("reviews");
                ListView lv_review = (ListView) findViewById(com.mysoft.mageon.way.R.id.lv_review);
                ArrayList<Review> list_review = new ArrayList<>();
                for(int ind = 0; ind < arr_reviews.length(); ind++){
                    JSONObject review = (JSONObject) arr_reviews.get(ind);
                    int mRating = 0;
                    String mReview = "";
                    String mReview_date = "";
                    if(!review.isNull("rating"))
                        mRating = review.getInt("rating");
                    if(!review.isNull("text"))
                        mReview = review.getString("text");
                    if(!review.isNull("text"))
                        mReview_date = review.getString("relative_time_description");
                    list_review.add(new Review(mRating, mReview, mReview_date));
                }
                MyListAdapter_review adapter = new MyListAdapter_review(this, list_review);
                lv_review.setAdapter(adapter);
            }
            if (!result.isNull("website")) {
                String str_website = result.getString("website");
                TextView website = (TextView) findViewById(com.mysoft.mageon.way.R.id.website);
                website.setText(str_website);
                website.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent callIntent = new Intent(Intent.ACTION_VIEW);
                        callIntent.setData(Uri.parse(((TextView) v).getText().toString()));
                        if (callIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(callIntent);
                        }
                    }
                });

            }
            if (!result.isNull("rating")) {
                int str_reiting = result.getInt("rating");
                RatingBar detiles_ratingBar = (RatingBar) findViewById(com.mysoft.mageon.way.R.id.detiles_ratingBar);
                detiles_ratingBar.setRating(str_reiting);
            }
        }catch (JSONException e) {
                e.printStackTrace();
        }
    }
}
