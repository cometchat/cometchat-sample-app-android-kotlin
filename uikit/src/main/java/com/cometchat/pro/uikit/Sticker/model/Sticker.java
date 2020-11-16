package com.cometchat.pro.uikit.Sticker.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Sticker implements Parcelable {
    String name;
    String url;
    String setName;

    public Sticker(String name,String url,String setName) {
        this.name = name;
        this.url = url;
        this.setName = setName;
    }

    protected Sticker(Parcel in) {
        name = in.readString();
        url = in.readString();
        setName = in.readString();
    }

    public static final Creator<Sticker> CREATOR = new Creator<Sticker>() {
        @Override
        public Sticker createFromParcel(Parcel in) {
            return new Sticker(in);
        }

        @Override
        public Sticker[] newArray(int size) {
            return new Sticker[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getSetName() {
        return setName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(url);
        parcel.writeString(setName);
    }
}