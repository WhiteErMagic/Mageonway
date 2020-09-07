package com.mysoft.mageon.way;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

public class SearchHistoryRecyclerAdapter extends RecyclerView.Adapter<SearchHistoryRecyclerAdapter.ViewHolder> {

    private List<String> records;
    private Context ctx;

    public SearchHistoryRecyclerAdapter(MapActivity ctx, List<String> records) {
        this.ctx = ctx;
        this.records = records;
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView textquery;
        public ImageButton ib_search_history_delete;

        public ViewHolder(View v) {
            super(v);
            textquery = (TextView) itemView.findViewById(com.mysoft.mageon.way.R.id.tv_history_query);
            ib_search_history_delete = (ImageButton) itemView.findViewById(com.mysoft.mageon.way.R.id.ib_search_history_delete);
        }
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(com.mysoft.mageon.way.R.layout.row_search_history, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        String record = records.get(position);
        holder.textquery.setText(record);
        holder.ib_search_history_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MapActivity) ctx).deleteRowHistory(holder.textquery.getText().toString());
            }
        });
        holder.textquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MapActivity) ctx).getRowHistory(holder.textquery.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }
}
