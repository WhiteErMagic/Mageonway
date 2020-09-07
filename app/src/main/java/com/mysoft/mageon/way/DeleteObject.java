package com.mysoft.mageon.way;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Boris on 05.02.2017.
 */

public class DeleteObject implements Parcelable {

    int id;
    String name;
    boolean del;

    public DeleteObject(int id, String name) {
        this.id = id;
        this.name = name;
    }

    protected DeleteObject(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
    }

    public static final Creator<DeleteObject> CREATOR = new Creator<DeleteObject>() {
        @Override
        public DeleteObject createFromParcel(Parcel in) {
            return new DeleteObject(in);
        }

        @Override
        public DeleteObject[] newArray(int size) {
            return new DeleteObject[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
    }
}
