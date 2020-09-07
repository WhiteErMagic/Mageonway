package com.mysoft.mageon.way;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Boris on 04.01.2017.
 */

class DBHelper extends SQLiteOpenHelper {
    private static DBHelper mDBHelper = null;
    private static SQLiteDatabase db;
    private DBHelper(Context context) {
        // конструктор суперкласса
        super(context, "myDB", null, 1);
    }

    public static DBHelper getInctanceDBHelper(Context ctx){
        if (mDBHelper==null) {
            mDBHelper = new DBHelper(ctx.getApplicationContext());
            db = mDBHelper.getWritableDatabase();
            /*db.execSQL("create table if not exists points ("
                    + "id integer primary key autoincrement,"
                    + "name_point text,"
                    + "signal boolean,"
                    + "file_signal text,"
                    + "latitude double,"
                    + "longitude double,"
                    + "distance int"+ ");");*/
        }
        return mDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        /*db.execSQL("create table if not exists points ("
                + "id integer primary key autoincrement,"
                + "name_point text,"
                + "signal boolean,"
                + "file_signal text,"
                + "latitude double,"
                + "longitude double,"
                + "distance int"+ ");");

        db.execSQL("create table if not exists stops ("
                + "id integer primary key autoincrement,"
                + "name_point text,"
                + "latitude double,"
                + "longitude double"+ ");");*/
        db.execSQL("create table if not exists points ("
                + "id integer primary key autoincrement,"
                + "name_point text,"
                + "signal boolean,"
                + "file_signal text,"
                + "latitude double,"
                + "longitude double,"
                + "distance int"+ ");");

        db.execSQL("create table if not exists historysearch (textquery text);");
    }
    //int id, text name_point, int signal, text file_signal, double latitude, double longitude, int distance , int favorite

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //**********
    //Творчество
    //**********

    public SQLiteDatabase getDB(){
        return db;
    }

    //Получение списка отслеживания точек маршрута
/*    public ArrayList<Point> getSeekList(int route) {
        ArrayList<Point> seek_points = new ArrayList<Point>();
        Cursor c = db.rawQuery("select id, name_point, signal, file_signal, latitude, longitude, distance, favorite from points " +
                "where id_route = " + route + " and signal > "+0+" order by order_point", null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    int id = c.getInt(0);
                    String name_point = c.getString(1).toString();
                    boolean signal = c.getInt(2)>0;
                    String file_signal = c.getString(3).toString();
                    double latitude = c.getDouble(4);
                    double longitude = c.getDouble(5);
                    int distance = c.getInt(6);
                    boolean favorite = c.getInt(7)>0;

                    seek_points.add(new Point(id, 0, null, name_point, signal, file_signal, latitude, longitude, distance));
                } while (c.moveToNext());
            }
        }
        c.close();
        return seek_points;
    }*/

    //Добавление новой точки маршрута
    public long insertPoint(Point arg) {
        ContentValues values = new ContentValues();
        values.put("name_point", arg.name_point);
        values.put("signal", arg.signal);
        values.put("file_signal", arg.file_signal);
        values.put("latitude", arg.latitude);
        values.put("longitude", arg.longitude);
        values.put("distance", arg.distance);
        return db.insert("points", null, values);
    }

    //Обновление реквизитов точки маршрута
    public long updatePoint(Point arg) {
        ContentValues values = new ContentValues();
        int id_point = arg.id;
        values.put("id", arg.id);
        values.put("name_point", arg.name_point);
        values.put("signal", arg.signal);
        values.put("file_signal", arg.file_signal);
        values.put("latitude", arg.latitude);
        values.put("longitude", arg.longitude);
        values.put("distance", arg.distance);

        return db.update("points", values, "id="+id_point, null);
    }

/*    //Получение списка точек маршрута
    public ArrayList<Point> getListPoints(int route) {
        ArrayList<Point> ar_poits = new ArrayList<Point>();
        Cursor c = db.rawQuery("select id, order_point, id_route, name_route, name_point, signal, file_signal, latitude, longitude, distance from points " +
                "where id_route =" + route, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    ar_poits.add(
                            //int id,
                            // int order_point,
                            // int id_route,
                            // String name_route,
                            // String name_point,
                            // boolean signal,
                            // String file_signal,
                            // double latitude,
                            // double longitude,
                            // int distance
                             new Point(c.getInt(0),
                                    c.getInt(1),
                                    c.getInt(2),
                                    c.getString(3).toString(),
                                    c.getString(4).toString(),
                                    c.getInt(5)>0,
                                    c.getString(6).toString(),
                                    c.getDouble(7),
                                    c.getDouble(8),
                                    c.getInt(9)
                             )
                             );
                } while (c.moveToNext());
            }
        }
        c.close();
        return ar_poits;
    }*/

    //Возвращает реквизиты точки по коду
    public Point getPoint(int id_point) {
        Point point = null;
        Cursor c = db.rawQuery("select id, name_point, signal, file_signal, latitude, longitude, distance from points " +
                "where id = " + id_point, null);
        if (c != null & c.getCount() > 0) {
            if (c.moveToFirst()) {
                //int id,
                // String name_point,
                // boolean signal,
                // String file_signal,
                // double latitude,
                // double longitude,
                // int distance
                // boolean control
                point = new Point(id_point,
                        c.getString(1).toString(),
                        c.getInt(2)>0,
                        c.getString(3).toString(),
                        c.getDouble(4),
                        c.getDouble(5),
                        c.getInt(6)
                );
            }
        }else{
            point = new Point(0,
                    "",
                    false,
                    "",
                    0,
                    0,
                    100
            );
        }
        c.close();
        return point;
    }

    public void addListHistorySearch(String arg) {
        Cursor c = db.rawQuery("select textquery from historysearch where textquery = '" + arg + "'", null);
        if (c == null || c.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put("textquery", arg);
            db.insert("historysearch", null, values);
        }
        c.close();
    }

    public List<String> getListHistorySearch(String arg) {
        List<String> newAr = new ArrayList<>();
        Cursor c;
        if(arg.length() == 0){
            c = db.rawQuery("select textquery from historysearch", null);
            if (c != null & c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        newAr.add(c.getString(0).toString());
                    }while (c.moveToNext());
                }
            }
        }else {
            c = db.rawQuery("select textquery from historysearch where textquery like ?", new String[]{arg + "%"});
            if (c != null & c.getCount() > 0) {
                if (c.moveToFirst()) {
                    do {
                        newAr.add(c.getString(0).toString());
                    }while (c.moveToNext());
                }
            }
        }
        c.close();
        Collections.sort(newAr);
        return newAr;
    }

    //Возвращает курсор точек
    public Cursor getCursorPoints() {
        Cursor c = db.rawQuery("select id as _id, name_point, signal, file_signal, latitude, longitude, distance from points", null);
        return c;
    }

    //Возвращает количество записей
    public int getCountPoints() {
        Cursor c = db.rawQuery("select count(*) from points", null);
        return c.getCount();
    }

    //Удаление точек
    public void deletePoint(int point) {
        db.execSQL("DELETE FROM points WHERE id=" + point);
    }

    public void deleteRowHistory(String arg) {
        db.execSQL("DELETE FROM historysearch WHERE textquery=?",new String[]{arg});
    }
}

