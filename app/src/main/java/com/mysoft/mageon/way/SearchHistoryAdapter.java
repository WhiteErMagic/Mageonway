package com.mysoft.mageon.way;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Display friend list
 *
 * @author eranga herath(erangaeb@gmail.com)
 */
public class SearchHistoryAdapter extends BaseAdapter implements Filterable {

    private MapActivity activity;
    private ItemsFilter itemsFilter;
    private ArrayList<String> items;
    private ArrayList<String> filteredItems;

    /**
     * Initialize context variables
     * @param activity friend list activity
     * @param items friend list
     */
    public SearchHistoryAdapter(MapActivity activity, ArrayList<String> items) {
        this.activity = activity;
        this.items = items;
        this.filteredItems = items;

        getFilter();
    }

    /**
     * Get size of user list
     * @return userList size
     */
    @Override
    public int getCount() {
        return filteredItems.size();
    }

    /**
     * Get specific item from user list
     * @param i item index
     * @return list item
     */
    @Override
    public Object getItem(int i) {
        return filteredItems.get(i);
    }

    /**
     * Get user list item id
     * @param i item index
     * @return current item id
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        final String item = (String) getItem(position);

        if (view == null) {
            LayoutInflater layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(com.mysoft.mageon.way.R.layout.row_search_history, parent, false);
            holder = new ViewHolder();
            holder.querytext = (TextView) view.findViewById(com.mysoft.mageon.way.R.id.tv_history_query);

            view.setTag(holder);
        } else {
            // get view holder back
            holder = (ViewHolder) view.getTag();
        }

        holder.querytext.setText(item);

        return view;
    }

    /**
     * Get custom filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (itemsFilter == null) {
            itemsFilter = new ItemsFilter();
        }

        return itemsFilter;
    }

    /**
     * Keep reference to children view to avoid unnecessary calls
     */
    static class ViewHolder {
        TextView querytext;
    }

    /**
     * Custom filter for friend list
     * Filter content in friend list according to the search text
     */
    private class ItemsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint!=null && constraint.length()>0) {
                ArrayList<String> tempList = new ArrayList<String >();

                // search content in friend list
                for (String user : items) {
                    if (user.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(user);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = items.size();
                filterResults.values = items;
            }

            return filterResults;
        }

        /**
         * Notify about filtered list to ui
         * @param constraint text
         * @param results filtered result
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (ArrayList<String>) results.values;
            notifyDataSetChanged();
        }
    }

}