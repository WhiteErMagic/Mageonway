package com.mysoft.mageon.way;

public class Point{
    int id;
    String name_point;
    boolean signal;
    String file_signal;
    double latitude;
    double longitude;
    int distance;

    public Point(int id, String name_point, boolean signal,  String file_signal, double latitude, double longitude, int distance){
        this.id = id;
        this.name_point = name_point;
        this.signal = signal;
        this.file_signal = file_signal;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
    }
}

