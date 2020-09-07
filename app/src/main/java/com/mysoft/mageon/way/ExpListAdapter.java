package com.mysoft.mageon.way;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ExpListAdapter extends BaseExpandableListAdapter {

    private ArrayList<ArrayList<String>> mGroups;
    private Context mContext;
    private boolean mOpen;

    public ExpListAdapter (Context context,ArrayList<ArrayList<String>> groups, boolean mOpen){
        mContext = context;
        mGroups = groups;
        this.mOpen = mOpen;
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mGroups.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mGroups.get(groupPosition).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(com.mysoft.mageon.way.R.layout.group_view, null);

        }

        if (isExpanded){
            ImageView btn_arrow = (ImageView) convertView.findViewById(com.mysoft.mageon.way.R.id.btn_arrow);
            btn_arrow.setSelected(true);
        }
        else{
            ImageView btn_arrow = (ImageView) convertView.findViewById(com.mysoft.mageon.way.R.id.btn_arrow);
            btn_arrow.setSelected(false);
        }

        TextView text_open = (TextView) convertView.findViewById(com.mysoft.mageon.way.R.id.text_open);
        if (this.mOpen) {
            text_open.setText(mContext.getResources().getString(com.mysoft.mageon.way.R.string.search_open));
            text_open.setTextColor(mContext.getResources().getColor(com.mysoft.mageon.way.R.color.colorTitle));
        } else {
            text_open.setText(mContext.getResources().getString(com.mysoft.mageon.way.R.string.search_close));
            text_open.setTextColor(mContext.getResources().getColor(com.mysoft.mageon.way.R.color.colorAccent));
        }

        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(com.mysoft.mageon.way.R.layout.child_view, null);
            convertView.setEnabled(false);
        }

        TextView text_week = (TextView) convertView.findViewById(com.mysoft.mageon.way.R.id.text_week);
        TextView text_hours = (TextView) convertView.findViewById(com.mysoft.mageon.way.R.id.text_hours);
        if(mGroups.get(groupPosition).get(childPosition).length() != 0) {
            int ind = mGroups.get(groupPosition).get(childPosition).indexOf(':');
            text_week.setText(mGroups.get(groupPosition).get(childPosition).substring(0, ind));
            text_hours = (TextView) convertView.findViewById(com.mysoft.mageon.way.R.id.text_hours);
            text_hours.setText(mGroups.get(groupPosition).get(childPosition).substring(ind + 1));
        }else{
            text_week.setText("");
            text_hours.setText("");
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
