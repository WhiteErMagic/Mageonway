package com.mysoft.mageon.way;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

public class DeleteActivity extends AppCompatActivity {
    ListView lv;
    Cursor cur;
    ArrayList<DeleteObject> ar;
    int favorite;
    DeleteAdapter customAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mysoft.mageon.way.R.layout.activity_delete);
        Toolbar mActionBarToolbar = (Toolbar) findViewById(com.mysoft.mageon.way.R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        ar = new ArrayList<DeleteObject>();
        cur = DBHelper.getInctanceDBHelper(this).getCursorPoints();

        lv = (ListView) findViewById(com.mysoft.mageon.way.R.id.list_delete);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //ar =  i.getParcelableArrayListExtra("list");
        customAdapter = null;
        if(cur != null)
            if (cur.moveToFirst()) {
                do {
                    ar.add(new DeleteObject(cur.getInt(0), cur.getString(1).toString()));
                } while (cur.moveToNext());
            }

        if(ar.size() > 0) {
            customAdapter = new DeleteAdapter(this, ar);
            lv.setAdapter(customAdapter);
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.mysoft.mageon.way.R.menu.menu_delete, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.mysoft.mageon.way.R.id.delete_check:
                DeleteAdapter.ViewHolder vh = null;
                Drawable f = item.getIcon();
                if(item.getTitle().equals(getResources().getString(com.mysoft.mageon.way.R.string.delete_b_all))) {
                    item.setTitle(com.mysoft.mageon.way.R.string.delete_b_all_no);
                    item.setIcon(com.mysoft.mageon.way.R.drawable.all_no);

                    for (int i = 0; i < lv.getCount(); i++) {
                        ar.get(i).del = true;
                    }
                    if(lv.getCount() > 0)
                        customAdapter.notifyDataSetChanged();
                }else {
                    item.setIcon(com.mysoft.mageon.way.R.drawable.all);
                    item.setTitle(com.mysoft.mageon.way.R.string.delete_b_all);
                    for (int i = 0; i < lv.getCount(); i++) {
                        ar.get(i).del = false;
                    }
                    if(lv.getCount() > 0)
                        customAdapter.notifyDataSetChanged();
                }
                break;
            case com.mysoft.mageon.way.R.id.delete:
                for (int i = 0; i < lv.getCount(); i++) {
                    if (ar.get(i).del)
                        DBHelper.getInctanceDBHelper(this).deletePoint(ar.get(i).id);
                }
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        Intent i_back = new Intent();
        i_back.putExtra("update", 1);
        setResult(RESULT_OK, i_back);
        super.finish();
    }
}
