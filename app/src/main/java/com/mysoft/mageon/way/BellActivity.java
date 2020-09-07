package com.mysoft.mageon.way;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class BellActivity extends AppCompatActivity {
    MediaPlayer mPlayer = null;
    File[] mFileList;
    String choice = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mysoft.mageon.way.R.layout.activity_bell);
        String temp = null;
        String newName = null;
        int marker = -1;
        Intent intent = getIntent();
        choice = intent.getStringExtra("puth_file");
        Button bell_cancel = (Button) findViewById(com.mysoft.mageon.way.R.id.bell_cancel);
        Button bell_ok = (Button) findViewById(com.mysoft.mageon.way.R.id.bell_ok);
        Button bell_test = (Button) findViewById(com.mysoft.mageon.way.R.id.bell_test);
        bell_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        bell_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBell(choice);
            }
        });

        bell_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( ((Button) v).getText().equals(getResources().getString(com.mysoft.mageon.way.R.string.b_test))) {
                    if(!choice.equals("")) {
                        ((Button) v).setText(com.mysoft.mageon.way.R.string.b_stop);
                        testBell(choice);
                    }
                } else{
                    ((Button) v).setText(com.mysoft.mageon.way.R.string.b_test);
                        stopTestBell();
                }
            }
        });

        ListView listfiles = (ListView) findViewById(com.mysoft.mageon.way.R.id.list_bells);
        listfiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                choice = mFileList[position].getAbsolutePath();
                if(mPlayer!=null)
                    if(mPlayer.isPlaying()==true) {
                        mPlayer.reset();
                        Button bell_test = (Button) findViewById(com.mysoft.mageon.way.R.id.bell_test);
                        bell_test.setText(com.mysoft.mageon.way.R.string.b_test);
                    }

            }
        });

        String dir;
        dir = Environment.getRootDirectory().getAbsolutePath() + "//media//audio//ringtones//";

        File mPath = new File(dir);
        mFileList = mPath.listFiles();

        ArrayList<String> list_name = new ArrayList<String>();
        for(int ind=0;ind<mFileList.length-1;ind++){
            temp = mFileList[ind].getName();
            newName = temp.replace("_", " ");
            newName = newName.replace(".ogg", "");
            newName = newName.replace(newName.charAt(0), Character.toUpperCase(newName.charAt(0)));
            list_name.add(newName.toString());
            if(choice.equals(mFileList[ind].getAbsolutePath()))
                marker = ind;
        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_single_choice, list_name);
        listfiles.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        listfiles.setAdapter(adapter);
        if(marker>=0)
            listfiles.setItemChecked(marker, true);
    }

    @Override
    public void onBackPressed()
    {
        // code here to show dialog
        super.onBackPressed();  // optional depending on your needs
        if (mPlayer != null) {
            mPlayer.release();
        }
        setResult(0);
    }

    private void getBell(String arg) {
        Intent i_back = new Intent();
        i_back.setData(Uri.parse(arg));
        setResult(RESULT_OK, i_back);
        finish();
    }

    private void stopTestBell(){
        if (mPlayer!= null)
            mPlayer.stop();
    }

    private void testBell(String arg){

        if (arg == null)
            return;
        if(mPlayer==null)
            mPlayer = new MediaPlayer();

        try {
            mPlayer.reset();
            mPlayer.setDataSource(arg);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finish() {
        super.finish();
        if (mPlayer != null) {
            mPlayer.release();
        }
    }



    private void alert(String arg){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(com.mysoft.mageon.way.R.string.attention).setMessage(arg).setCancelable(false).setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog ald = builder.create();
        ald.show();
    }
}
