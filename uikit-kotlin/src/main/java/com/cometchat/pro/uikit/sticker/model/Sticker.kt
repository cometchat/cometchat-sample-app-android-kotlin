package com.cometchat.pro.uikit.sticker.model

import android.os.Parcel
import android.os.Parcelable

class Sticker : Parcelable{
    var name: String? = null
    var url: String? = null
    var setName: String? = null


    constructor(parcel: Parcel) {
        name = parcel.readString()
        url = parcel.readString()
        setName = parcel.readString()
    }

    constructor(name: String?, url: String?, setName: String?) {
        this.name = name
        this.url = url
        this.setName = setName
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(url)
        parcel.writeString(setName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sticker> {
        override fun createFromParcel(parcel: Parcel): Sticker {
            return Sticker(parcel)
        }

        override fun newArray(size: Int): Array<Sticker?> {
            return arrayOfNulls(size)
        }
    }

}