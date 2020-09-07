package com.mysoft.mageon.way;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;


/**
 * Created by Boris on 03.01.2017.
 */

public class DeleteAdapter extends BaseAdapter{
    /*private LayoutInflater cursorInflater;

    public DeleteAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        cursorInflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View view = cursorInflater.inflate(R.layout.row_main, parent, false);
        holder.name = (TextView) view.findViewById(R.id.textView);
        holder.check = (CheckBox) view.findViewById(R.id.checkBox);
        view.setTag(holder);
        //holder.check.setOnCheckedChangeListener(new);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.name.setText(cursor.getString(cursor.getColumnIndex("name")));
        holder.id = cursor.getInt(0);
        //view.setTag(holder);
        //TextView textViewTitle = (TextView) view.findViewById(R.id.textView);
        //String title = cursor.getString(1);
        //textViewTitle.setText(title);
    }

    public static class ViewHolder {
        TextView name;
        CheckBox check;
        int id;
    }*/









    Context ctx;
    LayoutInflater inflter;
    ArrayList<DeleteObject> args;

    public DeleteAdapter(Context applicationContext, ArrayList<DeleteObject> args) {
        this.ctx = applicationContext;
        this.args = args;
        inflter = (LayoutInflater) applicationContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        if(args.size()<=0)
            return 1;
        return args.size();
    }

    @Override
    public DeleteObject getItem(int i) {
        if(args.size()<=0)
            return null;
        return args.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    //public String getSelected(){
        //return active_route_name;
    //}

    //public void setSelected(String arg){
      //  this.selected_route = arg;
    //}

    public static class ViewHolder{
       public TextView name;
       public CheckBox choice;
       public int id;
       public int id_list;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        DeleteObject  tempValues = getItem(i);
            if (convertView == null) {
                convertView = inflter.inflate(com.mysoft.mageon.way.R.layout.row_main, null);
                holder = new ViewHolder();
                holder.name = (TextView) convertView.findViewById(com.mysoft.mageon.way.R.id.textView);
                holder.choice = (CheckBox) convertView.findViewById(com.mysoft.mageon.way.R.id.checkBox);
                holder.name.setText(tempValues.name);
                holder.id = tempValues.id;
                holder.id_list = i;
                holder.choice.setChecked(tempValues.del);

                holder.choice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        View v_parent = (View) buttonView.getParent();
                        ViewHolder vh = (ViewHolder) v_parent.getTag();
                        ((ViewHolder) v_parent.getTag()).choice.setChecked(isChecked);
                        v_parent.setSelected(isChecked);
                        getItem(vh.id_list).del = isChecked;
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
                holder.name.setText(tempValues.name);
                holder.id = tempValues.id;
                holder.id_list = i;
                holder.choice.setChecked(tempValues.del);
            }

        return convertView;
    }
}

