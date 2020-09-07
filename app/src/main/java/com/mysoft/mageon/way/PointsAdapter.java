package com.mysoft.mageon.way;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;
import android.widget.Toast;


public class PointsAdapter extends CursorAdapter {
        Context cxt;
        //ArrayList<Object> checkList = new ArrayList<Object>();
        public PointsAdapter(Context context, Cursor cursor, int flag) {
            super(context, cursor);
            this.cxt = context;
        }

    public static class ViewHolder{
        public TextView name_point;
        public CheckedTextView cb_latitude;
        public CheckedTextView cb_longitude;
        public CheckBox control;
        public TextView distance;
        public int id;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

            final ViewHolder  holder = new ViewHolder();
            holder.name_point = (TextView) view.findViewById(com.mysoft.mageon.way.R.id.name_point);
            holder.name_point.setText(cursor.getString(cursor.getColumnIndex("name_point")));
            holder.cb_latitude = (CheckedTextView) view.findViewById(com.mysoft.mageon.way.R.id.row_latitude);
            holder.cb_latitude.setText(cxt.getResources().getString(com.mysoft.mageon.way.R.string.t_latitude));
            if(Double.parseDouble(cursor.getString(cursor.getColumnIndex("latitude"))) > 0) {
                holder.cb_latitude.setCheckMarkDrawable(com.mysoft.mageon.way.R.drawable.pict_true);
                holder.cb_latitude.setChecked(true);
            }else {
                holder.cb_latitude.setCheckMarkDrawable(com.mysoft.mageon.way.R.drawable.pict_false);
                holder.cb_latitude.setChecked(false);
            }
            holder.cb_longitude = (CheckedTextView) view.findViewById(com.mysoft.mageon.way.R.id.row_longitude);
            holder.cb_longitude.setText(cxt.getResources().getString(com.mysoft.mageon.way.R.string.t_longitude));
            holder.distance = (TextView) view.findViewById(com.mysoft.mageon.way.R.id.row_distance);
            holder.distance.setText(cursor.getString(cursor.getColumnIndex("distance")));

            holder.id = Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")));
            if(Double.parseDouble(cursor.getString(cursor.getColumnIndex("longitude"))) > 0) {
                holder.cb_longitude.setCheckMarkDrawable(com.mysoft.mageon.way.R.drawable.pict_true);
                holder.cb_longitude.setChecked(true);
            }else {
                holder.cb_longitude.setCheckMarkDrawable(com.mysoft.mageon.way.R.drawable.pict_false);
                holder.cb_longitude.setChecked(false);
            }

            holder.control = (CheckBox) view.findViewById(com.mysoft.mageon.way.R.id.row_chb_control);

            if (CurrentSostoyanie.getInstance().getAr_ControlID().contains((Object) holder.id)) {
                holder.control.setChecked(true);
            }else
                holder.control.setChecked(false);

            holder.control.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View h_parent = (View) v.getParent();
                    ViewHolder h = (ViewHolder) h_parent.getTag();
                    if(((CheckBox) v).isChecked()) {
                        if(!h.cb_latitude.isChecked() || !h.cb_longitude.isChecked() ) {
                            ((CheckBox) v).setChecked(false);
                            Toast.makeText(context.getApplicationContext(), context.getResources().getString(com.mysoft.mageon.way.R.string.error_check), Toast.LENGTH_LONG).show();
                        }else
                            CurrentSostoyanie.getInstance().addAr_ControlID((Object) h.id);
                    }else{
                            CurrentSostoyanie.getInstance().removeAr_ControlID((Object) h.id);
                    }
                }
            });


            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: // нажатие
                            v.setBackgroundColor(cxt.getResources().getColor(com.mysoft.mageon.way.R.color.colorSelect));
                            break;
                        case MotionEvent.ACTION_MOVE: // движение
                            v.setBackgroundColor(cxt.getResources().getColor(com.mysoft.mageon.way.R.color.colorSelect));
                            break;
                        case MotionEvent.ACTION_UP: // отпускание
                            v.setBackgroundColor(Color.TRANSPARENT);
                        case MotionEvent.ACTION_CANCEL:
                            v.setBackgroundColor(Color.TRANSPARENT);
                            break;
                    }
                    return false;
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ViewHolder tekTag = (ViewHolder) v.getTag();
                    Intent itn = new Intent(cxt, EditPointActivity.class);
                    itn.putExtra("id", tekTag.id);
                    ((MainActivity) cxt).startActivityForResult(itn, 1);

                    return false;
                }
            });

            view.setTag(holder);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(com.mysoft.mageon.way.R.layout.row_points, parent, false);

        return view;
    }
}

