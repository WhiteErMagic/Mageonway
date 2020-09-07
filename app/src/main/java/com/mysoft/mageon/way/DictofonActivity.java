package com.mysoft.mageon.way;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class DictofonActivity extends AppCompatActivity {
    MediaRecorder recorder;
    File  tempfile = null;
    Chronometer chronometer;
    MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mysoft.mageon.way.R.layout.activity_dictofon);
        recorder = new MediaRecorder();
        ImageButton btn_start = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
            }
        });
        final ImageButton btn_stop = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_stop);
        btn_stop.setEnabled(false);
        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_stop.isEnabled())
                    stopRecord();
            }
        });
        final ImageButton btn_play = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_play);
        btn_play.setPressed(false);
        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_play.isEnabled())
                    playRecord();
            }
        });
        final ImageButton btn_ok = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_ok);
        btn_ok.setPressed(false);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_ok.isEnabled())
                    okRecord();
            }
        });
        chronometer = (Chronometer) findViewById(com.mysoft.mageon.way.R.id.secunds);
    }

    private void startRecord(){
        //if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);
            Toast.makeText(getApplicationContext(), getResources().getString(com.mysoft.mageon.way.R.string.error_RECORD), Toast.LENGTH_SHORT).show();
        else {
            ImageButton btn_play = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_play);
            btn_play.setEnabled(false);
            btn_play.setImageResource(com.mysoft.mageon.way.R.drawable.play_false);
            ImageButton btn_ok = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_ok);
            btn_ok.setEnabled(false);
            btn_ok.setImageResource(com.mysoft.mageon.way.R.drawable.ok_hand_false);
            if (mPlayer != null)
                mPlayer.release();

            File dir = getExternalCacheDir();

            try {
                tempfile = File.createTempFile("sound", ".mp3", dir);
                recorder.reset();
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                recorder.setOutputFile(tempfile.getAbsolutePath());
                recorder.prepare();
                recorder.start();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
                ImageButton btn_stop = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_stop);
                btn_stop.setEnabled(true);
                btn_stop.setImageResource(com.mysoft.mageon.way.R.drawable.record_stop);
            } catch (Exception e) {
                //e.printStackTrace();
                Toast.makeText(DictofonActivity.this, getResources().getString(com.mysoft.mageon.way.R.string.error_dictofon), Toast.LENGTH_SHORT).show();
                finish();
            }

        }

    }

    private void stopRecord(){
        recorder.stop();
        chronometer.stop();
        if(tempfile != null) {
            ImageButton btn_play = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_play);
            btn_play.setEnabled(true);
            btn_play.setImageResource(com.mysoft.mageon.way.R.drawable.play);
            ImageButton btn_ok = (ImageButton) findViewById(com.mysoft.mageon.way.R.id.record_ok);
            btn_ok.setEnabled(true);
            btn_ok.setImageResource(com.mysoft.mageon.way.R.drawable.ok_hand);
        }
    }

    private void playRecord(){
        if(tempfile != null) {
            mPlayer = new MediaPlayer();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    chronometer.stop();
                }
            });
            try {
                mPlayer.setDataSource(tempfile.getAbsolutePath());
                mPlayer.prepare();
                mPlayer.start();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void okRecord(){
        if(tempfile != null) {
            final Dialog dialog = new Dialog(this);
            dialog.setContentView( com.mysoft.mageon.way.R.layout.add_name);
            final EditText et = (EditText) dialog.findViewById(com.mysoft.mageon.way.R.id.edit_name);
            dialog.setTitle(com.mysoft.mageon.way.R.string.record_namefile);
            dialog.findViewById(com.mysoft.mageon.way.R.id.but_save).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v){
                    String  file_name = et.getText().toString();
                    if(file_name.length() == 0)
                    //String path = getApplicationInfo().dataDir;
                    //File appdir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/way/");
                        Toast.makeText(DictofonActivity.this, getResources().getString(com.mysoft.mageon.way.R.string.error_name), Toast.LENGTH_SHORT).show();
                    else{
                        File appdir = new File(getExternalCacheDir() + "/mageon_sound/");
                        if (!appdir.exists())
                            appdir.mkdir();
                        File newfile = new File(appdir + "/" + file_name + ".mp3");
                        tempfile.renameTo(newfile);
                        dialog.dismiss();
                        addRecordingToMediaLibrary(newfile);
                    }
                }
            });
            dialog.findViewById(com.mysoft.mageon.way.R.id.but_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    }

    protected void addRecordingToMediaLibrary(File audiofile) {

        if(audiofile.exists()) {
            ContentValues values = new ContentValues(4);
            long current = System.currentTimeMillis();
            values.put(MediaStore.Audio.Media.TITLE, audiofile.getName());
            values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3");
            values.put(MediaStore.Audio.Media.DATA, audiofile.getAbsolutePath());

            ContentResolver contentResolver = getContentResolver();
            Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Uri newUri = contentResolver.insert(base, values);
            Intent i_back = new Intent();
            i_back.setData(newUri);
            setResult(RESULT_OK, i_back);
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mPlayer!=null)
            mPlayer.release();
        if(tempfile != null)
            if(tempfile.exists()) {
                tempfile.delete();
                tempfile = null;
            }
        if(recorder != null)
            recorder.release();
    }
}
