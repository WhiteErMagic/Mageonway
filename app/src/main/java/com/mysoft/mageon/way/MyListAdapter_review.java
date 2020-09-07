package com.mysoft.mageon.way;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MyListAdapter_review extends BaseAdapter {
    ArrayList<Review> mList_review;
    Context mContext;
    public MyListAdapter_review(Context context, ArrayList<Review> list_review) {
        mList_review = list_review;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mList_review.size();
    }

    @Override
    public Object getItem(int position) {
        return mList_review.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class ViewHolder {
        RatingBar review_rating;
        TextView review_date;
        TextView review_text;

        public ViewHolder(View view) {
            review_rating = (RatingBar) view.findViewById(com.mysoft.mageon.way.R.id.review_rating);
            review_text = (TextView) view.findViewById(com.mysoft.mageon.way.R.id.review_text);
            review_date = (TextView) view.findViewById(com.mysoft.mageon.way.R.id.review_date);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(com.mysoft.mageon.way.R.layout.row_review, parent, false);
            ViewHolder mHolder = new ViewHolder(convertView);
            Review currentItem = (Review) mList_review.get(position);
            mHolder.review_rating.setRating(currentItem.reiting);
            mHolder.review_text.setText(currentItem.review);
            mHolder.review_date.setText(currentItem.review_date);
            convertView.setTag(mHolder);
        }

        return convertView;
    }
}
