package com.mysoft.mageon.way;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

public class SearchRecyclerView extends RecyclerView.Adapter<SearchRecyclerView.ViewHolder> {

    private ArrayList<PlaceDetails> records;
    MapActivity ctx;

    public SearchRecyclerView(ArrayList<PlaceDetails> records, MapActivity ctx) {
        this.records = records;
        this.ctx = ctx;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mName;
        public TextView mAddress;
        public RatingBar mRating;
        public TextView mOpen_now;
        public boolean mOpen_now_boolean;
        public String mId;

        public ViewHolder(View v) {
            super(v);
            mName = (TextView) v.findViewById(com.mysoft.mageon.way.R.id.name);
            mAddress = (TextView) v.findViewById(com.mysoft.mageon.way.R.id.address);
            mRating = (RatingBar) v.findViewById(com.mysoft.mageon.way.R.id.rating);
            mOpen_now = (TextView) v.findViewById(com.mysoft.mageon.way.R.id.open_now);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v_par = LayoutInflater.from(parent.getContext()).inflate(com.mysoft.mageon.way.R.layout.row_list_search, parent, false);
        ImageView btn = (ImageView) v_par.findViewById(com.mysoft.mageon.way.R.id.btn_details);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CardView card_view = (CardView) v_par.findViewById(com.mysoft.mageon.way.R.id.card_view);
                ViewHolder tekholder = (ViewHolder) card_view.getTag();
                ctx.makeSearchDetiles(tekholder.mId, Boolean.toString(tekholder.mOpen_now_boolean));
            }
        });
        CardView card_view = (CardView) v_par.findViewById(com.mysoft.mageon.way.R.id.card_view);
        card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewHolder tekholder = (ViewHolder) v.getTag();
                ctx.getMarkerDetiles(tekholder.mId);
            }
        });
        return new ViewHolder(v_par);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PlaceDetails record = records.get(position);
        holder.mName.setText(record.mName);
        holder.mAddress.setText(record.mAddress);
        holder.mRating.setRating(record.mRating);
        holder.mOpen_now_boolean = record.mOpen;
        holder.mId = record.mID;
        if(record.mOpen) {
            holder.mOpen_now.setText(ctx.getResources().getString(com.mysoft.mageon.way.R.string.search_open));
            holder.mOpen_now.setTextColor(ctx.getResources().getColor(com.mysoft.mageon.way.R.color.colorTitle));
        }else{
            holder.mOpen_now.setText(ctx.getResources().getString(com.mysoft.mageon.way.R.string.search_close));
            holder.mOpen_now.setTextColor(ctx.getResources().getColor(com.mysoft.mageon.way.R.color.colorAccent));
        }
        CardView v = (CardView) holder.itemView.findViewById(com.mysoft.mageon.way.R.id.card_view);
        v.setTag(holder);
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
